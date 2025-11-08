package com.fakenews.api.dto;

import java.util.List;

public class BatchAnalysisResponse {
    private int totalAnalyzed;
    private List<FakeNewsResult> results;

    public BatchAnalysisResponse(int totalAnalyzed, List<FakeNewsResult> results) {
        this.totalAnalyzed = totalAnalyzed;
        this.results = results;
    }

    public int getTotalAnalyzed() { return totalAnalyzed; }
    public List<FakeNewsResult> getResults() { return results; }
}
