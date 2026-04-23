package com.finflow.backend.finance.transaction.application.service;

import com.finflow.backend.finance.transaction.application.dto.AnalyticsInsightItem;
import com.finflow.backend.finance.transaction.application.dto.AnalyticsInsightsOutput;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.common.enums.CategoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AnalyticsInsightsHelper {

    private static final String ANALYTICS_INSIGHTS_CACHE_PREFIX = "finflow:analytics-insights:";

    private final TransactionInsightEngine insightEngine;

    public Map<String, Object> buildPayload(
            String userId,
            List<Transaction> recentTransactions,
            String insightTier,
            int recentTransactionCount,
            int currentDayOfMonth,
            String asOfDate,
            String currentMonthLabel,
            String previousMonthLabel,
            String lookbackLabel
    ) {
        double totalIncome = recentTransactions.stream()
                .filter(t -> t.getType() == CategoryType.INCOME)
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
        double totalExpense = recentTransactions.stream()
                .filter(t -> t.getType() == CategoryType.EXPENSE)
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
        double netCashflow = totalIncome - totalExpense;

        List<Map<String, Object>> monthlySeries = insightEngine.buildMonthlySeries(recentTransactions);
        double avgIncomePrev2Months = insightEngine.averageForPreviousMonths(monthlySeries, "income", 2);
        double avgExpensePrev2Months = insightEngine.averageForPreviousMonths(monthlySeries, "expense", 2);
        List<Map<String, Object>> savingsRateSeries = insightEngine.buildSavingsRateSeries(monthlySeries);
        List<Map<String, Object>> previousMonthCategoryDelta = insightEngine.buildPreviousMonthCategoryDelta(monthlySeries);
        List<Map<String, Object>> previousMonthTopExpenseCategories = insightEngine.extractPreviousMonthTopExpenseCategories(monthlySeries);

        String cacheKey = userId + ":" + LocalDate.now();
        Map<String, Object> body = new HashMap<>();
        body.put("cacheKey", cacheKey);
        body.put("locale", "vi-VN");
        body.put("timezone", "Asia/Ho_Chi_Minh");
        body.put("currency", "VND");
        body.put("periodLabel", "THANG_NAY_VS_3_THANG_TRUOC");
        body.put("insightTier", insightTier);
        body.put("recentTransactionCount", recentTransactionCount);
        body.put("currentDayOfMonth", currentDayOfMonth);
        body.put("isBeginningOfMonth", currentDayOfMonth < 5);
        body.put("asOfDate", asOfDate);
        body.put("currentMonthLabel", currentMonthLabel);
        body.put("previousMonthLabel", previousMonthLabel);
        body.put("lookbackLabel", lookbackLabel);
        body.put("totalIncomeLookback", totalIncome);
        body.put("totalExpenseLookback", totalExpense);
        body.put("netCashflowLookback", netCashflow);
        body.put("avgIncomePrev2Months", avgIncomePrev2Months);
        body.put("avgExpensePrev2Months", avgExpensePrev2Months);
        body.put("savingsRateSeries", savingsRateSeries);
        body.put("previousMonthCategoryDelta", previousMonthCategoryDelta);
        body.put("previousMonthTopExpenseCategories", previousMonthTopExpenseCategories);
        body.put("monthlySeries", monthlySeries);
        return body;
    }

    public AnalyticsInsightsOutput fallbackResult(
            List<Transaction> recentTransactions,
            String insightTier,
            String lookbackLabel,
            String currentMonthLabel,
            String previousMonthLabel,
            int recentTransactionCount
    ) {
        double income = recentTransactions.stream()
                .filter(t -> t.getType() == CategoryType.INCOME)
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
        double expense = recentTransactions.stream()
                .filter(t -> t.getType() == CategoryType.EXPENSE)
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
        double ratio = income > 0 ? (expense / income) : 0.0;

        String scope = lookbackLabel != null && !lookbackLabel.isBlank()
                ? lookbackLabel
                : ("dữ liệu đến " + (currentMonthLabel != null && !currentMonthLabel.isBlank() ? currentMonthLabel : "hiện tại"));

        String warningMessage;
        if ("SPARSE".equals(insightTier)) {
            warningMessage = String.format(
                    "Trong %s, bạn mới có %d giao dịch — chưa đủ để kết luận xu hướng. Hãy ghi thêm để insight chính xác hơn.",
                    scope, Math.max(recentTransactionCount, 1));
        } else if (income <= 0 && expense > 0) {
            warningMessage = String.format(
                    "Trong %s, bạn có chi tiêu nhưng chưa ghi nhận thu nhập — nên kiểm tra lại dòng tiền.", scope);
        } else {
            warningMessage = String.format(
                    "Trong %s, tỷ lệ chi tiêu/thu nhập đang khoảng %.0f%% — theo dõi các khoản chi lớn.",
                    scope, ratio * 100.0);
        }

        String tipMessage;
        if ("SPARSE".equals(insightTier)) {
            tipMessage = String.format(
                    "Gợi ý cho %s: ghi đều giao dịch và phân loại để %s so sánh rõ hơn với %s.",
                    scope,
                    currentMonthLabel != null && !currentMonthLabel.isBlank() ? currentMonthLabel : "tháng hiện tại",
                    previousMonthLabel != null && !previousMonthLabel.isBlank() ? previousMonthLabel : "tháng trước");
        } else if (income - expense > 0) {
            tipMessage = String.format(
                    "Trong %s, dòng tiền đang dương — cân nhắc trích một phần vào quỹ dự phòng.", scope);
        } else {
            tipMessage = String.format(
                    "Trong %s, hãy đặt giới hạn cho danh mục chi lớn để cân bằng ngân sách.", scope);
        }

        if (warningMessage.length() > 220) warningMessage = warningMessage.substring(0, 217) + "...";
        if (tipMessage.length() > 220) tipMessage = tipMessage.substring(0, 217) + "...";

        return new AnalyticsInsightsOutput(List.of(
                new AnalyticsInsightItem("fallback-warning", "WARNING", "Cảnh báo chi tiêu", warningMessage, 0.65),
                new AnalyticsInsightItem("fallback-tip", "TIP", "Mẹo tài chính", tipMessage, 0.65)
        ), false);
    }

    public AnalyticsInsightsOutput insufficientRecentDataResult() {
        return new AnalyticsInsightsOutput(List.of(
                new AnalyticsInsightItem(
                        "insufficient-recent-data",
                        "TIP",
                        "AI đang học cách bạn sử dụng",
                        "Chưa có giao dịch trong 2 tháng gần nhất. "
                                + "Ghi thêm giao dịch để AI có dữ liệu phân tích thói quen chi tiêu của bạn.",
                        1.0
                )
        ), false);
    }

    public String analyticsInsightsCacheKey(String userId, LocalDate date, String insightTier) {
        return ANALYTICS_INSIGHTS_CACHE_PREFIX + userId + ":" + date + ":" + insightTier;
    }

    public long secondsUntilEndOfDayVn() {
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone);
        long sec = Duration.between(now, nextMidnight).getSeconds();
        return Math.max(60L, sec);
    }

    public boolean hasAnyTransactionInLastTwoCalendarMonths(List<Transaction> transactions) {
        LocalDate start = YearMonth.now().minusMonths(1).atDay(1);
        LocalDate end = LocalDate.now();
        return transactions.stream()
                .map(t -> t.getTransactionDate().toLocalDate())
                .anyMatch(d -> !d.isBefore(start) && !d.isAfter(end));
    }
}
