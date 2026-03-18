package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lịch Sử Giao Dịch - ghi lại từng lệnh MUA/BÁN/NẠPTỀN/RÚTTỀN trong một danh mục.
 *
 * Quy tắc:
 *  - BUY/SELL/DIVIDEND: symbol, quantity và price PHẢI có giá trị
 *  - DEPOSIT/WITHDRAW: symbol, quantity, price để NULL; totalAmount = số tiền nạp/rút
 */
@Entity
@Table(name = "trade_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TradeTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    Portfolio portfolio;

    /**
     * Mã cổ phiếu — NULL khi tradeType là DEPOSIT hoặc WITHDRAW.
     */
    @Column(length = 10)
    String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false, length = 10)
    TradeType tradeType;

    /**
     * Khối lượng — NULL khi tradeType là DEPOSIT hoặc WITHDRAW.
     * Dùng scale=4 để hỗ trợ chứng khoán phân đoạn (fractional shares).
     */
    @Column(precision = 19, scale = 4)
    BigDecimal quantity;

    /**
     * Giá giao dịch — NULL khi tradeType là DEPOSIT hoặc WITHDRAW.
     */
    @Column(precision = 19, scale = 2)
    BigDecimal price;

    /**
     * Tổng giá trị giao dịch (quantity * price) hoặc số tiền nạp/rút.
     * Luôn dương; dấu được xác định bởi tradeType.
     */
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    BigDecimal totalAmount;

    /** Phí giao dịch (môi giới...) */
    @Column(name = "fee_amount", precision = 19, scale = 2)
    @Builder.Default
    BigDecimal feeAmount = BigDecimal.ZERO;

    /** Thuế khi bán (thuế TNCN 0.1% tổng giá trị bán tại Việt Nam) */
    @Column(name = "tax_amount", precision = 19, scale = 2)
    @Builder.Default
    BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "transaction_date", nullable = false)
    LocalDateTime transactionDate;
}
