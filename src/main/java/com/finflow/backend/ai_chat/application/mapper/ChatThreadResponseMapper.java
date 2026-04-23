package com.finflow.backend.ai_chat.application.mapper;

import com.finflow.backend.ai_chat.application.dto.ChatThreadOutput;
import com.finflow.backend.ai_chat.domain.entity.ChatThread;

/**
 * Maps {@link ChatThread} domain entities to API responses. Shared by multiple use cases
 * so listing threads does not depend on another use case class.
 */
public final class ChatThreadResponseMapper {

    private ChatThreadResponseMapper() {
    }

    public static ChatThreadOutput toOutput(ChatThread thread) {
        return new ChatThreadOutput(
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
