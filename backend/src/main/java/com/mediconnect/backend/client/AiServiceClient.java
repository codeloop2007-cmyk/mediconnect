package com.mediconnect.backend.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AiServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    public AiServiceClient(@Value("${ai.service.url:http://localhost:8000}") String aiServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.aiServiceUrl = aiServiceUrl;
    }

    /**
     * Calls the Python AI microservice to generate a 384-dimensional vector embedding for the given text.
     * Returns a PostgreSQL-compatible vector literal string: "[0.0123, -0.0456, ...]"
     * Returns null if the AI service is unreachable (non-blocking failure).
     */
    public String getEmbeddingVectorString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            String endpoint = aiServiceUrl + "/api/v1/embeddings";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("text", text);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object rawEmbedding = response.getBody().get("embedding");
                if (rawEmbedding instanceof List) {
                    List<?> embList = (List<?>) rawEmbedding;
                    // Convert to PostgreSQL vector literal: "[0.0123, -0.0456, ...]"
                    return embList.toString();
                }
            }
        } catch (Exception e) {
            logger.warn("AI service unavailable at '{}', skipping embedding: {}", aiServiceUrl, e.getMessage());
        }
        return null;
    }
}
