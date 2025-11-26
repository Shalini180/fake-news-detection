package com.fakenews.dto;

public class SystemStats {
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
