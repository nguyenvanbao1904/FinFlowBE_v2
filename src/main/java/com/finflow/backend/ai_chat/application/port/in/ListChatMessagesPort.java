package com.finflow.backend.ai_chat.application.port.in;

import com.finflow.backend.ai_chat.domain.entity.ChatMessage;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.finflow.backend.ai_chat.presentation.response.ChatMessageResponse;
import com.finflow.backend.ai_chat.domain.entity.ChatMessageSource;
import com.finflow.backend.ai_chat.presentation.response.ChatMessageSourceResponse;
import java.util.stream.Collectors;
import com.finflow.backend.ai_chat.domain.entity.ChatThread;

public interface ListChatMessagesPort {
    List<ChatMessageResponse> execute(String userId, String threadId);
}
