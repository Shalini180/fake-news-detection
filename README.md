Fake-News Detection (AI + Full-Stack)

AI-powered web app that analyzes news articles and evaluates credibility using NLP, source reliability, and claim verification. Includes explainability + visual insights.

🔹 Live Demo: (Frontend + API deployed on Render)
🔹 Tech: Java + Spring Boot · JavaScript · Docker · NLP heuristics

⭐ Highlights

✅ Real-time fake-news scoring
✅ NLP-based content analysis
✅ Domain credibility scoring
✅ Claim extraction + evidence lookup
✅ Knowledge-Graph relationships
✅ Explainability (tokens + top words + reasons)
✅ Frontend + backend fully deployed

🧠 Architecture
Frontend (HTML + JS)
       ↓ REST
Backend (Spring Boot / Java)
       ↓
Core ML Logic + NLP + Explainability
       ↓
Knowledge Graph + Trie + MinHeap

🚀 Tech Stack
Backend

Java 17 · Spring Boot

Custom NLP pipeline (RoBERTa placeholder)

Knowledge Graph + Trie

MinHeap sorting

REST API

Frontend

HTML + CSS + Vanilla JS

Light/dark mode

Interactive results

Deployment

Render (Free tier)

Dockerized backend

📡 API Example
POST /api/v1/analyze
{
  "title": "Demo",
  "content": "BREAKING... unbelievable...",
  "source": "https://clickbait.net"
}


✅ Returns:

credibility score (0–1)

classification

key reasons

claim count

explainability vectors

🧱 Key Components
Component	Purpose
NLP Model	Fake-score + embeddings
Trie	Domain reputation
Min-Heap	Least-credible tracking
Knowledge Graph	Article → Claim → Evidence → Source
Explainability	Token weights + top words + reasons
🔍 Scoring Logic

Weighted model:

Feature	Weight
Content NLP	35%
Domain credibility	25%
Claim verification	25%
Cross-reference	15%

Outputs → LIKELY_FAKE / SUSPICIOUS / MIXED / CREDIBLE

🖥 UI Features

✔ Paste article + analyze
✔ See credibility score + highlights
✔ Explainability view:
– Top tokens
– Key reasons
– Top words
✔ Claim verification
✔ Local stats

⚙️ Run Locally
Backend
cd backend
mvn package
java -jar target/fake-news-detection-api-1.0.0.jar

Frontend

Open:

frontend/index.html

📌 Future Work

Integrate real transformer (HuggingFace)

External fact-check APIs

Browser extension

DB persistence
