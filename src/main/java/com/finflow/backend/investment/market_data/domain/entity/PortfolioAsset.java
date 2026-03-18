package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tài Sản Đang Nắm Giữ - snapshot số dư cổ phiếu hiện tại trong danh mục.
 *
 * Được cập nhật mỗi khi có TradeTransaction BUY/SELL:
 *  - BUY: totalQuantity += quantity; averagePrice = weighted average
 *  - SELL: totalQuantity -= quantity (không thay đổi averagePrice)
 *  - Khi totalQuantity = 0, record này nên được xóa hoặc ẩn đi
 *
 * Quan hệ với FinancialIndicator: symbol ↔ companyId để JOIN lấy PE, PB, ROE...
 */
@Entity
@Table(
    name = "portfolio_assets",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_portfolio_symbol",
        columnNames = {"portfolio_id", "symbol"}
    )
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PortfolioAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    Portfolio portfolio;

    /** Mã chứng khoán — dùng để JOIN với FinancialIndicator.companyId */
    @Column(nullable = false, length = 10)
    String symbol;

    /** Tổng khối lượng đang nắm giữ (scale=4 cho fractional shares) */
    @Column(name = "total_quantity", nullable = false, precision = 19, scale = 4)
    BigDecimal totalQuantity;

    /**
     * Giá vốn trung bình (Weighted Average Cost).
     * Công thức: (oldQty * oldAvg + buyQty * buyPrice) / (oldQty + buyQty)
     */
    @Column(name = "average_price", nullable = false, precision = 19, scale = 2)
    BigDecimal averagePrice;

    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
