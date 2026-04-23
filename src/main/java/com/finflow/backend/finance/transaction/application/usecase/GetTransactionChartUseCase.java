package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.common.enums.TransactionChartRange;
import com.finflow.backend.finance.transaction.application.query.GetTransactionChartQuery;
import com.finflow.backend.finance.transaction.application.dto.TransactionChartOutput;
import com.finflow.backend.finance.transaction.application.dto.TransactionChartOutput.ChartPointOutput;
import com.finflow.backend.finance.transaction.application.port.in.GetTransactionChartPort;
import com.finflow.backend.finance.transaction.application.service.TransactionChartHelper;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetTransactionChartUseCase implements GetTransactionChartPort {

    private final TransactionRepository transactionRepository;
    private final TransactionChartHelper chartHelper;

    @Transactional(readOnly = true)
    @Override
    public TransactionChartOutput execute(GetTransactionChartQuery request) {
        String userId = request.userId();
        TransactionChartRange range = request.range();
        LocalDate referenceDate = request.referenceDate();
        log.info("Fetching chart data for userId={}, range={}, referenceDate={}", userId, range, referenceDate);
        LocalDate today = LocalDate.now();
        LocalDate ref = referenceDate != null ? referenceDate : today;

        return switch (range) {
            case WEEK    -> buildWeekChart(userId, ref, today);
            case MONTH   -> buildMonthChart(userId, ref, today);
            case QUARTER -> buildQuarterChart(userId, ref, today);
            case YEAR    -> buildYearChart(userId, ref, today);
        };
    }

    private TransactionChartOutput buildWeekChart(String userId, LocalDate ref, LocalDate today) {
        LocalDate weekStart = ref.with(DayOfWeek.MONDAY);
        LocalDate weekEnd   = weekStart.plusDays(6);
        boolean hasNext = weekStart.isBefore(today.with(DayOfWeek.MONDAY));
        String label = weekStart.format(DateTimeFormatter.ofPattern("dd/MM")) +
                       "–" + weekEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                userId, weekStart.atStartOfDay(), weekEnd.atTime(23, 59, 59));
        Map<LocalDate, double[]> byDay = chartHelper.aggregateByDayWithTimezone(transactions);

        List<ChartPointOutput> points = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            double[] vals = byDay.getOrDefault(day, new double[]{0, 0});
            points.add(new ChartPointOutput(day.format(fmt), vals[0], vals[1]));
        }
        return new TransactionChartOutput(points, "Tuần " + label, hasNext);
    }

    private TransactionChartOutput buildMonthChart(String userId, LocalDate ref, LocalDate today) {
        YearMonth ym = YearMonth.from(ref);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd   = ym.atEndOfMonth();
        boolean hasNext = ym.isBefore(YearMonth.from(today));
        String periodLabel = "Tháng " + ym.getMonthValue() + "/" + ym.getYear();

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                userId, monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59));
        Map<LocalDate, double[]> byDay = chartHelper.aggregateByDayWithTimezone(transactions);

        List<ChartPointOutput> points = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            LocalDate day = ym.atDay(d);
            double[] vals = byDay.getOrDefault(day, new double[]{0, 0});
            points.add(new ChartPointOutput(day.format(fmt), vals[0], vals[1]));
        }
        return new TransactionChartOutput(points, periodLabel, hasNext);
    }

    private TransactionChartOutput buildQuarterChart(String userId, LocalDate ref, LocalDate today) {
        int quarter = (ref.getMonthValue() - 1) / 3;
        int firstMonth = quarter * 3 + 1;
        LocalDate quarterStart = LocalDate.of(ref.getYear(), firstMonth, 1);
        LocalDate quarterEnd   = YearMonth.of(ref.getYear(), firstMonth + 2).atEndOfMonth();
        int currentQuarter = (today.getMonthValue() - 1) / 3;
        boolean hasNext = quarterStart.isBefore(LocalDate.of(today.getYear(), currentQuarter * 3 + 1, 1));
        String periodLabel = "Quý " + (quarter + 1) + "/" + ref.getYear();

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                userId, quarterStart.atStartOfDay(), quarterEnd.atTime(23, 59, 59));
        Map<String, double[]> byMonth = chartHelper.aggregateByMonthWithTimezone(transactions);

        List<ChartPointOutput> points = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int i = 0; i < 3; i++) {
            YearMonth ym = YearMonth.of(ref.getYear(), firstMonth + i);
            String key = ym.getYear() + "-" + ym.getMonthValue();
            double[] vals = byMonth.getOrDefault(key, new double[]{0, 0});
            points.add(new ChartPointOutput(ym.atDay(1).format(fmt), vals[0], vals[1]));
        }
        return new TransactionChartOutput(points, periodLabel, hasNext);
    }

    private TransactionChartOutput buildYearChart(String userId, LocalDate ref, LocalDate today) {
        LocalDate yearStart = LocalDate.of(ref.getYear(), 1, 1);
        LocalDate yearEnd   = LocalDate.of(ref.getYear(), 12, 31);
        boolean hasNext = ref.getYear() < today.getYear();
        String periodLabel = "Năm " + ref.getYear();

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                userId, yearStart.atStartOfDay(), yearEnd.atTime(23, 59, 59));
        Map<String, double[]> byMonth = chartHelper.aggregateByMonthWithTimezone(transactions);

        List<ChartPointOutput> points = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int m = 1; m <= 12; m++) {
            String key = ref.getYear() + "-" + m;
            double[] vals = byMonth.getOrDefault(key, new double[]{0, 0});
            points.add(new ChartPointOutput(LocalDate.of(ref.getYear(), m, 1).format(fmt), vals[0], vals[1]));
        }
        return new TransactionChartOutput(points, periodLabel, hasNext);
    }
}
