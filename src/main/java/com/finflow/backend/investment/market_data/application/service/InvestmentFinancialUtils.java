package com.finflow.backend.investment.market_data.application.service;

import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.domain.entity.BankIncomeStatement;
import com.finflow.backend.investment.market_data.domain.entity.NonBankIncomeStatement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public final class InvestmentFinancialUtils {
    private InvestmentFinancialUtils() {
    }

    static Double computeEpsTtm(List<FinancialIndicator> indicators) {
        return sumLatestFourQuarters(
                indicators,
                i -> i.getEps() != null,
                FinancialIndicator::getYear,
                FinancialIndicator::getQuarter,
                i -> i.getEps().doubleValue()
        );
    }

    static Double computeNetRevenueTtm(List<NonBankIncomeStatement> incomes) {
        return sumLatestFourQuarters(
                incomes,
                i -> i.getNetRevenue() != null,
                NonBankIncomeStatement::getYear,
                NonBankIncomeStatement::getQuarter,
                i -> i.getNetRevenue().doubleValue()
        );
    }

    /**
     * Đồng bộ với chuẩn hoá CPLH trên iOS ({@code InvestmentRepository.normalizeSharesToBillion}).
     */
    public static double absoluteSharesFromCplh(double raw) {
        double a = Math.abs(raw);
        if (a >= 1_000_000) {
            return raw;
        }
        if (a >= 1_000) {
            return raw * 1_000_000.0;
        }
        return raw * 1_000_000_000.0;
    }

    static Double median(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) return null;

        List<Double> sorted = values.stream()
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .sorted()
                .toList();

        if (sorted.isEmpty()) return null;
        int n = sorted.size();
        if (n % 2 == 1) return sorted.get(n / 2);
        return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
    }

    /** Trung bình các giá trị khác null; null nếu không có điểm hợp lệ. */
    static Double mean(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<Double> nums = values.stream()
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .toList();
        if (nums.isEmpty()) {
            return null;
        }
        double sum = nums.stream().mapToDouble(Double::doubleValue).sum();
        return sum / nums.size();
    }

    static LocalDate quarterEndDate(int year, int quarter) {
        return switch (quarter) {
            case 1 -> LocalDate.of(year, 3, 31);
            case 2 -> LocalDate.of(year, 6, 30);
            case 3 -> LocalDate.of(year, 9, 30);
            case 4 -> LocalDate.of(year, 12, 31);
            default -> LocalDate.of(year, 12, 31);
        };
    }

    static LocalDate quarterEnd(FinancialIndicator i) {
        return quarterEndDate(i.getYear(), i.getQuarter());
    }

    static LocalDate quarterEnd(NonBankIncomeStatement i) {
        return quarterEndDate(i.getYear(), i.getQuarter());
    }

    static LocalDate quarterEnd(BankIncomeStatement i) {
        return quarterEndDate(i.getYear(), i.getQuarter());
    }

    /**
     * Revenue proxy cho ngân hàng (một quý, VND) khớp FireAnt P/S:
     * thu nhập lãi và các khoản tương tự (gộp) + phí dịch vụ ròng + thu nhập khác ròng.
     */
    static double bankQuarterTopLineVnd(BankIncomeStatement b) {
        if (b == null) {
            return 0;
        }
        double s = 0;
        if (b.getInterestAndSimilarIncome() != null) {
            s += b.getInterestAndSimilarIncome().doubleValue();
        }
        if (b.getNetFeeAndCommissionIncome() != null) {
            s += b.getNetFeeAndCommissionIncome().doubleValue();
        }
        if (b.getNetOtherIncomeOrExpenses() != null) {
            s += b.getNetOtherIncomeOrExpenses().doubleValue();
        }
        return s;
    }

    static boolean bankQuarterHasTopLine(BankIncomeStatement b) {
        return b != null
                && (b.getInterestAndSimilarIncome() != null
                || b.getNetFeeAndCommissionIncome() != null
                || b.getNetOtherIncomeOrExpenses() != null);
    }

    /** Tổng “top line” NH 4 quý gần nhất (cùng logic thời gian với DTT non-bank). */
    static Double computeBankTopLineTtm(List<BankIncomeStatement> incomes) {
        Double sum = sumLatestFourQuarters(
                incomes,
                InvestmentFinancialUtils::bankQuarterHasTopLine,
                BankIncomeStatement::getYear,
                BankIncomeStatement::getQuarter,
                InvestmentFinancialUtils::bankQuarterTopLineVnd
        );
        return sum != null && sum > 0 ? sum : null;
    }

    /** Tổng top-line NH 4 quý gần nhất có quarterEnd ≤ {@code d}. */
    public static Double bankTopLineTtmAsOf(List<BankIncomeStatement> incomesAsc, LocalDate d) {
        Double sum = sumLatestFourAsOf(
                incomesAsc,
                InvestmentFinancialUtils::bankQuarterHasTopLine,
                InvestmentFinancialUtils::quarterEnd,
                d,
                InvestmentFinancialUtils::bankQuarterTopLineVnd
        );
        return sum != null && sum > 0 ? sum : null;
    }

    /**
     * Bản ghi chỉ số mới nhất có ngày cuối kỳ ≤ {@code d} ({@code indicators} sort year asc, quarter asc).
     */
    public static FinancialIndicator latestIndicatorAsOf(List<FinancialIndicator> indicatorsAsc, LocalDate d) {
        if (indicatorsAsc == null || indicatorsAsc.isEmpty()) {
            return null;
        }
        FinancialIndicator best = null;
        for (FinancialIndicator fi : indicatorsAsc) {
            if (!quarterEnd(fi).isAfter(d)) {
                best = fi;
            }
        }
        return best;
    }

    /** Tổng EPS 4 quý gần nhất có quarterEnd ≤ {@code d}. */
    public static Double epsTtmAsOf(List<FinancialIndicator> indicatorsAsc, LocalDate d) {
        return sumLatestFourAsOf(
                indicatorsAsc,
                i -> i.getEps() != null,
                InvestmentFinancialUtils::quarterEnd,
                d,
                i -> i.getEps().doubleValue()
        );
    }

    public static Double netRevenueTtmAsOf(List<NonBankIncomeStatement> incomesAsc, LocalDate d) {
        return sumLatestFourAsOf(
                incomesAsc,
                i -> i.getNetRevenue() != null,
                InvestmentFinancialUtils::quarterEnd,
                d,
                i -> i.getNetRevenue().doubleValue()
        );
    }

    private static <T> Double sumLatestFourQuarters(
            List<T> items,
            Predicate<T> valid,
            ToDoubleFunction<T> yearFn,
            ToDoubleFunction<T> quarterFn,
            ToDoubleFunction<T> valueFn
    ) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        List<T> latest4 = items.stream()
                .filter(valid)
                .sorted(
                        Comparator.comparingDouble(yearFn).reversed()
                                .thenComparing(Comparator.comparingDouble(quarterFn).reversed())
                )
                .limit(4)
                .toList();
        if (latest4.isEmpty()) {
            return null;
        }
        return latest4.stream().mapToDouble(valueFn).sum();
    }

    private static <T> Double sumLatestFourAsOf(
            List<T> items,
            Predicate<T> valid,
            Function<T, LocalDate> dateFn,
            LocalDate maxDate,
            ToDoubleFunction<T> valueFn
    ) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        List<T> latest4 = items.stream()
                .filter(valid)
                .filter(i -> !dateFn.apply(i).isAfter(maxDate))
                .sorted(Comparator.comparing(dateFn).reversed())
                .limit(4)
                .toList();
        if (latest4.isEmpty()) {
            return null;
        }
        return latest4.stream().mapToDouble(valueFn).sum();
    }

}
