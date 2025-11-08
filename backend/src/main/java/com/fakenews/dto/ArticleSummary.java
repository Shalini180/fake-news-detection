package com.fakenews.api.dto;

public class ArticleSummary {
    private String id;
    private String title;
    private String source;
    private double credibilityScore;

    public ArticleSummary(String id, String title, String source, double credibilityScore) {
        this.id = id;
        this.title = title;
        this.source = source;
        this.credibilityScore = credibilityScore;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSource() { return source; }
    public double getCredibilityScore() { return credibilityScore; }
}
