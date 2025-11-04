package com.fakenews.model;

/**
 * Piece of evidence used to verify/contradict a claim.
 * relevanceScore in [0..1].
 * supports=true means it supports the claim; false means it contradicts.
 */
public class Evidence {
    private String id;
    private String text;
    private String sourceUrl;
    private double relevanceScore;
    private boolean supports;

    public Evidence(String id, String text, String sourceUrl, double relevanceScore, boolean supports) {
        this.id = id;
        this.text = text;
        this.sourceUrl = sourceUrl;
        this.relevanceScore = Math.max(0.0, Math.min(1.0, relevanceScore));
        this.supports = supports;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public String getSourceUrl() { return sourceUrl; }
    public double getRelevanceScore() { return relevanceScore; }
    public boolean isSupports() { return supports; }

    @Override
    public String toString() {
        return "Evidence{id='" + id + "', supports=" + supports + ", rel=" + relevanceScore + "}";
    }
}
