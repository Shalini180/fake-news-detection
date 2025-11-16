import torch
import torch.nn as nn
from transformers import RobertaModel

class RobertaFakeNewsClassifier(nn.Module):
    """Enhanced RoBERTa classifier for fake news detection"""
    
    def __init__(self, num_labels=2, dropout_rate=0.3):
        super(RobertaFakeNewsClassifier, self).__init__()
        
        # Load pre-trained RoBERTa
        self.roberta = RobertaModel.from_pretrained('roberta-base')
        
        # Classification head with residual connections
        self.dropout1 = nn.Dropout(dropout_rate)
        self.fc1 = nn.Linear(768, 512)
        self.bn1 = nn.BatchNorm1d(512)
        self.relu = nn.ReLU()
        
        self.dropout2 = nn.Dropout(dropout_rate)
        self.fc2 = nn.Linear(512, 256)
        self.bn2 = nn.BatchNorm1d(256)
        
        self.dropout3 = nn.Dropout(dropout_rate / 2)
        self.fc3 = nn.Linear(256, num_labels)
        
    def forward(self, input_ids, attention_mask):
        # Get RoBERTa outputs
        outputs = self.roberta(
            input_ids=input_ids,
            attention_mask=attention_mask,
            output_attentions=True  # For explainability
        )
        
        # Use [CLS] token
        pooled_output = outputs.last_hidden_state[:, 0, :]
        attention_weights = outputs.attentions  # For visualization
        
        # Classification layers
        x = self.dropout1(pooled_output)
        x = self.fc1(x)
        x = self.bn1(x)
        x = self.relu(x)
        
        x = self.dropout2(x)
        x = self.fc2(x)
        x = self.bn2(x)
        x = self.relu(x)
        
        x = self.dropout3(x)
        logits = self.fc3(x)
        
        return logits, pooled_output, attention_weights
