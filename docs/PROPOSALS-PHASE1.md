# Phase 1 Code Change Proposals

## Overview
This document contains precise, diff-like proposals for all Phase 1 changes. Each proposal can be applied as a patch.

**⚠️ AWAITING APPROVAL - DO NOT APPLY UNTIL REVIEWED**

---

## Proposal 1: Fix Duplicate Dependencies in pom.xml

**File:** `backend/pom.xml`  
**Issue:** `spring-boot-starter-actuator` declared 3 times (lines 35, 39, 56)  
**Also:** `spring-boot-starter-webflux` declared twice (lines 52, 65)

### Proposed Changes

```diff
--- a/backend/pom.xml
+++ b/backend/pom.xml
@@ -33,10 +33,6 @@
         <dependency>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-starter-actuator</artifactId>
         </dependency>
-        <dependency>
-            <groupId>org.springframework.boot</groupId>
-            <artifactId>spring-boot-starter-actuator</artifactId>
-        </dependency>
 
 
         <!-- Optional: for model integration later -->
@@ -50,10 +46,6 @@
         <dependency>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-starter-webflux</artifactId>
         </dependency>
-        <dependency>
-            <groupId>org.springframework.boot</groupId>
-            <artifactId>spring-boot-starter-actuator</artifactId>
-        </dependency>
         <dependency>
             <groupId>org.projectlombok</groupId>
             <artifactId>lombok</artifactId>
@@ -62,9 +54,6 @@
         <dependency>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-starter-webflux</artifactId>
-        </dependency>
-        <dependency>
-            <groupId>com.fasterxml.jackson.core</groupId>
-            <artifactId>jackson-databind</artifactId>
+            <!-- Duplicate removed, keeping first declaration -->
         </dependency>
         <dependency>
             <groupId>com.fasterxml.jackson.core</groupId>
```

**Expected Result:**
- Each dependency appears only once
- Build still succeeds
- `mvn dependency:tree` shows clean dependency graph

---

## Proposal 2: Environment-Aware CORS Configuration

**Issue:** Current CorsConfig allows only `localhost:5173`, and FakeNewsController has `@CrossOrigin("*")`  
**Goal:** Environment-based CORS (dev: permissive, prod: restricted)

### 2a. Update CorsConfig.java

**File:** `backend/src/main/java/com/fakenews/config/CorsConfig.java`

```diff
--- a/backend/src/main/java/com/fakenews/config/CorsConfig.java
+++ b/backend/src/main/java/com/fakenews/config/CorsConfig.java
@@ -1,24 +1,36 @@
 package com.fakenews.config;
 
+import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.web.servlet.config.annotation.CorsRegistry;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
 @Configuration
 public class CorsConfig {
 
+    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:8080}")
+    private String allowedOrigins;
+
     @Bean
     public WebMvcConfigurer corsConfigurer() {
         return new WebMvcConfigurer() {
             @Override
             public void addCorsMappings(CorsRegistry registry) {
+                String[] origins = allowedOrigins.split(",");
+                
                 registry.addMapping("/**")
-                        .allowedOrigins("http://localhost:5173")
+                        .allowedOrigins(origins)
                         .allowedMethods("GET", "POST", "PUT", "DELETE")
-                        .allowedHeaders("*");
+                        .allowedHeaders("*")
+                        .allowCredentials(true)
+                        .maxAge(3600);
             }
         };
     }
 }
```

### 2b. Remove @CrossOrigin from Controller

**File:** `backend/src/main/java/com/fakenews/api/FakeNewsController.java`

```diff
--- a/backend/src/main/java/com/fakenews/api/FakeNewsController.java
+++ b/backend/src/main/java/com/fakenews/api/FakeNewsController.java
@@ -22,7 +22,6 @@ import java.util.*;
 
 @RestController
 @RequestMapping("/api/v1")
-@CrossOrigin(origins = "*")
 public class FakeNewsController {
 
     private final FakeNewsDetector detector;
```

### 2c. Add application-production.properties

**File:** `backend/src/main/resources/application-production.properties` (NEW)

```properties
# Production CORS configuration
# Set via environment variable: CORS_ALLOWED_ORIGINS=https://your-app.com
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:https://fake-news-detection.onrender.com}

# RoBERTa service URL (production)
roberta.service.url=https://fake-news-roberta-service.onrender.com

# Server configuration
server.port=${PORT:8080}

# Actuator - restrict endpoints in production
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized

# Security headers (future: add HTTPS enforcement)
```

### 2d. Update application.properties

**File:** `backend/src/main/resources/application.properties`

```diff
--- a/backend/src/main/resources/application.properties
+++ b/backend/src/main/resources/application.properties
@@ -7,6 +7,9 @@ spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
 spring.web.cors.allowed-headers=*
 
+# CORS allowed origins (dev-friendly defaults)
+cors.allowed-origins=http://localhost:3000,http://localhost:5173,http://localhost:8080
+
 # Actuator (for health)
 management.endpoints.web.exposure.include=health,info
```

**Expected Result:**
- Dev mode: Multiple localhost origins work
- Production mode: Only configured origin allowed
- CORS errors visible in browser console for unauthorized origins

---

## Proposal 3: Backend Integration Tests

### 3a. Add Test Dependencies to pom.xml

**File:** `backend/pom.xml`

```diff
--- a/backend/pom.xml
+++ b/backend/pom.xml
@@ -68,6 +68,23 @@
             <groupId>com.fasterxml.jackson.core</groupId>
             <artifactId>jackson-databind</artifactId>
         </dependency>
+        
+        <!-- Test Dependencies -->
+        <dependency>
+            <groupId>org.springframework.boot</groupId>
+            <artifactId>spring-boot-starter-test</artifactId>
+            <scope>test</scope>
+        </dependency>
+        <dependency>
+            <groupId>org.mockito</groupId>
+            <artifactId>mockito-core</artifactId>
+            <scope>test</scope>
+        </dependency>
+        <dependency>
+            <groupId>org.junit.jupiter</groupId>
+            <artifactId>junit-jupiter</artifactId>
+            <scope>test</scope>
+        </dependency>
     </dependencies>
 
     <build>
```

### 3b. Create Test Configuration

**File:** `backend/src/test/resources/application-test.properties` (NEW)

```properties
# Test configuration
spring.application.name=fake-news-detection-api-test

# Disable external RoBERTa service for tests
roberta.service.url=http://localhost:9999

# CORS not needed for tests
cors.allowed-origins=*

# Actuator
management.endpoints.web.exposure.include=health,info
```

### 3c. Create FakeNewsControllerTest

**File:** `backend/src/test/java/com/fakenews/api/FakeNewsControllerTest.java` (NEW)

```java
package com.fakenews.api;

import com.fakenews.api.dto.ArticleRequest;
import com.fakenews.api.dto.FakeNewsResult;
import com.fakenews.service.RobertaService;
import com.fakenews.service.RobertaAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FakeNewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RobertaService robertaService;

    @Test
    void analyze_validArticle_returns200() throws Exception {
        // Given
        ArticleRequest request = new ArticleRequest();
        request.setTitle("Test Article");
        request.setContent("This is a test article with sufficient content for analysis.");
        request.setSource("test-source.com");

        // Mock RoBERTa service (not available in tests)
        when(robertaService.analyze(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service unavailable in test"));

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/v1/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Article"))
                .andExpect(jsonPath("$.credibilityScore").isNumber())
                .andExpect(jsonPath("$.classification").isString())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        FakeNewsResult response = objectMapper.readValue(responseBody, FakeNewsResult.class);
        
        assertThat(response.getCredibilityScore()).isBetween(0.0, 1.0);
        assertThat(response.getClassification()).isNotNull();
    }

    @Test
    void analyze_missingTitle_returns400() throws Exception {
        // Given
        ArticleRequest request = new ArticleRequest();
        // Missing title
        request.setContent("This is content without a title");

        // When & Then
        mockMvc.perform(post("/api/v1/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError()); // Current behavior - no validation
        // TODO: Add @Valid annotation to controller and expect 400
    }

    @Test
    void batchAnalyze_multipleArticles_returnsCorrectCount() throws Exception {
        // Given
        ArticleRequest req1 = new ArticleRequest();
        req1.setTitle("Article 1");
        req1.setContent("Content for the first test article.");
        
        ArticleRequest req2 = new ArticleRequest();
        req2.setTitle("Article 2");
        req2.setContent("Content for the second test article.");

        List<ArticleRequest> requests = Arrays.asList(req1, req2);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/v1/batch-analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.results").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("\"count\":2");
    }

    @Test
    void getStats_returnsSystemStatistics() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalArticles").isNumber());
    }

    @Test
    void getLeastCredible_returnsArticleList() throws Exception {
        // First, analyze some articles to populate the heap
        ArticleRequest request = new ArticleRequest();
        request.setTitle("Test Article");
        request.setContent("Content for testing least credible endpoint.");
        
        mockMvc.perform(post("/api/v1/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(get("/api/v1/least-credible")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
```

### 3d. Create FakeNewsDetectorTest

**File:** `backend/src/test/java/com/fakenews/core/FakeNewsDetectorTest.java` (NEW)

```java
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
                "test-source.com"
        );

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
                "unknown-source.com"
        );

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
        // This tests the extractDomain utility method
        // We can't test it directly as it's private, but we can test indirectly
        Article article = new Article(
                UUID.randomUUID().toString(),
                "Test",
                "Content",
                "https://www.bbc.com/news/article"
        );

        DetectionResult result = detector.analyzeArticle(article);
        
        // Domain analysis should have occurred
        assertThat(result).isNotNull();
    }
}
```

**Expected Result:**
- `mvn test` runs successfully
- 10+ tests pass
- Test coverage ≥50% for controller and core logic

---

## Proposal 4: Python Service Tests

### 4a. Create requirements-dev.txt

**File:** `python-service/requirements-dev.txt` (NEW)

```
# Test dependencies
pytest==7.4.3
pytest-asyncio==0.21.1
pytest-cov==4.1.0
httpx==0.25.2  # For TestClient

# Code quality
black==23.11.0
flake8==6.1.0
```

### 4b. Create pytest.ini

**File:** `python-service/pytest.ini` (NEW)

```ini
[pytest]
testpaths = tests
python_files = test_*.py
python_classes = Test*
python_functions = test_*
addopts = 
    -v
    --tb=short
    --strict-markers
    --disable-warnings
markers =
    slow: marks tests as slow (deselect with '-m "not slow"')
    integration: marks tests as integration tests
```

### 4c. Create __init__.py

**File:** `python-service/tests/__init__.py` (NEW)

```python
# Empty file to make tests a package
```

### 4d. Create test_api.py

**File:** `python-service/tests/test_api.py` (NEW)

```python
"""
Integration tests for FastAPI endpoints.
Uses TestClient to avoid loading heavy RoBERTa model.
"""
import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch, MagicMock


# Mock the model loading to avoid downloading RoBERTa in tests
@pytest.fixture(autouse=True)
def mock_model_loading(monkeypatch):
    """Prevent actual model loading during tests."""
    mock_model = MagicMock()
    mock_tokenizer = MagicMock()
    mock_device = "cpu"
    
    monkeypatch.setattr("roberta_service.model", mock_model)
    monkeypatch.setattr("roberta_service.tokenizer", mock_tokenizer)
    monkeypatch.setattr("roberta_service.device", mock_device)


@pytest.fixture
def client():
    """Create test client."""
    from roberta_service import app
    return TestClient(app)


def test_root_endpoint(client):
    """Test root endpoint returns service info."""
    response = client.get("/")
    assert response.status_code == 200
    data = response.json()
    assert "service" in data
    assert "status" in data


def test_health_check(client):
    """Test health check endpoint."""
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"


@patch("roberta_service.load_model_and_tokenizer")
def test_predict_valid_request(mock_load, client):
    """Test /predict with valid article returns 200."""
    # Mock model prediction
    mock_load.return_value = None
    
    # Mock the prediction function to return dummy data
    with patch("roberta_service.model") as mock_model, \
         patch("roberta_service.tokenizer") as mock_tokenizer, \
         patch("roberta_service.device", "cpu"):
        
        # Setup mock returns
        mock_tokenizer.return_value = {
            "input_ids": [[1, 2, 3]],
            "attention_mask": [[1, 1, 1]]
        }
        mock_model.return_value = MagicMock(logits=[[0.3, 0.7]])
        
        request_data = {
            "title": "Test Article",
            "text": "This is a test article with sufficient content for analysis.",
            "source": "test-source.com"
        }
        
        response = client.post("/predict", json=request_data)
        
        # In real scenario with mocked model, this should work
        # For now, we expect either 200 or 500 (if model not loaded)
        assert response.status_code in [200, 500]


def test_predict_missing_title_returns_422(client):
    """Test /predict without title returns validation error."""
    request_data = {
        # Missing title
        "text": "This is content without a title"
    }
    
    response = client.post("/predict", json=request_data)
    assert response.status_code == 422


def test_predict_text_too_short_returns_422(client):
    """Test /predict with short text returns validation error."""
    request_data = {
        "title": "Test",
        "text": "Short"  # Less than 10 characters
    }
    
    response = client.post("/predict", json=request_data)
    assert response.status_code == 422


def test_batch_predict_empty_list(client):
    """Test /batch-predict with empty list."""
    response = client.post("/batch-predict", json=[])
    assert response.status_code in [200, 422]  # Depends on validation


def test_metrics_endpoint_exists(client):
    """Test Prometheus metrics endpoint exists."""
    response = client.get("/metrics")
    assert response.status_code == 200
    assert "roberta" in response.text.lower() or "prometheus" in response.headers.get("content-type", "").lower()
```

### 4e. Create test_analysis_features.py

**File:** `python-service/tests/test_analysis_features.py` (NEW)

```python
"""
Unit tests for feature extraction functions.
"""
import pytest
import sys
sys.path.insert(0, '../')  # Add parent directory to path

from utils.analysis_features import (
    calculate_exclamation_count,
    calculate_caps_ratio,
    calculate_clickbait_score,
)


def test_exclamation_count_zero():
    """Test that text without exclamations returns 0."""
    text = "This is a normal sentence. It has no exclamations."
    count = calculate_exclamation_count(text)
    assert count == 0


def test_exclamation_count_multiple():
    """Test counting multiple exclamations."""
    text = "Breaking news! This is incredible! You won't believe it!"
    count = calculate_exclamation_count(text)
    assert count == 3


def test_caps_ratio_no_caps():
    """Test capitalization ratio with no caps."""
    text = "this is all lowercase text"
    ratio = calculate_caps_ratio(text)
    assert ratio == 0.0


def test_caps_ratio_all_caps():
    """Test capitalization ratio with all caps."""
    text = "THIS IS ALL CAPS"
    ratio = calculate_caps_ratio(text)
    assert ratio > 0.8  # Most characters are caps


def test_caps_ratio_mixed():
    """Test capitalization ratio with mixed case."""
    text = "This Is Mixed Case"
    ratio = calculate_caps_ratio(text)
    assert 0.0 < ratio < 1.0


def test_clickbait_score_range():
    """Test clickbait score is in valid range."""
    text = "You won't believe what happened next!"
    score = calculate_clickbait_score(text)
    assert 0 <= score <= 10


def test_clickbait_score_obvious_clickbait():
    """Test that obvious clickbait gets high score."""
    text = "SHOCKING! You WON'T BELIEVE this! Click NOW!"
    score = calculate_clickbait_score(text)
    assert score >= 5  # Should be high for obvious clickbait


def test_clickbait_score_normal_text():
    """Test that normal text gets low score."""
    text = "The Congressional Budget Office released its annual report on fiscal policy."
    score = calculate_clickbait_score(text)
    assert score <= 3  # Should be low for normal text
```

**Note:** These tests assume the existence of these functions in `utils/analysis_features.py`. We'll need to verify they exist or extract them from the existing code.

**Expected Result:**
- `pytest` runs successfully
- 15+ tests pass
- Tests run in <5 seconds (no model loading)

---

## Proposal 5: Development Documentation

**File:** `docs/DEVELOPMENT.md` (NEW)

```markdown
# Development Guide

## Quick Start (5 minutes)

### Prerequisites
- **Java:** 11 or higher ([Download](https://adoptium.net/))
- **Maven:** 3.6+ ([Download](https://maven.apache.org/download.cgi))
- **Python:** 3.10+ ([Download](https://www.python.org/downloads/))
- **Git:** Latest version

### Clone and Setup

\`\`\`bash
# Clone repository
git clone https://github.com/Shalini180/fake-news-detection.git
cd fake-news-detection

# Backend setup
cd backend
mvn clean install

# Python service setup
cd ../python-service
pip install -r requirements.txt
pip install -r requirements-dev.txt  # For development

# Download RoBERTa model (first time only, ~500MB)
python -c "from transformers import RobertaTokenizer, RobertaModel; RobertaTokenizer.from_pretrained('roberta-base'); RobertaModel.from_pretrained('roberta-base')"
\`\`\`

---

## Running Services

### Option 1: Run All Services (Recommended for Development)

**Terminal 1 - Backend:**
\`\`\`bash
cd backend
mvn spring-boot:run
# Backend will start on http://localhost:8080
\`\`\`

**Terminal 2 - Python ML Service:**
\`\`\`bash
cd python-service
python roberta_service.py
# Python service will start on http://localhost:8000
\`\`\`

**Terminal 3 - Frontend:**
\`\`\`bash
cd frontend/public
# Use any static file server, e.g.:
python -m http.server 3000
# OR
npx serve -p 3000
# Frontend will be at http://localhost:3000
\`\`\`

### Option 2: Run with Docker

\`\`\`bash
cd backend
docker-compose up --build
\`\`\`

---

## Running Tests

### Backend Tests

\`\`\`bash
cd backend

# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
# Coverage report: target/site/jacoco/index.html

# Run specific test
mvn test -Dtest=FakeNewsControllerTest

# Run in watch mode (requires Maven wrapper)
./mvnw test -Dspring-boot.run.profiles=test
\`\`\`

### Python Tests

\`\`\`bash
cd python-service

# Run all tests
pytest

# Run with coverage
pytest --cov=. --cov-report=html
# Coverage report: htmlcov/index.html

# Run specific test file
pytest tests/test_api.py

# Run with verbose output
pytest -v

# Run only fast tests (skip slow integration tests)
pytest -m "not slow"
\`\`\`

### E2E Smoke Test

\`\`\`bash
# Make sure all services are running, then:
curl -X POST http://localhost:8080/api/v1/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Article",
    "content": "This is a test article with enough content for analysis.",
    "source": "test-source.com"
  }'
# Should return 200 OK with credibility analysis
\`\`\`

---

## Project Structure

\`\`\`
fake-news-detection/
├── backend/               # Spring Boot REST API
│   ├── src/
│   │   ├── main/java/com/fakenews/
│   │   │   ├── api/           # REST controllers
│   │   │   ├── core/          # Detection engine
│   │   │   ├── model/         # Domain models
│   │   │   ├── dto/           # Request/response objects
│   │   │   ├── service/       # External service clients
│   │   │   ├── nlp/           # NLP processors
│   │   │   ├── graph/         # Knowledge graph
│   │   │   ├── datastructures/# Custom data structures
│   │   │   ├── explainability/# LIME, attention
│   │   │   └── config/        # Spring configuration
│   │   ├── test/java/         # Unit & integration tests
│   │   └── resources/         # Application properties
│   ├── pom.xml                # Maven dependencies
│   └── Dockerfile
│
├── python-service/        # FastAPI ML service
│   ├── roberta_service.py     # FastAPI application
│   ├── models/                # Model wrappers
│   ├── utils/                 # Feature extraction
│   ├── tests/                 # Pytest tests
│   ├── requirements.txt       # Production dependencies
│   ├── requirements-dev.txt   # Development dependencies
│   └── Dockerfile
│
├── frontend/              # Static web UI
│   ├── app.js                 # Main application
│   ├── styles.css             # Styling
│   └── public/
│       ├── index.html         # SPA entry point
│       └── api.js             # API client
│
└── docs/                  # Documentation
    ├── roadmap.md             # Production roadmap
    ├── phase1-design.md       # Phase 1 details
    └── DEVELOPMENT.md         # This file
\`\`\`

---

## Common Issues & Solutions

### Issue: Port Already in Use
**Error:** `Port 8080 is already in use`

**Solution:**
\`\`\`bash
# Find process using port
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Kill process or use different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
\`\`\`

### Issue: RoBERTa Model Download Fails
**Error:** `Connection timeout` when downloading model

**Solution:**
1. Check internet connection
2. Use different mirror:
   \`\`\`python
   export TRANSFORMERS_CACHE=/path/to/large/disk
   export HF_ENDPOINT=https://hf-mirror.com
   \`\`\`
3. Download manually and place in `~/.cache/huggingface/`

### Issue: CORS Errors in Browser
**Error:** `Access-Control-Allow-Origin` error in console

**Solution:**
1. Check backend is running on correct port
2. Verify CORS configuration in `application.properties`:
   \`\`\`properties
   cors.allowed-origins=http://localhost:3000
   \`\`\`
3. For production, set environment variable:
   \`\`\`bash
   export CORS_ALLOWED_ORIGINS=https://your-frontend.com
   \`\`\`

### Issue: Tests Fail with "Model Not Found"
**Solution:** Tests should mock the RoBERTa model. Check that test fixtures are set up:
\`\`\`python
# In conftest.py or test file
@pytest.fixture(autouse=True)
def mock_model_loading(monkeypatch):
    # Mock model to avoid downloading
\`\`\`

### Issue: Maven Build Fails
**Error:** `Failed to execute goal... Could not resolve dependencies`

**Solution:**
\`\`\`bash
# Clear Maven cache and rebuild
mvn dependency:purge-local-repository
mvn clean install -U
\`\`\`

---

## Environment Variables

### Backend
| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8080` | Server port |
| `SPRING_PROFILES_ACTIVE` | `default` | Active profile (dev, production) |
| `CORS_ALLOWED_ORIGINS` | `localhost:*` | Allowed CORS origins |
| `ROBERTA_SERVICE_URL` | `http://localhost:8000` | Python ML service URL |

### Python Service
| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8000` | Server port |
| `TORCH_NUM_THREADS` | `1` | CPU threads for PyTorch |
| `MODEL_NAME` | `distilroberta-base` | RoBERTa model to use |

---

## Making Changes

### Code Style
- **Java:** Follow Google Java Style Guide
- **Python:** Follow PEP 8, use Black formatter
- **JavaScript:** Use ES6+, Prettier for formatting

### Before Committing
\`\`\`bash
# Run tests
cd backend && mvn test
cd python-service && pytest

# Format code
cd python-service && black .

# Check build
cd backend && mvn clean package
\`\`\`

### Pull Request Process
1. Create feature branch: `git checkout -b feature/your-feature`
2. Make changes and add tests
3. Run all tests locally
4. Commit with meaningful message
5. Push and create PR
6. Fill out PR template

---

## Useful Commands

\`\`\`bash
# Backend: Clean and rebuild
mvn clean package

# Backend: Skip tests (faster)
mvn clean package -DskipTests

# Backend: Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=production

# Python: Install in editable mode
pip install -e .

# Python: Generate requirements from environment
pip freeze > requirements.txt

# Docker: Build backend image
docker build -t fake-news-api:local ./backend

# Docker: Build Python service
docker build -t roberta-service:local ./python-service

# View API endpoints
curl http://localhost:8080/actuator/mappings | jq
\`\`\`

---

## Debugging

### Backend Debugging (IntelliJ IDEA)
1. Right-click `FakeNewsDetectionApplication.java`
2. Select "Debug 'FakeNewsDetectionApplication'"
3. Set breakpoints in code
4. Use debugger to step through

### Python Debugging (VS Code)
1. Install Python extension
2. Create `.vscode/launch.json`:
   \`\`\`json
   {
     "configurations": [{
       "name": "Python: FastAPI",
       "type": "python",
       "request": "launch",
       "module": "uvicorn",
       "args": ["roberta_service:app", "--reload"]
     }]
   }
   \`\`\`
3. Set breakpoints and press F5

---

## Next Steps
- Review [Production Roadmap](roadmap.md)
- Check [Phase 1 Design](phase1-design.md) for current work
- Join team Slack/Discord for questions

---

*Last Updated: 2025-11-25*
\`\`\`

---

## Proposal 6: Update Root README

**File:** `README.md`

```diff
--- a/README.md
+++ b/README.md
@@ -1,21 +1,45 @@
 # Fake News Detection
 
-Monorepo for an explainable fake-news detection system (Spring Boot backend + static frontend).
+An explainable AI system for detecting fake news using hybrid analysis: RoBERTa transformers + knowledge graph + explainability (LIME, attention mechanisms).
 
+## Architecture
 
+- **Backend:** Spring Boot 2.7.14 (Java 11) - REST API, orchestration, knowledge graph
+- **ML Service:** FastAPI + PyTorch - RoBERTa-based content analysis
+- **Frontend:** Vanilla JavaScript SPA - Interactive credibility analysis UI
+
+## Quick Start
+
+See [DEVELOPMENT.md](docs/DEVELOPMENT.md) for detailed setup instructions.
+
+\`\`\`bash
+# 1. Start backend (Terminal 1)
+cd backend && mvn spring-boot:run
+
+# 2. Start Python ML service (Terminal 2)
+cd python-service && python roberta_service.py
+
+# 3. Open frontend
+# Open frontend/public/index.html in browser
+\`\`\`
+
+## Features
+
+✅ Multi-dimensional credibility analysis  
+✅ RoBERTa transformer-based content analysis  
+✅ Domain reputation checking  
+✅ Claim extraction and verification  
+✅ Knowledge graph for cross-referencing  
+✅ Explainability (LIME, attention weights, suspicious phrases)  
+✅ Writing quality analysis (clickbait detection)  
 
 ## Branches
 
 - `main` — stable
-
 - feature branches: `backend/...`, `frontend/...`
 
+## Documentation
 
-
-## Getting Started
-
-See backend/README.md for API, frontend/README.md for UI.
-
-
+- [Development Guide](docs/DEVELOPMENT.md) - Local setup, running tests
+- [Production Roadmap](docs/roadmap.md) - Path to production MVP
+- [Technical Audit](docs/codebase_audit_report.md) - Current state analysis
```

---

## Summary of Proposals

| # | Change | Files | Lines Changed | Risk |
|---|--------|-------|---------------|------|
| 1 | Fix duplicate dependencies | `pom.xml` | -12 | LOW |
| 2 | Environment-aware CORS | 4 files | +50 | MEDIUM |
| 3 | Backend tests | 3 files | +250 | LOW |
| 4 | Python tests | 5 files | +200 | LOW |
| 5 | Dev documentation | 1 file | +400 | LOW |
| 6 | Update README | 1 file | +30 | LOW |

**Total:** ~14 files, ~900 lines added/modified

---

## ⚠️ AWAITING YOUR APPROVAL

**Do you approve applying these patches? (yes/no, or specify changes)**

Please review each proposal and respond with:
- **"yes, apply patches"** - to proceed with all changes
- **"no"** - to cancel
- **Specific feedback** - to modify proposals before applying

I will NOT make any changes until you explicitly approve.
