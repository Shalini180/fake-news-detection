package com.fakenews.dto;

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
    private Double robertaConfidence;
    private Double sentimentScore;
    private Map writingQuality;
    private List suspiciousPhrases;
    private String riskLevel;
    private String riskIcon;
    private List extractedClaims;
    private UncertaintyDto uncertainty; // NEW: Uncertainty quantification metrics

    // ===== getters =====
    public String getArticleId() {
        return articleId;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public double getCredibilityScore() {
        return credibilityScore;
    }

    public String getClassification() {
        return classification;
    }

    public String getExplanation() {
        return explanation;
    }

    public Map<String, Double> getFeatureScores() {
        return featureScores;
    }

    public int getClaimsCount() {
        return claimsCount;
    }

    public List<String> getKeyReasons() {
        return keyReasons;
    }

    public List<AttentionToken> getAttentionTokens() {
        return attentionTokens;
    }

    public List<TopWord> getTopWords() {
        return topWords;
    }

    public Double getRobertaConfidence() {
        return robertaConfidence;
    }

    public Double getSentimentScore() {
        return sentimentScore;
    }

    public Map getWritingQuality() {
        return writingQuality;
    }

    public List getSuspiciousPhrases() {
        return suspiciousPhrases;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getRiskIcon() {
        return riskIcon;
    }

    public List getExtractedClaims() {
        return extractedClaims;
    }

    public UncertaintyDto getUncertain

ty() { return uncertainty; }

    // ===== setters =====
    public void setArticleId(String id) {
        this.articleId = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setCredibilityScore(double score) {
        this.credibilityScore = score;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setFeatureScores(Map<String, Double> fs) {
        this.featureScores = fs;
    }

    public void setClaimsCount(int count) {
        this.claimsCount = count;
    }

    public void setKeyReasons(List<String> keyReasons) {
        this.keyReasons = keyReasons;
    }

    public void setAttentionTokens(List<AttentionToken> tokens) {
        this.attentionTokens = tokens;
    }

    public void setTopWords(List<TopWord> words) {
        this.topWords = words;
    }

    public void setRobertaConfidence(Double robertaConfidence) {
        this.robertaConfidence = robertaConfidence;
    }

    public void setSentimentScore(Double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public void setWritingQuality(Map writingQuality) {
        this.writingQuality = writingQuality;
    }

    public void setSuspiciousPhrases(List suspiciousPhrases) {
        this.suspiciousPhrases = suspiciousPhrases;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setRiskIcon(String riskIcon) {
        this.riskIcon = riskIcon;
    }

    public void setExtractedClaims(List extractedClaims) {
        this.extractedClaims = extractedClaims;
    }

    public void setUncertainty(UncertaintyDto uncertainty) {
        this.uncertainty = uncertainty;
    }
}
