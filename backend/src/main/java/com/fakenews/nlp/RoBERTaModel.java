package com.fakenews.nlp;

import java.util.*;

/**
 * Simulated RoBERTa-based NLP model.
 * In production, replace with DJL/ONNX inference and a proper tokenizer.
 */
public class RoBERTaModel {
    private static final int EMB_DIM = 768;

    // --- Classification (fake vs real) ---
    public ClassificationResult classify(String text) {
        Map<String, Double> scores = new HashMap<>();
        double fakeScore = computeFakeScore(text);
        scores.put("fake", fakeScore);
        scores.put("real", 1.0 - fakeScore);

        double[] attentionWeights = computeAttentionWeights(text);
        String[] tokens = tokenize(text);

        return new ClassificationResult(scores, attentionWeights, tokens);
    }

    // --- Embeddings + similarity ---
    public double[] getEmbedding(String text) {
        Random r = new Random(text == null ? 0 : text.hashCode());
        double[] v = new double[EMB_DIM];
        for (int i = 0; i < EMB_DIM; i++) v[i] = r.nextGaussian();
        return normalize(v);
    }

    public double computeSimilarity(String t1, String t2) {
        double[] a = getEmbedding(String.valueOf(t1));
        double[] b = getEmbedding(String.valueOf(t2));
        return cosine(a, b);
    }

    // --- Heuristics (demo only) ---
    private double computeFakeScore(String text) {
        if (text == null || text.isBlank()) return 0.5;
        double s = 0.5;
        String upper = text.toUpperCase(Locale.ROOT);
        String[] hot = {"BREAKING","SHOCKING","UNBELIEVABLE","AMAZING","INCREDIBLE","TERRIFIED","CATASTROPHIC"};
        int hits = 0;
        for (String h : hot) if (upper.contains(h)) hits++;
        s += Math.min(hits * 0.08, 0.3);
        if (text.contains("!!!")) s += 0.1;
        if (!text.matches(".*\\b(according to|said|reported|published)\\b.*")) s += 0.05;
        return clamp01(s);
    }

    private double[] computeAttentionWeights(String text) {
        String[] toks = tokenize(String.valueOf(text));
        double[] w = new double[toks.length];
        Set<String> hot = new HashSet<>(Arrays.asList(
                "breaking","shocking","unbelievable","amazing","incredible","terrified","catastrophic"));
        Random rand = new Random(Objects.hash(text));
        for (int i = 0; i < toks.length; i++) {
            String t = toks[i].toLowerCase(Locale.ROOT);
            w[i] = hot.contains(t) ? 0.9 : 0.25 + rand.nextDouble() * 0.45;
        }
        return normalize(w);
    }

    private String[] tokenize(String text) {
        if (text == null || text.isBlank()) return new String[0];
        return text.trim().split("\\s+");
    }

    private static double[] normalize(double[] v) {
        double s = 0;
        for (double x : v) s += x * x;
        double n = Math.sqrt(s);
        if (n > 0) for (int i = 0; i < v.length; i++) v[i] /= n;
        return v;
    }

    private static double cosine(double[] a, double[] b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private static double clamp01(double v) { return Math.max(0.0, Math.min(1.0, v)); }

    // --- DTO ---
    public static class ClassificationResult {
        private final Map<String, Double> classScores;
        private final double[] attentionWeights;
        private final String[] tokens;

        public ClassificationResult(Map<String, Double> scores, double[] attention, String[] tokens) {
            this.classScores = scores;
            this.attentionWeights = attention;
            this.tokens = tokens;
        }

        public Map<String, Double> getClassScores() { return classScores; }
        public double[] getAttentionWeights() { return attentionWeights; }
        public String[] getTokens() { return tokens; }
        public double getFakeScore() { return classScores.getOrDefault("fake", 0.5); }
    }
}
