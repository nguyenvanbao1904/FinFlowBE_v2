package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BankIncomeStatementRequestInput(
        String companyId,
        Integer year,
        Integer quarter,
        BigDecimal netInterestIncome,
        BigDecimal netFeeAndCommissionIncome,
        BigDecimal netOtherIncomeOrExpenses,
        BigDecimal profitAfterTax,
        BigDecimal interestExpense,
        BigDecimal netProfit,
        BigDecimal totalOperatingIncome,
        BigDecimal totalOperatingExpense,
        BigDecimal creditRiskProvisionsExpense,
        BigDecimal interestAndSimilarIncome
) {}
