package com.fakenews.explainability;

import java.util.*;
import com.fakenews.explainability.ExplainabilityEngine.WordImportance;

public class LIMEExplanation {
    private final List<WordImportance> topWords;

    public LIMEExplanation(List<WordImportance> topWords) {
        this.topWords = topWords != null ? topWords : new ArrayList<>();
    }

    public List<WordImportance> getTopWords() { return topWords; }
}
