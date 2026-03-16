package com.finflow.backend.finance.wealth.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "wealth_account_type")
@EntityListeners(AuditingEntityListener.class)
public class WealthAccountType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false, unique = true, length = 64)
    String code;

    @Column(nullable = false, length = 128)
    String displayName;

    @Column(length = 64)
    String icon;

    @Column(length = 32)
    String color;

    @Column(name = "is_transaction_eligible", nullable = false)
    Boolean isTransactionEligible;

    /** True for types where balance is stored as negative (e.g. LOAN). Used by clients for sign and display. */
    @Column(name = "is_debt", nullable = false)
    @Builder.Default
    Boolean isDebt = false;
}
