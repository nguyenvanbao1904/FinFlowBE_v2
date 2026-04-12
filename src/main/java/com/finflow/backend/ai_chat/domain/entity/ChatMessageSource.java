package com.finflow.backend.ai_chat.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "chat_message_sources",
        indexes = {
                @Index(name = "idx_chat_message_sources_message", columnList = "message_id")
        }
)
public class ChatMessageSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "message_id", nullable = false, length = 36)
    String messageId;

    @Column(name = "chunk_id", length = 100)
    String chunkId;

    @Column(name = "source_title", length = 255)
    String sourceTitle;

    @Column(name = "page_number")
    Integer pageNumber;

    @Column(precision = 12, scale = 6)
    BigDecimal score;
}
