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
    @Column(precision = 10, scale = 2)
    private BigDecimal pe; // P/E
    
    @Column(precision = 10, scale = 2)
    private BigDecimal pb; // P/B
    
    @Column(precision = 10, scale = 2)
    private BigDecimal ps; // P/S
    
    @Column(precision = 10, scale = 2)
    private BigDecimal roe; // ROE (%)
    
    @Column(precision = 10, scale = 2)
    private BigDecimal roa; // ROA (%)
    
    @Column(precision = 19, scale = 2)
    private BigDecimal eps; // EPS (VND)
    
    @Column(precision = 19, scale = 2)
    private BigDecimal bvps; // BVPS (VND)
    
    @Column(name = "gross_margin", precision = 10, scale = 2)
    private BigDecimal lng; // Biên LN gộp (%) - Gross Margin
    
    @Column(name = "net_margin", precision = 10, scale = 2)
    private BigDecimal lnr; // Biên LN ròng (%) - Net Margin
    
    @Column(name = "shares_outstanding", precision = 19, scale = 2)
    private BigDecimal cplh; // Số CP lưu hành (Triệu CP)
}
