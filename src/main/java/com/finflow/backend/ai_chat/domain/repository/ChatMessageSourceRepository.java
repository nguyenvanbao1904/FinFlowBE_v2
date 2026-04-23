package com.finflow.backend.ai_chat.domain.repository;

import com.finflow.backend.ai_chat.domain.entity.ChatMessageSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ChatMessageSourceRepository extends JpaRepository<ChatMessageSource, Long> {
    List<ChatMessageSource> findByMessageIdIn(Collection<String> messageIds);
    void deleteByMessageIdIn(Collection<String> messageIds);
}
