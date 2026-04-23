package com.finflow.backend.finance.transaction.application.service;

import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.common.enums.CategoryType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Component
public class TransactionChartHelper {

    private static final ZoneOffset UTC = ZoneOffset.UTC;
    private static final ZoneId ASIA_HCM = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Aggregates transactions by day, converting UTC to Asia/Ho_Chi_Minh timezone first.
     * Returns Map&lt;LocalDate, [income, expense]&gt;
     */
    public Map<LocalDate, double[]> aggregateByDayWithTimezone(List<Transaction> transactions) {
        return transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        tx -> {
                            ZonedDateTime localZdt = tx.getTransactionDate()
                                    .atZone(UTC)
                                    .withZoneSameInstant(ASIA_HCM);
                            return localZdt.toLocalDate();
                        },
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                txList -> sumIncomeExpense(txList)
                        )
                ));
    }

    /**
     * Aggregates transactions by month, converting UTC to Asia/Ho_Chi_Minh timezone first.
     * Returns Map&lt;"year-month", [income, expense]&gt;
     */
    public Map<String, double[]> aggregateByMonthWithTimezone(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> {
                            ZonedDateTime localZdt = tx.getTransactionDate()
                                    .atZone(UTC)
                                    .withZoneSameInstant(ASIA_HCM);
                            return localZdt.getYear() + "-" + localZdt.getMonthValue();
                        },
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                txList -> sumIncomeExpense(txList)
                        )
                ));
    }

    private double[] sumIncomeExpense(List<Transaction> txList) {
        double income = txList.stream()
                .filter(tx -> tx.getType() == CategoryType.INCOME)
                .mapToDouble(tx -> tx.getAmount().doubleValue())
                .sum();
        double expense = txList.stream()
                .filter(tx -> tx.getType() == CategoryType.EXPENSE)
                .mapToDouble(tx -> tx.getAmount().doubleValue())
                .sum();
        return new double[]{income, expense};
    }
}
