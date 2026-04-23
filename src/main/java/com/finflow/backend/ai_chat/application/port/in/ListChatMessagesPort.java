package com.finflow.backend.ai_chat.application.port.in;

import com.finflow.backend.ai_chat.application.query.ListChatMessagesQuery;
import com.finflow.backend.ai_chat.application.dto.ChatMessageOutput;
import org.springframework.data.domain.Page;

public interface ListChatMessagesPort {
    Page<ChatMessageOutput> execute(ListChatMessagesQuery query);
}
