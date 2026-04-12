package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.application.port.in.GetTransactionAnalyticsInsightsPort;
import com.finflow.backend.finance.transaction.application.port.out.AnalyticsCachePort;
import com.finflow.backend.finance.transaction.application.port.out.DataAiAnalyticsPort;
import com.finflow.backend.finance.transaction.application.result.AnalyticsInsightItem;
import com.finflow.backend.finance.transaction.application.result.AnalyticsInsightsResult;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetTransactionAnalyticsInsightsUseCase implements GetTransactionAnalyticsInsightsPort {

    private static final String ANALYTICS_INSIGHTS_CACHE_PREFIX = "finflow:analytics-insights:";

    private final TransactionRepository transactionRepository;
    private final AnalyticsCachePort analyticsCachePort;
    private final DataAiAnalyticsPort dataAiAnalyticsPort;

    @Value("${finflow.analytics-insights.min-transactions-for-full:5}")
    private int minTransactionsForFull;

    @Value("${finflow.analytics-insights.lookback-days:90}")
    private int lookbackDays;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public AnalyticsInsightsResult execute(String userId) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDateTime endAtExclusive = monthStart.plusMonths(1).atStartOfDay();
        LocalDateTime startOfFourMonthWindow = monthStart.minusMonths(3).atStartOfDay();
        LocalDateTime startOfLookback = now.minusDays(lookbackDays).atStartOfDay();
        LocalDateTime startAt = startOfFourMonthWindow.isBefore(startOfLookback)
                ? startOfFourMonthWindow
                : startOfLookback;

        List<Transaction> recentTransactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                        userId, startAt, endAtExclusive
                );

        if (!hasAnyTransactionInLastTwoCalendarMonths(recentTransactions)) {
            log.info("Analytics insights: skip LLM — no transactions in last 2 calendar months userId={}", userId);
            return insufficientRecentDataResult();
        }

        long countLookback = recentTransactions.stream()
                .filter(t -> !t.getTransactionDate().isBefore(startOfLookback)
                        && t.getTransactionDate().isBefore(endAtExclusive))
                .count();

        String insightTier = countLookback >= (long) minTransactionsForFull ? "FULL" : "SPARSE";

        String asOfDate = now.toString();
        YearMonth ym = YearMonth.from(now);
        String currentMonthLabel = String.format("tháng %d/%d", ym.getMonthValue(), ym.getYear());
        YearMonth prevYm = ym.minusMonths(1);
        String previousMonthLabel = String.format("tháng %d/%d", prevYm.getMonthValue(), prevYm.getYear());
        String lookbackLabel = String.format("%d ngày gần nhất (tính đến %s)", lookbackDays, asOfDate);

        String cacheKey = analyticsInsightsCacheKey(userId, now, insightTier);
        Optional<AnalyticsInsightsResult> cached = analyticsCachePort.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        try {
            Map<String, Object> payload = buildPayload(
                    userId,
                    recentTransactions,
                    insightTier,
                    (int) countLookback,
                    now.getDayOfMonth(),
                    asOfDate,
                    currentMonthLabel,
                    previousMonthLabel,
                    lookbackLabel
            );
            List<AnalyticsInsightItem> items = dataAiAnalyticsPort.fetchInsights(payload);

            if (items.isEmpty()) {
                return fallbackResult(recentTransactions, insightTier, lookbackLabel,
                        currentMonthLabel, previousMonthLabel, (int) countLookback);
            }

            List<AnalyticsInsightItem> sorted = items.stream()
                    .sorted(Comparator.comparing(AnalyticsInsightItem::type))
                    .limit(2)
                    .collect(Collectors.toList());

            AnalyticsInsightsResult result = new AnalyticsInsightsResult(sorted, false);
            analyticsCachePort.put(cacheKey, result, secondsUntilEndOfDayVn(), TimeUnit.SECONDS);
            return result;
        } catch (AppException e) {
            log.warn("Analytics insights upstream error, fallback to heuristic. userId={} code={}", userId, e.getErrorCode().getCode());
            return fallbackResult(recentTransactions, insightTier, lookbackLabel,
                    currentMonthLabel, previousMonthLabel, (int) countLookback);
        }
    }

    // --- Payload builder (pure business logic) ---

    private Map<String, Object> buildPayload(
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

        List<Map<String, Object>> monthlySeries = buildMonthlySeries(recentTransactions);
        double avgIncomePrev2Months = averageForPreviousMonths(monthlySeries, "income", 2);
        double avgExpensePrev2Months = averageForPreviousMonths(monthlySeries, "expense", 2);
        List<Map<String, Object>> savingsRateSeries = buildSavingsRateSeries(monthlySeries);
        List<Map<String, Object>> previousMonthCategoryDelta = buildPreviousMonthCategoryDelta(monthlySeries);
        List<Map<String, Object>> previousMonthTopExpenseCategories = extractPreviousMonthTopExpenseCategories(monthlySeries);

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

    // --- Heuristic fallback ---

    private AnalyticsInsightsResult fallbackResult(
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

        return new AnalyticsInsightsResult(List.of(
                new AnalyticsInsightItem("fallback-warning", "WARNING", "Cảnh báo chi tiêu", warningMessage, 0.65),
                new AnalyticsInsightItem("fallback-tip", "TIP", "Mẹo tài chính", tipMessage, 0.65)
        ), false);
    }

    private AnalyticsInsightsResult insufficientRecentDataResult() {
        return new AnalyticsInsightsResult(List.of(
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

    // --- Utility / time helpers ---

    private static String analyticsInsightsCacheKey(String userId, LocalDate date, String insightTier) {
        return ANALYTICS_INSIGHTS_CACHE_PREFIX + userId + ":" + date + ":" + insightTier;
    }

    private static long secondsUntilEndOfDayVn() {
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone);
        long sec = Duration.between(now, nextMidnight).getSeconds();
        return Math.max(60L, sec);
    }

    private boolean hasAnyTransactionInLastTwoCalendarMonths(List<Transaction> transactions) {
        LocalDate start = YearMonth.now().minusMonths(1).atDay(1);
        LocalDate end = LocalDate.now();
        return transactions.stream()
                .map(t -> t.getTransactionDate().toLocalDate())
                .anyMatch(d -> !d.isBefore(start) && !d.isAfter(end));
    }

    // --- Monthly series helpers ---

    private double averageForPreviousMonths(List<Map<String, Object>> monthlySeries, String key, int monthCount) {
        if (monthlySeries == null || monthlySeries.size() < monthCount + 1) return 0.0;
        int endExclusive = monthlySeries.size() - 1;
        int startInclusive = Math.max(0, endExclusive - monthCount);
        List<Map<String, Object>> slice = monthlySeries.subList(startInclusive, endExclusive);
        if (slice.isEmpty()) return 0.0;
        double sum = slice.stream()
                .mapToDouble(m -> asDouble(m.get(key)) == null ? 0.0 : asDouble(m.get(key)))
                .sum();
        return sum / slice.size();
    }

    private List<Map<String, Object>> buildSavingsRateSeries(List<Map<String, Object>> monthlySeries) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> point : monthlySeries) {
            double income = asDouble(point.get("income")) == null ? 0.0 : asDouble(point.get("income"));
            double net = asDouble(point.get("net")) == null ? 0.0 : asDouble(point.get("net"));
            double savingsRate = income > 0 ? (net / income) * 100.0 : 0.0;
            result.add(Map.of("month", String.valueOf(point.get("month")), "savingsRatePct", savingsRate));
        }
        return result;
    }

    private List<Map<String, Object>> buildPreviousMonthCategoryDelta(List<Map<String, Object>> monthlySeries) {
        if (monthlySeries.size() < 3) return List.of();
        Map<String, Object> previousMonth = monthlySeries.get(monthlySeries.size() - 2);
        List<Map<String, Object>> baselineMonths = monthlySeries.subList(
                Math.max(0, monthlySeries.size() - 4), Math.max(0, monthlySeries.size() - 2));

        Map<String, Double> previousCat = readCategoryAmountMap(previousMonth.get("topExpenseCategories"));
        Map<String, List<Double>> baselineCat = new LinkedHashMap<>();
        for (Map<String, Object> month : baselineMonths) {
            Map<String, Double> cat = readCategoryAmountMap(month.get("topExpenseCategories"));
            for (Map.Entry<String, Double> entry : cat.entrySet()) {
                baselineCat.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
            }
        }

        return previousCat.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    double previousAmount = entry.getValue();
                    List<Double> baseline = baselineCat.getOrDefault(name, List.of());
                    double baselineAvg = baseline.isEmpty()
                            ? 0.0 : baseline.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    double deltaPct = baselineAvg > 0 ? ((previousAmount - baselineAvg) / baselineAvg) * 100.0 : 0.0;
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", name);
                    row.put("previousAmount", previousAmount);
                    row.put("baselineAvgAmount", baselineAvg);
                    row.put("deltaPct", deltaPct);
                    return row;
                })
                .sorted((a, b) -> {
                    double ad = asDouble(a.get("deltaPct")) == null ? 0.0 : asDouble(a.get("deltaPct"));
                    double bd = asDouble(b.get("deltaPct")) == null ? 0.0 : asDouble(b.get("deltaPct"));
                    return Double.compare(bd, ad);
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    private Map<String, Double> readCategoryAmountMap(Object rawTopCategories) {
        if (!(rawTopCategories instanceof List<?> rawList)) return Map.of();
        Map<String, Double> out = new LinkedHashMap<>();
        for (Object raw : rawList) {
            if (!(raw instanceof Map<?, ?> item)) continue;
            String name = asString(item.get("name"));
            Double amount = asDouble(item.get("amount"));
            if (name == null || amount == null) continue;
            out.put(name, amount);
        }
        return out;
    }

    private List<Map<String, Object>> extractPreviousMonthTopExpenseCategories(List<Map<String, Object>> monthlySeries) {
        if (monthlySeries.size() < 2) return List.of();
        Object raw = monthlySeries.get(monthlySeries.size() - 2).get("topExpenseCategories");
        if (!(raw instanceof List<?> list)) return List.of();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                String name = asString(map.get("name"));
                Double amount = asDouble(map.get("amount"));
                Double sharePct = asDouble(map.get("sharePct"));
                if (name == null || amount == null) continue;
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name", name);
                row.put("amount", amount);
                if (sharePct != null) row.put("sharePct", sharePct);
                out.add(row);
            }
        }
        return out;
    }

    private List<Map<String, Object>> buildMonthlySeries(List<Transaction> transactions) {
        YearMonth current = YearMonth.now();
        List<YearMonth> months = List.of(
                current.minusMonths(3), current.minusMonths(2), current.minusMonths(1), current);

        List<Map<String, Object>> result = new ArrayList<>();
        for (YearMonth month : months) {
            List<Transaction> monthTx = transactions.stream()
                    .filter(t -> YearMonth.from(t.getTransactionDate()).equals(month))
                    .collect(Collectors.toList());

            double income = monthTx.stream().filter(t -> t.getType() == CategoryType.INCOME)
                    .mapToDouble(t -> t.getAmount().doubleValue()).sum();
            double expense = monthTx.stream().filter(t -> t.getType() == CategoryType.EXPENSE)
                    .mapToDouble(t -> t.getAmount().doubleValue()).sum();
            double net = income - expense;

            Map<String, Double> catMap = new LinkedHashMap<>();
            for (Transaction t : monthTx) {
                if (t.getType() != CategoryType.EXPENSE || t.getCategory() == null) continue;
                String name = t.getCategory().getName();
                catMap.put(name, catMap.getOrDefault(name, 0.0) + t.getAmount().doubleValue());
            }
            List<Map<String, Object>> topCat = catMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(3)
                    .map(e -> {
                        double amount = e.getValue();
                        double sharePct = expense > 0 ? (amount / expense) * 100.0 : 0.0;
                        return Map.<String, Object>of("name", e.getKey(), "amount", amount, "sharePct", sharePct);
                    })
                    .collect(Collectors.toList());

            result.add(Map.of("month", month.toString(), "income", income,
                    "expense", expense, "net", net, "topExpenseCategories", topCat));
        }
        return result;
    }

    private String asString(Object value) {
        if (value == null) return null;
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? null : s;
    }

    private Double asDouble(Object value) {
        if (value == null) return null;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
