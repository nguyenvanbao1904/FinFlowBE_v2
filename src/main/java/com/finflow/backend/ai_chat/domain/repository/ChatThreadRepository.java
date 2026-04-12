package com.finflow.backend.ai_chat.domain.repository;

import com.finflow.backend.ai_chat.domain.entity.ChatThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatThreadRepository extends JpaRepository<ChatThread, String> {
    Optional<ChatThread> findByIdAndUserId(String id, String userId);
    List<ChatThread> findByUserIdOrderByUpdatedAtDesc(String userId);
}
