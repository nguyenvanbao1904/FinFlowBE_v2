package com.finflow.backend.investment.portfolio.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.common.infrastructure.AbstractRestDataAiAdapter;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioInsightItem;
import com.finflow.backend.investment.portfolio.application.port.out.DataAiPortfolioInsightsPort;
import com.finflow.backend.investment.portfolio.exception.PortfolioErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP adapter that calls data_ai_service to fetch portfolio insights.
 */
@Component
@Slf4j
public class RestDataAiPortfolioInsightsAdapter extends AbstractRestDataAiAdapter implements DataAiPortfolioInsightsPort {

    public RestDataAiPortfolioInsightsAdapter(
            ObjectMapper objectMapper,
            @Value("${data.ai.base-url:http://localhost:8001}") String dataAiBaseUrl,
            @Value("${data.ai.internal-api-key:}") String dataAiInternalApiKey
    ) {
        super(objectMapper, dataAiBaseUrl, dataAiInternalApiKey);
    }

    @Override
    public List<PortfolioInsightItem> fetchInsights(Map<String, Object> payload) {
        try {
            String jsonBody = objectMapper.writeValueAsString(payload);
            var request = buildPostRequest("/api/v1/ai/portfolio-insights", jsonBody);
            String responseBody = sendRequest(request, "portfolio-insights");

            Map<String, Object> body = objectMapper.readValue(responseBody, new TypeReference<>() {});
            return parseInsights(body);
        } catch (AppException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(PortfolioErrorCode.INSIGHTS_UPSTREAM_ERROR);
        } catch (Exception e) {
            log.warn("data_ai_service portfolio-insights call failed: {}", e.getMessage());
            throw new AppException(PortfolioErrorCode.INSIGHTS_UPSTREAM_ERROR);
        }
    }

    private List<PortfolioInsightItem> parseInsights(Map<String, Object> body) {
        Object raw = body.get("insights");
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<PortfolioInsightItem> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) continue;
            String category = asString(map.get("category"));
            String message = asString(map.get("message"));
            if (category == null || message == null) continue;
            result.add(new PortfolioInsightItem(category, message));
        }
        return result;
    }
}
