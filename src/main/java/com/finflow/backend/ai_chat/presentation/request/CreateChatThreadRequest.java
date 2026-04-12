package com.finflow.backend.ai_chat.presentation.request;

import jakarta.validation.constraints.Size;

public record CreateChatThreadRequest(
        @Size(max = 255, message = "CHAT_THREAD_TITLE_TOO_LONG")
        String title
) {
}
