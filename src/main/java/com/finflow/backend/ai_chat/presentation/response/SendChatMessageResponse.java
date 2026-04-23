package com.finflow.backend.ai_chat.presentation.response;

public record SendChatMessageResponse(
        String threadId,
        boolean needsClarification,
        String clarificationQuestion,
        ChatMessageResponse userMessage,
        ChatMessageResponse assistantMessage
) {}
