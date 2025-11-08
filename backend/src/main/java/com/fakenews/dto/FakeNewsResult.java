package com.fakenews.api.dto;

import java.util.List;
import java.util.Map;

public class FakeNewsResult {

    private String articleId;
    private String title;
    private String source;
    private double credibilityScore;
    private String classification;
    private String explanation;
    private Map<String, Double> featureScores;
    private int claimsCount;
    private List<String> keyReasons;
    private List<AttentionToken> attentionTokens;
    private List<TopWord> topWords;

    // ===== getters =====
    public String getArticleId() { return articleId; }
    public String getTitle() { return title; }
    public String getSource() { return source; }
    public double getCredibilityScore() { return credibilityScore; }
    public String getClassification() { return classification; }
    public String getExplanation() { return explanation; }
    public Map<String, Double> getFeatureScores() { return featureScores; }
    public int getClaimsCount() { return claimsCount; }
    public List<String> getKeyReasons() { return keyReasons; }
    public List<AttentionToken> getAttentionTokens() { return attentionTokens; }
    public List<TopWord> getTopWords() { return topWords; }

    // ===== setters =====
    public void setArticleId(String id) { this.articleId = id; }
    public void setTitle(String title) { this.title = title; }
    public void setSource(String source) { this.source = source; }
    public void setCredibilityScore(double score) { this.credibilityScore = score; }
    public void setClassification(String classification) { this.classification = classification; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public void setFeatureScores(Map<String, Double> fs) { this.featureScores = fs; }
    public void setClaimsCount(int count) { this.claimsCount = count; }
    public void setKeyReasons(List<String> keyReasons) { this.keyReasons = keyReasons; }
    public void setAttentionTokens(List<AttentionToken> tokens) { this.attentionTokens = tokens; }
    public void setTopWords(List<TopWord> words) { this.topWords = words; }
}
