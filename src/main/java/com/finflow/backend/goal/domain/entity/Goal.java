package com.finflow.backend.goal.domain.entity;

import com.finflow.backend.goal.domain.enums.GoalStatus; // Nhớ tạo Enum này
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "goals")
@EntityListeners(AuditingEntityListener.class)
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String userId;

    @Column(nullable = false)
    String name;

    String description;

    @Column(nullable = false)
    BigDecimal targetAmount;

    @Builder.Default
    @Column(nullable = false)
    BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    LocalDate startDate;

    @Column(nullable = false)
    LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    GoalStatus status = GoalStatus.IN_PROGRESS;

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;
}