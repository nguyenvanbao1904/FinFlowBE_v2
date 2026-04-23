package com.finflow.backend.ai_chat.application.usecase;

import com.finflow.backend.ai_chat.application.mapper.ChatThreadResponseMapper;
import com.finflow.backend.ai_chat.application.dto.ChatThreadOutput;
import com.finflow.backend.ai_chat.application.port.in.ListChatThreadsPort;
import com.finflow.backend.ai_chat.application.query.ListChatThreadsQuery;
import com.finflow.backend.ai_chat.domain.repository.ChatThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ListChatThreadsUseCase implements ListChatThreadsPort {

    private final ChatThreadRepository chatThreadRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<ChatThreadOutput> execute(ListChatThreadsQuery request) {
        return chatThreadRepository.findByUserId(request.userId(), request.pageable())
                .map(ChatThreadResponseMapper::toOutput);
    }
}
