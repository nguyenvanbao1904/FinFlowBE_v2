package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record BankIncomeStatementRequestDTO(
    @NotBlank String companyId,
    @NotNull Integer year,
    @NotNull Integer quarter,
    BigDecimal profitAfterTax,
    BigDecimal netInterestIncome,
    BigDecimal netFeeAndCommissionIncome,
    BigDecimal netOtherIncomeOrExpenses,
    BigDecimal interestAndSimilarExpenses
) {}
