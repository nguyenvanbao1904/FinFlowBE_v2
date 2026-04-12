package com.finflow.backend.ai_chat.presentation.response;

import java.math.BigDecimal;

public record ChatMessageSourceResponse(
        String chunkId,
        String sourceTitle,
        Integer pageNumber,
        BigDecimal score
) {
}
