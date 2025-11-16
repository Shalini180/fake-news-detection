package com.fakenews.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
public class RobertaService {

    private final WebClient webClient;

    @Value("${roberta.service.url:http://localhost:8000}")
    private String robertaServiceUrl;

    public RobertaService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(robertaServiceUrl)
                .build();
    }

    public RobertaAnalysisResponse analyze(String title, String content, String source) {
        try {
            log.info("Sending request to RoBERTa service");

            Map<String, String> request = new HashMap<>();
            request.put("title", title);
            request.put("text", content);
            if (source != null) {
                request.put("source", source);
            }

            RobertaAnalysisResponse response = webClient.post()
                    .uri("/predict")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(RobertaAnalysisResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            log.info("RoBERTa prediction: {} (confidence: {})",
                    response.getPrediction(),
                    response.getConfidence());

            return response;

        } catch (WebClientResponseException e) {
            log.error("RoBERTa service error: {}", e.getMessage());
            throw new RuntimeException("RoBERTa service error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling RoBERTa service", e);
            throw new RuntimeException("Failed to get RoBERTa prediction", e);
        }
    }

    public boolean isHealthy() {
        try {
            JsonNode health = webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            return "healthy".equals(health.get("status").asText());
        } catch (Exception e) {
            log.warn("RoBERTa service health check failed: {}", e.getMessage());
            return false;
        }
    }
}