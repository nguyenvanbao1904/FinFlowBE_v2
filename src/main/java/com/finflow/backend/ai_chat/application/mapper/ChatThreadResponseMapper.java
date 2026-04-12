package com.finflow.backend.ai_chat.application.mapper;

import com.finflow.backend.ai_chat.domain.entity.ChatThread;
import com.finflow.backend.ai_chat.presentation.response.ChatThreadResponse;

/**
 * Maps {@link ChatThread} domain entities to API responses. Shared by multiple use cases
 * so listing threads does not depend on another use case class.
 */
public final class ChatThreadResponseMapper {

    private ChatThreadResponseMapper() {
    }

    public static ChatThreadResponse toResponse(ChatThread thread) {
        return new ChatThreadResponse(
                thread.getId(),
                thread.getTitle(),
                thread.getLastTicker(),
                thread.getLastYear(),
                thread.getContextSummary(),
                thread.getCreatedAt(),
                thread.getUpdatedAt()
        );
    }
}
