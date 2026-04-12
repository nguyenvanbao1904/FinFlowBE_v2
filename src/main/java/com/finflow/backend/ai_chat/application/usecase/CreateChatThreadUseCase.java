package com.finflow.backend.ai_chat.application.usecase;

import com.finflow.backend.ai_chat.application.port.in.CreateChatThreadPort;

import com.finflow.backend.ai_chat.application.command.CreateChatThreadCommand;
import com.finflow.backend.ai_chat.application.mapper.ChatThreadResponseMapper;
import com.finflow.backend.ai_chat.domain.entity.ChatThread;
import com.finflow.backend.ai_chat.domain.repository.ChatThreadRepository;
import com.finflow.backend.ai_chat.presentation.response.ChatThreadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateChatThreadUseCase implements CreateChatThreadPort {

    private final ChatThreadRepository chatThreadRepository;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public ChatThreadResponse execute(CreateChatThreadCommand command) {
        String userId = command.userId();
        String normalizedTitle = normalizeTitle(command.title());

        ChatThread thread = ChatThread.builder()
                .userId(userId)
                .title(normalizedTitle)
                .build();

        ChatThread saved = chatThreadRepository.save(thread);
        return ChatThreadResponseMapper.toResponse(saved);
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return "Cuộc trò chuyện mới";
        }
        return title.trim();
    }

}
