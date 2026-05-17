package com.finflow.backend.finance.transaction.infrastructure.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.common.infrastructure.AbstractRestDataAiAdapter;
import com.finflow.backend.finance.transaction.application.port.out.DataAiAnalyticsPort;
import com.finflow.backend.finance.transaction.application.dto.AnalyticsInsightItem;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP adapter that calls data_ai_service to fetch analytics insights.
 * Lives in infrastructure so that the use case depends only on {@link DataAiAnalyticsPort}.
 */
@Component
@Slf4j
public class RestDataAiAnalyticsAdapter extends AbstractRestDataAiAdapter implements DataAiAnalyticsPort {

    public RestDataAiAnalyticsAdapter(
            ObjectMapper objectMapper,
            @Value("${data.ai.base-url:http://localhost:8001}") String dataAiBaseUrl,
            @Value("${data.ai.internal-api-key:}") String dataAiInternalApiKey
    ) {
        super(objectMapper, dataAiBaseUrl, dataAiInternalApiKey);
    }

    @Override
    public List<AnalyticsInsightItem> fetchInsights(Map<String, Object> payload) {
        try {
            String jsonBody = objectMapper.writeValueAsString(payload);
            var request = buildPostRequest("/api/v1/ai/analytics-insights", jsonBody);
            String responseBody = sendRequest(request, "analytics-insights");

            Map<String, Object> body = objectMapper.readValue(responseBody, new TypeReference<>() {});
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
}
