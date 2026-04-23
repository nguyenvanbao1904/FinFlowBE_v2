package com.finflow.backend.ai_chat.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.backend.ai_chat.application.port.out.AiChatGatewayPort;
import com.finflow.backend.ai_chat.exception.ChatErrorCode;
import com.finflow.backend.ai_chat.infrastructure.config.DataAiProperties;
import com.finflow.backend.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestDataAiChatAdapter implements AiChatGatewayPort {

    private static final String ORCHESTRATE_PATH = "/api/v1/ai/chat/orchestrate";
    private static final String THREAD_SUMMARY_PATH = "/api/v1/ai/chat/thread-summary";

    private final ObjectMapper objectMapper;
    private final DataAiProperties dataAiProperties;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public OrchestrateResult orchestrate(OrchestrateCommand command) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("thread_id", command.threadId());
        payload.put("user_id", command.userId());
        payload.put("user_message", command.userMessage());
        payload.put("context_summary", command.contextSummary());

        List<Map<String, Object>> messages = new ArrayList<>();
        for (ConversationMessage item : command.lastMessages()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("role", item.role());
            row.put("content", item.content());
            row.put("created_at", item.createdAt());
            messages.add(row);
        }
        payload.put("last_messages", messages);

        JsonNode root = postJson(ORCHESTRATE_PATH, payload, ChatErrorCode.CHAT_AI_UPSTREAM_ERROR);

        String toolCallsJson = toJsonString(root.path("tool_calls"));
        if (toolCallsJson == null || toolCallsJson.isBlank() || "null".equals(toolCallsJson)) {
            toolCallsJson = "[]";
        }

        List<Citation> citations = new ArrayList<>();
        JsonNode rawCitations = root.path("citations");
        if (rawCitations.isArray()) {
            for (JsonNode row : rawCitations) {
                citations.add(new Citation(
                        asText(row, "chunk_id"),
                        asText(row, "source_title"),
                        asInteger(row, "page_number"),
                        asDouble(row, "score")
                ));
            }
        }

        JsonNode contextUpdate = root.path("context_update");

        return new OrchestrateResult(
                asText(root, "assistant_message"),
                root.path("needs_clarification").asBoolean(false),
                asText(root, "clarification_question"),
                asText(root, "provider"),
                asText(root, "model"),
                asInteger(root, "input_tokens"),
                asInteger(root, "output_tokens"),
                asInteger(root, "total_tokens"),
                asBigDecimal(root, "cost_usd"),
                asInteger(root, "latency_ms"),
                toolCallsJson,
                citations,
                asText(contextUpdate, "last_ticker"),
                asInteger(contextUpdate, "last_year")
        );
    }

    @Override
    public ThreadSummaryResult summarizeThread(
            String threadId,
            String userId,
            String currentSummary,
            List<ConversationMessage> lastMessages
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("thread_id", threadId);
        payload.put("user_id", userId);
        payload.put("existing_summary", currentSummary);

        List<Map<String, Object>> messages = new ArrayList<>();
        for (ConversationMessage item : lastMessages) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("role", item.role());
            row.put("content", item.content());
            row.put("created_at", item.createdAt());
            messages.add(row);
        }
        payload.put("recent_messages", messages);

        JsonNode root = postJson(THREAD_SUMMARY_PATH, payload, ChatErrorCode.CHAT_SUMMARY_UPSTREAM_ERROR);

        return new ThreadSummaryResult(
                asText(root, "context_summary"),
                asText(root, "current_ticker"),
                asInteger(root, "current_period")
        );
    }

    private JsonNode postJson(String path, Map<String, Object> payload, ChatErrorCode errorCode) {
        String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new AppException(errorCode);
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(dataAiProperties.getBaseUrl() + path))
                .timeout(Duration.ofSeconds(Math.max(10, dataAiProperties.getChatTimeoutSeconds())))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        if (dataAiProperties.getInternalApiKey() != null && !dataAiProperties.getInternalApiKey().isBlank()) {
            requestBuilder.header("X-Internal-Api-Key", dataAiProperties.getInternalApiKey());
        }

        String correlationId = MDC.get("correlationId");
        if (correlationId != null && !correlationId.isBlank()) {
            requestBuilder.header("X-Correlation-ID", correlationId);
        }

        try {
            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("data_ai_service error status={} path={} body={}", response.statusCode(), path, response.body());
                throw new AppException(errorCode);
            }
            return objectMapper.readTree(response.body());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("data_ai_service call failed path={} reason={}", path, e.getMessage());
            throw new AppException(errorCode);
        }
    }

    private static String asText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private static Integer asInteger(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asInt();
    }

    private static Double asDouble(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asDouble();
    }

    private static BigDecimal asBigDecimal(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        try {
            return new BigDecimal(v.asText()).setScale(8, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    private String toJsonString(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
