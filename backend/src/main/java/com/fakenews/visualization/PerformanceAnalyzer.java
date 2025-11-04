package com.fakenews.analysis;

import com.fakenews.core.DetectionResult;
import com.fakenews.core.FakeNewsDetector;
import com.fakenews.model.Article;

import java.util.*;

/**
 * Simple performance & validation utilities to benchmark the pipeline locally.
 */
public class PerformanceAnalyzer {

    // ---------- Throughput / latency ----------
    public PerformanceReport analyzePerformance(FakeNewsDetector detector, List<Article> testArticles) {
        long start = System.currentTimeMillis();

        List<DetectionResult> results = new ArrayList<>();
        Map<String, Long> timings = new HashMap<>();

        for (Article a : testArticles) {
            long t0 = System.currentTimeMillis();
            DetectionResult r = detector.analyzeArticle(a);
            long dt = System.currentTimeMillis() - t0;
            results.add(r);
            timings.put(a.getId(), dt);
        }

        long total = System.currentTimeMillis() - start;
        return new PerformanceReport(results, timings, total);
    }

    // ---------- Validation (needs labels) ----------
    public ValidationReport validateDetection(FakeNewsDetector detector,
                                              List<Article> testArticles,
                                              Map<String, Boolean> groundTruthIsFake) {
        int tp = 0, tn = 0, fp = 0, fn = 0;

        for (Article a : testArticles) {
            DetectionResult r = detector.analyzeArticle(a);
            boolean predFake = r.getArticle().getCredibilityScore() > 0.6;
            Boolean actualFake = groundTruthIsFake.get(a.getId());
            if (actualFake == null) continue;

            if (predFake && actualFake) tp++;
            else if (predFake && !actualFake) fp++;
            else if (!predFake && !actualFake) tn++;
            else fn++;
        }

        double precision = safeDiv(tp, tp + fp);
        double recall    = safeDiv(tp, tp + fn);
        double f1        = (precision + recall) > 0 ? 2 * (precision * recall) / (precision + recall) : 0;
        double accuracy  = safeDiv(tp + tn, tp + tn + fp + fn);

        return new ValidationReport(precision, recall, f1, accuracy, tp, tn, fp, fn);
    }

    private double safeDiv(double a, double b) { return b == 0 ? 0 : a / b; }

    // ---------- DTOs ----------
    public static class PerformanceReport {
        private final List<DetectionResult> results;
        private final Map<String, Long> timings;
        private final long totalTimeMs;

        public PerformanceReport(List<DetectionResult> results, Map<String, Long> timings, long totalTimeMs) {
            this.results = results;
            this.timings = timings;
            this.totalTimeMs = totalTimeMs;
        }

        public List<DetectionResult> getResults() { return results; }
        public Map<String, Long> getTimings() { return timings; }
        public long getTotalTimeMs() { return totalTimeMs; }

        public String generateReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n╔════════════════════════════════════════════════════════════╗\n");
            sb.append("║              PERFORMANCE ANALYSIS REPORT                   ║\n");
            sb.append("╚════════════════════════════════════════════════════════════╝\n\n");

            sb.append("Total Articles Processed: ").append(results.size()).append("\n");
            sb.append("Total Processing Time: ").append(totalTimeMs).append(" ms\n");

            double avg = results.isEmpty() ? 0 : (double) totalTimeMs / results.size();
            sb.append("Average Time per Article: ").append(String.format(Locale.ROOT, "%.2f", avg)).append(" ms\n");

            if (!timings.isEmpty()) {
                long max = Collections.max(timings.values());
                long min = Collections.min(timings.values());
                sb.append("Max Processing Time: ").append(max).append(" ms\n");
                sb.append("Min Processing Time: ").append(min).append(" ms\n");
            }

            double throughput = totalTimeMs > 0 ? (results.size() * 1000.0) / totalTimeMs : 0.0;
            sb.append("Throughput: ").append(String.format(Locale.ROOT, "%.2f", throughput)).append(" articles/second\n");

            return sb.toString();
        }
    }

    public static class ValidationReport {
        private final double precision, recall, f1Score, accuracy;
        private final int tp, tn, fp, fn;

        public ValidationReport(double precision, double recall, double f1Score,
                                double accuracy, int tp, int tn, int fp, int fn) {
            this.precision = precision;
            this.recall = recall;
            this.f1Score = f1Score;
            this.accuracy = accuracy;
            this.tp = tp; this.tn = tn; this.fp = fp; this.fn = fn;
        }

        public double getPrecision() { return precision; }
        public double getRecall()    { return recall; }
        public double getF1Score()   { return f1Score; }
        public double getAccuracy()  { return accuracy; }
        public int getTp() { return tp; } public int getTn() { return tn; }
        public int getFp() { return fp; } public int getFn() { return fn; }

        public String generateReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n╔════════════════════════════════════════════════════════════╗\n");
            sb.append("║            VALIDATION METRICS REPORT                       ║\n");
            sb.append("╚════════════════════════════════════════════════════════════╝\n\n");

            sb.append("Confusion Matrix:\n");
            sb.append("                 Predicted Fake | Predicted Real\n");
            sb.append(String.format(Locale.ROOT, "  Actual Fake:   %14d | %14d%n", tp, fn));
            sb.append(String.format(Locale.ROOT, "  Actual Real:   %14d | %14d%n%n", fp, tn));

            sb.append("Performance Metrics:\n");
            sb.append("  • Accuracy:  ").append(String.format(Locale.ROOT, "%.3f", accuracy)).append("\n");
            sb.append("  • Precision: ").append(String.format(Locale.ROOT, "%.3f", precision)).append("\n");
            sb.append("  • Recall:    ").append(String.format(Locale.ROOT, "%.3f", recall)).append("\n");
            sb.append("  • F1-Score:  ").append(String.format(Locale.ROOT, "%.3f", f1Score)).append("\n");

            return sb.toString();
        }
    }
}
