package com.finflow.backend.ai_chat.application.dto;

import java.math.BigDecimal;

public record ChatMessageSourceOutput(
        String chunkId,
        String sourceTitle,
        Integer pageNumber,
        BigDecimal score
) {}
