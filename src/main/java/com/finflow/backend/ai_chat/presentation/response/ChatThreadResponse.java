package com.finflow.backend.ai_chat.presentation.response;

import java.time.LocalDateTime;

public record ChatThreadResponse(
        String id,
        String title,
        String lastTicker,
        Integer lastYear,
        String contextSummary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
