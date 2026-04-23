package com.finflow.backend.finance.transaction.infrastructure.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.application.port.out.AnalyzeTransactionWithAiPort;
import com.finflow.backend.finance.transaction.application.result.TransactionPrefillResult;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * HTTP adapter that calls data_ai_service for transaction prefill suggestions.
 * Lives in infrastructure so that the use case depends only on {@link AnalyzeTransactionWithAiPort}.
 */
@Component
@Slf4j
public class RestAiTransactionPrefillAdapter implements AnalyzeTransactionWithAiPort {

    private final ObjectMapper objectMapper;
    private final String dataAiBaseUrl;
    private final String dataAiInternalApiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public RestAiTransactionPrefillAdapter(
            ObjectMapper objectMapper,
            @Value("${data.ai.base-url:http://localhost:8001}") String dataAiBaseUrl,
            @Value("${data.ai.internal-api-key:}") String dataAiInternalApiKey
    ) {
        this.objectMapper = objectMapper;
        this.dataAiBaseUrl = dataAiBaseUrl;
        this.dataAiInternalApiKey = dataAiInternalApiKey;
    }

    @Override
    public TransactionPrefillResult analyze(
            String rawText,
            List<Map<String, ?>> categories,
            List<Map<String, ?>> accounts,
            List<Map<String, ?>> recentHistory
    ) {
        Map<String, Object> body = Map.of(
                "rawText", rawText,
                "categories", categories,
                "accounts", accounts,
                "recentHistory", recentHistory,
                "locale", "vi-VN",
                "timezone", "Asia/Ho_Chi_Minh",
                "source", "text"
        );

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(dataAiBaseUrl + "/api/v1/ai/transaction-prefill"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));

            if (dataAiInternalApiKey != null && !dataAiInternalApiKey.isBlank()) {
                builder.header("X-Internal-Api-Key", dataAiInternalApiKey);
            }

            HttpResponse<String> response = httpClient
                    .send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("data_ai_service (prefill) responded with status {}", response.statusCode());
                throw new AppException(TransactionErrorCode.AI_PREFILL_UPSTREAM_ERROR);
            }

            Map<String, Object> parsed = objectMapper.readValue(response.body(), new TypeReference<>() {});
            return mapToPrefillResult(parsed);

        } catch (AppException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(TransactionErrorCode.AI_PREFILL_UPSTREAM_ERROR);
        } catch (Exception e) {
            log.warn("data_ai_service (prefill) call failed: {}", e.getMessage());
            throw new AppException(TransactionErrorCode.AI_PREFILL_UPSTREAM_ERROR);
        }
    }

    private TransactionPrefillResult mapToPrefillResult(Map<String, Object> raw) {
        return new TransactionPrefillResult(
                parseBigDecimal(raw.get("amount")),
                asString(raw.get("type")),
                asString(raw.get("categoryId")),
                asString(raw.get("accountId")),
                asString(raw.get("note")),
                asString(raw.get("transactionDate"))
        );
    }

    private String asString(Object value) {
        if (value == null) return null;
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? null : s;
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return null;
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
