# Phase 1: Foundation - Detailed Design

## Overview
This phase establishes the foundation for production readiness by adding critical test coverage, fixing security issues, and creating developer documentation.

**Duration:** 1.5 weeks  
**Effort:** 20-24 hours  
**Risk Reduction:** 🔴 CRITICAL → 🟡 MEDIUM

---

## Task Breakdown

### Task 1: Fix Duplicate Dependencies in pom.xml ✅
**Priority:** P0 (Critical - 5 minutes)  
**Risk:** Build confusion, potential version conflicts  
**Impact:** 10/10 | Effort: 1/10

#### Files to Touch
- `backend/pom.xml` (lines 35, 39, 56)

#### Approach
Remove duplicate declarations of `spring-boot-starter-actuator`. Keep only one instance at line 35.

#### Expected Observable Behavior
- `mvn dependency:tree` shows actuator dependency only once
- Build succeeds without warnings
- No functional change

---

### Task 2: Harden CORS Configuration ✅
**Priority:** P0 (Critical Security - 2 hours)  
**Risk:** CSRF attacks, unauthorized cross-origin requests  
**Impact:** 10/10 | Effort: 3/10

#### Files to Touch
1. `backend/src/main/java/com/fakenews/config/CorsConfig.java` (new file)
2. `backend/src/main/resources/application.properties` (add CORS config)
3. `backend/src/main/resources/application-production.properties` (new file)
4. `backend/src/main/java/com/fakenews/api/FakeNewsController.java` (remove `@CrossOrigin("*")`)

#### Approach
1. **Create environment-aware CORS configuration:**
   - Development: Allow `http://localhost:*` for local development
   - Production: Allow specific frontend origin from environment variable
   
2. **Use Spring profiles:**
   - Default profile: permissive (for local dev)
   - Production profile: restrictive

3. **Remove controller-level `@CrossOrigin("*")`** - move to global config

#### Expected Observable Behavior
- **Dev mode:** Frontend at `http://localhost:3000` can call API
- **Production mode:** Only configured origin can call API
- **Test:** Browser console shows CORS errors when calling from unauthorized origin in prod

---

### Task 3: Backend Integration Tests - FakeNewsController ✅
**Priority:** P1 (High - 6 hours)  
**Risk:** Regression in API endpoints  
**Impact:** 10/10 | Effort: 6/10

#### Files to Touch
1. `backend/pom.xml` (add test dependencies)
2. `backend/src/test/java/com/fakenews/api/FakeNewsControllerTest.java` (new file)
3. `backend/src/test/resources/application-test.properties` (new file)

#### Approach
1. **Add test dependencies:**
   - `spring-boot-starter-test` (includes JUnit 5, Mockito, MockMvc)
   - `spring-boot-starter-webflux` (for WebTestClient - already present)

2. **Test `/analyze` endpoint:**
   - **Happy path:** Valid article returns 200 OK with correct structure
   - **Validation errors:** Missing title returns 400
   - **Validation errors:** Content too short returns 400
   - **Mock RoBERTa service:** Use WireMock or `@MockBean`

3. **Test `/batch-analyze` endpoint:**
   - Multiple articles processed correctly
   - Batch count matches input

4. **Test `/stats` endpoint:**
   - Returns system statistics

5. **Test `/least-credible` endpoint:**
   - Returns articles in correct order

#### Test Structure
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class FakeNewsControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean RobertaService robertaService;
    
    @Test void analyze_validArticle_returns200() { ... }
    @Test void analyze_missingTitle_returns400() { ... }
    @Test void batchAnalyze_multipleArticles_returnsCorrectCount() { ... }
}
```

#### Expected Observable Behavior
- `mvn test` runs and passes all tests
- Test output shows 5+ passing tests
- Code coverage for `FakeNewsController` ≥70%

---

### Task 4: Backend Unit Tests - FakeNewsDetector ✅
**Priority:** P1 (High - 3 hours)  
**Risk:** Incorrect credibility scoring  
**Impact:** 9/10 | Effort: 4/10

#### Files to Touch
1. `backend/src/test/java/com/fakenews/core/FakeNewsDetectorTest.java` (new file)

#### Approach
1. **Test core detection pipeline:**
   - Article analysis produces DetectionResult
   - Credibility score is within 0.0-1.0 range
   - Classification matches score thresholds

2. **Test domain credibility:**
   - Known reputable domain gets high score
   - Unknown domain gets neutral score

3. **Test claim extraction:**
   - Articles with claims return non-empty claim list

4. **Test knowledge graph integration:**
   - Articles added to graph
   - Cross-references detected

#### Test Structure
```java
class FakeNewsDetectorTest {
    private FakeNewsDetector detector;
    
    @BeforeEach void setUp() { 
        detector = new FakeNewsDetector(); 
    }
    
    @Test void analyzeArticle_validInput_returnsResult() { ... }
    @Test void computeFinalScore_weightedCorrectly() { ... }
}
```

#### Expected Observable Behavior
- `mvn test` shows detector tests passing
- Validates core business logic
- Code coverage for `FakeNewsDetector` ≥60%

---

### Task 5: Python Service Tests ✅
**Priority:** P1 (High - 5 hours)  
**Risk:** ML service regressions  
**Impact:** 9/10 | Effort: 5/10

#### Files to Touch
1. `python-service/requirements-dev.txt` (new file - test dependencies)
2. `python-service/tests/__init__.py` (new file)
3. `python-service/tests/test_api.py` (new file)
4. `python-service/tests/test_analysis_features.py` (new file)
5. `python-service/pytest.ini` (new file - pytest config)

#### Approach

##### 5a. FastAPI Endpoint Tests (`test_api.py`)
1. **Test `/health` endpoint:**
   - Returns 200 OK
   - Model status reported

2. **Test `/predict` endpoint:**
   - Valid request returns 200 with correct schema
   - Missing title returns 422 validation error
   - Content too short returns 422

3. **Use TestClient from FastAPI:**
   - No need to start server
   - Mock model loading to avoid downloading RoBERTa

##### 5b. Feature Extraction Tests (`test_analysis_features.py`)
1. **Test writing quality metrics:**
   - Exclamation count calculated correctly
   - Capitalization ratio accurate
   - Clickbait score in 0-10 range

2. **Test suspicious phrase detection:**
   - Known phrases identified
   - Clean text returns empty list

#### Test Structure
```python
# tests/test_api.py
from fastapi.testclient import TestClient
from roberta_service import app

def test_health_check():
    client = TestClient(app)
    response = client.get("/health")
    assert response.status_code == 200

def test_predict_valid_request():
    client = TestClient(app)
    response = client.post("/predict", json={
        "title": "Test Article",
        "text": "This is a test article with sufficient content."
    })
    assert response.status_code == 200
    data = response.json()
    assert "prediction" in data
    assert "confidence" in data
```

#### Expected Observable Behavior
- `pytest` command runs successfully
- All tests pass (10+ tests)
- No model download during tests (mocked)
- Test run completes in <5 seconds

---

### Task 6: Development Documentation ✅
**Priority:** P2 (Medium - 2 hours)  
**Risk:** Onboarding friction for new developers  
**Impact:** 7/10 | Effort: 3/10

#### Files to Touch
1. `docs/DEVELOPMENT.md` (new file)
2. `README.md` (update with link to DEVELOPMENT.md)

#### Approach
Create comprehensive development guide covering:

1. **Prerequisites:**
   - Java 11+, Maven 3.6+
   - Python 3.10+
   - Docker (optional)

2. **Local Setup:**
   - Clone repository
   - Install dependencies (backend + Python)
   - Download RoBERTa model (Python service)

3. **Running Services:**
   - Backend: `mvn spring-boot:run`
   - Python service: `python roberta_service.py`
   - Frontend: Static file server

4. **Running Tests:**
   - Backend: `mvn test`
   - Python: `pytest`

5. **Common Issues:**
   - Port conflicts
   - Model download failures
   - CORS errors

6. **Project Structure:**
   - Directory layout
   - Key modules

7. **Making Changes:**
   - Coding standards
   - Test expectations
   - PR process

#### Expected Observable Behavior
- New developer can follow DEVELOPMENT.md and start in <15 minutes
- All commands execute successfully
- Troubleshooting section addresses common issues

---

## Verification Plan

### Automated Verification
After implementing all changes:

1. **Backend Tests:**
   ```bash
   cd backend
   mvn clean test
   ```
   **Expected:** All tests pass, 0 failures

2. **Python Tests:**
   ```bash
   cd python-service
   pytest -v
   ```
   **Expected:** All tests pass, 10+ tests

3. **Build Verification:**
   ```bash
   cd backend
   mvn clean package
   ```
   **Expected:** Build succeeds, no duplicate dependency warnings

### Manual Verification

1. **CORS Testing:**
   - Start backend with prod profile: `SPRING_PROFILES_ACTIVE=production mvn spring-boot:run`
   - Try accessing from `http://localhost:3000` → Should fail (CORS error)
   - Configure allowed origin → Should succeed

2. **End-to-End Smoke Test:**
   - Start all services (backend, Python, frontend)
   - Submit test article through UI
   - Verify response includes credibility score, classification, RoBERTa analysis

### Documentation Verification
- Follow DEVELOPMENT.md step-by-step on fresh machine
- Confirm all commands work
- Time the setup process (target: <15 minutes)

---

## Success Criteria

- [ ] `mvn test` passes with ≥10 backend tests
- [ ] `pytest` passes with ≥10 Python tests
- [ ] No duplicate dependencies in `pom.xml`
- [ ] CORS restricted in production profile
- [ ] `docs/DEVELOPMENT.md` created with setup guide
- [ ] `docs/roadmap.md` created with full plan
- [ ] All changes documented in `docs/changelog-phase1.md`

---

## Rollback Plan
If any issues occur:
1. All changes in separate commits
2. Git revert specific commits
3. Tests ensure no breaking changes merged

---

*Phase 1 Design - Ready for Implementation*
