package com.finflow.backend.investment.market_data.infrastructure.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.market_data.application.port.in.GetFairValuePort;
import com.finflow.backend.investment.market_data.exception.MarketDataErrorCode;
import com.finflow.backend.investment.market_data.presentation.response.FairValueResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP adapter that calls data_ai_service /api/v1/ai/fair-value to compute
 * AI-powered fair value using the industry PE/PB/PS playbook.
 */
@Component
@Slf4j
public class RestDataAiFairValueAdapter implements GetFairValuePort {

    private final ObjectMapper objectMapper;
    private final String dataAiBaseUrl;
    private final String dataAiInternalApiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public RestDataAiFairValueAdapter(
            ObjectMapper objectMapper,
            @Value("${data.ai.base-url:http://localhost:8001}") String dataAiBaseUrl,
            @Value("${data.ai.internal-api-key:}") String dataAiInternalApiKey
    ) {
        this.objectMapper = objectMapper;
        this.dataAiBaseUrl = dataAiBaseUrl;
        this.dataAiInternalApiKey = dataAiInternalApiKey;
    }

    @Override
    public FairValueResponse execute(String symbol, Integer targetYear) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("symbol", symbol);
            if (targetYear != null) {
                payload.put("targetYear", targetYear);
            }

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(dataAiBaseUrl + "/api/v1/ai/fair-value"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)));

            if (dataAiInternalApiKey != null && !dataAiInternalApiKey.isBlank()) {
                builder.header("X-Internal-Api-Key", dataAiInternalApiKey);
            }

            HttpResponse<String> response = httpClient
                    .send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("data_ai_service fair-value responded with status {}", response.statusCode());
                throw new AppException(MarketDataErrorCode.FAIR_VALUE_UPSTREAM_ERROR);
            }

            Map<String, Object> body = objectMapper.readValue(response.body(), new TypeReference<>() {});
            return mapToResponse(body);

        } catch (AppException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(MarketDataErrorCode.FAIR_VALUE_UPSTREAM_ERROR);
        } catch (Exception e) {
            log.warn("data_ai_service fair-value call failed: {}", e.getMessage());
            throw new AppException(MarketDataErrorCode.FAIR_VALUE_UPSTREAM_ERROR);
        }
    }

    private FairValueResponse mapToResponse(Map<String, Object> body) {
        return FairValueResponse.builder()
                .symbol(asString(body.get("symbol")))
                .companyName(asString(body.get("companyName")))
                .targetYear(asInt(body.get("targetYear")))
                .industryKey(asString(body.get("industryKey")))
                .method(asString(body.get("method")))
                .weightsUsed(asString(body.get("weightsUsed")))
                .priceComposite(asDouble(body.get("priceComposite")))
                .pricePE(asDouble(body.get("pricePE")))
                .pricePB(asDouble(body.get("pricePB")))
                .pricePS(asDouble(body.get("pricePS")))
                .livePrice(asDouble(body.get("livePrice")))
                .upsidePct(asDouble(body.get("upsidePct")))
                .verdict(asString(body.get("verdict")))
                .peTarget(asDouble(body.get("peTarget")))
                .pbTarget(asDouble(body.get("pbTarget")))
                .cagr(asDouble(body.get("cagr")))
                .error(asString(body.get("error")))
                .build();
    }

    private String asString(Object value) {
        if (value == null) return null;
        String s = String.valueOf(value).trim();
        return s.isEmpty() || "null".equals(s) ? null : s;
    }

    private double asDouble(Object value) {
        if (value == null) return 0.0;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private int asInt(Object value) {
        if (value == null) return 0;
        try {
            return (int) Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }
}
