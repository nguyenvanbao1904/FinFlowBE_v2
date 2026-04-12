package com.finflow.backend.ai_chat.application.usecase;

import com.finflow.backend.ai_chat.application.port.in.RefreshThreadSummaryPort;

import com.finflow.backend.ai_chat.application.port.out.AiChatGatewayPort;
import com.finflow.backend.ai_chat.domain.entity.ChatMessage;
import com.finflow.backend.ai_chat.domain.entity.ChatThread;
import com.finflow.backend.ai_chat.domain.repository.ChatMessageRepository;
import com.finflow.backend.ai_chat.domain.repository.ChatThreadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshThreadSummaryUseCase implements RefreshThreadSummaryPort {

    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiChatGatewayPort dataAiChatGateway;

    @Transactional
    @Override
    public void execute(String threadId) {
        ChatThread thread = chatThreadRepository.findById(threadId).orElse(null);
        if (thread == null) {
            return;
        }

        long totalMessages = chatMessageRepository.countByThreadId(threadId);
        if (totalMessages <= 10 || totalMessages % 10 != 0) {
            return;
        }

        List<ChatMessage> lastMessagesDesc = chatMessageRepository.findTop10ByThreadIdOrderByCreatedAtDesc(threadId);
        List<ChatMessage> lastMessages = new ArrayList<>(lastMessagesDesc);
        Collections.reverse(lastMessages);

        List<AiChatGatewayPort.ConversationMessage> payloadMessages = lastMessages.stream()
                .map(m -> new AiChatGatewayPort.ConversationMessage(
                        m.getRole(),
                        m.getContent(),
                        m.getCreatedAt() == null ? null : m.getCreatedAt().toString()
                ))
                .toList();

        AiChatGatewayPort.ThreadSummaryResult summaryResult = dataAiChatGateway.summarizeThread(
                thread.getId(),
                thread.getUserId(),
                thread.getContextSummary(),
                payloadMessages
        );

        if (summaryResult.contextSummary() != null && !summaryResult.contextSummary().isBlank()) {
            thread.setContextSummary(summaryResult.contextSummary());
        }
        if (summaryResult.currentTicker() != null && !summaryResult.currentTicker().isBlank()) {
            thread.setLastTicker(summaryResult.currentTicker());
        }
        if (summaryResult.currentPeriod() != null) {
            thread.setLastYear(summaryResult.currentPeriod());
        }

        chatThreadRepository.save(thread);
    }
}
