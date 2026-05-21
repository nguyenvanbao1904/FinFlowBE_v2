package com.finflow.backend.investment.portfolio.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Rổ Danh Mục - đại diện cho một danh mục đầu tư của user.
 * Mỗi user có thể tạo nhiều danh mục (lướt sóng, tích sản, v.v.)
 * cashBalance: số dư tiền mặt rảnh rỗi trong rổ, chờ mua chứng khoán.
 */
@Entity
@Table(name = "portfolios")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    /**
     * Tham chiếu tới User.id (String UUID) — nhất quán với identity module.
     * Dùng String thay vì FK @ManyToOne để tuân thủ quy tắc cross-module isolation.
     */
    @Column(name = "user_id", nullable = false)
    String userId;

    @Column(nullable = false)
    String name; // VD: "Danh mục lướt sóng", "Tích sản hưu trí"

    /**
     * Wealth account that represents this manual brokerage portfolio in net worth.
     * Stored as UUID only to keep finance.wealth and investment.portfolio decoupled.
     */
    @Column(name = "wealth_account_id")
    UUID wealthAccountId;

    /** Số dư tiền mặt rảnh rỗi trong danh mục, chờ mua chứng khoán */
    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    BigDecimal cashBalance = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
