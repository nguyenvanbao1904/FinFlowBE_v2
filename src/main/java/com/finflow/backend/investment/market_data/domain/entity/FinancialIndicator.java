package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "financial_indicators")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "company_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class FinancialIndicator {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int quarter;

    // Các chỉ số map chính xác từ Python code
    @Column(precision = 20, scale = 6)
    private BigDecimal pe; // P/E

    @Column(precision = 20, scale = 6)
    private BigDecimal pb; // P/B

    @Column(precision = 20, scale = 6)
    private BigDecimal ps; // P/S

    @Column(precision = 20, scale = 6)
    private BigDecimal roe; // ROE (%)

    @Column(precision = 20, scale = 6)
    private BigDecimal roa; // ROA (%)
    
    @Column(precision = 19, scale = 2)
    private BigDecimal eps; // EPS (VND)
    
    @Column(precision = 19, scale = 2)
    private BigDecimal bvps; // BVPS (VND)
    
    @Column(name = "gross_margin", precision = 20, scale = 6)
    private BigDecimal lng; // Biên LN gộp (%) - Gross Margin

    @Column(name = "net_margin", precision = 20, scale = 6)
    private BigDecimal lnr; // Biên LN ròng (%) - Net Margin
    
    @Column(name = "shares_outstanding", precision = 19, scale = 2)
    private BigDecimal cplh; // Số CP lưu hành (Triệu CP)

    @Column(name = "sale_growth", precision = 20, scale = 6)
    private BigDecimal saleGrowth;

    @Column(name = "profit_growth", precision = 20, scale = 6)
    private BigDecimal profitGrowth;

    @Column(name = "current_ratio", precision = 20, scale = 6)
    private BigDecimal currentRatio;

    @Column(name = "total_debt_over_equity", precision = 20, scale = 6)
    private BigDecimal totalDebtOverEquity;

    @Column(name = "ev_over_ebitda", precision = 20, scale = 6)
    private BigDecimal evOverEbitda;

    @Column(name = "inventory_turnover", precision = 20, scale = 6)
    private BigDecimal inventoryTurnover;

    @Column(name = "payout_ratio", precision = 20, scale = 6)
    private BigDecimal payoutRatio;

    @Column(name = "cash_dividend", precision = 20, scale = 6)
    private BigDecimal cashDividend;

    @Column(name = "share_at_period_end", precision = 20, scale = 2)
    private BigDecimal shareAtPeriodEnd;

    @Column(name = "nim", precision = 20, scale = 6)
    private BigDecimal nim;

    @Column(name = "yoea", precision = 20, scale = 6)
    private BigDecimal yoea;

    @Column(name = "cof", precision = 20, scale = 6)
    private BigDecimal cof;

    @Column(name = "cir", precision = 20, scale = 6)
    private BigDecimal cir;

    @Column(name = "ldr", precision = 20, scale = 6)
    private BigDecimal ldr;

    @Column(name = "npl_to_loan", precision = 20, scale = 6)
    private BigDecimal nplToLoan;

    @Column(name = "loanloss_reserves_to_npl", precision = 20, scale = 6)
    private BigDecimal loanlossReservesToNPL;
}
