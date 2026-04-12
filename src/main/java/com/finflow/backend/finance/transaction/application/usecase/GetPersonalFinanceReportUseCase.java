package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.port.in.GetPersonalFinanceReportPort;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds a comprehensive, pre-computed personal finance report
 * for the AI chat agent. All numbers are calculated here so the LLM
 * only needs to interpret and narrate — no math required from the model.
 *
 * Called via internal API (X-Internal-Api-Key), NOT via user JWT.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GetPersonalFinanceReportUseCase implements GetPersonalFinanceReportPort {

        private final TransactionRepository transactionRepository;

        @Transactional(readOnly = true)
        @Override
        public Map<String, Object> execute(String userId) {
                LocalDate now = LocalDate.now();
                YearMonth currentMonth = YearMonth.from(now);
                // Fetch 4 months of data (current + 3 previous)
                LocalDateTime startAt = currentMonth.minusMonths(3).atDay(1).atStartOfDay();
                LocalDateTime endAt = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

                List<Transaction> transactions = transactionRepository
                                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                                                userId, startAt, endAt);

                if (transactions.isEmpty()) {
                        return Map.of(
                                        "status", "NO_DATA",
                                        "message", "Chưa có giao dịch nào trong 4 tháng gần nhất.");
                }

                // ── Build monthly series ──
                List<Map<String, Object>> monthlySeries = buildMonthlySeries(transactions, currentMonth);

                // ── Current month stats ──
                Map<String, Object> currentMonthStats = monthlySeries.isEmpty()
                                ? Map.of()
                                : monthlySeries.get(monthlySeries.size() - 1);

                // ── Previous month stats ──
                Map<String, Object> previousMonthStats = monthlySeries.size() >= 2
                                ? monthlySeries.get(monthlySeries.size() - 2)
                                : Map.of();

                // ── Overall summary (all 4 months) ──
                double totalIncome = transactions.stream()
                                .filter(t -> t.getType() == CategoryType.INCOME)
                                .mapToDouble(t -> t.getAmount().doubleValue()).sum();
                double totalExpense = transactions.stream()
                                .filter(t -> t.getType() == CategoryType.EXPENSE)
                                .mapToDouble(t -> t.getAmount().doubleValue()).sum();

                // ── Top expense categories (across entire period) ──
                Map<String, Double> categoryTotals = new LinkedHashMap<>();
                for (Transaction t : transactions) {
                        if (t.getType() != CategoryType.EXPENSE || t.getCategory() == null)
                                continue;
                        String name = t.getCategory().getName();
                        categoryTotals.merge(name, t.getAmount().doubleValue(), Double::sum);
                }
                List<Map<String, Object>> topCategories = categoryTotals.entrySet().stream()
                                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                                .limit(8)
                                .map(e -> {
                                        double pct = totalExpense > 0 ? (e.getValue() / totalExpense) * 100.0 : 0;
                                        return Map.<String, Object>of(
                                                        "name", e.getKey(),
                                                        "totalAmount", Math.round(e.getValue()),
                                                        "sharePct", Math.round(pct * 10.0) / 10.0);
                                })
                                .collect(Collectors.toList());

                // ── Savings rate per month ──
                List<Map<String, Object>> savingsRates = new ArrayList<>();
                for (Map<String, Object> m : monthlySeries) {
                        double inc = toDouble(m.get("income"));
                        double net = toDouble(m.get("net"));
                        double rate = inc > 0 ? (net / inc) * 100.0 : 0;
                        savingsRates.add(Map.of(
                                        "month", m.get("month"),
                                        "savingsRatePct", Math.round(rate * 10.0) / 10.0));
                }

                // ── Month-over-month spending change for previous month ──
                List<Map<String, Object>> categoryDelta = buildCategoryDelta(monthlySeries);

                // ── Assemble final report ──
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("status", "OK");
                report.put("reportDate", now.toString());
                report.put("currentMonth", currentMonth.toString());
                report.put("currentDayOfMonth", now.getDayOfMonth());
                report.put("periodCovered", currentMonth.minusMonths(3) + " to " + currentMonth);
                report.put("totalTransactions", transactions.size());
                report.put("totalIncome", Math.round(totalIncome));
                report.put("totalExpense", Math.round(totalExpense));
                report.put("netCashflow", Math.round(totalIncome - totalExpense));
                report.put("overallSavingsRate",
                                totalIncome > 0
                                                ? Math.round(((totalIncome - totalExpense) / totalIncome) * 1000.0)
                                                                / 10.0
                                                : 0);
                report.put("monthlySeries", monthlySeries);
                report.put("savingsRateSeries", savingsRates);
                report.put("topExpenseCategories", topCategories);
                report.put("previousMonthCategoryDelta", categoryDelta);
                report.put("currentMonthStats", currentMonthStats);
                report.put("previousMonthStats", previousMonthStats);

                return report;
        }

        // ── Helpers ──

        private List<Map<String, Object>> buildMonthlySeries(
                        List<Transaction> transactions, YearMonth currentMonth) {
                List<YearMonth> months = List.of(
                                currentMonth.minusMonths(3),
                                currentMonth.minusMonths(2),
                                currentMonth.minusMonths(1),
                                currentMonth);

                List<Map<String, Object>> series = new ArrayList<>();
                for (YearMonth month : months) {
                        List<Transaction> monthTx = transactions.stream()
                                        .filter(t -> YearMonth.from(t.getTransactionDate()).equals(month))
                                        .collect(Collectors.toList());

                        double income = monthTx.stream()
                                        .filter(t -> t.getType() == CategoryType.INCOME)
                                        .mapToDouble(t -> t.getAmount().doubleValue()).sum();
                        double expense = monthTx.stream()
                                        .filter(t -> t.getType() == CategoryType.EXPENSE)
                                        .mapToDouble(t -> t.getAmount().doubleValue()).sum();

                        // Top 5 expense categories for this month
                        Map<String, Double> catMap = new LinkedHashMap<>();
                        for (Transaction t : monthTx) {
                                if (t.getType() != CategoryType.EXPENSE || t.getCategory() == null)
                                        continue;
                                catMap.merge(t.getCategory().getName(), t.getAmount().doubleValue(), Double::sum);
                        }
                        List<Map<String, Object>> topCat = catMap.entrySet().stream()
                                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                                        .limit(5)
                                        .map(e -> {
                                                double pct = expense > 0 ? (e.getValue() / expense) * 100.0 : 0;
                                                return Map.<String, Object>of(
                                                                "name", e.getKey(),
                                                                "amount", Math.round(e.getValue()),
                                                                "sharePct", Math.round(pct * 10.0) / 10.0);
                                        })
                                        .collect(Collectors.toList());

                        Map<String, Object> point = new LinkedHashMap<>();
                        point.put("month", month.toString());
                        point.put("income", Math.round(income));
                        point.put("expense", Math.round(expense));
                        point.put("net", Math.round(income - expense));
                        point.put("transactionCount", monthTx.size());
                        point.put("topExpenseCategories", topCat);
                        series.add(point);
                }
                return series;
        }

        private List<Map<String, Object>> buildCategoryDelta(List<Map<String, Object>> monthlySeries) {
                if (monthlySeries.size() < 3)
                        return List.of();

                Map<String, Object> prevMonth = monthlySeries.get(monthlySeries.size() - 2);
                List<Map<String, Object>> baselineMonths = monthlySeries.subList(
                                Math.max(0, monthlySeries.size() - 4),
                                Math.max(0, monthlySeries.size() - 2));

                Map<String, Double> prevCat = readCatMap(prevMonth.get("topExpenseCategories"));
                Map<String, List<Double>> baselineCat = new LinkedHashMap<>();
                for (Map<String, Object> m : baselineMonths) {
                        readCatMap(m.get("topExpenseCategories"))
                                        .forEach((k, v) -> baselineCat.computeIfAbsent(k, x -> new ArrayList<>())
                                                        .add(v));
                }

                return prevCat.entrySet().stream()
                                .map(e -> {
                                        String name = e.getKey();
                                        double prevAmt = e.getValue();
                                        List<Double> bl = baselineCat.getOrDefault(name, List.of());
                                        double blAvg = bl.isEmpty() ? 0
                                                        : bl.stream().mapToDouble(Double::doubleValue).average()
                                                                        .orElse(0);
                                        double delta = blAvg > 0 ? ((prevAmt - blAvg) / blAvg) * 100.0 : 0;
                                        Map<String, Object> row = new LinkedHashMap<>();
                                        row.put("name", name);
                                        row.put("previousAmount", Math.round(prevAmt));
                                        row.put("baselineAvg", Math.round(blAvg));
                                        row.put("deltaPct", Math.round(delta * 10.0) / 10.0);
                                        return row;
                                })
                                .sorted(Comparator
                                                .comparingDouble((Map<String, Object> m) -> toDouble(m.get("deltaPct")))
                                                .reversed())
                                .limit(5)
                                .collect(Collectors.toList());
        }

        @SuppressWarnings("unchecked")
        private Map<String, Double> readCatMap(Object raw) {
                if (!(raw instanceof List<?> list))
                        return Map.of();
                Map<String, Double> out = new LinkedHashMap<>();
                for (Object item : list) {
                        if (item instanceof Map<?, ?> m) {
                                String name = m.get("name") != null ? m.get("name").toString() : null;
                                Double amt = toDoubleOrNull(m.get("amount"));
                                if (name != null && amt != null)
                                        out.put(name, amt);
                        }
                }
                return out;
        }

        private static double toDouble(Object v) {
                if (v == null)
                        return 0;
                try {
                        return Double.parseDouble(v.toString());
                } catch (Exception e) {
                        return 0;
                }
        }

        private static Double toDoubleOrNull(Object v) {
                if (v == null)
                        return null;
                try {
                        return Double.parseDouble(v.toString());
                } catch (Exception e) {
                        return null;
                }
        }
}
