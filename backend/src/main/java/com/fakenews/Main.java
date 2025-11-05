package com.fakenews;

import com.fakenews.core.*;
import com.fakenews.model.*;
import com.fakenews.visualization.VisualizationEngine;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== FAKE NEWS DETECTION – CLI DEMO ===\n");

        FakeNewsDetector detector = new FakeNewsDetector();
        List<Article> articles = createSampleArticles();

        List<DetectionResult> results = new ArrayList<>();
        System.out.println("Analyzing articles...\n");
        for (Article a : articles) {
            DetectionResult r = detector.analyzeArticle(a);
            results.add(r);
            System.out.println("----------------------------------------");
            System.out.print(r.generateReport());
            System.out.println();
        }

        // Top-N least credible
        System.out.println("\n=== TOP 3 LEAST CREDIBLE ARTICLES ===\n");
        List<Article> least = detector.getLeastCredibleArticles(3);
        for (int i = 0; i < least.size(); i++) {
            Article a = least.get(i);
            System.out.printf("%d) %s%n   Score: %.2f%n   Source: %s%n%n",
                    i + 1, a.getTitle(), a.getCredibilityScore(), a.getSource());
        }

        // ASCII charts / DOT export
        VisualizationEngine viz = new VisualizationEngine();
        System.out.println(viz.generateCredibilityChart(articles));

        if (!articles.isEmpty()) {
            String rootId = articles.get(0).getId();
            String dot = viz.generateGraphVizDOT(detector.getKnowledgeGraph(), rootId);
            viz.saveVisualization(dot, "knowledge-graph.dot");
            System.out.println("Graphviz DOT saved to knowledge-graph.dot");
        }

        System.out.println("\n=== DONE ===");
    }

    private static List<Article> createSampleArticles() {
        List<Article> list = new ArrayList<>();

        list.add(new Article(
                "art001",
                "BREAKING: SHOCKING Discovery That Changes EVERYTHING!!!",
                "BREAKING NEWS!!! Scientists have made an UNBELIEVABLE discovery that will SHOCK the world. " +
                        "This AMAZING finding is INCREDIBLE and will change your life FOREVER. You won't believe what happened next! " +
                        "Sources say this is the most CATASTROPHIC event in history. Everyone is TERRIFIED.",
                "clickbait.net/news"
        ));

        list.add(new Article(
                "art002",
                "Climate Change Report Released by Scientific Panel",
                "According to a new report published by the Intergovernmental Panel on Climate Change, " +
                        "global temperatures have risen by 1.1 degrees Celsius since pre-industrial times. " +
                        "Dr. Sarah Johnson, lead author of the study, said the findings are based on comprehensive data analysis. " +
                        "The report was peer-reviewed and published in Nature Climate. Multiple research institutions contributed to the study.",
                "reuters.com/environment"
        ));

        list.add(new Article(
                "art003",
                "New Technology Breakthrough Announced",
                "A major technology company announced a breakthrough in quantum computing today. " +
                        "The CEO claims this will revolutionize the industry. Some experts are AMAZED while others remain skeptical. " +
                        "The company's stock price increased by 15% following the announcement.",
                "technews.example.com/articles"
        ));

        list.add(new Article(
                "art004",
                "Economic Growth Figures Released for Q3",
                "The Bureau of Economic Analysis reported today that GDP growth for the third quarter was 2.8%, " +
                        "according to preliminary estimates. Chief economist Mark Williams stated that the growth was driven " +
                        "by consumer spending and business investment. The report, released at 8:30 AM EST, includes detailed breakdowns by sector.",
                "apnews.com/business"
        ));

        return list;
    }
}
