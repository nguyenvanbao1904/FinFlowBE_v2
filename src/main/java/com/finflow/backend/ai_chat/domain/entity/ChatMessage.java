package com.finflow.backend.ai_chat.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "chat_messages",
        indexes = {
                @Index(name = "idx_chat_messages_thread_created", columnList = "thread_id, created_at"),
                @Index(name = "idx_chat_messages_thread_role", columnList = "thread_id, role")
        }
)
public class ChatMessage {

    @Id
    @Column(length = 36, nullable = false)
    String id;

    @Column(name = "thread_id", nullable = false, length = 36)
    String threadId;

    @Column(nullable = false, length = 16)
    String role;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    String content;

    @Column(name = "tool_call_id", length = 255)
    String toolCallId;

    @Column(length = 32)
    String provider;

    @Column(length = 64)
    String model;

    @Column(name = "input_tokens")
    Integer inputTokens;

    @Column(name = "output_tokens")
    Integer outputTokens;

    @Column(name = "total_tokens")
    Integer totalTokens;

    @Column(name = "cost_usd", precision = 18, scale = 8)
    BigDecimal costUsd;

    @Column(name = "latency_ms")
    Integer latencyMs;

    @Lob
    @Column(name = "tool_calls_json", columnDefinition = "LONGTEXT")
    String toolCallsJson;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        if (role != null) {
            role = role.trim().toLowerCase();
        }
    }
}
