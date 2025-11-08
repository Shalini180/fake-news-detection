package com.fakenews.explainability;

import java.util.*;

public class ComprehensiveExplanation {
    private final String naturalLanguageExplanation;
    private final List<String> keyReasons;
    private final AttentionExplanation attentionExplanation;
    private final LIMEExplanation limeExplanation;

    public ComprehensiveExplanation(String nle, List<String> reasons,
                                    AttentionExplanation att, LIMEExplanation lime) {
        this.naturalLanguageExplanation = nle;
        this.keyReasons = reasons != null ? reasons : new ArrayList<>();
        this.attentionExplanation = att;
        this.limeExplanation = lime;
    }

    public String getNaturalLanguageExplanation() { return naturalLanguageExplanation; }
    public List<String> getKeyReasons() { return keyReasons; }
    public AttentionExplanation getAttentionExplanation() { return attentionExplanation; }
    public LIMEExplanation getLimeExplanation() { return limeExplanation; }
}
