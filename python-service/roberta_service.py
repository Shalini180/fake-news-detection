from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Optional, Dict
import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification
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
# Global variables for model (lazy-loaded)
model = None
tokenizer = None
explainer = None
device = None

# Small model to use on limited-memory instances
# Change to your lightweight fine-tuned model if you have one (e.g. "your-username/distilroberta-finetuned")
MODEL_NAME = "distilroberta-base"
# Limit CPU threads to reduce memory/CPU contention (helps on tiny instances)
TORCH_NUM_THREADS = 1


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

class Uncertainty(BaseModel):
    """Uncertainty quantification metrics"""
    entropy: float = Field(..., ge=0.0, le=1.0, description="Predictive entropy [0-1]")
    variance: float = Field(..., ge=0.0, description="Variance across MC samples")
    stdDev: float = Field(..., ge=0.0, description="Standard deviation")
    confidenceInterval: List[float] = Field(..., description="95% CI [lower, upper]")
    mcSamples: int = Field(default=20, description="Number of MC dropout samples")

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
    
    # Uncertainty quantification (NEW)
    uncertainty: Optional[Uncertainty] = None
    
    # Metadata
    processing_time_ms: float
    timestamp: str
    model_version: str = "roberta-base-v1.0"

from transformers import AutoTokenizer, AutoModelForSequenceClassification

# NOTE: we intentionally do NOT load the heavy model at startup to keep memory usage low.
# The model & tokenizer will be lazy-loaded on first prediction request.
@app.on_event("startup")
async def startup_event():
    global device
    logger.info("Starting service (model will be lazy-loaded on first request).")
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    logger.info(f"Using device: {device}")
    # restrict PyTorch threads which helps memory/CPU on tiny instances
    try:
        torch.set_num_threads(TORCH_NUM_THREADS)
    except Exception:
        # ignore if not supported in this runtime
        pass

def load_model_and_tokenizer():
    """
    Lazy-load the tokenizer and small model into memory.
    Uses low_cpu_mem_usage to lower peak memory during load.
    """
    global model, tokenizer, explainer, device

    if model is not None and tokenizer is not None:
        return

    logger.info("🚀 Lazy-loading tokenizer and model (low memory mode)...")

    # Load tokenizer (small, fast)
    tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)

    # If you have a custom classifier class (RobertaFakeNewsClassifier) fine-tuned and saved,
    # TWO options:
    # 1) Load a small fine-tuned HF model with AutoModelForSequenceClassification:
    #    model = AutoModelForSequenceClassification.from_pretrained(<model_id>, low_cpu_mem_usage=True)
    # 2) Use your custom class and load saved state dict (but ensure model size is small).
    #
    # We'll attempt HF AutoModel (small) first, falling back to custom classifier if needed.

    try:
        model = AutoModelForSequenceClassification.from_pretrained(
            MODEL_NAME,
            low_cpu_mem_usage=True,
            torch_dtype=torch.float32,
        )
        logger.info("✓ Loaded AutoModelForSequenceClassification from hub.")
    except Exception as e:
        logger.warning(f"Could not load HF AutoModel (fallback to custom model). Reason: {e}")
        # Fallback to custom architecture + weights if present (ensure it's small)
        model = RobertaFakeNewsClassifier(num_labels=2, dropout_rate=0.3)
        try:
            model.load_state_dict(torch.load('models/roberta_fakenews_best.pt', map_location=device))
            logger.info("✓ Loaded trained model weights into custom classifier")
        except FileNotFoundError:
            logger.warning("⚠ Trained model not found, using randomly initialized custom model (accuracy may be low)")

    # Move model to device (likely CPU on Render free instance)
    model.to(device)
    model.eval()

    # Initialize explainer with tokenizer (if your ExplainabilityEngine expects model too, adapt accordingly)
    try:
        explainer = ExplainabilityEngine(tokenizer)
    except Exception:
        explainer = None
        logger.warning("Explainer initialization failed or skipped (will still return basic predictions).")

    logger.info("✅ Model & tokenizer loaded.")


# ============================================================================
# UNCERTAINTY QUANTIFICATION FUNCTIONS
# ============================================================================

def compute_predictive_entropy(probabilities: torch.Tensor) -> float:
    """
    Compute predictive entropy from softmax probabilities.
    Entropy measures uncertainty: 0 = completely certain, 1 = maximally uncertain.
    
    Args:
        probabilities: Tensor of shape [batch_size, num_classes]
    
    Returns:
        Normalized entropy in [0, 1]
    """
    # Avoid log(0) by adding small epsilon
    epsilon = 1e-10
    probs = probabilities + epsilon
    
    # H(p) = -sum(p * log(p))
    entropy = -torch.sum(probs * torch.log(probs), dim=1)
    
    # Normalize by max entropy (log(num_classes))
    num_classes = probabilities.shape[1]
    max_entropy = torch.log(torch.tensor(float(num_classes)))
    normalized_entropy = entropy / max_entropy
    
    return float(normalized_entropy.item())


def monte_carlo_dropout_inference(
    model,
    input_ids: torch.Tensor,
    attention_mask: torch.Tensor,
    n_samples: int = 20
) -> Dict:
    """
    Perform Monte Carlo Dropout inference by running multiple stochastic forward passes.
    
    Args:
        model: The model with dropout layers
        input_ids: Input token IDs
        attention_mask: Attention mask
        n_samples: Number of stochastic forward passes
    
    Returns:
        Dict with 'mean_probs', 'variance', 'std_dev', 'all_predictions'
    """
    # Enable dropout at inference time
    model.train()  # Set to train mode to enable dropout
    
    all_predictions = []
    
    for _ in range(n_samples):
        with torch.no_grad():
            # Forward pass with dropout enabled
            logits, _, _ = model(input_ids, attention_mask)
            probabilities = torch.softmax(logits, dim=1)
            all_predictions.append(probabilities.cpu())
    
    # Stack predictions: shape [n_samples, batch_size, num_classes]
    all_predictions = torch.stack(all_predictions)
    
    # Compute statistics
    mean_probs = torch.mean(all_predictions, dim=0)
    variance = torch.var(all_predictions, dim=0)
    std_dev = torch.std(all_predictions, dim=0)
    
    # Restore eval mode
    model.eval()
    
    return {
        'mean_probs': mean_probs,
        'variance': variance,
        'std_dev': std_dev,
        'all_predictions': all_predictions
    }


def compute_confidence_interval(predictions: torch.Tensor, confidence_level: float = 0.95) -> List[float]:
    """
    Compute confidence interval for the prediction using MC samples.
    
    Args:
        predictions: Tensor of shape [n_samples, batch_size, num_classes]
        confidence_level: Confidence level (default 0.95 for 95% CI)
    
    Returns:
        [lower_bound, upper_bound] for the dominant class probability
    """
    # Get predictions for the fake news class (index 1)
    fake_probs = predictions[:, 0, 1]  # Shape: [n_samples]
    
    # Compute percentiles
    alpha = 1 - confidence_level
    lower_percentile = (alpha / 2) * 100
    upper_percentile = (1 - alpha / 2) * 100
    
    lower_bound = torch.quantile(fake_probs, lower_percentile / 100)
    upper_bound = torch.quantile(fake_probs, upper_percentile / 100)
    
    return [float(lower_bound.item()), float(upper_bound.item())]


def compute_uncertainty_metrics(
    model,
    input_ids: torch.Tensor,
    attention_mask: torch.Tensor,
    base_probabilities: torch.Tensor,
    n_mc_samples: int = 20
) -> Uncertainty:
    """
    Compute all uncertainty metrics for a given prediction.
    
    Args:
        model: The classification model
        input_ids: Input token IDs
        attention_mask: Attention mask
        base_probabilities: Initial prediction probabilities
        n_mc_samples: Number of MC dropout samples
    
    Returns:
        Uncertainty object with all metrics
    """
    # 1. Predictive Entropy
    entropy = compute_predictive_entropy(base_probabilities)
    
    # 2. Monte Carlo Dropout
    mc_results = monte_carlo_dropout_inference(model, input_ids, attention_mask, n_mc_samples)
    
    # 3. Variance and Std Dev (for fake class, index 1)
    variance = float(mc_results['variance'][0, 1].item())
    std_dev = float(mc_results['std_dev'][0, 1].item())
    
    # 4. Confidence Interval
    conf_interval = compute_confidence_interval(mc_results['all_predictions'], confidence_level=0.95)
    
    return Uncertainty(
        entropy=entropy,
        variance=variance,
        stdDev=std_dev,
        confidenceInterval=conf_interval,
        mcSamples=n_mc_samples
    )


# ============================================================================
# API ENDPOINTS
# ============================================================================

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

    # Ensure model & tokenizer are loaded lazily (first request will incur load time)
    if model is None or tokenizer is None:
        # synchronous load; this may make the first request slow but avoids OOM on startup
        load_model_and_tokenizer()

    try:
        # Combine title and text
        full_text = f"{request.title}. {request.text}"
        
        logger.info(f"Processing request - Title: {request.title[:50]}...")
        
        # Tokenize
        encoding = tokenizer.encode_plus(
            full_text,
            add_special_tokens=True,
            max_length=256,
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
        
        # ============================================================================
        # UNCERTAINTY QUANTIFICATION (NEW)
        # ============================================================================
        try:
            uncertainty = compute_uncertainty_metrics(
                model=model,
                input_ids=input_ids,
                attention_mask=attention_mask,
                base_probabilities=probabilities,
                n_mc_samples=20
            )
            logger.info(f"Uncertainty - Entropy: {uncertainty.entropy:.3f}, StdDev: {uncertainty.stdDev:.4f}")
        except Exception as e:
            logger.warning(f"Uncertainty computation failed: {e}. Continuing without uncertainty metrics.")
            uncertainty = None
        
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
            uncertainty=uncertainty,  # NEW: Include uncertainty metrics
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
