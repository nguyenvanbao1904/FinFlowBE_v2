package com.finflow.backend.investment.market_data.application.service;

import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.domain.entity.BankIncomeStatement;
import com.finflow.backend.investment.market_data.domain.entity.NonBankIncomeStatement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class InvestmentFinancialUtils {
    private InvestmentFinancialUtils() {
    }

    static Double computeEpsTtm(List<FinancialIndicator> indicators) {
        if (indicators == null || indicators.isEmpty()) return null;

        // Sum eps across latest 4 available quarters (latest by (year, quarter) desc).
        List<FinancialIndicator> latest4 = indicators.stream()
                .filter(i -> i.getEps() != null)
                .sorted(
                        Comparator.comparingInt(FinancialIndicator::getYear).reversed()
                                .thenComparing(Comparator.comparingInt(FinancialIndicator::getQuarter).reversed())
                )
                .limit(4)
                .toList();
        if (latest4.isEmpty()) return null;

        return latest4.stream()
                .map(FinancialIndicator::getEps)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
    }

    /**
     * Tổng doanh thu thuần 4 quý gần nhất (chỉ {@code netRevenue}, không dùng {@code totalRevenue}).
     */
    static Double computeNetRevenueTtm(List<NonBankIncomeStatement> incomes) {
        if (incomes == null || incomes.isEmpty()) return null;
        List<NonBankIncomeStatement> latest4 = incomes.stream()
                .filter(i -> i.getNetRevenue() != null)
                .sorted(
                        Comparator.comparingInt(NonBankIncomeStatement::getYear).reversed()
                                .thenComparing(Comparator.comparingInt(NonBankIncomeStatement::getQuarter).reversed())
                )
                .limit(4)
                .toList();
        if (latest4.isEmpty()) return null;
        return latest4.stream()
                .map(NonBankIncomeStatement::getNetRevenue)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
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
     * NH không có một dòng “doanh thu thuần” như DN thường; P/S dùng proxy quy mô hoạt động (một quý, VND):
     * thu nhập lãi thuần + lãi dịch vụ + thu nhập khác.
     */
    static double bankQuarterTopLineVnd(BankIncomeStatement b) {
        if (b == null) {
            return 0;
        }
        double s = 0;
        if (b.getNetInterestIncome() != null) {
            s += b.getNetInterestIncome().doubleValue();
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
                && (b.getNetInterestIncome() != null
                || b.getNetFeeAndCommissionIncome() != null
                || b.getNetOtherIncomeOrExpenses() != null);
    }

    /** Tổng “top line” NH 4 quý gần nhất (cùng logic thời gian với DTT non-bank). */
    static Double computeBankTopLineTtm(List<BankIncomeStatement> incomes) {
        if (incomes == null || incomes.isEmpty()) {
            return null;
        }
        List<BankIncomeStatement> latest4 = incomes.stream()
                .filter(InvestmentFinancialUtils::bankQuarterHasTopLine)
                .sorted(
                        Comparator.comparingInt(BankIncomeStatement::getYear).reversed()
                                .thenComparing(Comparator.comparingInt(BankIncomeStatement::getQuarter).reversed())
                )
                .limit(4)
                .toList();
        if (latest4.isEmpty()) {
            return null;
        }
        double sum = latest4.stream().mapToDouble(InvestmentFinancialUtils::bankQuarterTopLineVnd).sum();
        return sum > 0 ? sum : null;
    }

    /** Tổng top-line NH 4 quý gần nhất có quarterEnd ≤ {@code d}. */
    public static Double bankTopLineTtmAsOf(List<BankIncomeStatement> incomesAsc, LocalDate d) {
        if (incomesAsc == null || incomesAsc.isEmpty()) {
            return null;
        }
        List<BankIncomeStatement> latest4 = incomesAsc.stream()
                .filter(InvestmentFinancialUtils::bankQuarterHasTopLine)
                .filter(i -> !quarterEnd(i).isAfter(d))
                .sorted(Comparator.comparing((BankIncomeStatement i) -> quarterEnd(i)).reversed())
                .limit(4)
                .toList();
        if (latest4.isEmpty()) {
            return null;
        }
        double sum = latest4.stream().mapToDouble(InvestmentFinancialUtils::bankQuarterTopLineVnd).sum();
        return sum > 0 ? sum : null;
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
        if (indicatorsAsc == null || indicatorsAsc.isEmpty()) {
            return null;
        }
        List<FinancialIndicator> latest4 = indicatorsAsc.stream()
                .filter(i -> i.getEps() != null && !quarterEnd(i).isAfter(d))
                .sorted(Comparator.comparing((FinancialIndicator i) -> quarterEnd(i)).reversed())
                .limit(4)
                .toList();
        if (latest4.isEmpty()) {
            return null;
        }
        return latest4.stream()
                .map(FinancialIndicator::getEps)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
    }

    /** Tổng doanh thu thuần 4 quý gần nhất có quarterEnd ≤ {@code d} (chỉ {@code netRevenue}). */
    public static Double netRevenueTtmAsOf(List<NonBankIncomeStatement> incomesAsc, LocalDate d) {
        if (incomesAsc == null || incomesAsc.isEmpty()) {
            return null;
        }
        List<NonBankIncomeStatement> latest4 = incomesAsc.stream()
                .filter(i -> i.getNetRevenue() != null && !quarterEnd(i).isAfter(d))
                .sorted(Comparator.comparing((NonBankIncomeStatement i) -> quarterEnd(i)).reversed())
                .limit(4)
                .toList();
        if (latest4.isEmpty()) {
            return null;
        }
        return latest4.stream()
                .map(NonBankIncomeStatement::getNetRevenue)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
    }

}
