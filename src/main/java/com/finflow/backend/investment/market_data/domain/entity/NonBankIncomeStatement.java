package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "non_bank_income_statements")
@DiscriminatorValue("NON_BANK")
@Getter
@Setter
public class NonBankIncomeStatement extends IncomeStatement {

    @Column(name = "net_revenue", precision = 19, scale = 2)
    private BigDecimal netRevenue; // Doanh thu thuần

    // --- DOANH THU CHI TIẾT ---
    @Column(name = "total_revenue", precision = 19, scale = 2)
    private BigDecimal totalRevenue; // Doanh thu (đồng)

    // --- LỢI NHUẬN ---
    @Column(name = "net_profit", precision = 19, scale = 2)
    private BigDecimal netProfit; // Lợi nhuận thuần
}
