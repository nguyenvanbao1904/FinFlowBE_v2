package com.finflow.backend.ai_chat.application.dto;

public record SendChatMessageOutput(
        String threadId,
        boolean needsClarification,
        String clarificationQuestion,
        ChatMessageOutput userMessage,
        ChatMessageOutput assistantMessage
) {}
