package com.finflow.backend.ai_chat.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendChatMessageRequest(
        @NotBlank(message = "CHAT_MESSAGE_CONTENT_REQUIRED")
        @Size(max = 8000, message = "CHAT_MESSAGE_CONTENT_TOO_LONG")
        String content
) {
}
