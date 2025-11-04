package com.fakenews.explainability;

import java.util.*;
import com.fakenews.explainability.ExplainabilityEngine.TokenAttention;

public class AttentionExplanation {
    private final List<TokenAttention> tokenAttentions;

    public AttentionExplanation(List<TokenAttention> tokenAttentions) {
        this.tokenAttentions = tokenAttentions != null ? tokenAttentions : new ArrayList<>();
    }

    public List<TokenAttention> getTokenAttentions() { return tokenAttentions; }
}
