package com.fakenews.model;

import java.util.*;

/**
 * A factual claim extracted from an article.
 * verificationScore in [0..1]: 1 = well-supported, 0 = contradicted.
 */
public class Claim {
    private String id;
    private String text;
    private String articleId;
    private List<Evidence> evidences;
    private double verificationScore;

    public Claim(String id, String text, String articleId) {
        this.id = id;
        this.text = text;
        this.articleId = articleId;
        this.evidences = new ArrayList<>();
        this.verificationScore = 0.0;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public String getArticleId() { return articleId; }
    public List<Evidence> getEvidences() { return evidences; }
    public double getVerificationScore() { return verificationScore; }

    public void addEvidence(Evidence evidence) {
        if (evidence != null) this.evidences.add(evidence);
    }

    public void setVerificationScore(double score) {
        this.verificationScore = Math.max(0.0, Math.min(1.0, score));
    }

    @Override
    public String toString() {
        return "Claim{id='" + id + "', articleId='" + articleId + "', score=" + verificationScore + "}";
    }
}
