package com.finflow.backend.finance.wealth.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@Table(name = "wealth_account")
@EntityListeners(AuditingEntityListener.class)
public class WealthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String userId;

    @Column(nullable = false, length = 256)
    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_type_id", nullable = false)
    WealthAccountType wealthAccountType;

    @Builder.Default
    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    Boolean isSynced = false;

    @Builder.Default
    @Column(name = "include_in_net_worth", nullable = false)
    Boolean includeInNetWorth = true;

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;
}
