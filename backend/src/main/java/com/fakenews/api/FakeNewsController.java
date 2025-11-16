package com.fakenews.api;

import com.fakenews.core.DetectionResult;
import com.fakenews.core.FakeNewsDetector;
import com.fakenews.explainability.AttentionExplanation;
import com.fakenews.explainability.ComprehensiveExplanation;
import com.fakenews.explainability.ExplainabilityEngine;
import com.fakenews.explainability.LIMEExplanation;
import com.fakenews.graph.KnowledgeGraph;
import com.fakenews.model.Article;
import com.fakenews.api.dto.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fakenews.service.RobertaService;
import com.fakenews.service.RobertaAnalysisResponse;


import java.util.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class FakeNewsController {

    private final FakeNewsDetector detector;
    private final RobertaService robertaService;

    public FakeNewsController(RobertaService robertaService) {
        this.detector = new FakeNewsDetector();
        this.robertaService = robertaService;
    }

    // ===========================
    //   ANALYZE SINGLE ARTICLE
    // ===========================
    @PostMapping("/analyze")
    public ResponseEntity<FakeNewsResult> analyzeArticle(@RequestBody ArticleRequest request) {
        try {
            Article article = new Article(
                    UUID.randomUUID().toString(),
                    request.getTitle(),
                    request.getContent(),
                    request.getSource()
            );

            RobertaAnalysisResponse robertaAnalysis = null;
            try {
                robertaAnalysis = robertaService.analyze(
                        request.getTitle(),
                        request.getContent(),
                        request.getSource()
                );
            } catch (Exception e) {
                log.error("RoBERTa service unavailable, using fallback", e);
            }

            DetectionResult result = detector.analyzeArticle(article);
            FakeNewsResult dto = buildResponse(result, robertaAnalysis);

            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ===========================
    //     BATCH ANALYSIS
    // ===========================
    @PostMapping("/batch-analyze")
    public ResponseEntity<BatchAnalysisResponse> batchAnalyze(@RequestBody List<ArticleRequest> requests) {

        List<Article> articles = new ArrayList<>();
        for (ArticleRequest req : requests) {
            articles.add(new Article(
                    UUID.randomUUID().toString(),
                    req.getTitle(),
                    req.getContent(),
                    req.getSource()
            ));
        }

        List<DetectionResult> results = detector.analyzeMultipleArticles(articles);

        List<FakeNewsResult> responses = results.stream()
                .map(this::buildResponse)
                .toList();

        return ResponseEntity.ok(new BatchAnalysisResponse(responses.size(), responses));
    }

    // ===========================
    //        SYSTEM STATS
    // ===========================
    @GetMapping("/stats")
    public ResponseEntity<SystemStats> getSystemStats() {

        int total = detector.getCredibilityHeap().size();
        KnowledgeGraph kg = detector.getKnowledgeGraph();
        double avg = 0.0;

        return ResponseEntity.ok(
                new SystemStats(total, 0, avg)
        );
    }

    // ===========================
    //   LEAST CREDIBLE ARTICLES
    // ===========================
    @GetMapping("/least-credible")
    public ResponseEntity<List<ArticleSummary>> getLeastCredible(
            @RequestParam(defaultValue = "5") int limit
    ) {

        List<Article> arts = detector.getLeastCredibleArticles(limit);

        List<ArticleSummary> out = arts.stream()
                .map(a -> new ArticleSummary(
                        a.getId(),
                        a.getTitle(),
                        a.getSource(),
                        a.getCredibilityScore()
                ))
                .toList();

        return ResponseEntity.ok(out);
    }

    // ===========================
    //     BUILD RESPONSE DTO
    // ===========================
    private FakeNewsResult buildResponse(DetectionResult result, RobertaAnalysisResponse roberta) {

        Article article = result.getArticle();
        ComprehensiveExplanation ex = result.getExplanation();

        FakeNewsResult dto = new FakeNewsResult();
        dto.setArticleId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setSource(article.getSource());
        dto.setCredibilityScore(article.getCredibilityScore());

        dto.setClassification(classify(article.getCredibilityScore()));

        dto.setExplanation(ex != null ? ex.getNaturalLanguageExplanation() : "");
        dto.setFeatureScores(article.getFeatureScores());
        dto.setClaimsCount(result.getClaims().size());
        dto.setKeyReasons(ex != null ? ex.getKeyReasons() : List.of());

        if (roberta != null) {
            dto.setRobertaConfidence(roberta.getConfidence());
            dto.setSentimentScore(roberta.getSentimentScore());
            dto.setWritingQuality(roberta.getWritingQuality());
            dto.setSuspiciousPhrases(roberta.getSuspiciousPhrases());
            dto.setRiskLevel(roberta.getRiskLevel().getLevel());
            dto.setRiskIcon(roberta.getRiskLevel().getIcon());
            dto.setExtractedClaims(roberta.getExtractedClaims());
        }

        dto.setAttentionTokens(
                buildAttentionTokens(ex != null ? ex.getAttentionExplanation() : null)
        );

        dto.setTopWords(
                buildTopWords(ex != null ? ex.getLimeExplanation() : null)
        );

        return dto;
    }

    // ===========================
    //      CLASSIFICATION
    // ===========================
    private String classify(double s) {
        if (s > 0.7) return "LIKELY_FAKE";
        if (s > 0.5) return "SUSPICIOUS";
        if (s > 0.3) return "MIXED_SIGNALS";
        return "LIKELY_CREDIBLE";
    }

    // ===========================
    //   ATTENTION TOKENS → DTO
    // ===========================
    private List<AttentionToken> buildAttentionTokens(AttentionExplanation att) {

        if (att == null) return List.of();

        return att.getTokenAttentions().stream()
                .limit(50)
                .map(t -> new AttentionToken(t.getToken(), t.getWeight()))
                .toList();
    }

    // ===========================
    //     TOP WORDS → DTO
    // ===========================
    private List<TopWord> buildTopWords(LIMEExplanation lime) {

        if (lime == null) return List.of();

        return lime.getTopWords().stream()
                .map(w -> new TopWord(w.getWord(), w.getImportance()))
                .toList();
    }

}
