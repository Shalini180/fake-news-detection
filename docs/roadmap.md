# Fake News Detection System - Production Roadmap

## Overview

This roadmap transforms the fake-news-detection system from a researchy prototype to a production-ready MVP. Tasks are organized by phase and prioritized by **risk × impact / effort**.

---

## Risk Assessment Summary

| Risk Area | Severity | Current State | Target State |
|-----------|----------|---------------|--------------|
| **Test Coverage** | 🔴 CRITICAL | 0% - No tests | 80%+ integration coverage |
| **Data Persistence** | 🔴 CRITICAL | In-memory only | PostgreSQL with migrations |
| **Security (CORS)** | 🔴 CRITICAL | Wildcard `*` | Environment-based restrictions |
| **CI/CD** | 🟡 HIGH | None | GitHub Actions pipeline |
| **Documentation** | 🟡 HIGH | Minimal README | API docs + setup guides |
| **Monitoring** | 🟡 MEDIUM | Basic health checks | Prometheus + alerting |
| **Error Handling** | 🟡 MEDIUM | Generic errors | Structured error responses |

---

## Phase 1: Foundation (Week 1-2)
**Goal:** Establish testing foundation, fix critical security issues, document development workflow

### Priority 1: Critical Fixes (Impact: 10/10, Effort: 2/10)
- [x] **Fix duplicate `spring-boot-starter-actuator` in `pom.xml`**
  - **Risk:** Build confusion, potential version conflicts
  - **Effort:** 5 minutes
  - **Files:** `backend/pom.xml` (lines 35, 39, 56)
  
- [x] **Harden CORS configuration**
  - **Risk:** CSRF attacks, unauthorized access
  - **Effort:** 2 hours
  - **Approach:** Environment-aware config (dev: `*`, prod: specific origins)
  - **Files:** `backend/src/main/java/com/fakenews/config/CorsConfig.java`, `application.properties`, `application-production.properties`

### Priority 2: Test Coverage - Backend (Impact: 10/10, Effort: 6/10)
- [ ] **Integration tests for `/analyze` endpoint**
  - **Observable:** `mvn test` passes, 200 OK for valid request, 400 for invalid
  - **Coverage Target:** Happy path + validation errors
  - **Effort:** 4 hours
  - **Files:** `backend/src/test/java/com/fakenews/api/FakeNewsControllerTest.java`

- [ ] **Integration tests for `/batch-analyze` endpoint**
  - **Observable:** Batch processing works, returns correct count
  - **Effort:** 2 hours
  - **Files:** `backend/src/test/java/com/fakenews/api/FakeNewsControllerTest.java`

- [ ] **Unit tests for `FakeNewsDetector` core logic**
  - **Observable:** Credibility scoring algorithm verified
  - **Effort:** 3 hours
  - **Files:** `backend/src/test/java/com/fakenews/core/FakeNewsDetectorTest.java`

### Priority 3: Test Coverage - Python Service (Impact: 9/10, Effort: 4/10)
- [ ] **Integration tests for `/predict` endpoint**
  - **Observable:** `pytest` passes, validates request/response schema
  - **Effort:** 3 hours
  - **Files:** `python-service/tests/test_api.py`

- [ ] **Unit tests for feature extraction**
  - **Observable:** Writing quality metrics calculated correctly
  - **Effort:** 2 hours
  - **Files:** `python-service/tests/test_analysis_features.py`

### Priority 4: Documentation (Impact: 7/10, Effort: 3/10)
- [ ] **Create `docs/DEVELOPMENT.md`**
  - **Content:** Local setup, running services, running tests, common issues
  - **Effort:** 2 hours
  - **Observable:** New developer can start in <15 minutes

- [ ] **Update root `README.md`**
  - **Content:** Architecture diagram, quick start, link to DEVELOPMENT.md
  - **Effort:** 1 hour

### Phase 1 Exit Criteria
✅ All tests passing (`mvn test`, `pytest`)  
✅ CORS restricted in production profile  
✅ No duplicate dependencies in `pom.xml`  
✅ `docs/DEVELOPMENT.md` exists with setup instructions  
✅ Test coverage ≥50% for critical paths  

**Estimated Duration:** 1.5 weeks  
**Total Effort:** 20-24 hours

---

## Phase 2: Reliability (Week 3-4)
**Goal:** Add persistence, robust error handling, external service resilience

### Priority 1: Database Persistence (Impact: 10/10, Effort: 7/10)
- [ ] **Add PostgreSQL dependency and configuration**
  - **Files:** `pom.xml`, `application.properties`, `docker-compose.yml`
  - **Effort:** 2 hours

- [ ] **Create JPA entities for Article, Claim, Evidence**
  - **Files:** `backend/src/main/java/com/fakenews/entity/`
  - **Effort:** 4 hours

- [ ] **Implement repository layer**
  - **Files:** `backend/src/main/java/com/fakenews/repository/`
  - **Effort:** 3 hours

- [ ] **Add database migrations (Flyway or Liquibase)**
  - **Files:** `backend/src/main/resources/db/migration/`
  - **Effort:** 3 hours

- [ ] **Refactor `FakeNewsDetector` to use repositories**
  - **Risk:** Breaks existing in-memory logic
  - **Effort:** 6 hours
  - **Observable:** Articles persisted after restart

### Priority 2: Error Handling (Impact: 8/10, Effort: 5/10)
- [ ] **Global exception handler in Spring Boot**
  - **Files:** `backend/src/main/java/com/fakenews/api/GlobalExceptionHandler.java`
  - **Effort:** 2 hours
  - **Observable:** Structured error responses (status, message, timestamp)

- [ ] **Python service error handling**
  - **Files:** `python-service/roberta_service.py`
  - **Effort:** 2 hours
  - **Observable:** 422 for validation errors, 500 with details

- [ ] **Frontend error display improvements**
  - **Files:** `frontend/app.js`
  - **Effort:** 2 hours
  - **Observable:** Specific error messages shown to user

### Priority 3: External Service Resilience (Impact: 9/10, Effort: 6/10)
- [ ] **Add Resilience4j circuit breaker for RoBERTa service**
  - **Files:** `pom.xml`, `RobertaService.java`, `application.properties`
  - **Effort:** 4 hours
  - **Observable:** Graceful degradation when Python service down

- [ ] **Implement fallback logic**
  - **Approach:** Return analysis without RoBERTa confidence if service unavailable
  - **Effort:** 2 hours

- [ ] **Add retry logic with exponential backoff**
  - **Effort:** 2 hours

### Priority 4: Logging and Observability (Impact: 7/10, Effort: 4/10)
- [ ] **Add structured JSON logging (Logback)**
  - **Files:** `backend/src/main/resources/logback-spring.xml`
  - **Effort:** 2 hours

- [ ] **Add Micrometer + Prometheus metrics to backend**
  - **Files:** `pom.xml`, `application.properties`
  - **Effort:** 3 hours
  - **Observable:** `/actuator/prometheus` endpoint

- [ ] **Centralized request/response logging**
  - **Files:** `backend/src/main/java/com/fakenews/config/LoggingInterceptor.java`
  - **Effort:** 2 hours

### Phase 2 Exit Criteria
✅ Articles persisted to PostgreSQL  
✅ All error scenarios return structured responses  
✅ RoBERTa service failures handled gracefully  
✅ Prometheus metrics exposed  
✅ Structured logging in place  

**Estimated Duration:** 2 weeks  
**Total Effort:** 35-40 hours

---

## Phase 3: Production Hardening (Week 5-6)
**Goal:** CI/CD, API documentation, security, performance optimization

### Priority 1: CI/CD Pipeline (Impact: 10/10, Effort: 6/10)
- [ ] **Create GitHub Actions workflow**
  - **Files:** `.github/workflows/ci.yml`
  - **Jobs:** Lint, test, build, Docker build
  - **Effort:** 4 hours

- [ ] **Add deployment workflow**
  - **Files:** `.github/workflows/deploy.yml`
  - **Target:** Render.com or Kubernetes
  - **Effort:** 4 hours

- [ ] **Docker Compose for local integration testing**
  - **Files:** `docker-compose.yml` (complete with all services)
  - **Effort:** 3 hours

### Priority 2: API Documentation (Impact: 8/10, Effort: 4/10)
- [ ] **Add Swagger/OpenAPI annotations**
  - **Files:** `FakeNewsController.java`, DTOs
  - **Effort:** 3 hours

- [ ] **Configure Swagger UI**
  - **Files:** `pom.xml`, `application.properties`
  - **Effort:** 1 hour
  - **Observable:** Swagger UI at `/swagger-ui.html`

- [ ] **Generate OpenAPI spec**
  - **Files:** `docs/openapi.yaml`
  - **Effort:** 1 hour

### Priority 3: Security Hardening (Impact: 9/10, Effort: 7/10)
- [ ] **Add input validation and sanitization**
  - **Files:** `ArticleRequest.java`, `AnalysisRequest` (Python)
  - **Effort:** 2 hours

- [ ] **Add rate limiting (Spring Cloud Gateway or nginx)**
  - **Effort:** 4 hours

- [ ] **Add basic authentication (API keys)**
  - **Files:** `SecurityConfig.java`, `application.properties`
  - **Effort:** 4 hours

- [ ] **HTTPS enforcement in production**
  - **Files:** `application-production.properties`
  - **Effort:** 1 hour

### Priority 4: Performance and Caching (Impact: 7/10, Effort: 6/10)
- [ ] **Add Redis caching for RoBERTa responses**
  - **Files:** `pom.xml`, `CacheConfig.java`, `RobertaService.java`
  - **Effort:** 4 hours
  - **Observable:** Cache hit/miss metrics

- [ ] **Enable database connection pooling**
  - **Files:** `application.properties` (HikariCP config)
  - **Effort:** 1 hour

- [ ] **Add async processing for batch jobs**
  - **Files:** `AsyncConfig.java`, `FakeNewsDetector.java`
  - **Effort:** 4 hours

### Priority 5: Deployment and Operations (Impact: 8/10, Effort: 5/10)
- [ ] **Create Kubernetes manifests (if applicable)**
  - **Files:** `k8s/deployment.yaml`, `k8s/service.yaml`
  - **Effort:** 4 hours

- [ ] **Add deployment scripts**
  - **Files:** `scripts/deploy.sh`, `scripts/rollback.sh`
  - **Effort:** 2 hours

- [ ] **Set up alerting (Prometheus Alertmanager)**
  - **Files:** `alerting-rules.yml`
  - **Effort:** 3 hours

### Phase 3 Exit Criteria
✅ CI/CD pipeline running on every PR  
✅ Swagger UI accessible  
✅ Rate limiting active  
✅ Redis caching operational  
✅ Production deployment automated  

**Estimated Duration:** 2 weeks  
**Total Effort:** 40-45 hours

---

## Risk Mitigation Strategy

### High-Risk Items (Do First)
1. ✅ **CORS hardening** - Security vulnerability (Phase 1)
2. ✅ **Test coverage** - Prevents regressions (Phase 1)
3. **Database persistence** - Data loss risk (Phase 2)
4. **Circuit breaker** - Service dependency risk (Phase 2)
5. **CI/CD** - Manual deployment errors (Phase 3)

### Technical Debt Addressed
- ✅ Duplicate dependencies in `pom.xml` (Phase 1)
- Frontend API URL hardcoding (Phase 2 - environment config)
- Empty `docs/` and `scripts/` directories (Phase 1-3)
- No database persistence (Phase 2)
- Missing API documentation (Phase 3)

---

## Success Metrics

| Metric | Current | Phase 1 Target | Phase 3 Target |
|--------|---------|----------------|----------------|
| Test Coverage | 0% | 50% | 80% |
| API Response Time (p95) | Unknown | <2s | <500ms |
| Uptime | Unknown | 95% | 99.5% |
| Security Score (OWASP) | C | B | A |
| Deployment Time | Manual | 10 min | <5 min (automated) |
| Documentation Coverage | 20% | 60% | 90% |

---

## Timeline Summary

- **Week 1-2:** Phase 1 (Foundation) ✅ Critical security + tests
- **Week 3-4:** Phase 2 (Reliability) - Persistence + resilience
- **Week 5-6:** Phase 3 (Production Hardening) - CI/CD + security
- **Week 7:** Buffer for issues, final QA

**Total Duration:** 6-7 weeks to production-ready MVP

---

## Next Steps

1. ✅ Review and approve Phase 1 tasks
2. Execute Phase 1 (see detailed design below)
3. Demo Phase 1 deliverables
4. Plan Phase 2 based on learnings
5. Iterate until production-ready

---

*Last Updated: 2025-11-25*  
*Status: Phase 1 Planning Complete*
