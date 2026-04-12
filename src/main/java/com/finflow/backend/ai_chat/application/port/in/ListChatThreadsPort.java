package com.finflow.backend.ai_chat.application.port.in;

import com.finflow.backend.ai_chat.presentation.response.ChatThreadResponse;
import java.util.List;

public interface ListChatThreadsPort {
    List<ChatThreadResponse> execute(String userId);
}
