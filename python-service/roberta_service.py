from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Optional, Dict
import torch
from transformers import RobertaTokenizer
import logging
from prometheus_client import Counter, Histogram, make_asgi_app
import time
from datetime import datetime

# Import custom modules
from models.roberta_model import RobertaFakeNewsClassifier
from utils.explainability import ExplainabilityEngine
from utils.analysis_features import AnalysisFeatures

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Initialize FastAPI
app = FastAPI(
    title="RoBERTa Fake News Detection API",
    description="Advanced fake news detection with explainability",
    version="1.0.0"
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Prometheus metrics
REQUEST_COUNT = Counter('roberta_requests_total', 'Total requests')
REQUEST_DURATION = Histogram('roberta_request_duration_seconds', 'Request duration')
PREDICTION_COUNT = Counter('roberta_predictions_total', 'Predictions by type', ['prediction'])

# Add prometheus metrics endpoint
metrics_app = make_asgi_app()
app.mount("/metrics", metrics_app)

# Global variables for model
model = None
tokenizer = None
explainer = None
device = None

# Request/Response Models
class AnalysisRequest(BaseModel):
    title: str = Field(..., min_length=1, max_length=500)
    text: str = Field(..., min_length=10, max_length=10000)
    source: Optional[str] = None

class TokenImportance(BaseModel):
    token: str
    importance: float

class RiskLevel(BaseModel):
    level: str
    icon: str
    description: str

class AnalysisResponse(BaseModel):
    # Core prediction
    prediction: str
    confidence: float
    credibility_score: float
    
    # Explainability
    top_tokens: List[Dict]
    suspicious_phrases: List[str]
    key_reasons: List[str]
    attention_highlights: Optional[List[Dict]] = None
    
    # Additional analysis
    sentiment_score: float
    writing_quality: Dict
    extracted_claims: List[str]
    risk_level: RiskLevel
    
    # Metadata
    processing_time_ms: float
    timestamp: str
    model_version: str = "roberta-base-v1.0"

@app.on_event("startup")
async def load_model():
    """Load model at startup"""
    global model, tokenizer, explainer, device
    
    logger.info("🚀 Loading RoBERTa model...")
    
    try:
        device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        logger.info(f"Using device: {device}")
        
        # Load tokenizer
        tokenizer = RobertaTokenizer.from_pretrained('roberta-base')
        
        # Load model
        model = RobertaFakeNewsClassifier(num_labels=2, dropout_rate=0.3)
        
        # Try to load trained weights, fallback to pre-trained
        try:
            model.load_state_dict(
                torch.load('models/roberta_fakenews_best.pt', map_location=device)
            )
            logger.info("✓ Loaded trained model weights")
        except FileNotFoundError:
            logger.warning("⚠ Trained model not found, using base RoBERTa")
            logger.warning("⚠ Model will work but accuracy may be lower")
        
        model.to(device)
        model.eval()
        
        # Initialize explainer
        explainer = ExplainabilityEngine(tokenizer)
        
        logger.info("✓ Model loaded successfully")
        
    except Exception as e:
        logger.error(f"❌ Error loading model: {str(e)}")
        raise

@app.get("/")
async def root():
    return {
        "service": "RoBERTa Fake News Detection API",
        "status": "running",
        "version": "1.0.0",
        "endpoints": {
            "predict": "/predict",
            "health": "/health",
            "metrics": "/metrics"
        }
    }

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "model_loaded": model is not None,
        "device": str(device),
        "timestamp": datetime.now().isoformat()
    }

@app.post("/predict", response_model=AnalysisResponse)
async def predict(request: AnalysisRequest, background_tasks: BackgroundTasks):
    """Main prediction endpoint with full analysis"""
    
    start_time = time.time()
    REQUEST_COUNT.inc()
    
    try:
        # Combine title and text
        full_text = f"{request.title}. {request.text}"
        
        logger.info(f"Processing request - Title: {request.title[:50]}...")
        
        # Tokenize
        encoding = tokenizer.encode_plus(
            full_text,
            add_special_tokens=True,
            max_length=512,
            padding='max_length',
            truncation=True,
            return_attention_mask=True,
            return_tensors='pt'
        )
        
        input_ids = encoding['input_ids'].to(device)
        attention_mask = encoding['attention_mask'].to(device)
        
        # Predict
        with torch.no_grad():
            logits, embeddings, attention_weights = model(input_ids, attention_mask)
            probabilities = torch.softmax(logits, dim=1)
            prediction_idx = torch.argmax(probabilities, dim=1).item()
            confidence = probabilities[0][prediction_idx].item()
        
        prediction = 'FAKE' if prediction_idx == 1 else 'REAL'
        credibility_score = 1.0 - probabilities[0][1].item()
        
        # Get explainability features
        token_importance = explainer.get_token_importance(
            model, input_ids, attention_mask, prediction_idx
        )
        
        suspicious_phrases = explainer.extract_suspicious_phrases(token_importance)
        
        attention_highlights = explainer.analyze_attention_patterns(
            attention_weights, input_ids, tokenizer
        )
        
        # Additional analysis
        sentiment_score = AnalysisFeatures.analyze_sentiment(full_text)
        writing_quality = AnalysisFeatures.analyze_writing_quality(full_text)
        extracted_claims = AnalysisFeatures.extract_claims(full_text)
        risk_level = AnalysisFeatures.calculate_risk_level(
            confidence if prediction == 'FAKE' else 1 - confidence,
            writing_quality
        )
        
        # Generate explanation
        key_reasons = explainer.generate_explanation(
            prediction, confidence, suspicious_phrases,
            sentiment_score, writing_quality
        )
        
        # Track prediction type
        PREDICTION_COUNT.labels(prediction=prediction).inc()
        
        processing_time = (time.time() - start_time) * 1000
        
        logger.info(
            f"Prediction: {prediction} ({confidence:.2%}) - "
            f"Time: {processing_time:.0f}ms"
        )
        
        response = AnalysisResponse(
            prediction=prediction,
            confidence=confidence,
            credibility_score=credibility_score,
            top_tokens=token_importance[:20],
            suspicious_phrases=suspicious_phrases,
            key_reasons=key_reasons,
            attention_highlights=attention_highlights[:10],
            sentiment_score=sentiment_score,
            writing_quality=writing_quality,
            extracted_claims=extracted_claims,
            risk_level=risk_level,
            processing_time_ms=processing_time,
            timestamp=datetime.now().isoformat()
        )
        
        REQUEST_DURATION.observe(processing_time / 1000)
        
        return response
        
    except Exception as e:
        logger.error(f"Prediction error: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Prediction error: {str(e)}")

@app.post("/batch_predict")
async def batch_predict(requests: List[AnalysisRequest]):
    """Batch prediction endpoint"""
    
    if len(requests) > 10:
        raise HTTPException(status_code=400, detail="Maximum 10 requests per batch")
    
    results = []
    for req in requests:
        result = await predict(req, None)
        results.append(result)
    
    return {"results": results, "count": len(results)}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "roberta_service:app",
        host="0.0.0.0",
        port=8000,
        reload=False,
        workers=1
    )
