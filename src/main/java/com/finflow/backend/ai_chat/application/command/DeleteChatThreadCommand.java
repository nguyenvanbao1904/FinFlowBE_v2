package com.finflow.backend.ai_chat.application.command;

public record DeleteChatThreadCommand(
        String userId,
        String threadId
) {}
