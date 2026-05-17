package com.finflow.backend.common.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Base class for HTTP adapters calling data_ai_service endpoints.
 * Provides shared HTTP client setup, request building, and helper methods.
 */
@Slf4j
public abstract class AbstractRestDataAiAdapter {

    protected final ObjectMapper objectMapper;
    protected final String dataAiBaseUrl;
    protected final String dataAiInternalApiKey;

    protected final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    protected AbstractRestDataAiAdapter(
            ObjectMapper objectMapper,
            String dataAiBaseUrl,
            String dataAiInternalApiKey
    ) {
        this.objectMapper = objectMapper;
        this.dataAiBaseUrl = dataAiBaseUrl;
        this.dataAiInternalApiKey = dataAiInternalApiKey;
    }

    /**
     * Build HTTP POST request with JSON body and optional internal API key header.
     */
    protected HttpRequest buildPostRequest(String endpoint, String jsonBody) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(dataAiBaseUrl + endpoint))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (dataAiInternalApiKey != null && !dataAiInternalApiKey.isBlank()) {
            builder.header("X-Internal-Api-Key", dataAiInternalApiKey);
        }

        return builder.build();
    }

    /**
     * Send HTTP request and return response body as string.
     * Throws exception if status code is not 2xx.
     */
    protected String sendRequest(HttpRequest request, String operationName) throws Exception {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            log.warn("data_ai_service {} responded with status {}", operationName, response.statusCode());
            throw new IllegalStateException("Upstream error: " + response.statusCode());
        }

        return response.body();
    }

    /**
     * Helper to safely cast Object to String, returning null if not a string or empty.
     */
    protected String asString(Object value) {
        if (value == null) return null;
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? null : s;
    }

    /**
     * Helper to safely parse Object to Double, returning null if parsing fails.
     */
    protected Double asDouble(Object value) {
        if (value == null) return null;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
