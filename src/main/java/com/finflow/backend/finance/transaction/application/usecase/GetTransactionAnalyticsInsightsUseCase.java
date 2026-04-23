package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.application.port.in.GetTransactionAnalyticsInsightsPort;
import com.finflow.backend.finance.transaction.application.query.GetTransactionAnalyticsInsightsQuery;
import com.finflow.backend.finance.transaction.application.port.out.AnalyticsCachePort;
import com.finflow.backend.finance.transaction.application.port.out.DataAiAnalyticsPort;
import com.finflow.backend.finance.transaction.application.dto.AnalyticsInsightItem;
import com.finflow.backend.finance.transaction.application.dto.AnalyticsInsightsOutput;
import com.finflow.backend.finance.transaction.application.service.AnalyticsInsightsHelper;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetTransactionAnalyticsInsightsUseCase implements GetTransactionAnalyticsInsightsPort {

    private final TransactionRepository transactionRepository;
    private final AnalyticsCachePort analyticsCachePort;
    private final DataAiAnalyticsPort dataAiAnalyticsPort;
    private final AnalyticsInsightsHelper helper;

    @Value("${finflow.analytics-insights.min-transactions-for-full:5}")
    private int minTransactionsForFull;

    @Value("${finflow.analytics-insights.lookback-days:90}")
    private int lookbackDays;

    @Transactional(readOnly = true)
    @Override
    public AnalyticsInsightsOutput execute(GetTransactionAnalyticsInsightsQuery request) {
        String userId = request.userId();
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

        if (!helper.hasAnyTransactionInLastTwoCalendarMonths(recentTransactions)) {
            log.info("Analytics insights: skip LLM — no transactions in last 2 calendar months userId={}", userId);
            return helper.insufficientRecentDataResult();
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

        String cacheKey = helper.analyticsInsightsCacheKey(userId, now, insightTier);
        Optional<AnalyticsInsightsOutput> cached = analyticsCachePort.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        try {
            Map<String, Object> payload = helper.buildPayload(
                    userId, recentTransactions, insightTier, (int) countLookback,
                    now.getDayOfMonth(), asOfDate, currentMonthLabel, previousMonthLabel, lookbackLabel
            );
            List<AnalyticsInsightItem> items = dataAiAnalyticsPort.fetchInsights(payload);

            if (items.isEmpty()) {
                return helper.fallbackResult(recentTransactions, insightTier, lookbackLabel,
                        currentMonthLabel, previousMonthLabel, (int) countLookback);
            }

            List<AnalyticsInsightItem> sorted = items.stream()
                    .sorted(Comparator.comparing(AnalyticsInsightItem::type))
                    .limit(2)
                    .collect(Collectors.toList());

            AnalyticsInsightsOutput result = new AnalyticsInsightsOutput(sorted, false);
            analyticsCachePort.put(cacheKey, result, helper.secondsUntilEndOfDayVn(), TimeUnit.SECONDS);
            return result;
        } catch (AppException e) {
            log.warn("Analytics insights upstream error, fallback to heuristic. userId={} code={}", userId, e.getErrorCode().getCode());
            return helper.fallbackResult(recentTransactions, insightTier, lookbackLabel,
                    currentMonthLabel, previousMonthLabel, (int) countLookback);
        }
    }
}
