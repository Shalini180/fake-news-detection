package com.fakenews.core;

import com.fakenews.model.*;
import com.fakenews.graph.*;
import com.fakenews.datastructures.*;
import com.fakenews.nlp.*;
import com.fakenews.explainability.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FakeNewsDetector {
    private final KnowledgeGraph knowledgeGraph;
    private final DomainTrie domainTrie;
    private final CredibilityMinHeap credibilityHeap;
    private final EvidenceRetriever evidenceRetriever;
    private final RoBERTaModel nlpModel;
    private final ClaimExtractor claimExtractor;
    private final ExplainabilityEngine explainabilityEngine;

    // Caches / indices (thread-friendly for API usage)
    private final Map<String, Article> articleCache;
    private final Map<String, Set<String>> sourceToArticleIds;

    public FakeNewsDetector() {
        this.knowledgeGraph = new KnowledgeGraph();
        this.domainTrie = new DomainTrie();
        this.credibilityHeap = new CredibilityMinHeap();
        this.nlpModel = new RoBERTaModel();
        this.evidenceRetriever = new EvidenceRetriever(nlpModel);
        this.claimExtractor = new ClaimExtractor();
        this.explainabilityEngine = new ExplainabilityEngine(nlpModel);

        this.articleCache = new ConcurrentHashMap<>();
        this.sourceToArticleIds = new ConcurrentHashMap<>();

        initializeDomainCredibility();
    }

    // ===== Main pipeline =====
    public DetectionResult analyzeArticle(Article article) {
        System.out.println("Analyzing article: " + article.getTitle());

        // 1) Content analysis (NLP)
        RoBERTaModel.ClassificationResult nlpResult = nlpModel.classify(article.getContent());
        double contentScore = nlpResult.getFakeScore();
        article.addFeatureScore("content_analysis", clamp01(contentScore));

        // 2) Domain credibility
        double domainScore = checkDomainCredibility(article.getSource());
        article.addFeatureScore("domain_credibility", clamp01(domainScore));

        // 3) Extract + verify claims (evidence-aware)
        List<Claim> claims = claimExtractor.extractClaims(article);
        double claimsScore = verifyClaims(claims);
        article.addFeatureScore("claims_verification", clamp01(claimsScore));

        // 4) Add to knowledge graph (+ reverse link from SOURCE to ARTICLE for traversal)
        addToKnowledgeGraph(article, claims);

        // 5) Cross-reference against peers (same source, similar content)
        double crossRefScore = crossReferenceArticles(article);
        article.addFeatureScore("cross_reference", clamp01(crossRefScore));

        // 6) Compute final fake-ness score
        double finalScore = computeFinalCredibilityScore(article);
        article.setCredibilityScore(clamp01(finalScore));

        // 7) Track in min-heap (for least-credible lookups)
        credibilityHeap.insert(article.getId(), article.getCredibilityScore());

        // 8) Explainability bundle
        ComprehensiveExplanation explanation =
                explainabilityEngine.generateComprehensiveExplanation(article);

        // Cache the article (must be after score computed)
        articleCache.put(article.getId(), article);

        return new DetectionResult(article, claims, explanation);
    }

    // ===== Helpers =====
    private double checkDomainCredibility(String source) {
        String domain = extractDomain(source);
        // Try exact, then fallback (e.g., news.reuters.com -> reuters.com)
        Double score = domainTrie.search(domain);
        if (score == null) {
            score = domainTrie.searchWithFallback(domain);
        }
        if (score != null) {
            // Trie stores "fake-ness" for bad sites in [0..1] (lower=credible, higher=fake).
            // We treat this as a fake score already, so return as-is.
            return clamp01(score);
        }
        // Unknown domain => neutral/suspicious
        return 0.5;
    }

    private double verifyClaims(List<Claim> claims) {
        if (claims == null || claims.isEmpty()) return 0.3; // Few/no claims -> slightly suspicious

        double totalTruthiness = 0.0;
        int verifiedCount = 0;

        for (Claim claim : claims) {
            List<Evidence> evidences = evidenceRetriever.retrieveRelevantEvidence(claim, 5);
            claim.getEvidences().addAll(evidences);

            double verificationScore = evidenceRetriever.verifyClaimWithEvidence(claim, evidences);
            // verificationScore is in [0..1], where 1=supported, 0=contradicted
            claim.setVerificationScore(clamp01(verificationScore));

            totalTruthiness += verificationScore;
            verifiedCount++;
        }

        // Convert truthiness to a fake-ness component (1 - mean truth)
        return verifiedCount > 0 ? clamp01(1.0 - (totalTruthiness / verifiedCount)) : 0.5;
    }

    private void addToKnowledgeGraph(Article article, List<Claim> claims) {
        // Article node
        knowledgeGraph.addNode(article.getId(), "ARTICLE", article);

        // Source node: stable by registrable domain
        String sourceDomain = extractDomain(article.getSource());
        String sourceId = "source_" + sourceDomain;
        knowledgeGraph.addNode(sourceId, "SOURCE", sourceDomain);

        // Link ARTICLE -> SOURCE and SOURCE -> ARTICLE (reverse helps traversal for related lookup)
        knowledgeGraph.addEdge(article.getId(), sourceId, "PUBLISHED_BY", 1.0);
        knowledgeGraph.addEdge(sourceId, article.getId(), "HAS_ARTICLE", 1.0);

        // Keep an index for fast peer lookup
        indexArticleBySource(sourceDomain, article.getId());

        // Claims + evidence nodes
        for (Claim claim : claims) {
            knowledgeGraph.addNode(claim.getId(), "CLAIM", claim);
            knowledgeGraph.addEdge(article.getId(), claim.getId(), "CONTAINS", 1.0);

            for (Evidence evidence : claim.getEvidences()) {
                String evidenceId = "evidence_" + evidence.getId();
                knowledgeGraph.addNode(evidenceId, "EVIDENCE", evidence);
                String relationship = evidence.isSupports() ? "SUPPORTS" : "CONTRADICTS";
                knowledgeGraph.addEdge(claim.getId(), evidenceId, relationship, Math.max(0.1, evidence.getRelevanceScore()));
            }
        }
    }

    private void indexArticleBySource(String domain, String articleId) {
        sourceToArticleIds.computeIfAbsent(domain, d -> ConcurrentHashMap.newKeySet()).add(articleId);
    }

    private double crossReferenceArticles(Article article) {
        String domain = extractDomain(article.getSource());
        Set<String> peers = sourceToArticleIds.getOrDefault(domain, Collections.emptySet());

        double sumCred = 0.0;
        int count = 0;

        for (String peerId : peers) {
            if (peerId.equals(article.getId())) continue;
            Article peer = articleCache.get(peerId);
            if (peer == null || peer.getCredibilityScore() <= 0) continue;

            double sim = nlpModel.computeSimilarity(article.getContent(), peer.getContent());
            if (sim > 0.7) {
                sumCred += peer.getCredibilityScore();
                count++;
            }
        }
        // If close peers are fake, we tilt fake-ness up; otherwise neutral.
        return count > 0 ? clamp01(sumCred / count) : 0.5;
    }

    private double computeFinalCredibilityScore(Article article) {
        Map<String, Double> s = article.getFeatureScores();

        // Weighted sum (fake-ness components)
        double contentWeight = 0.35;
        double domainWeight  = 0.25;
        double claimsWeight  = 0.25;
        double crossRefWeight= 0.15;

        double finalScore =
                s.getOrDefault("content_analysis", 0.5) * contentWeight +
                        s.getOrDefault("domain_credibility", 0.5) * domainWeight +
                        s.getOrDefault("claims_verification", 0.5) * claimsWeight +
                        s.getOrDefault("cross_reference", 0.5) * crossRefWeight;

        return clamp01(finalScore);
    }

    // ===== Batch & insights =====
    public List<DetectionResult> analyzeMultipleArticles(List<Article> articles) {
        List<DetectionResult> results = new ArrayList<>();
        for (Article a : articles) {
            results.add(analyzeArticle(a));
        }
        detectCoordinatedMisinformation(results);
        return results;
    }

    private void detectCoordinatedMisinformation(List<DetectionResult> results) {
        // Simple clustering by source among likely fake articles
        Map<String, List<Article>> bySource = results.stream()
                .map(DetectionResult::getArticle)
                .filter(a -> a.getCredibilityScore() > 0.7)
                .collect(Collectors.groupingBy(a -> extractDomain(a.getSource())));

        System.out.println("Detected " + bySource.size() + " potential coordinated sources");
    }

    // ===== Utils / accessors =====
    private double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private String extractDomain(String source) {
        if (source == null) return "";
        String host = source.toLowerCase(Locale.ROOT).trim();
        int scheme = host.indexOf("://");
        if (scheme >= 0) host = host.substring(scheme + 3);
        int slash = host.indexOf('/');
        if (slash >= 0) host = host.substring(0, slash);
        int colon = host.indexOf(':');
        if (colon >= 0) host = host.substring(0, colon);

        // naive registrable domain (no PSL)
        String[] parts = host.split("\\.");
        if (parts.length >= 2) {
            String sld = parts[parts.length - 2];
            String tld = parts[parts.length - 1];
            return sld + "." + tld;
        }
        return host;
    }

    public KnowledgeGraph getKnowledgeGraph() { return knowledgeGraph; }
    public CredibilityMinHeap getCredibilityHeap() { return credibilityHeap; }
}
