import re
import numpy as np
from typing import Dict

class AnalysisFeatures:
    """Extract additional analysis features from text"""
    
    @staticmethod
    def analyze_sentiment(text: str) -> float:
        """
        Simple sentiment analysis
        Returns: -1 (negative) to 1 (positive)
        """
        # Emotional words (simplified - consider using TextBlob or VADER)
        positive_words = [
            'amazing', 'excellent', 'wonderful', 'great', 'fantastic',
            'perfect', 'love', 'best', 'awesome', 'brilliant'
        ]
        negative_words = [
            'terrible', 'awful', 'horrible', 'worst', 'hate',
            'disaster', 'fail', 'poor', 'bad', 'disappointing'
        ]
        
        text_lower = text.lower()
        pos_count = sum(1 for word in positive_words if word in text_lower)
        neg_count = sum(1 for word in negative_words if word in text_lower)
        
        total = pos_count + neg_count
        if total == 0:
            return 0.0
        
        return (pos_count - neg_count) / total
    
    @staticmethod
    def analyze_writing_quality(text: str) -> Dict:
        """Analyze writing quality indicators"""
        
        # Count various quality indicators
        exclamation_count = text.count('!')
        question_count = text.count('?')
        caps_words = len([w for w in text.split() if w.isupper() and len(w) > 1])
        total_words = len(text.split())
        
        caps_ratio = caps_words / total_words if total_words > 0 else 0
        
        # Detect clickbait patterns
        clickbait_patterns = [
            r'\bshock(?:ing)?\b',
            r'\bamazing\b',
            r'\bunbelievable\b',
            r'\byou won\'t believe\b',
            r'\bwhat happens next\b',
            r'\bclick here\b',
            r'\bbreaking\b'
        ]
        
        clickbait_score = sum(
            1 for pattern in clickbait_patterns 
            if re.search(pattern, text, re.IGNORECASE)
        )
        
        return {
            'exclamation_count': exclamation_count,
            'question_count': question_count,
            'caps_ratio': caps_ratio,
            'clickbait_score': clickbait_score,
            'avg_sentence_length': len(text) / max(text.count('.'), 1)
        }
    
    @staticmethod
    def extract_claims(text: str) -> list:
        """Extract potential factual claims from text"""
        
        # Sentences that might be claims
        sentences = text.split('.')
        
        # Claim indicators
        claim_indicators = [
            r'\b(?:prove[sd]?|confirm[sd]?|show[sn]?)\b',
            r'\b(?:research|study|report) (?:shows?|reveals?|finds?)\b',
            r'\b(?:scientist[s]?|expert[s]?|doctor[s]?) (?:say|claim|believe)\b',
            r'\d+%',  # Percentages
            r'\b(?:always|never|all|none|every)\b'  # Absolute claims
        ]
        
        potential_claims = []
        for sentence in sentences:
            sentence = sentence.strip()
            if len(sentence) < 10:
                continue
            
            for pattern in claim_indicators:
                if re.search(pattern, sentence, re.IGNORECASE):
                    potential_claims.append(sentence)
                    break
        
        return potential_claims[:5]  # Top 5 claims
    
    @staticmethod
    def calculate_risk_level(confidence: float, writing_quality: Dict) -> Dict:
        """Calculate overall risk level"""
        
        # Base risk from model confidence
        if confidence > 0.9:
            risk = 'HIGH'
            color = '🔴'
        elif confidence > 0.7:
            risk = 'MEDIUM'
            color = '🟠'
        elif confidence > 0.5:
            risk = 'LOW'
            color = '🟡'
        else:
            risk = 'SAFE'
            color = '🟢'
        
        # Adjust based on writing quality
        if writing_quality['clickbait_score'] >= 3:
            if risk == 'SAFE':
                risk = 'LOW'
                color = '🟡'
        
        return {
            'level': risk,
            'icon': color,
            'description': f"{risk} risk of misinformation"
        }
