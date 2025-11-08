package com.fakenews.core;

import com.fakenews.model.*;
import com.fakenews.explainability.*;

import java.util.*;

public class DetectionResult {
    private final Article article;
    private final List<Claim> claims;
    private final ComprehensiveExplanation explanation;
    private final Date analysisTimestamp;

    public DetectionResult(Article article, List<Claim> claims, ComprehensiveExplanation explanation) {
        this.article = article;
        this.claims = claims != null ? claims : new ArrayList<>();
        this.explanation = explanation;
        this.analysisTimestamp = new Date();
    }

    public Article getArticle() { return article; }
    public List<Claim> getClaims() { return claims; }
    public ComprehensiveExplanation getExplanation() { return explanation; }
    public Date getAnalysisTimestamp() { return analysisTimestamp; }

    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== FAKE NEWS DETECTION REPORT ===\n\n");
        report.append("Article: ").append(nz(article.getTitle())).append("\n");
        report.append("Source: ").append(nz(article.getSource())).append("\n");
        report.append("Credibility Score (fake-ness): ").append(
                String.format(Locale.ROOT, "%.2f", article.getCredibilityScore())).append("\n");
        report.append("Classification: ").append(getClassification()).append("\n\n");

        if (explanation != null) {
            report.append("Explanation: ").append(nz(explanation.getNaturalLanguageExplanation())).append("\n\n");
            List<String> reasons = explanation.getKeyReasons();
            if (reasons != null && !reasons.isEmpty()) {
                report.append("Key Reasons:\n");
                for (String reason : reasons) {
                    report.append("  - ").append(reason).append("\n");
                }
                report.append("\n");
            }
        }

        report.append("Claims Analyzed: ").append(claims.size()).append("\n");
        for (Claim claim : claims) {
            String text = nz(claim.getText());
            String preview = text.length() > 80 ? text.substring(0, 80) + "..." : text;
            report.append("  - \"").append(preview).append("\"\n");
            report.append("    Verification Score: ").append(
                    String.format(Locale.ROOT, "%.2f", claim.getVerificationScore())).append("\n");
        }

        report.append("\n=== END REPORT ===\n");
        return report.toString();
    }

    private String nz(String s) { return s == null ? "" : s; }

    private String getClassification() {
        double score = article.getCredibilityScore();
        if (score > 0.7) return "LIKELY_FAKE";
        if (score > 0.5) return "SUSPICIOUS";
        if (score > 0.3) return "MIXED_SIGNALS";
        return "LIKELY_CREDIBLE";
    }
}
