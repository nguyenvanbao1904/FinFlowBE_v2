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

    @Column(name = "net_interest_income", precision = 19, scale = 2)
    private BigDecimal netInterestIncome; // Thu nhập lãi thuần

    @Column(name = "net_fee_commission_income", precision = 19, scale = 2)
    private BigDecimal netFeeAndCommissionIncome; // Lãi thuần từ hoạt động dịch vụ

    @Column(name = "net_other_income_expenses", precision = 19, scale = 2)
    private BigDecimal netOtherIncomeOrExpenses; // Lãi/lỗ thuần từ hoạt động khác

    @Column(name = "interest_similar_expenses", precision = 19, scale = 2)
    private BigDecimal interestAndSimilarExpenses; // Chi phí lãi và các khoản tương tự
}
