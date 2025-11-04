package com.fakenews.model;

import java.util.*;

/**
 * Domain model for an article under analysis.
 * Notes:
 * - credibilityScore is a "fake-ness" score in [0..1] (higher => more likely fake).
 * - featureScores holds component scores (e.g., content_analysis, domain_credibility, etc.)
 */
public class Article {
    private String id;
    private String title;
    private String content;
    private String source;
    private Date publishDate;
    private List<String> claims;                   // plain strings (extracted summaries)
    private double credibilityScore;               // final fake-ness score
    private Map<String, Double> featureScores;     // per-feature fake-ness components

    public Article(String id, String title, String content, String source) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.source = source;
        this.publishDate = new Date();
        this.claims = new ArrayList<>();
        this.featureScores = new HashMap<>();
        this.credibilityScore = 0.0;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getSource() { return source; }
    public Date getPublishDate() { return publishDate; }
    public List<String> getClaims() { return claims; }
    public double getCredibilityScore() { return credibilityScore; }
    public Map<String, Double> getFeatureScores() { return featureScores; }

    // Mutators
    public void setCredibilityScore(double score) { this.credibilityScore = clamp01(score); }

    public void addClaim(String claim) {
        if (claim != null && !claim.isBlank()) this.claims.add(claim);
    }

    public void addFeatureScore(String feature, double score) {
        if (feature == null || feature.isBlank()) return;
        this.featureScores.put(feature, clamp01(score));
    }

    private double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    @Override
    public String toString() {
        return "Article{id='" + id + "', title='" + title + "', source='" + source +
                "', score=" + credibilityScore + "}";
    }
}
