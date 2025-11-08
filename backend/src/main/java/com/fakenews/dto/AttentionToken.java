package com.fakenews.api.dto;

public class AttentionToken {
    private String token;
    private Double weight;

    public AttentionToken() {}
    public AttentionToken(String token, Double weight) {
        this.token = token;
        this.weight = weight;
    }

    public String getToken() { return token; }
    public Double getWeight() { return weight; }
    public void setToken(String token) { this.token = token; }
    public void setWeight(Double weight) { this.weight = weight; }
}
