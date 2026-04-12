package com.finflow.backend.ai_chat.application.usecase;

import com.finflow.backend.ai_chat.application.port.in.ListChatMessagesPort;

import com.finflow.backend.ai_chat.domain.entity.ChatMessage;
import com.finflow.backend.ai_chat.domain.entity.ChatMessageSource;
import com.finflow.backend.ai_chat.domain.entity.ChatThread;
import com.finflow.backend.ai_chat.domain.repository.ChatMessageRepository;
import com.finflow.backend.ai_chat.domain.repository.ChatMessageSourceRepository;
import com.finflow.backend.ai_chat.domain.repository.ChatThreadRepository;
import com.finflow.backend.ai_chat.exception.ChatErrorCode;
import com.finflow.backend.ai_chat.presentation.response.ChatMessageResponse;
import com.finflow.backend.ai_chat.presentation.response.ChatMessageSourceResponse;
import com.finflow.backend.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListChatMessagesUseCase implements ListChatMessagesPort {

    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageSourceRepository chatMessageSourceRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public List<ChatMessageResponse> execute(String userId, String threadId) {
        ChatThread thread = chatThreadRepository.findByIdAndUserId(threadId, userId)
                .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_THREAD_NOT_FOUND));

        List<ChatMessage> messages = chatMessageRepository.findByThreadIdOrderByCreatedAtAsc(thread.getId());
        if (messages.isEmpty()) {
            return List.of();
        }

        List<String> messageIds = messages.stream().map(ChatMessage::getId).toList();
        Map<String, List<ChatMessageSourceResponse>> sourceMap = chatMessageSourceRepository.findByMessageIdIn(messageIds)
                .stream()
                .collect(Collectors.groupingBy(
                        ChatMessageSource::getMessageId,
                        Collectors.mapping(ListChatMessagesUseCase::toSourceResponse, Collectors.toList())
                ));

        return messages.stream()
                .map(message -> toResponse(message, sourceMap.getOrDefault(message.getId(), Collections.emptyList())))
                .toList();
    }

    private static ChatMessageResponse toResponse(ChatMessage message, List<ChatMessageSourceResponse> sources) {
        return new ChatMessageResponse(
                message.getId(),
                message.getThreadId(),
                message.getRole(),
                message.getContent(),
                message.getProvider(),
                message.getModel(),
                message.getInputTokens(),
                message.getOutputTokens(),
                message.getTotalTokens(),
                message.getCostUsd(),
                message.getLatencyMs(),
                message.getToolCallsJson(),
                message.getCreatedAt(),
                sources
        );
    }

    private static ChatMessageSourceResponse toSourceResponse(ChatMessageSource source) {
        return new ChatMessageSourceResponse(
                source.getChunkId(),
                source.getSourceTitle(),
                source.getPageNumber(),
                source.getScore()
        );
    }
}
