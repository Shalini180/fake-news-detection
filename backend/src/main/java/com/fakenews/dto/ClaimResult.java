package com.fakenews.api.dto;

public class ClaimResult {
    private String text;
    private Double verificationScore;

    public ClaimResult() {}
    public ClaimResult(String text, Double verificationScore) {
        this.text = text;
        this.verificationScore = verificationScore;
    }

    public String getText() { return text; }
    public Double getVerificationScore() { return verificationScore; }
    public void setText(String text) { this.text = text; }
    public void setVerificationScore(Double verificationScore) { this.verificationScore = verificationScore; }
}
