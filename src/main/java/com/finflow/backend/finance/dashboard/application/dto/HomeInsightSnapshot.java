package com.finflow.backend.finance.dashboard.application.dto;

public record HomeInsightSnapshot(
        String userId,
        String locale,
        String timezone,
        String currency,
        double netWorth,
        double liquidAssets,
        double debtTotal,
        double investmentAssets,
        double totalBalance,
        double totalIncome,
        double totalExpense,
        double budgetTargetTotal,
        double budgetSpentTotal,
        int portfolioCount,
        double portfolioCashTotal,
        String primaryPortfolioName,
        double investmentTotalValue
) {
}
