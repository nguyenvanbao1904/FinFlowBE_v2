package com.finflow.backend.ai_chat.application.port.in;

import com.finflow.backend.ai_chat.application.command.DeleteChatThreadCommand;

public interface DeleteChatThreadPort {
    void execute(DeleteChatThreadCommand command);
}
