package com.finflow.backend.ai_chat.application.usecase;

import com.finflow.backend.ai_chat.application.mapper.ChatThreadResponseMapper;
import com.finflow.backend.ai_chat.application.port.in.ListChatThreadsPort;
import com.finflow.backend.ai_chat.domain.repository.ChatThreadRepository;
import com.finflow.backend.ai_chat.presentation.response.ChatThreadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListChatThreadsUseCase implements ListChatThreadsPort {

    private final ChatThreadRepository chatThreadRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public List<ChatThreadResponse> execute(String userId) {
        return chatThreadRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(ChatThreadResponseMapper::toResponse)
                .toList();
    }
}
