package com.finflow.backend.ai_chat.application.port.in;

import com.finflow.backend.ai_chat.presentation.response.ChatThreadResponse;
import com.finflow.backend.ai_chat.application.command.CreateChatThreadCommand;
import com.finflow.backend.ai_chat.domain.entity.ChatThread;

public interface CreateChatThreadPort {
    ChatThreadResponse execute(CreateChatThreadCommand command);
}
