package com.finflow.backend.finance.dashboard.infrastructure.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.common.infrastructure.AbstractRestDataAiAdapter;
import com.finflow.backend.finance.dashboard.application.dto.HomeInsightOutput;
import com.finflow.backend.finance.dashboard.application.port.out.DataAiHomeInsightPort;
import com.finflow.backend.finance.dashboard.exception.DashboardErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RestDataAiHomeInsightAdapter extends AbstractRestDataAiAdapter implements DataAiHomeInsightPort {

    public RestDataAiHomeInsightAdapter(
            ObjectMapper objectMapper,
            @Value("${data.ai.base-url:http://localhost:8001}") String dataAiBaseUrl,
            @Value("${data.ai.internal-api-key:}") String dataAiInternalApiKey
    ) {
        super(objectMapper, dataAiBaseUrl, dataAiInternalApiKey);
    }

    @Override
    public HomeInsightOutput generate(Map<String, Object> payload) {
        try {
            String jsonBody = objectMapper.writeValueAsString(payload);
            var request = buildPostRequest("/api/v1/ai/home-insight", jsonBody);
            String responseBody = sendRequest(request, "home-insight");
            Map<String, Object> body = objectMapper.readValue(responseBody, new TypeReference<>() {});
            return parseResponse(body);
        } catch (AppException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AppException(DashboardErrorCode.HOME_INSIGHT_UPSTREAM_ERROR);
        } catch (Exception exception) {
            log.warn("data_ai_service home-insight call failed: {}", exception.getMessage());
            throw new AppException(DashboardErrorCode.HOME_INSIGHT_UPSTREAM_ERROR);
        }
    }

    private HomeInsightOutput parseResponse(Map<String, Object> body) {
        String title = asString(body.get("title"));
        String message = asString(body.get("message"));
        List<String> warnings = parseWarnings(body.get("warnings"));
        boolean cached = Boolean.parseBoolean(String.valueOf(body.getOrDefault("cached", false)));
        return new HomeInsightOutput(
                title != null ? title : "Gợi ý hôm nay",
                message != null ? message : "",
                warnings,
                cached
        );
    }

    private List<String> parseWarnings(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .map(this::asString)
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }
}
