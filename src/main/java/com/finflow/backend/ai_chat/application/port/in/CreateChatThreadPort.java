package com.finflow.backend.ai_chat.application.port.in;

import com.finflow.backend.ai_chat.application.command.CreateChatThreadCommand;
import com.finflow.backend.ai_chat.application.dto.ChatThreadOutput;

public interface CreateChatThreadPort {
    ChatThreadOutput execute(CreateChatThreadCommand command);
}
