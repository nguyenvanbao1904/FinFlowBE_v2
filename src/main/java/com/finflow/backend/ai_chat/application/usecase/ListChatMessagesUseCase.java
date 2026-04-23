package com.finflow.backend.ai_chat.application.usecase;

import com.finflow.backend.ai_chat.application.port.in.ListChatMessagesPort;
import com.finflow.backend.ai_chat.application.query.ListChatMessagesQuery;
import com.finflow.backend.ai_chat.application.dto.ChatMessageOutput;
import com.finflow.backend.ai_chat.application.dto.ChatMessageSourceOutput;

import com.finflow.backend.ai_chat.domain.entity.ChatMessage;
import com.finflow.backend.ai_chat.domain.entity.ChatMessageSource;
import com.finflow.backend.ai_chat.domain.entity.ChatThread;
import com.finflow.backend.ai_chat.domain.repository.ChatMessageRepository;
import com.finflow.backend.ai_chat.domain.repository.ChatMessageSourceRepository;
import com.finflow.backend.ai_chat.domain.repository.ChatThreadRepository;
import com.finflow.backend.ai_chat.exception.ChatErrorCode;
import com.finflow.backend.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    @Override
    public Page<ChatMessageOutput> execute(ListChatMessagesQuery request) {
        String userId = request.userId();
        String threadId = request.threadId();
        ChatThread thread = chatThreadRepository.findByIdAndUserId(threadId, userId)
                .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_THREAD_NOT_FOUND));

        Page<ChatMessage> messagePage = chatMessageRepository.findByThreadId(thread.getId(), request.pageable());
        if (messagePage.isEmpty()) {
            return messagePage.map(m -> toResponse(m, Collections.emptyList()));
        }

        List<String> messageIds = messagePage.getContent().stream().map(ChatMessage::getId).toList();
        Map<String, List<ChatMessageSourceOutput>> sourceMap = chatMessageSourceRepository.findByMessageIdIn(messageIds)
                .stream()
                .collect(Collectors.groupingBy(
                        ChatMessageSource::getMessageId,
                        Collectors.mapping(ListChatMessagesUseCase::toSourceResponse, Collectors.toList())
                ));

        return messagePage.map(message ->
                toResponse(message, sourceMap.getOrDefault(message.getId(), Collections.emptyList())));
    }

    private static ChatMessageOutput toResponse(ChatMessage message, List<ChatMessageSourceOutput> sources) {
        return new ChatMessageOutput(
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

    private static ChatMessageSourceOutput toSourceResponse(ChatMessageSource source) {
        return new ChatMessageSourceOutput(
                source.getChunkId(),
                source.getSourceTitle(),
                source.getPageNumber(),
                source.getScore()
        );
    }
}

