package com.fakenews.visualization;

import com.fakenews.core.DetectionResult;
import com.fakenews.explainability.AttentionExplanation;
import com.fakenews.explainability.ComprehensiveExplanation;
import com.fakenews.explainability.LIMEExplanation;
import com.fakenews.explainability.ExplainabilityEngine;
import com.fakenews.graph.GraphEdge;
import com.fakenews.graph.GraphNode;
import com.fakenews.graph.KnowledgeGraph;
import com.fakenews.model.Article;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Helper for generating lightweight visual outputs (HTML, DOT, ASCII, JSON)
 * to inspect analysis results during development.
 */
public class VisualizationEngine {

    // ---------- Attention HTML (tokens heat) ----------
    public String generateAttentionVisualization(Article article, AttentionExplanation explanation) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<meta charset='utf-8'>\n<title>Attention: ")
                .append(escapeHTML(article.getTitle())).append("</title>\n");
        html.append("<style>\n");
        html.append("body{font-family:sans-serif;padding:16px}\n");
        html.append(".token{display:inline-block;margin:2px;padding:4px;border-radius:6px}\n");
        html.append(".high-attention{background-color:rgba(255,0,0,0.8);}\n");
        html.append(".medium-attention{background-color:rgba(255,165,0,0.5);}\n");
        html.append(".low-attention{background-color:rgba(0,255,0,0.3);}\n");
        html.append("</style>\n</head>\n<body>\n");
        html.append("<h2>Attention Visualization: ").append(escapeHTML(article.getTitle())).append("</h2>\n");
        html.append("<div class='content'>\n");

        List<ExplainabilityEngine.TokenAttention> tokens = explanation.getTokenAttentions();
        for (ExplainabilityEngine.TokenAttention t : tokens) {
            String css = getAttentionClass(t.getWeight());
            html.append("<span class='token ").append(css).append("' title='Attention: ")
                    .append(String.format(Locale.ROOT, "%.3f", t.getWeight()))
                    .append("'>").append(escapeHTML(t.getToken())).append("</span>\n");
        }

        html.append("</div>\n</body>\n</html>");
        return html.toString();
    }

    // ---------- Graphviz DOT ----------
    public String generateGraphVizDOT(KnowledgeGraph graph, String rootNodeId) {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph FakeNewsGraph {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=box, style=rounded];\n\n");

        Set<String> visited = new HashSet<>();
        Deque<String> q = new ArrayDeque<>();
        q.offer(rootNodeId);
        visited.add(rootNodeId);

        while (!q.isEmpty()) {
            String nodeId = q.poll();
            GraphNode node = graph.getNode(nodeId);

            if (node != null) {
                String color = getNodeColor(node.getType());
                dot.append("  \"").append(nodeId).append("\" [fillcolor=\"")
                        .append(color).append("\", style=filled];\n");

                List<GraphEdge> edges = graph.getEdges(nodeId);
                for (GraphEdge edge : edges) {
                    dot.append("  \"").append(edge.getFrom()).append("\" -> \"")
                            .append(edge.getTo()).append("\" [label=\"")
                            .append(edge.getRelationship()).append("\", weight=")
                            .append(edge.getWeight()).append("];\n");

                    if (visited.add(edge.getTo())) q.offer(edge.getTo());
                }
            }
        }

        dot.append("}\n");
        return dot.toString();
    }

    // ---------- ASCII charts ----------
    public String generateCredibilityChart(List<Article> articles) {
        StringBuilder chart = new StringBuilder();
        chart.append("\nCredibility Score Chart (0 = Credible, 1 = Fake)\n");
        chart.append("======================================================================\n");

        for (Article a : articles) {
            String title = truncate(a.getTitle(), 30);
            double score = a.getCredibilityScore();
            int bar = (int) Math.round(score * 40);
            chart.append(String.format("%-30s |", title));
            chart.append("█".repeat(Math.max(0, bar)));
            chart.append(" ".repeat(Math.max(0, 40 - bar)));
            chart.append(String.format("| %.2f%n", score));
        }

        chart.append("======================================================================\n");
        return chart.toString();
    }

    public String generateFeatureImportanceChart(Map<String, Double> features) {
        StringBuilder chart = new StringBuilder();
        chart.append("\nFeature Importance Analysis\n");
        chart.append("======================================================================\n");

        List<Map.Entry<String, Double>> sorted = new ArrayList<>(features.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        for (Map.Entry<String, Double> e : sorted) {
            String feature = truncate(formatFeatureName(e.getKey()), 25);
            double v = e.getValue();
            int bar = (int) Math.round(v * 40);
            chart.append(String.format("%-25s |", feature));
            chart.append("▓".repeat(Math.max(0, bar)));
            chart.append("░".repeat(Math.max(0, 40 - bar)));
            chart.append(String.format("| %.3f%n", v));
        }

        chart.append("======================================================================\n");
        return chart.toString();
    }

    // ---------- JSON export ----------
    public String exportToJSON(List<DetectionResult> results) {
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"results\": [\n");

        for (int i = 0; i < results.size(); i++) {
            DetectionResult r = results.get(i);
            Article a = r.getArticle();
            ComprehensiveExplanation ex = r.getExplanation();

            json.append("    {\n");
            json.append("      \"id\": \"").append(escapeJSON(a.getId())).append("\",\n");
            json.append("      \"title\": \"").append(escapeJSON(a.getTitle())).append("\",\n");
            json.append("      \"source\": \"").append(escapeJSON(a.getSource())).append("\",\n");
            json.append("      \"credibility_score\": ").append(a.getCredibilityScore()).append(",\n");
            json.append("      \"classification\": \"").append(getClassification(a)).append("\",\n");
            json.append("      \"claims_count\": ").append(r.getClaims().size()).append(",\n");
            json.append("      \"explanation\": \"").append(escapeJSON(
                    ex != null ? ex.getNaturalLanguageExplanation() : "")).append("\",\n");

            json.append("      \"features\": {\n");
            List<String> keys = new ArrayList<>(a.getFeatureScores().keySet());
            for (int j = 0; j < keys.size(); j++) {
                String k = keys.get(j);
                json.append("        \"").append(escapeJSON(k)).append("\": ")
                        .append(a.getFeatureScores().get(k));
                if (j < keys.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("      }\n");

            json.append("    }");
            if (i < results.size() - 1) json.append(",");
            json.append("\n");
        }

        json.append("  ]\n}\n");
        return json.toString();
    }

    // ---------- Statistical report ----------
    public String generateStatisticalReport(List<DetectionResult> results) {
        StringBuilder report = new StringBuilder();
        report.append("\n╔════════════════════════════════════════════════════════════╗\n");
        report.append("║         FAKE NEWS DETECTION STATISTICAL REPORT             ║\n");
        report.append("╚════════════════════════════════════════════════════════════╝\n\n");

        int total = results.size();
        int likelyFake = 0, suspicious = 0, credible = 0;
        double avg = 0.0;

        for (DetectionResult r : results) {
            double s = r.getArticle().getCredibilityScore();
            avg += s;
            if (s > 0.7)      likelyFake++;
            else if (s > 0.5) suspicious++;
            else              credible++;
        }
        if (total > 0) avg /= total;

        report.append("Total Articles Analyzed: ").append(total).append("\n");
        report.append("Average Credibility Score: ").append(String.format(Locale.ROOT, "%.3f", avg)).append("\n\n");
        report.append("Classification Breakdown:\n");
        report.append("  ├─ Likely Fake:      ").append(likelyFake)
                .append(" (").append(pct(likelyFake, total)).append(")\n");
        report.append("  ├─ Suspicious:       ").append(suspicious)
                .append(" (").append(pct(suspicious, total)).append(")\n");
        report.append("  └─ Likely Credible:  ").append(credible)
                .append(" (").append(pct(credible, total)).append(")\n\n");

        report.append("Average Feature Scores:\n");
        Map<String, Double> avgFeatures = computeAverageFeatures(results);
        for (Map.Entry<String, Double> e : avgFeatures.entrySet()) {
            report.append("  • ").append(formatFeatureName(e.getKey())).append(": ")
                    .append(String.format(Locale.ROOT, "%.3f", e.getValue())).append("\n");
        }

        report.append("\n").append("═".repeat(62)).append("\n");
        return report.toString();
    }

    // ---------- Helpers ----------
    private String getAttentionClass(double w) {
        if (w > 0.7) return "high-attention";
        if (w > 0.4) return "medium-attention";
        return "low-attention";
    }

    private String getNodeColor(String type) {
        if (type == null) return "lightgray";
        switch (type) {
            case "ARTICLE":  return "lightblue";
            case "CLAIM":    return "lightyellow";
            case "SOURCE":   return "lightgreen";
            case "EVIDENCE": return "lightcoral";
            default:         return "lightgray";
        }
    }

    private String truncate(String s, int n) {
        if (s == null) return "";
        if (s.length() <= n) return s;
        return s.substring(0, Math.max(0, n - 3)) + "...";
    }

    private String escapeJSON(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String escapeHTML(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }

    private String formatFeatureName(String name) {
        if (name == null || name.isBlank()) return "";
        String spaced = name.replace("_", " ");
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    private String pct(int part, int total) {
        double p = total == 0 ? 0 : (100.0 * part / total);
        return String.format(Locale.ROOT, "%.1f%%", p);
    }

    private Map<String, Double> computeAverageFeatures(List<DetectionResult> results) {
        Map<String, Double> totals = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();

        for (DetectionResult r : results) {
            for (Map.Entry<String, Double> e : r.getArticle().getFeatureScores().entrySet()) {
                totals.merge(e.getKey(), e.getValue(), Double::sum);
                counts.merge(e.getKey(), 1, Integer::sum);
            }
        }
        Map<String, Double> out = new HashMap<>();
        for (String k : totals.keySet()) {
            out.put(k, totals.get(k) / counts.get(k));
        }
        return out;
    }

    private String getClassification(Article a) {
        double s = a.getCredibilityScore();
        if (s > 0.7) return "LIKELY_FAKE";
        if (s > 0.5) return "SUSPICIOUS";
        if (s > 0.3) return "MIXED_SIGNALS";
        return "LIKELY_CREDIBLE";
    }

    // Save to file (optional)
    public void saveVisualization(String content, String filename) {
        try (PrintWriter w = new PrintWriter(new FileWriter(filename))) {
            w.print(content);
            System.out.println("Visualization saved to: " + filename);
        } catch (IOException e) {
            System.err.println("Error saving visualization: " + e.getMessage());
        }
    }
}
