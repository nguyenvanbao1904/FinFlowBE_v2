package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.TransactionChartRange;
import com.finflow.backend.finance.transaction.application.port.in.GetTransactionChartPort;
import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.transaction.presentation.response.TransactionChartResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionChartResponse.ChartDataPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetTransactionChartUseCase implements GetTransactionChartPort {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public TransactionChartResponse execute(String userId, TransactionChartRange range, LocalDate referenceDate) {
        log.info("Fetching chart data for userId={}, range={}, referenceDate={}", userId, range, referenceDate);
        LocalDate today = LocalDate.now();
        LocalDate ref = referenceDate != null ? referenceDate : today;

        return switch (range) {
            case WEEK   -> buildWeekChart(userId, ref, today);
            case MONTH  -> buildMonthChart(userId, ref, today);
            case QUARTER -> buildQuarterChart(userId, ref, today);
            case YEAR   -> buildYearChart(userId, ref, today);
        };
    }

    // ─── WEEK ────────────────────────────────────────────────────────────────

    private TransactionChartResponse buildWeekChart(String userId, LocalDate ref, LocalDate today) {
        LocalDate weekStart = ref.with(DayOfWeek.MONDAY);
        LocalDate weekEnd   = weekStart.plusDays(6);

        LocalDate currentWeekStart = today.with(DayOfWeek.MONDAY);
        boolean hasNext = weekStart.isBefore(currentWeekStart);

        String label = weekStart.format(DateTimeFormatter.ofPattern("dd/MM")) +
                       "–" + weekEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                userId, weekStart.atStartOfDay(), weekEnd.atTime(23, 59, 59));
        Map<LocalDate, double[]> byDay = aggregateByDayWithTimezone(transactions);

        List<ChartDataPoint> points = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            double[] vals = byDay.getOrDefault(day, new double[]{0, 0});
            points.add(new ChartDataPoint(day.format(fmt), vals[0], vals[1]));
        }
        return new TransactionChartResponse(points, "Tuần " + label, hasNext);
    }

    // ─── MONTH ───────────────────────────────────────────────────────────────

    private TransactionChartResponse buildMonthChart(String userId, LocalDate ref, LocalDate today) {
        YearMonth ym = YearMonth.from(ref);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd   = ym.atEndOfMonth();   // handles 28/29/30/31 automatically

        YearMonth currentYm = YearMonth.from(today);
        boolean hasNext = ym.isBefore(currentYm);

        String periodLabel = "Tháng " + ym.getMonthValue() + "/" + ym.getYear();

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                userId, monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59));
        Map<LocalDate, double[]> byDay = aggregateByDayWithTimezone(transactions);

        List<ChartDataPoint> points = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            LocalDate day = ym.atDay(d);
            double[] vals = byDay.getOrDefault(day, new double[]{0, 0});
            points.add(new ChartDataPoint(day.format(fmt), vals[0], vals[1]));
        }
        return new TransactionChartResponse(points, periodLabel, hasNext);
    }

    // ─── QUARTER ─────────────────────────────────────────────────────────────

    private TransactionChartResponse buildQuarterChart(String userId, LocalDate ref, LocalDate today) {
        int quarter = (ref.getMonthValue() - 1) / 3; // 0-based
        int firstMonth = quarter * 3 + 1;
        LocalDate quarterStart = LocalDate.of(ref.getYear(), firstMonth, 1);
        LocalDate quarterEnd   = YearMonth.of(ref.getYear(), firstMonth + 2).atEndOfMonth();

        int currentQuarter = (today.getMonthValue() - 1) / 3;
        LocalDate currentQuarterStart = LocalDate.of(today.getYear(), currentQuarter * 3 + 1, 1);
        boolean hasNext = quarterStart.isBefore(currentQuarterStart);

        String periodLabel = "Quý " + (quarter + 1) + "/" + ref.getYear();

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                userId, quarterStart.atStartOfDay(), quarterEnd.atTime(23, 59, 59));
        Map<String, double[]> byMonth = aggregateByMonthWithTimezone(transactions);

        List<ChartDataPoint> points = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int i = 0; i < 3; i++) {
            YearMonth ym = YearMonth.of(ref.getYear(), firstMonth + i);
            String key = ym.getYear() + "-" + ym.getMonthValue();
            double[] vals = byMonth.getOrDefault(key, new double[]{0, 0});
            // Use first day of month for display
            LocalDate firstDayOfMonth = ym.atDay(1);
            points.add(new ChartDataPoint(firstDayOfMonth.format(fmt), vals[0], vals[1]));
        }
        return new TransactionChartResponse(points, periodLabel, hasNext);
    }

    // ─── YEAR ─────────────────────────────────────────────────────────────────

    private TransactionChartResponse buildYearChart(String userId, LocalDate ref, LocalDate today) {
        LocalDate yearStart = LocalDate.of(ref.getYear(), 1, 1);
        LocalDate yearEnd   = LocalDate.of(ref.getYear(), 12, 31);
        boolean hasNext = ref.getYear() < today.getYear();

        String periodLabel = "Năm " + ref.getYear();

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                userId, yearStart.atStartOfDay(), yearEnd.atTime(23, 59, 59));
        Map<String, double[]> byMonth = aggregateByMonthWithTimezone(transactions);

        List<ChartDataPoint> points = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int m = 1; m <= 12; m++) {
            String key = ref.getYear() + "-" + m;
            double[] vals = byMonth.getOrDefault(key, new double[]{0, 0});
            // Use first day of month for display
            LocalDate firstDayOfMonth = LocalDate.of(ref.getYear(), m, 1);
            points.add(new ChartDataPoint(firstDayOfMonth.format(fmt), vals[0], vals[1]));
        }
        return new TransactionChartResponse(points, periodLabel, hasNext);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final ZoneId ASIA_HCM = ZoneId.of("Asia/Ho_Chi_Minh");

    /** 
     * Aggregates transactions by day, converting UTC to Asia/Ho_Chi_Minh timezone first.
     * Returns Map<LocalDate, [income, expense]>
     */
    private Map<LocalDate, double[]> aggregateByDayWithTimezone(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> {
                            // Convert UTC to Asia/Ho_Chi_Minh timezone
                            ZonedDateTime localZdt = tx.getTransactionDate()
                                    .atZone(UTC)
                                    .withZoneSameInstant(ASIA_HCM);
                            return localZdt.toLocalDate();
                        },
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                txList -> {
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
                        )
                ));
    }

    /** 
     * Aggregates transactions by month, converting UTC to Asia/Ho_Chi_Minh timezone first.
     * Returns Map<"year-month", [income, expense]>
     */
    private Map<String, double[]> aggregateByMonthWithTimezone(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> {
                            // Convert UTC to Asia/Ho_Chi_Minh timezone
                            ZonedDateTime localZdt = tx.getTransactionDate()
                                    .atZone(UTC)
                                    .withZoneSameInstant(ASIA_HCM);
                            return localZdt.getYear() + "-" + localZdt.getMonthValue();
                        },
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                txList -> {
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
                        )
                ));
    }
}
