package com.finflow.backend.ai_chat.application.dto;

import java.time.LocalDateTime;

public record ChatThreadOutput(
        String id,
        String title,
        String lastTicker,
        Integer lastYear,
        String contextSummary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
