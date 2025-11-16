package com.fakenews.service;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RobertaAnalysisResponse {
    private String prediction;
    private Double confidence;
    private Double credibilityScore;
    private List<Map> topTokens;
    private List suspiciousPhrases;
    private List keyReasons;
    private List<Map> attentionHighlights;
    private Double sentimentScore;
    private Map writingQuality;
    private List extractedClaims;
    private RiskLevel riskLevel;
    private Double processingTimeMs;
    private String timestamp;
    private String modelVersion;

    @Data
    public static class RiskLevel {
        private String level;
        private String icon;
        private String description;
    }
}