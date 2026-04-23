package com.finflow.backend.ai_chat.application.usecase;

import com.finflow.backend.ai_chat.application.command.DeleteChatThreadCommand;
import com.finflow.backend.ai_chat.application.port.in.DeleteChatThreadPort;
import com.finflow.backend.ai_chat.domain.entity.ChatMessage;
import com.finflow.backend.ai_chat.domain.entity.ChatThread;
import com.finflow.backend.ai_chat.domain.repository.ChatMessageRepository;
import com.finflow.backend.ai_chat.domain.repository.ChatMessageSourceRepository;
import com.finflow.backend.ai_chat.domain.repository.ChatThreadRepository;
import com.finflow.backend.ai_chat.exception.ChatErrorCode;
import com.finflow.backend.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeleteChatThreadUseCase implements DeleteChatThreadPort {

    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageSourceRepository chatMessageSourceRepository;

    @Transactional
    @Override
    public void execute(DeleteChatThreadCommand command) {
        ChatThread thread = chatThreadRepository.findByIdAndUserId(command.threadId(), command.userId())
                .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_THREAD_NOT_FOUND));

        List<String> messageIds = chatMessageRepository
                .findByThreadId(thread.getId(), PageRequest.of(0, Integer.MAX_VALUE))
                .map(ChatMessage::getId)
                .getContent();

        if (!messageIds.isEmpty()) {
            chatMessageSourceRepository.deleteByMessageIdIn(messageIds);
        }
        chatMessageRepository.deleteByThreadId(thread.getId());
        chatThreadRepository.delete(thread);
    }
}
