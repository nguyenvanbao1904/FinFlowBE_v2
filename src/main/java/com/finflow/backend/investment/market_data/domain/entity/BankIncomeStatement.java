package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "bank_income_statements")
@DiscriminatorValue("BANK")
@Getter
@Setter
public class BankIncomeStatement extends IncomeStatement {

    // --- THU NHẬP LÃI ---
    @Column(name = "interest_expense", precision = 19, scale = 2)
    private BigDecimal interestExpense; // Chi phí lãi và các khoản tương tự

    @Column(name = "net_interest_income", precision = 19, scale = 2)
    private BigDecimal netInterestIncome; // Thu nhập lãi thuần

    @Column(name = "net_fee_commission_income", precision = 19, scale = 2)
    private BigDecimal netFeeAndCommissionIncome; // Lãi thuần từ hoạt động dịch vụ

    // --- THU NHẬP KHÁC ---
    @Column(name = "net_other_income_expenses", precision = 19, scale = 2)
    private BigDecimal netOtherIncomeOrExpenses; // Lãi/lỗ thuần từ hoạt động khác

    // --- LỢI NHUẬN ---
    @Column(name = "net_profit", precision = 19, scale = 2)
    private BigDecimal netProfit; // Lợi nhuận thuần

    @Column(name = "total_operating_income", precision = 20, scale = 2)
    private BigDecimal totalOperatingIncome;

    @Column(name = "total_operating_expense", precision = 20, scale = 2)
    private BigDecimal totalOperatingExpense;

    @Column(name = "credit_risk_provisions_expense", precision = 20, scale = 2)
    private BigDecimal creditRiskProvisionsExpense;

    @Column(name = "interest_and_similar_income", precision = 20, scale = 2)
    private BigDecimal interestAndSimilarIncome;
}
