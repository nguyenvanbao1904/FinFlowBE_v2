package com.finflow.backend.finance.transaction.application.dto;

import java.util.List;

/**
 * Structured personal-finance report for the internal AI agent API.
 */
public record PersonalFinanceReportOutput(
        String status,
        String message,
        Data data
) {
    public static PersonalFinanceReportOutput noData(String message) {
        return new PersonalFinanceReportOutput("NO_DATA", message, null);
    }

    public record Data(
            String reportDate,
            String currentMonth,
            int currentDayOfMonth,
            String periodCovered,
            int totalTransactions,
            long totalIncome,
            long totalExpense,
            long netCashflow,
            double overallSavingsRate,
            List<MonthlyPoint> monthlySeries,
            List<SavingsRatePoint> savingsRateSeries,
            List<TopExpenseCategory> topExpenseCategories,
            List<CategoryDelta> previousMonthCategoryDelta,
            MonthlyPoint currentMonthStats,
            MonthlyPoint previousMonthStats
    ) {}

    public record MonthlyPoint(
            String month,
            Long income,
            Long expense,
            Long net,
            Integer transactionCount,
            List<MonthTopCategory> topExpenseCategories
    ) {}

    public record MonthTopCategory(String name, long amount, double sharePct) {}

    public record TopExpenseCategory(String name, long totalAmount, double sharePct) {}

    public record SavingsRatePoint(String month, double savingsRatePct) {}

    public record CategoryDelta(String name, long previousAmount, long baselineAvg, double deltaPct) {}
}
