package com.finflow.backend.finance.transaction.presentation.response;

import java.util.List;

/**
 * Presentation DTO for the internal AI-agent finance-report endpoint.
 * Mirrors {@code PersonalFinanceReportOutput} structure — kept as simple
 * records because the only consumer is the Python AI service.
 */
public record InternalFinanceReportResponse(
        String status,
        String message,
        Data data
) {
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
