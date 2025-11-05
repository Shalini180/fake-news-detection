package com.fakenews.api;

import com.fakenews.core.DetectionResult;
import com.fakenews.core.FakeNewsDetector;
import com.fakenews.explainability.AttentionExplanation;
import com.fakenews.explainability.ComprehensiveExplanation;
import com.fakenews.explainability.ExplainabilityEngine;
import com.fakenews.explainability.LIMEExplanation;
import com.fakenews.graph.KnowledgeGraph;
import com.fakenews.model.Article;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class FakeNewsController {

    private final FakeNewsDetector detector;

    public FakeNewsController() {
        this.detector = new FakeNewsDetector();
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeArticle(@RequestBody ArticleRequest request) {
        try {
            Article article = new Article(
                    UUID.randomUUID().toString(),
                    request.getTitle(),
                    request.getContent(),
                    request.getSource()
            );

            DetectionResult result = detector.analyzeArticle(article);
            AnalysisResponse response = buildResponse(result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

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
        List<AnalysisResponse> responses = new ArrayList<>();
        for (DetectionResult r : results) responses.add(buildResponse(r));

        return ResponseEntity.ok(new BatchAnalysisResponse(responses.size(), responses));
    }

    @GetMapping("/stats")
    public ResponseEntity<SystemStats> getSystemStats() {
        int total = detector.getCredibilityHeap().size();
        KnowledgeGraph kg = detector.getKnowledgeGraph();
        int nodes = kg.getNode("root") != null ? kg.bfs("root").size() : 0;
        double avg = 0.0; // (optional) compute from heap if you store values separately
        return ResponseEntity.ok(new SystemStats(total, nodes, avg));
    }

    @GetMapping("/least-credible")
    public ResponseEntity<List<ArticleSummary>> getLeastCredible(@RequestParam(defaultValue = "5") int limit) {
        List<Article> arts = detector.getLeastCredibleArticles(limit);
        List<ArticleSummary> out = new ArrayList<>();
        for (Article a : arts) {
            out.add(new ArticleSummary(a.getId(), a.getTitle(), a.getSource(), a.getCredibilityScore()));
        }
        return ResponseEntity.ok(out);
    }

    // ---------- helpers ----------
    private AnalysisResponse buildResponse(DetectionResult result) {
        Article article = result.getArticle();
        ComprehensiveExplanation ex = result.getExplanation();

        return new AnalysisResponse(
                article.getId(),
                article.getCredibilityScore(),
                classify(article.getCredibilityScore()),
                ex != null ? ex.getNaturalLanguageExplanation() : "",
                article.getFeatureScores(),
                result.getClaims().size(),
                ex != null ? ex.getKeyReasons() : Collections.emptyList(),
                buildAttentionTokens(ex != null ? ex.getAttentionExplanation() : null),
                buildTopWords(ex != null ? ex.getLimeExplanation() : null)
        );
    }

    private String classify(double s) {
        if (s > 0.7) return "LIKELY_FAKE";
        if (s > 0.5) return "SUSPICIOUS";
        if (s > 0.3) return "MIXED_SIGNALS";
        return "LIKELY_CREDIBLE";
    }

    private List<AttentionToken> buildAttentionTokens(AttentionExplanation att) {
        List<AttentionToken> tokens = new ArrayList<>();
        if (att != null) {
            for (ExplainabilityEngine.TokenAttention ta : att.getTokenAttentions()) {
                tokens.add(new AttentionToken(ta.getToken(), ta.getWeight()));
            }
        }
        return tokens.subList(0, Math.min(50, tokens.size()));
    }

    private List<WordImportance> buildTopWords(LIMEExplanation lime) {
        List<WordImportance> words = new ArrayList<>();
        if (lime != null) {
            for (ExplainabilityEngine.WordImportance wi : lime.getTopWords()) {
                words.add(new WordImportance(wi.getWord(), wi.getImportance()));
            }
        }
        return words;
    }

    // ---------- DTOs ----------
    public static class ArticleRequest {
        private String title;
        private String content;
        private String source;
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getSource() { return source; }
        public void setTitle(String title) { this.title = title; }
        public void setContent(String content) { this.content = content; }
        public void setSource(String source) { this.source = source; }
    }

    public static class AnalysisResponse {
        private String articleId;
        private double credibilityScore;
        private String classification;
        private String explanation;
        private Map<String, Double> featureScores;
        private int claimsCount;
        private List<String> keyReasons;
        private List<AttentionToken> attentionTokens;
        private List<WordImportance> topWords;

        public AnalysisResponse(String articleId, double credibilityScore, String classification,
                                String explanation, Map<String, Double> featureScores, int claimsCount,
                                List<String> keyReasons, List<AttentionToken> attentionTokens,
                                List<WordImportance> topWords) {
            this.articleId = articleId;
            this.credibilityScore = credibilityScore;
            this.classification = classification;
            this.explanation = explanation;
            this.featureScores = featureScores;
            this.claimsCount = claimsCount;
            this.keyReasons = keyReasons;
            this.attentionTokens = attentionTokens;
            this.topWords = topWords;
        }

        public String getArticleId() { return articleId; }
        public double getCredibilityScore() { return credibilityScore; }
        public String getClassification() { return classification; }
        public String getExplanation() { return explanation; }
        public Map<String, Double> getFeatureScores() { return featureScores; }
        public int getClaimsCount() { return claimsCount; }
        public List<String> getKeyReasons() { return keyReasons; }
        public List<AttentionToken> getAttentionTokens() { return attentionTokens; }
        public List<WordImportance> getTopWords() { return topWords; }
    }

    public static class AttentionToken {
        private String token;
        private double weight;
        public AttentionToken(String token, double weight) { this.token = token; this.weight = weight; }
        public String getToken() { return token; }
        public double getWeight() { return weight; }
    }

    public static class WordImportance {
        private String word;
        private double importance;
        public WordImportance(String word, double importance) { this.word = word; this.importance = importance; }
        public String getWord() { return word; }
        public double getImportance() { return importance; }
    }

    public static class SystemStats {
        private int totalArticlesAnalyzed;
        private int knowledgeGraphNodes;
        private double averageCredibilityScore;
        public SystemStats(int totalArticlesAnalyzed, int knowledgeGraphNodes, double averageCredibilityScore) {
            this.totalArticlesAnalyzed = totalArticlesAnalyzed;
            this.knowledgeGraphNodes = knowledgeGraphNodes;
            this.averageCredibilityScore = averageCredibilityScore;
        }
        public int getTotalArticlesAnalyzed() { return totalArticlesAnalyzed; }
        public int getKnowledgeGraphNodes() { return knowledgeGraphNodes; }
        public double getAverageCredibilityScore() { return averageCredibilityScore; }
    }

    public static class ArticleSummary {
        private String id;
        private String title;
        private String source;
        private double credibilityScore;
        public ArticleSummary(String id, String title, String source, double credibilityScore) {
            this.id = id; this.title = title; this.source = source; this.credibilityScore = credibilityScore;
        }
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getSource() { return source; }
        public double getCredibilityScore() { return credibilityScore; }
    }

    public static class BatchAnalysisResponse {
        private int totalAnalyzed;
        private List<AnalysisResponse> results;
        public BatchAnalysisResponse(int totalAnalyzed, List<AnalysisResponse> results) {
            this.totalAnalyzed = totalAnalyzed; this.results = results;
        }
        public int getTotalAnalyzed() { return totalAnalyzed; }
        public List<AnalysisResponse> getResults() { return results; }
    }
}
