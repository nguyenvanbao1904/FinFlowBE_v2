package com.finflow.backend.finance.transaction.application.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransactionSummaryOutput(
        BigDecimal totalBalance,
        BigDecimal totalIncome,
        BigDecimal totalExpense
) {}
