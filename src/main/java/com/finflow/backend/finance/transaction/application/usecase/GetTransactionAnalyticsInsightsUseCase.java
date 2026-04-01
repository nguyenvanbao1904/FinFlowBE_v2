package com.finflow.backend.finance.transaction.application.usecase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.backend.common.redis.RedisService;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.transaction.presentation.response.TransactionAnalyticsInsightResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionAnalyticsInsightsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetTransactionAnalyticsInsightsUseCase {

    private static final String ANALYTICS_INSIGHTS_CACHE_PREFIX = "finflow:analytics-insights:";

    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    @Value("${data.ai.base-url:http://localhost:8001}")
    private String dataAiBaseUrl;

    @Value("${data.ai.internal-api-key:}")
    private String dataAiInternalApiKey;

    @Value("${finflow.analytics-insights.min-transactions-for-full:5}")
    private int minTransactionsForFull;

    @Value("${finflow.analytics-insights.lookback-days:90}")
    private int lookbackDays;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public TransactionAnalyticsInsightsResponse execute(String userId) {
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
            return insufficientRecentDataResponse();
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
        TransactionAnalyticsInsightsResponse cached = redisService.getSilently(cacheKey, TransactionAnalyticsInsightsResponse.class);
        if (cached != null && cached.getInsights() != null && !cached.getInsights().isEmpty()) {
            return TransactionAnalyticsInsightsResponse.builder()
                    .insights(cached.getInsights())
                    .cached(true)
                    .build();
        }

        try {
            Map<String, Object> aiResponse = callDataAi(
                    userId,
                    recentTransactions,
                    insightTier,
                    (int) countLookback,
                    asOfDate,
                    currentMonthLabel,
                    previousMonthLabel,
                    lookbackLabel
            );
            TransactionAnalyticsInsightsResponse result = mapResponse(aiResponse);
            redisService.setSilently(cacheKey, result, secondsUntilEndOfDayVn(), TimeUnit.SECONDS);
            return result;
        } catch (Exception e) {
            log.warn("Analytics insights AI failed, fallback to heuristic. userId={} reason={}", userId, e.getMessage());
            return fallbackResponse(
                    recentTransactions,
                    insightTier,
                    lookbackLabel,
                    currentMonthLabel,
                    previousMonthLabel,
                    (int) countLookback
            );
        }
    }

    private Map<String, Object> callDataAi(
            String userId,
            List<Transaction> recentTransactions,
            String insightTier,
            int recentTransactionCount,
            String asOfDate,
            String currentMonthLabel,
            String previousMonthLabel,
            String lookbackLabel
    ) throws Exception {
        double totalIncome = recentTransactions.stream()
                .filter(t -> t.getType() == CategoryType.INCOME)
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
        double totalExpense = recentTransactions.stream()
                .filter(t -> t.getType() == CategoryType.EXPENSE)
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
        double netCashflow = totalIncome - totalExpense;

        Map<String, Double> byCategory = new LinkedHashMap<>();
        for (Transaction t : recentTransactions) {
            if (t.getType() != CategoryType.EXPENSE || t.getCategory() == null) continue;
            String name = t.getCategory().getName();
            byCategory.put(name, byCategory.getOrDefault(name, 0.0) + t.getAmount().doubleValue());
        }
        List<Map<String, Object>> monthlySeries = buildMonthlySeries(recentTransactions);

        List<Map<String, Object>> topCategories = byCategory.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    double amount = e.getValue();
                    double sharePct = totalExpense > 0 ? (amount / totalExpense) * 100.0 : 0.0;
                    return Map.<String, Object>of(
                            "name", e.getKey(),
                            "amount", amount,
                            "sharePct", sharePct
                    );
                })
                .collect(Collectors.toList());

        String cacheKey = userId + ":" + LocalDate.now();
        Map<String, Object> body = new HashMap<>();
        body.put("cacheKey", cacheKey);
        body.put("locale", "vi-VN");
        body.put("timezone", "Asia/Ho_Chi_Minh");
        body.put("currency", "VND");
        body.put("periodLabel", "THANG_NAY_VS_3_THANG_TRUOC");
        body.put("insightTier", insightTier);
        body.put("recentTransactionCount", recentTransactionCount);
        body.put("asOfDate", asOfDate);
        body.put("currentMonthLabel", currentMonthLabel);
        body.put("previousMonthLabel", previousMonthLabel);
        body.put("lookbackLabel", lookbackLabel);
        body.put("totalIncome", totalIncome);
        body.put("totalExpense", totalExpense);
        body.put("netCashflow", netCashflow);
        body.put("topExpenseCategories", topCategories);
        body.put("monthlySeries", monthlySeries);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(dataAiBaseUrl + "/api/v1/ai/analytics-insights"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));

        if (dataAiInternalApiKey != null && !dataAiInternalApiKey.isBlank()) {
            requestBuilder.header("X-Internal-Api-Key", dataAiInternalApiKey);
        }

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("data_ai_service returned " + response.statusCode());
        }

        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    private TransactionAnalyticsInsightsResponse mapResponse(Map<String, Object> aiResponse) {
        Object rawInsights = aiResponse.get("insights");
        List<TransactionAnalyticsInsightResponse> insights = new ArrayList<>();
        if (rawInsights instanceof List<?> list) {
            for (Object raw : list) {
                if (!(raw instanceof Map<?, ?> item)) continue;
                String type = asString(item.get("type"));
                String title = asString(item.get("title"));
                String message = asString(item.get("message"));
                if (type == null || title == null || message == null) continue;
                Double confidence = asDouble(item.get("confidence"));
                insights.add(TransactionAnalyticsInsightResponse.builder()
                        .id(asString(item.get("id")) == null ? title : asString(item.get("id")))
                        .type(type)
                        .title(title)
                        .message(message)
                        .confidence(confidence == null ? 0.0 : confidence)
                        .build());
            }
        }

        if (insights.isEmpty()) {
            return fallbackResponse(List.of(), "FULL", "", "", "", 0);
        }

        insights = insights.stream()
                .sorted(Comparator.comparing(TransactionAnalyticsInsightResponse::getType))
                .limit(2)
                .collect(Collectors.toList());

        boolean cached = Boolean.TRUE.equals(aiResponse.get("cached"));
        return TransactionAnalyticsInsightsResponse.builder()
                .insights(insights)
                .cached(cached)
                .build();
    }

    private TransactionAnalyticsInsightsResponse fallbackResponse(
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
                    scope,
                    Math.max(recentTransactionCount, 1)
            );
        } else if (income <= 0 && expense > 0) {
            warningMessage = String.format(
                    "Trong %s, bạn có chi tiêu nhưng chưa ghi nhận thu nhập — nên kiểm tra lại dòng tiền.",
                    scope
            );
        } else {
            warningMessage = String.format(
                    "Trong %s, tỷ lệ chi tiêu/thu nhập đang khoảng %.0f%% — theo dõi các khoản chi lớn.",
                    scope,
                    ratio * 100.0
            );
        }

        String tipMessage;
        if ("SPARSE".equals(insightTier)) {
            tipMessage = String.format(
                    "Gợi ý cho %s: ghi đều giao dịch và phân loại để %s so sánh rõ hơn với %s.",
                    scope,
                    currentMonthLabel != null && !currentMonthLabel.isBlank() ? currentMonthLabel : "tháng hiện tại",
                    previousMonthLabel != null && !previousMonthLabel.isBlank() ? previousMonthLabel : "tháng trước"
            );
        } else if (income - expense > 0) {
            tipMessage = String.format(
                    "Trong %s, dòng tiền đang dương — cân nhắc trích một phần vào quỹ dự phòng.",
                    scope
            );
        } else {
            tipMessage = String.format(
                    "Trong %s, hãy đặt giới hạn cho danh mục chi lớn để cân bằng ngân sách.",
                    scope
            );
        }

        if (warningMessage.length() > 220) {
            warningMessage = warningMessage.substring(0, 217) + "...";
        }
        if (tipMessage.length() > 220) {
            tipMessage = tipMessage.substring(0, 217) + "...";
        }

        return TransactionAnalyticsInsightsResponse.builder()
                .cached(false)
                .insights(List.of(
                        TransactionAnalyticsInsightResponse.builder()
                                .id("fallback-warning")
                                .type("WARNING")
                                .title("Cảnh báo chi tiêu")
                                .message(warningMessage)
                                .confidence(0.65)
                                .build(),
                        TransactionAnalyticsInsightResponse.builder()
                                .id("fallback-tip")
                                .type("TIP")
                                .title("Mẹo tài chính")
                                .message(tipMessage)
                                .confidence(0.65)
                                .build()
                ))
                .build();
    }

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

    private TransactionAnalyticsInsightsResponse insufficientRecentDataResponse() {
        return TransactionAnalyticsInsightsResponse.builder()
                .cached(false)
                .insights(List.of(
                        TransactionAnalyticsInsightResponse.builder()
                                .id("insufficient-recent-data")
                                .type("TIP")
                                .title("AI đang học cách bạn sử dụng")
                                .message(
                                        "Chưa có giao dịch trong 2 tháng gần nhất. "
                                                + "Ghi thêm giao dịch để AI có dữ liệu phân tích thói quen chi tiêu của bạn."
                                )
                                .confidence(1.0)
                                .build()
                ))
                .build();
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

    private List<Map<String, Object>> buildMonthlySeries(List<Transaction> transactions) {
        YearMonth current = YearMonth.now();
        List<YearMonth> months = List.of(
                current.minusMonths(3),
                current.minusMonths(2),
                current.minusMonths(1),
                current
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (YearMonth month : months) {
            List<Transaction> monthTx = transactions.stream()
                    .filter(t -> YearMonth.from(t.getTransactionDate()).equals(month))
                    .collect(Collectors.toList());

            double income = monthTx.stream()
                    .filter(t -> t.getType() == CategoryType.INCOME)
                    .mapToDouble(t -> t.getAmount().doubleValue())
                    .sum();
            double expense = monthTx.stream()
                    .filter(t -> t.getType() == CategoryType.EXPENSE)
                    .mapToDouble(t -> t.getAmount().doubleValue())
                    .sum();
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
                        return Map.<String, Object>of(
                                "name", e.getKey(),
                                "amount", amount,
                                "sharePct", sharePct
                        );
                    })
                    .collect(Collectors.toList());

            result.add(Map.of(
                    "month", month.toString(),
                    "income", income,
                    "expense", expense,
                    "net", net,
                    "topExpenseCategories", topCat
            ));
        }
        return result;
    }
}
