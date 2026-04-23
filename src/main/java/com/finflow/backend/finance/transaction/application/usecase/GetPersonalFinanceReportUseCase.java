package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.dto.PersonalFinanceReportOutput;
import com.finflow.backend.finance.transaction.application.port.in.GetPersonalFinanceReportPort;
import com.finflow.backend.finance.transaction.application.query.GetPersonalFinanceReportQuery;
import com.finflow.backend.finance.transaction.application.service.PersonalFinanceReportHelper;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.common.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetPersonalFinanceReportUseCase implements GetPersonalFinanceReportPort {

    private final TransactionRepository transactionRepository;
    private final PersonalFinanceReportHelper helper;

    @Transactional(readOnly = true)
    @Override
    public PersonalFinanceReportOutput execute(GetPersonalFinanceReportQuery query) {
        String userId = query.userId();
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(now);
        LocalDateTime startAt = currentMonth.minusMonths(3).atDay(1).atStartOfDay();
        LocalDateTime endAt = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                        userId, startAt, endAt);

        if (transactions.isEmpty()) {
            return PersonalFinanceReportOutput.noData("Chưa có giao dịch nào trong 4 tháng gần nhất.");
        }

        List<PersonalFinanceReportOutput.MonthlyPoint> monthlySeries =
                helper.buildMonthlySeries(transactions, currentMonth);

        PersonalFinanceReportOutput.MonthlyPoint currentMonthStats = monthlySeries.isEmpty()
                ? null : monthlySeries.get(monthlySeries.size() - 1);
        PersonalFinanceReportOutput.MonthlyPoint previousMonthStats = monthlySeries.size() >= 2
                ? monthlySeries.get(monthlySeries.size() - 2) : null;

        double totalIncome = transactions.stream()
                .filter(t -> t.getType() == CategoryType.INCOME)
                .mapToDouble(t -> t.getAmount().doubleValue()).sum();
        double totalExpense = transactions.stream()
                .filter(t -> t.getType() == CategoryType.EXPENSE)
                .mapToDouble(t -> t.getAmount().doubleValue()).sum();

        Map<String, Double> categoryTotals = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() != CategoryType.EXPENSE || t.getCategory() == null) continue;
            categoryTotals.merge(t.getCategory().getName(), t.getAmount().doubleValue(), Double::sum);
        }
        List<PersonalFinanceReportOutput.TopExpenseCategory> topCategories = categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(8)
                .map(e -> {
                    double pct = totalExpense > 0 ? (e.getValue() / totalExpense) * 100.0 : 0;
                    return new PersonalFinanceReportOutput.TopExpenseCategory(
                            e.getKey(), Math.round(e.getValue()), Math.round(pct * 10.0) / 10.0);
                })
                .collect(Collectors.toList());

        List<PersonalFinanceReportOutput.SavingsRatePoint> savingsRates = new ArrayList<>();
        for (PersonalFinanceReportOutput.MonthlyPoint m : monthlySeries) {
            double inc = m.income() != null ? m.income() : 0;
            double net = m.net() != null ? m.net() : 0;
            double rate = inc > 0 ? (net / inc) * 100.0 : 0;
            savingsRates.add(new PersonalFinanceReportOutput.SavingsRatePoint(
                    m.month(), Math.round(rate * 10.0) / 10.0));
        }

        List<PersonalFinanceReportOutput.CategoryDelta> categoryDelta = helper.buildCategoryDelta(monthlySeries);

        PersonalFinanceReportOutput.Data data = new PersonalFinanceReportOutput.Data(
                now.toString(),
                currentMonth.toString(),
                now.getDayOfMonth(),
                currentMonth.minusMonths(3) + " to " + currentMonth,
                transactions.size(),
                Math.round(totalIncome),
                Math.round(totalExpense),
                Math.round(totalIncome - totalExpense),
                totalIncome > 0
                        ? Math.round(((totalIncome - totalExpense) / totalIncome) * 1000.0) / 10.0
                        : 0,
                monthlySeries,
                savingsRates,
                topCategories,
                categoryDelta,
                currentMonthStats,
                previousMonthStats
        );
        return new PersonalFinanceReportOutput("OK", null, data);
    }
}
