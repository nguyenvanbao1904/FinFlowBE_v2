package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

final class InvestmentFinancialUtils {
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
}

