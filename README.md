# Fake News Detection

An explainable AI system for detecting fake news using hybrid analysis: RoBERTa transformers + knowledge graph + explainability (LIME, attention mechanisms).

## Architecture

- **Backend:** Spring Boot 2.7.14 (Java 11) - REST API, orchestration, knowledge graph
- **ML Service:** FastAPI + PyTorch - RoBERTa-based content analysis
- **Frontend:** Vanilla JavaScript SPA - Interactive credibility analysis UI

## Quick Start

See [DEVELOPMENT.md](docs/DEVELOPMENT.md) for detailed setup instructions.

```bash
# 1. Start backend (Terminal 1)
cd backend && mvn spring-boot:run

# 2. Start Python ML service (Terminal 2)
cd python-service && python roberta_service.py

# 3. Open frontend
# Open frontend/public/index.html in browser
```

## Features

✅ Multi-dimensional credibility analysis  
✅ RoBERTa transformer-based content analysis  
✅ Domain reputation checking  
✅ Claim extraction and verification  
✅ Knowledge graph for cross-referencing  
✅ Explainability (LIME, attention weights, suspicious phrases)  
✅ Writing quality analysis (clickbait detection)  

## Branches

- `main` — stable
- feature branches: `backend/...`, `frontend/...`

## Documentation

- [Development Guide](docs/DEVELOPMENT.md) - Local setup, running tests
- [Production Roadmap](docs/roadmap.md) - Path to production MVP
- [Technical Audit](docs/codebase_audit_report.md) - Current state analysis
