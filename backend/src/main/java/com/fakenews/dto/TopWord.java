package com.fakenews.dto;

public class TopWord {
    private String word;
    private Double importance;

    public TopWord() {}
    public TopWord(String word, Double importance) {
        this.word = word;
        this.importance = importance;
    }

    public String getWord() { return word; }
    public Double getImportance() { return importance; }
    public void setWord(String word) { this.word = word; }
    public void setImportance(Double importance) { this.importance = importance; }
}
