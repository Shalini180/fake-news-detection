# Development Guide

## Quick Start (5 minutes)

### Prerequisites
- **Java:** 11 or higher ([Download](https://adoptium.net/))
- **Maven:** 3.6+ ([Download](https://maven.apache.org/download.cgi))
- **Python:** 3.10+ ([Download](https://www.python.org/downloads/))
- **Git:** Latest version

### Clone and Setup

```bash
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
```

---

## Running Services

### Option 1: Run All Services (Recommended for Development)

**Terminal 1 - Backend:**
```bash
cd backend
mvn spring-boot:run
# Backend will start on http://localhost:8080
```

**Terminal 2 - Python ML Service:**
```bash
cd python-service
python roberta_service.py
# Python service will start on http://localhost:8000
```

**Terminal 3 - Frontend:**
```bash
cd frontend/public
# Use any static file server, e.g.:
python -m http.server 3000
# OR
npx serve -p 3000
# Frontend will be at http://localhost:3000
```

### Option 2: Run with Docker

```bash
cd backend
docker-compose up --build
```

---

## Running Tests

### Backend Tests

```bash
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
```

### Python Tests

```bash
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
```

### E2E Smoke Test

```bash
# Make sure all services are running, then:
curl -X POST http://localhost:8080/api/v1/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Article",
    "content": "This is a test article with enough content for analysis.",
    "source": "test-source.com"
  }'
# Should return 200 OK with credibility analysis
```

---

## Project Structure

```
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
├── frontend/              # Static web UI (Research Dashboard)
│   ├── app.js                 # Main application logic
│   ├── styles.css             # Core styling
│   └── public/
│       ├── index.html         # SPA entry point
│       ├── api.js             # API client
│       ├── demo-data.js       # Demo scenarios
│       ├── session.js         # Session history manager
│       ├── visualizations.js  # Chart.js visualizations
│       ├── graph-viz.js       # Knowledge graph renderer
│       └── layout.css         # Responsive layout
│
└── docs/                  # Documentation
    ├── roadmap.md             # Production roadmap
    ├── phase1-design.md       # Phase 1 details
    ├── UI-RESEARCH-DASHBOARD.md # UI feature guide
    └── DEVELOPMENT.md         # This file
```

---

## Common Issues & Solutions

### Issue: Port Already in Use
**Error:** `Port 8080 is already in use`

**Solution:**
```bash
# Find process using port
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Kill process or use different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Issue: RoBERTa Model Download Fails
**Error:** `Connection timeout` when downloading model

**Solution:**
1. Check internet connection
2. Use different mirror:
   ```python
   export TRANSFORMERS_CACHE=/path/to/large/disk
   export HF_ENDPOINT=https://hf-mirror.com
   ```
3. Download manually and place in `~/.cache/huggingface/`

### Issue: CORS Errors in Browser
**Error:** `Access-Control-Allow-Origin` error in console

**Solution:**
1. Check backend is running on correct port
2. Verify CORS configuration in `application.properties`:
   ```properties
   cors.allowed-origins=http://localhost:3000
   ```
3. For production, set environment variable:
   ```bash
   export CORS_ALLOWED_ORIGINS=https://your-frontend.com
   ```

### Issue: Tests Fail with "Model Not Found"
**Solution:** Tests should mock the RoBERTa model. Check that test fixtures are set up correctly in `tests/test_api.py`.

### Issue: Maven Build Fails
**Error:** `Failed to execute goal... Could not resolve dependencies`

**Solution:**
```bash
# Clear Maven cache and rebuild
mvn dependency:purge-local-repository
mvn clean install -U
```

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
```bash
# Run tests
cd backend && mvn test
cd python-service && pytest

# Format code
cd python-service && black .

# Check build
cd backend && mvn clean package
```

### Pull Request Process
1. Create feature branch: `git checkout -b feature/your-feature`
2. Make changes and add tests
3. Run all tests locally
4. Commit with meaningful message
5. Push and create PR
6. Fill out PR template

---

## Useful Commands

```bash
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
```

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
   ```json
   {
     "configurations": [{
       "name": "Python: FastAPI",
       "type": "python",
       "request": "launch",
       "module": "uvicorn",
       "args": ["roberta_service:app", "--reload"]
     }]
   }
   ```
3. Set breakpoints and press F5

---

## Research Dashboard UI

The frontend is a research-grade dashboard with advanced visualizations and session management. For complete UI documentation, see [UI-RESEARCH-DASHBOARD.md](UI-RESEARCH-DASHBOARD.md).

### Quick UI Guide

**Demo Mode** (Test without backend):
1. Click "🎬 Demo" button in query panel
2. Select scenario (High Risk / Credible / Mixed / Claims / Suspicious Domain)
3. Click "Load Demo" - results render instantly

**Session History**:
- All analyses auto-save to localStorage (last 50)
- Click any history item to reload results
- Double-click to select for comparison
- Click trash icon to clear history

**Temporal Analytics**:
- Navigate to "Temporal" tab
- View credibility score timeline
- See risk distribution histogram
- Compare 2 analyses side-by-side

**Export Session**:
- Navigate to "System" tab
- Click "📥 Download Session JSON"
- File contains all analyses + metadata

**Layout**:
- **Desktop:** 3 panels (Query | Results | History)
- **Tablet:** 2 panels (History becomes modal)
- **Mobile:** Stacked panels with overlays

**Features**:
- ✅ Chart.js visualizations (gauge, time series, bars)
- ✅ Token attention heatmap
- ✅ Knowledge graph (SVG)
- ✅ Claims filtering by status
- ✅ Theme toggle (persisted)

---

## Next Steps
- Review [Production Roadmap](roadmap.md)
- Check [Phase 1 Design](phase1-design.md) for current work
- Explore [UI Research Dashboard](UI-RESEARCH-DASHBOARD.md) for frontend features
- Join team Slack/Discord for questions

---

*Last Updated: 2025-11-25*
