package com.finflow.backend.ai_chat.domain.repository;

import com.finflow.backend.ai_chat.domain.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    Page<ChatMessage> findByThreadId(String threadId, Pageable pageable);
    List<ChatMessage> findTop20ByThreadIdOrderByCreatedAtDesc(String threadId);
    List<ChatMessage> findTop10ByThreadIdOrderByCreatedAtDesc(String threadId);
    long countByThreadId(String threadId);
    void deleteByThreadId(String threadId);
}
