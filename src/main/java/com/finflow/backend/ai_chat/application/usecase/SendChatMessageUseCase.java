package com.finflow.backend.ai_chat.application.usecase;

import com.finflow.backend.ai_chat.application.port.in.SendChatMessagePort;

import com.finflow.backend.ai_chat.application.command.SendChatMessageCommand;
import com.finflow.backend.ai_chat.application.dto.ChatMessageOutput;
import com.finflow.backend.ai_chat.application.dto.ChatMessageSourceOutput;
import com.finflow.backend.ai_chat.application.dto.SendChatMessageOutput;
import com.finflow.backend.ai_chat.application.event.ThreadSummaryRefreshRequestedEvent;
import com.finflow.backend.ai_chat.application.port.out.AiChatGatewayPort;
import com.finflow.backend.ai_chat.domain.entity.ChatMessage;
import com.finflow.backend.ai_chat.domain.entity.ChatMessageSource;
import com.finflow.backend.ai_chat.domain.entity.ChatThread;
import com.finflow.backend.ai_chat.domain.repository.ChatMessageRepository;
import com.finflow.backend.ai_chat.domain.repository.ChatMessageSourceRepository;
import com.finflow.backend.ai_chat.domain.repository.ChatThreadRepository;
import com.finflow.backend.ai_chat.exception.ChatErrorCode;
import com.finflow.backend.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sends a user message, calls the AI service, and persists the response.
 * <p>
 * IMPORTANT: The AI HTTP call (up to 120s) must NOT be inside a @Transactional boundary.
 * Otherwise, a DB connection is held for the entire duration, starving the connection pool.
 * The flow is split into 3 phases: save user msg → call AI (no tx) → save AI response.
 */
@Component
@RequiredArgsConstructor
public class SendChatMessageUseCase implements SendChatMessagePort {

    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageSourceRepository chatMessageSourceRepository;
    private final AiChatGatewayPort dataAiChatGateway;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public SendChatMessageOutput execute(SendChatMessageCommand command) {
        Phase1Result phase1 = saveUserMessage(command);

        AiChatGatewayPort.OrchestrateResult aiResult = dataAiChatGateway.orchestrate(
                new AiChatGatewayPort.OrchestrateCommand(
                        phase1.thread.getId(),
                        command.userId(),
                        phase1.normalizedContent,
                        phase1.thread.getContextSummary(),
                        phase1.recentMessages.stream()
                                .map(m -> new AiChatGatewayPort.ConversationMessage(
                                        m.getRole(),
                                        m.getContent(),
                                        m.getCreatedAt() == null ? null : m.getCreatedAt().toString()
                                ))
                                .toList()
                )
        );

        return saveAiResponse(phase1.thread, phase1.userMessage, aiResult);
    }

    @Transactional
    protected Phase1Result saveUserMessage(SendChatMessageCommand command) {
        String userId = command.userId();
        String threadId = command.threadId();

        ChatThread thread = chatThreadRepository.findByIdAndUserId(threadId, userId)
                .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_THREAD_NOT_FOUND));

        String normalizedContent = normalizeContent(command.content());

        ChatMessage userMessage = ChatMessage.builder()
                .threadId(thread.getId())
                .role("user")
                .content(normalizedContent)
                .build();
        userMessage = chatMessageRepository.save(userMessage);

        List<ChatMessage> recentMessagesDesc = chatMessageRepository.findTop20ByThreadIdOrderByCreatedAtDesc(thread.getId());
        List<ChatMessage> recentMessages = new ArrayList<>(recentMessagesDesc);
        Collections.reverse(recentMessages);

        return new Phase1Result(thread, normalizedContent, recentMessages, userMessage);
    }

    @Transactional
    protected SendChatMessageOutput saveAiResponse(ChatThread thread, ChatMessage userMessage,
                                                    AiChatGatewayPort.OrchestrateResult aiResult) {
        String assistantContent = aiResult.assistantMessage();
        if (assistantContent == null || assistantContent.isBlank()) {
            assistantContent = aiResult.clarificationQuestion() != null && !aiResult.clarificationQuestion().isBlank()
                    ? aiResult.clarificationQuestion()
                    : "Xin lỗi, tôi chưa thể xử lý yêu cầu lúc này.";
        }

        ChatMessage assistantMessage = ChatMessage.builder()
                .threadId(thread.getId())
                .role("assistant")
                .content(assistantContent)
                .provider(aiResult.provider())
                .model(aiResult.model())
                .inputTokens(aiResult.inputTokens())
                .outputTokens(aiResult.outputTokens())
                .totalTokens(aiResult.totalTokens())
                .costUsd(scaleCost(aiResult.costUsd()))
                .latencyMs(aiResult.latencyMs())
                .toolCallsJson(aiResult.toolCallsJson())
                .build();
        assistantMessage = chatMessageRepository.save(assistantMessage);

        List<ChatMessageSource> sources = new ArrayList<>();
        for (AiChatGatewayPort.Citation citation : aiResult.citations()) {
            sources.add(ChatMessageSource.builder()
                    .messageId(assistantMessage.getId())
                    .chunkId(citation.chunkId())
                    .sourceTitle(citation.sourceTitle())
                    .pageNumber(citation.pageNumber())
                    .score(citation.score() == null ? null : BigDecimal.valueOf(citation.score()).setScale(6, RoundingMode.HALF_UP))
                    .build());
        }
        if (!sources.isEmpty()) {
            chatMessageSourceRepository.saveAll(sources);
        }

        if (aiResult.lastTicker() != null && !aiResult.lastTicker().isBlank()) {
            thread.setLastTicker(aiResult.lastTicker());
        }
        if (aiResult.lastYear() != null) {
            thread.setLastYear(aiResult.lastYear());
        }
        chatThreadRepository.save(thread);

        long totalMessages = chatMessageRepository.countByThreadId(thread.getId());
        if (totalMessages > 10 && totalMessages % 10 == 0) {
            eventPublisher.publishEvent(new ThreadSummaryRefreshRequestedEvent(thread.getId()));
        }

        List<ChatMessageSourceOutput> sourceOutputs = sources.stream()
                .map(s -> new ChatMessageSourceOutput(s.getChunkId(), s.getSourceTitle(), s.getPageNumber(), s.getScore()))
                .toList();

        ChatMessageOutput userMessageOutput = toMessageOutput(userMessage, Collections.emptyList());
        ChatMessageOutput assistantMessageOutput = toMessageOutput(assistantMessage, sourceOutputs);

        return new SendChatMessageOutput(
                thread.getId(),
                aiResult.needsClarification(),
                aiResult.clarificationQuestion(),
                userMessageOutput,
                assistantMessageOutput
        );
    }

    private static ChatMessageOutput toMessageOutput(ChatMessage message, List<ChatMessageSourceOutput> sources) {
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

    private static String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new AppException(ChatErrorCode.CHAT_MESSAGE_CONTENT_REQUIRED);
        }
        return content.trim();
    }

    private static BigDecimal scaleCost(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(8, RoundingMode.HALF_UP);
    }

    private record Phase1Result(ChatThread thread, String normalizedContent, List<ChatMessage> recentMessages,
                                    ChatMessage userMessage) {}
}
