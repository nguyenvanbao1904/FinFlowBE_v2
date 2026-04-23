package com.finflow.backend.ai_chat.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ChatMessageOutput(
        String id,
        String threadId,
        String role,
        String content,
        String provider,
        String model,
        Integer inputTokens,
        Integer outputTokens,
        Integer totalTokens,
        BigDecimal costUsd,
        Integer latencyMs,
        String toolCallsJson,
        LocalDateTime createdAt,
        List<ChatMessageSourceOutput> sources
) {}
