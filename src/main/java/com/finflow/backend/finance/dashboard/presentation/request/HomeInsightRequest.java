package com.finflow.backend.finance.dashboard.presentation.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeInsightRequest {
    private String locale;
    private String timezone;
    private String currency;
    private double netWorth;
    private double liquidAssets;
    private double debtTotal;
    private double investmentAssets;
    private double totalBalance;
    private double totalIncome;
    private double totalExpense;
    private double budgetTargetTotal;
    private double budgetSpentTotal;
    private int portfolioCount;
    private double portfolioCashTotal;
    private String primaryPortfolioName;
    private double investmentTotalValue;
}
