package com.finflow.backend.finance.transaction.application.service;

import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.common.enums.CategoryType;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Stateless computation engine for transaction analytics insights.
 * Extracted from GetTransactionAnalyticsInsightsUseCase to keep use case thin.
 */
@Component
public class TransactionInsightEngine {

    public List<Map<String, Object>> buildMonthlySeries(List<Transaction> transactions) {
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

    public double averageForPreviousMonths(List<Map<String, Object>> monthlySeries, String key, int monthCount) {
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

    public List<Map<String, Object>> buildSavingsRateSeries(List<Map<String, Object>> monthlySeries) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> point : monthlySeries) {
            double income = asDouble(point.get("income")) == null ? 0.0 : asDouble(point.get("income"));
            double net = asDouble(point.get("net")) == null ? 0.0 : asDouble(point.get("net"));
            double savingsRate = income > 0 ? (net / income) * 100.0 : 0.0;
            result.add(Map.of("month", String.valueOf(point.get("month")), "savingsRatePct", savingsRate));
        }
        return result;
    }

    public List<Map<String, Object>> buildPreviousMonthCategoryDelta(List<Map<String, Object>> monthlySeries) {
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

    public List<Map<String, Object>> extractPreviousMonthTopExpenseCategories(List<Map<String, Object>> monthlySeries) {
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

    // --- private helpers ---

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
