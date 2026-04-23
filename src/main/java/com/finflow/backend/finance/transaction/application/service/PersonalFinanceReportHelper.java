package com.finflow.backend.finance.transaction.application.service;

import com.finflow.backend.finance.transaction.application.dto.PersonalFinanceReportOutput;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.common.enums.CategoryType;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PersonalFinanceReportHelper {

    public List<PersonalFinanceReportOutput.MonthlyPoint> buildMonthlySeries(
            List<Transaction> transactions, YearMonth currentMonth) {
        List<YearMonth> months = List.of(
                currentMonth.minusMonths(3),
                currentMonth.minusMonths(2),
                currentMonth.minusMonths(1),
                currentMonth);

        List<PersonalFinanceReportOutput.MonthlyPoint> series = new ArrayList<>();
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

            Map<String, Double> catMap = new LinkedHashMap<>();
            for (Transaction t : monthTx) {
                if (t.getType() != CategoryType.EXPENSE || t.getCategory() == null) {
                    continue;
                }
                catMap.merge(t.getCategory().getName(), t.getAmount().doubleValue(), Double::sum);
            }
            List<PersonalFinanceReportOutput.MonthTopCategory> topCat = catMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(5)
                    .map(e -> {
                        double pct = expense > 0 ? (e.getValue() / expense) * 100.0 : 0;
                        return new PersonalFinanceReportOutput.MonthTopCategory(
                                e.getKey(), Math.round(e.getValue()), Math.round(pct * 10.0) / 10.0);
                    })
                    .collect(Collectors.toList());

            series.add(new PersonalFinanceReportOutput.MonthlyPoint(
                    month.toString(),
                    Math.round(income),
                    Math.round(expense),
                    Math.round(income - expense),
                    monthTx.size(),
                    topCat
            ));
        }
        return series;
    }

    public List<PersonalFinanceReportOutput.CategoryDelta> buildCategoryDelta(
            List<PersonalFinanceReportOutput.MonthlyPoint> monthlySeries) {
        if (monthlySeries.size() < 3) {
            return List.of();
        }

        PersonalFinanceReportOutput.MonthlyPoint prevMonth = monthlySeries.get(monthlySeries.size() - 2);
        List<PersonalFinanceReportOutput.MonthlyPoint> baselineMonths = monthlySeries.subList(
                Math.max(0, monthlySeries.size() - 4),
                Math.max(0, monthlySeries.size() - 2));

        Map<String, Double> prevCat = readCatMap(prevMonth.topExpenseCategories());
        Map<String, List<Double>> baselineCat = new LinkedHashMap<>();
        for (PersonalFinanceReportOutput.MonthlyPoint m : baselineMonths) {
            readCatMap(m.topExpenseCategories())
                    .forEach((k, v) -> baselineCat.computeIfAbsent(k, x -> new ArrayList<>()).add(v));
        }

        return prevCat.entrySet().stream()
                .map(e -> {
                    String name = e.getKey();
                    double prevAmt = e.getValue();
                    List<Double> bl = baselineCat.getOrDefault(name, List.of());
                    double blAvg = bl.isEmpty()
                            ? 0
                            : bl.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    double delta = blAvg > 0 ? ((prevAmt - blAvg) / blAvg) * 100.0 : 0;
                    return new PersonalFinanceReportOutput.CategoryDelta(
                            name,
                            Math.round(prevAmt),
                            Math.round(blAvg),
                            Math.round(delta * 10.0) / 10.0);
                })
                .sorted(Comparator
                        .comparingDouble(PersonalFinanceReportOutput.CategoryDelta::deltaPct)
                        .reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private Map<String, Double> readCatMap(List<PersonalFinanceReportOutput.MonthTopCategory> list) {
        if (list == null || list.isEmpty()) {
            return Map.of();
        }
        Map<String, Double> out = new LinkedHashMap<>();
        for (PersonalFinanceReportOutput.MonthTopCategory item : list) {
            out.put(item.name(), (double) item.amount());
        }
        return out;
    }
}
