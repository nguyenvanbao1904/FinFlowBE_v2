package com.finflow.backend.ai_chat.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

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
        name = "chat_threads",
        indexes = {
                @Index(name = "idx_chat_threads_user_updated", columnList = "user_id, updated_at")
        }
)
public class ChatThread {

    @Id
    @Column(length = 36, nullable = false)
    String id;

    @Column(name = "user_id", nullable = false, length = 64)
    String userId;

    @Column(nullable = false, length = 255)
    String title;

    @Column(name = "last_ticker", length = 16)
    String lastTicker;

    @Column(name = "last_year")
    Integer lastYear;

    @Column(name = "context_summary", columnDefinition = "TEXT")
    String contextSummary;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (title == null || title.isBlank()) {
            title = "Cuộc trò chuyện mới";
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
