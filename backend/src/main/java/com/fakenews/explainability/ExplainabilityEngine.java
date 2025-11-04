package com.fakenews.explainability;

import com.fakenews.model.Article;
import com.fakenews.nlp.RoBERTaModel;

import java.util.*;

/**
 * Generates a human-friendly explanation bundle used by the API and UI.
 * This stub fabricates attention and LIME-like outputs in a consistent format.
 */
public class ExplainabilityEngine {
    private final RoBERTaModel model;

    public ExplainabilityEngine(RoBERTaModel model) { this.model = model; }

    public ComprehensiveExplanation generateComprehensiveExplanation(Article article) {
        // Attention
        String[] tokens = tokenize(article.getContent());
        double[] weights = model.classify(article.getContent()).getAttentionWeights();
        List<TokenAttention> atts = new ArrayList<>();
        for (int i = 0; i < Math.min(tokens.length, weights.length); i++) {
            atts.add(new TokenAttention(tokens[i], clamp01(weights[i])));
        }
        AttentionExplanation attention = new AttentionExplanation(atts);

        // LIME-ish top words (fake importance by frequency of hot words)
        List<WordImportance> top = topWords(article.getContent());
        LIMEExplanation lime = new LIMEExplanation(top);

        // Reasons
        List<String> reasons = new ArrayList<>();
        long hotCount = top.stream().filter(w -> w.word.matches("(?i)(BREAKING|SHOCKING|UNBELIEVABLE|AMAZING|INCREDIBLE|TERRIFIED|CATASTROPHIC).*")).count();
        if (hotCount >= 2) reasons.add("High use of sensational language");
        if (article.getSource() != null && article.getSource().toLowerCase(Locale.ROOT).contains("clickbait")) reasons.add("Low-credibility source domain");
        if (reasons.isEmpty()) reasons.add("Language and sourcing appear balanced");

        String nl =
                article.getCredibilityScore() > 0.7 ? "This article is likely fake given language patterns and domain signals."
                        : article.getCredibilityScore() > 0.5 ? "This article shows mixed/suspicious indicators and warrants fact checking."
                        : "This article appears credible based on linguistic and sourcing signals.";

        return new ComprehensiveExplanation(nl, reasons, attention, lime);
    }

    private String[] tokenize(String text) {
        if (text == null || text.isBlank()) return new String[0];
        return text.trim().split("\\s+");
    }

    private List<WordImportance> topWords(String content) {
        if (content == null) return Collections.emptyList();
        String[] toks = content.toUpperCase(Locale.ROOT).split("\\s+");
        String[] focus = {"BREAKING","SHOCKING","UNBELIEVABLE","AMAZING","INCREDIBLE","TERRIFIED","CATASTROPHIC"};
        Set<String> focusSet = new HashSet<>(Arrays.asList(focus));
        Map<String,Integer> counts = new HashMap<>();
        for (String t : toks) if (focusSet.stream().anyMatch(t::contains)) counts.put(t, counts.getOrDefault(t, 0)+1);
        List<WordImportance> out = new ArrayList<>();
        for (Map.Entry<String,Integer> e : counts.entrySet()) {
            out.add(new WordImportance(e.getKey(), Math.min(1.0, e.getValue() * 0.15)));
        }
        out.sort((a,b) -> Double.compare(b.importance, a.importance));
        if (out.size() > 10) out = out.subList(0, 10);
        return out;
    }

    private static double clamp01(double v) { return Math.max(0.0, Math.min(1.0, v)); }

    // --- Types consumed by controller/visualization ---

    public static class TokenAttention {
        private final String token;
        private final double weight;
        public TokenAttention(String token, double weight) { this.token = token; this.weight = weight; }
        public String getToken() { return token; }
        public double getWeight() { return weight; }
    }

    public static class WordImportance {
        private final String word;
        private final double importance;
        public WordImportance(String word, double importance) { this.word = word; this.importance = importance; }
        public String getWord() { return word; }
        public double getImportance() { return importance; }
    }
}
