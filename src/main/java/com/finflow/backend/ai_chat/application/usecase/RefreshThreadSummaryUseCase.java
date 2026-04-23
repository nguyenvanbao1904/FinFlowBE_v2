package com.finflow.backend.ai_chat.application.usecase;

import com.finflow.backend.ai_chat.application.port.in.RefreshThreadSummaryPort;
import com.finflow.backend.ai_chat.application.command.RefreshThreadSummaryCommand;

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

/**
 * Refreshes the thread context summary by calling AI service.
 * Split into phases to avoid holding DB connection during AI HTTP call.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshThreadSummaryUseCase implements RefreshThreadSummaryPort {

    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiChatGatewayPort dataAiChatGateway;

    @Override
    public void execute(RefreshThreadSummaryCommand request) {
        // Phase 1: Load data (short tx)
        Phase1Data data = loadThreadData(request.threadId());
        if (data == null) return;

        // Phase 2: Call AI (NO transaction)
        AiChatGatewayPort.ThreadSummaryResult summaryResult = dataAiChatGateway.summarizeThread(
                data.thread.getId(),
                data.thread.getUserId(),
                data.thread.getContextSummary(),
                data.payloadMessages
        );

        // Phase 3: Save result (short tx)
        saveSummaryResult(data.thread.getId(), summaryResult);
    }

    @Transactional(readOnly = true)
    protected Phase1Data loadThreadData(String threadId) {
        ChatThread thread = chatThreadRepository.findById(threadId).orElse(null);
        if (thread == null) return null;

        long totalMessages = chatMessageRepository.countByThreadId(threadId);
        if (totalMessages <= 10 || totalMessages % 10 != 0) return null;

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

        return new Phase1Data(thread, payloadMessages);
    }

    @Transactional
    protected void saveSummaryResult(String threadId, AiChatGatewayPort.ThreadSummaryResult summaryResult) {
        ChatThread thread = chatThreadRepository.findById(threadId).orElse(null);
        if (thread == null) return;

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

    private record Phase1Data(ChatThread thread, List<AiChatGatewayPort.ConversationMessage> payloadMessages) {}
}
