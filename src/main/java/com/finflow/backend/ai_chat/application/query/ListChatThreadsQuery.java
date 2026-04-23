package com.finflow.backend.ai_chat.application.query;

import org.springframework.data.domain.Pageable;

public record ListChatThreadsQuery(
        String userId,
        Pageable pageable
) {}
