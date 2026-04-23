package com.finflow.backend.ai_chat.application.port.in;

import com.finflow.backend.ai_chat.application.query.ListChatThreadsQuery;
import com.finflow.backend.ai_chat.application.dto.ChatThreadOutput;
import org.springframework.data.domain.Page;

public interface ListChatThreadsPort {
    Page<ChatThreadOutput> execute(ListChatThreadsQuery query);
}
