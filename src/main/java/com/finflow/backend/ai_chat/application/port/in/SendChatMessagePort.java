package com.finflow.backend.ai_chat.application.port.in;

import com.finflow.backend.ai_chat.application.command.SendChatMessageCommand;
import com.finflow.backend.ai_chat.application.dto.SendChatMessageOutput;

public interface SendChatMessagePort {
    SendChatMessageOutput execute(SendChatMessageCommand command);
}
