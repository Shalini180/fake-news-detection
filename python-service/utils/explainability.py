import torch
import numpy as np
from typing import List, Tuple, Dict

class ExplainabilityEngine:
    """Provides explanations for model predictions"""
    
    def __init__(self, tokenizer):
        self.tokenizer = tokenizer
    
    def get_token_importance(self, model, input_ids, attention_mask, prediction_idx):
        """Calculate token importance using gradient-based attribution"""
        
        # Enable gradients for input
        input_ids_grad = input_ids.clone().requires_grad_(True)
        
        # Forward pass
        logits, _, _ = model(input_ids_grad, attention_mask)
        
        # Backward pass on predicted class
        target_logit = logits[0][prediction_idx]
        target_logit.backward()
        
        # Get gradient magnitudes
        gradients = input_ids_grad.grad.abs().squeeze().cpu().numpy()
        tokens = self.tokenizer.convert_ids_to_tokens(input_ids.squeeze().cpu().numpy())
        
        # Filter special tokens and create importance list
        token_importance = []
        for token, grad in zip(tokens, gradients):
            if token not in ['<s>', '</s>', '<pad>', '<unk>'] and grad > 0:
                token_importance.append({
                    'token': token.replace('Ġ', ' '),  # Clean BPE artifacts
                    'importance': float(grad)
                })
        
        # Sort by importance
        token_importance.sort(key=lambda x: x['importance'], reverse=True)
        
        return token_importance
    
    def extract_suspicious_phrases(self, tokens_with_importance, threshold=0.5):
        """Extract phrases with high importance scores"""
        
        max_importance = max(t['importance'] for t in tokens_with_importance)
        normalized_threshold = threshold * max_importance
        
        suspicious_phrases = []
        current_phrase = []
        
        for item in tokens_with_importance:
            if item['importance'] >= normalized_threshold:
                current_phrase.append(item['token'])
            else:
                if current_phrase:
                    suspicious_phrases.append(' '.join(current_phrase))
                    current_phrase = []
        
        if current_phrase:
            suspicious_phrases.append(' '.join(current_phrase))
        
        return suspicious_phrases[:5]  # Top 5 phrases
    
    def generate_explanation(self, prediction, confidence, suspicious_phrases, 
                           sentiment_score=None, writing_quality=None):
        """Generate human-readable explanation"""
        
        reasons = []
        
        # Prediction-based reasons
        if prediction == 'FAKE':
            if confidence > 0.9:
                reasons.append("Very high confidence in fake news detection")
            elif confidence > 0.7:
                reasons.append("Strong indicators of misinformation detected")
            else:
                reasons.append("Multiple suspicious patterns found")
        
        # Phrase-based reasons
        if suspicious_phrases:
            phrases_str = ', '.join([f'"{p}"' for p in suspicious_phrases[:3]])
            reasons.append(f"Suspicious phrases detected: {phrases_str}")
        
        # Sentiment-based reasons
        if sentiment_score:
            if sentiment_score < -0.5:
                reasons.append("Highly negative emotional tone detected")
            elif sentiment_score > 0.8:
                reasons.append("Unusually positive framing may indicate bias")
        
        # Writing quality reasons
        if writing_quality:
            if writing_quality['exclamation_count'] > 3:
                reasons.append(f"Excessive punctuation usage ({writing_quality['exclamation_count']} exclamation marks)")
            if writing_quality['caps_ratio'] > 0.3:
                reasons.append("Excessive capitalization detected (clickbait indicator)")
        
        return reasons
    
    def analyze_attention_patterns(self, attention_weights, input_ids, tokenizer):
        """Analyze attention patterns to find important relationships"""
        
        # Get last layer attention (most relevant)
        last_layer_attention = attention_weights[-1][0].mean(dim=0)  # Average across heads
        
        tokens = tokenizer.convert_ids_to_tokens(input_ids[0].cpu().numpy())
        
        # Find tokens with highest attention
        attention_scores = last_layer_attention[0, 1:].cpu().numpy()  # Skip CLS token
        
        top_indices = attention_scores.argsort()[-10:][::-1]
        
        attention_highlights = [
            {
                'token': tokens[idx + 1].replace('Ġ', ' '),
                'attention_score': float(attention_scores[idx])
            }
            for idx in top_indices
        ]
        
        return attention_highlights
