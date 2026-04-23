package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BankIncomeStatementRequestInput(
        String companyId,
        Integer year,
        Integer quarter,
        BigDecimal netInterestIncome,
        BigDecimal feeAndCommissionIncome,
        BigDecimal otherIncome,
        BigDecimal profitAfterTax,
        BigDecimal interestExpense
) {}
