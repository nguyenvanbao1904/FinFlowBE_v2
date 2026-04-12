package com.finflow.backend.ai_chat.application.command;

public record SendChatMessageCommand(
    String userId,
    String threadId,
    String content
) {}
