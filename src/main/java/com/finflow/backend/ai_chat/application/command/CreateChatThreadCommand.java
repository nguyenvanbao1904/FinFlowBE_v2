package com.finflow.backend.ai_chat.application.command;

public record CreateChatThreadCommand(
    String userId,
    String title
) {}
