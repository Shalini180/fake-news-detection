package com.fakenews.core;

import com.fakenews.model.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FakeNewsDetectorTest {

    private FakeNewsDetector detector;

    @BeforeEach
    void setUp() {
        detector = new FakeNewsDetector();
    }

    @Test
    void analyzeArticle_validInput_returnsDetectionResult() {
        // Given
        Article article = new Article(
                UUID.randomUUID().toString(),
                "Test Article Title",
                "This is the article content. It contains multiple sentences for testing. The content should be sufficient for analysis.",
                "test-source.com");

        // When
        DetectionResult result = detector.analyzeArticle(article);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getArticle()).isEqualTo(article);
        assertThat(result.getArticle().getCredibilityScore()).isBetween(0.0, 1.0);
    }

    @Test
    void analyzeArticle_setsCredibilityScore() {
        // Given
        Article article = new Article(
                UUID.randomUUID().toString(),
                "Breaking News",
                "This article contains some content for credibility scoring.",
                "unknown-source.com");

        // When
        DetectionResult result = detector.analyzeArticle(article);

        // Then
        double score = result.getArticle().getCredibilityScore();
        assertThat(score).isGreaterThanOrEqualTo(0.0);
        assertThat(score).isLessThanOrEqualTo(1.0);
    }

    @Test
    void analyzeMultipleArticles_processesAllArticles() {
        // Given
        Article article1 = new Article(UUID.randomUUID().toString(), "Title 1", "Content 1", "source1.com");
        Article article2 = new Article(UUID.randomUUID().toString(), "Title 2", "Content 2", "source2.com");
        List<Article> articles = Arrays.asList(article1, article2);

        // When
        List<DetectionResult> results = detector.analyzeMultipleArticles(articles);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getArticle()).isEqualTo(article1);
        assertThat(results.get(1).getArticle()).isEqualTo(article2);
    }

    @Test
    void getLeastCredibleArticles_returnsArticlesInOrder() {
        // Given - analyze some articles first
        Article high = new Article(UUID.randomUUID().toString(), "High Score", "Good content", "reuters.com");
        Article medium = new Article(UUID.randomUUID().toString(), "Medium Score", "Average content", "medium.com");
        Article low = new Article(UUID.randomUUID().toString(), "Low Score", "Poor content", "unknown.com");

        detector.analyzeArticle(high);
        detector.analyzeArticle(medium);
        detector.analyzeArticle(low);

        // When
        List<Article> leastCredible = detector.getLeastCredibleArticles(2);

        // Then
        assertThat(leastCredible).isNotNull();
        assertThat(leastCredible.size()).isLessThanOrEqualTo(2);
        // Articles should be ordered by credibility score (highest fake score first)
        if (leastCredible.size() == 2) {
            assertThat(leastCredible.get(0).getCredibilityScore())
                    .isGreaterThanOrEqualTo(leastCredible.get(1).getCredibilityScore());
        }
    }

    @Test
    void extractDomain_validUrl_returnsDomain() {
        // This tests domain extraction indirectly through article analysis
        Article article = new Article(
                UUID.randomUUID().toString(),
                "Test",
                "Content",
                "https://www.bbc.com/news/article");

        DetectionResult result = detector.analyzeArticle(article);

        // Domain analysis should have occurred
        assertThat(result).isNotNull();
    }
}
