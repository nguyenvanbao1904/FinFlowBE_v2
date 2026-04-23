package com.finflow.backend.finance.transaction.infrastructure.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.application.port.out.DataAiAnalyticsPort;
import com.finflow.backend.finance.transaction.application.dto.AnalyticsInsightItem;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP adapter that calls data_ai_service to fetch analytics insights.
 * Lives in infrastructure so that the use case depends only on {@link DataAiAnalyticsPort}.
 */
@Component
@Slf4j
public class RestDataAiAnalyticsAdapter implements DataAiAnalyticsPort {

    private final ObjectMapper objectMapper;
    private final String dataAiBaseUrl;
    private final String dataAiInternalApiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public RestDataAiAnalyticsAdapter(
            ObjectMapper objectMapper,
            @Value("${data.ai.base-url:http://localhost:8001}") String dataAiBaseUrl,
            @Value("${data.ai.internal-api-key:}") String dataAiInternalApiKey
    ) {
        this.objectMapper = objectMapper;
        this.dataAiBaseUrl = dataAiBaseUrl;
        this.dataAiInternalApiKey = dataAiInternalApiKey;
    }

    @Override
    public List<AnalyticsInsightItem> fetchInsights(Map<String, Object> payload) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(dataAiBaseUrl + "/api/v1/ai/analytics-insights"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)));

            if (dataAiInternalApiKey != null && !dataAiInternalApiKey.isBlank()) {
                builder.header("X-Internal-Api-Key", dataAiInternalApiKey);
            }

            HttpResponse<String> response = httpClient
                    .send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("data_ai_service responded with status {}", response.statusCode());
                throw new AppException(TransactionErrorCode.ANALYTICS_UPSTREAM_ERROR);
            }

            Map<String, Object> body = objectMapper.readValue(response.body(), new TypeReference<>() {});
            return parseInsights(body);
        } catch (AppException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(TransactionErrorCode.ANALYTICS_UPSTREAM_ERROR);
        } catch (Exception e) {
            log.warn("data_ai_service call failed: {}", e.getMessage());
            throw new AppException(TransactionErrorCode.ANALYTICS_UPSTREAM_ERROR);
        }
    }

    private List<AnalyticsInsightItem> parseInsights(Map<String, Object> body) {
        Object raw = body.get("insights");
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<AnalyticsInsightItem> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) continue;
            String type = asString(map.get("type"));
            String title = asString(map.get("title"));
            String message = asString(map.get("message"));
            if (type == null || title == null || message == null) continue;
            String id = asString(map.get("id"));
            Double confidence = asDouble(map.get("confidence"));
            result.add(new AnalyticsInsightItem(
                    id != null ? id : title,
                    type,
                    title,
                    message,
                    confidence != null ? confidence : 0.0
            ));
        }
        return result;
    }

    private String asString(Object value) {
        if (value == null) return null;
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? null : s;
    }

    private Double asDouble(Object value) {
        if (value == null) return null;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
