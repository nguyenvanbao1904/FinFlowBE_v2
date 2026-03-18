package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "balance_sheets")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "company_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class BalanceSheet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int quarter;

    // --- CÁC CHỈ TIÊU CHUNG ---
    @Column(name = "cash_and_equivalents", precision = 19, scale = 2)
    private BigDecimal cashAndCashEquivalents; // Tiền và tương đương tiền
    
    @Column(name = "total_assets", precision = 19, scale = 2)
    private BigDecimal totalAssets; // TỔNG CỘNG TÀI SẢN

    @Column(name = "equity", precision = 19, scale = 2)
    private BigDecimal equity; // VỐN CHỦ SỞ HỮU

    @Column(name = "total_capital", precision = 19, scale = 2)
    private BigDecimal totalCapital; // TỔNG CỘNG NGUỒN VỐN
}
