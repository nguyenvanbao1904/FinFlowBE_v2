package com.finflow.backend.investment.market_data.application.service;

import com.finflow.backend.investment.market_data.application.dto.InvestmentAnalysisOutput;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class InvestmentAnalysisLimits {
    private InvestmentAnalysisLimits() {
    }

    public static List<InvestmentAnalysisOutput.ValuationPoint> applyValuationYearLimit(
            List<InvestmentAnalysisOutput.ValuationPoint> points,
            Integer annualLimit,
            Comparator<InvestmentAnalysisOutput.ValuationPoint> valuationAsc
    ) {
        long limit = InvestmentAnalysisNumberUtils.normalizeLimit(annualLimit);
        if (limit == Long.MAX_VALUE) {
            return points;
        }
        Set<Integer> years = points.stream()
                .map(InvestmentAnalysisOutput.ValuationPoint::year)
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .limit(limit)
                .collect(java.util.stream.Collectors.toSet());
        return points.stream()
                .filter(p -> p.year() != null && years.contains(p.year()))
                .sorted(valuationAsc)
                .toList();
    }

    public static List<InvestmentAnalysisOutput.DividendPoint> applyDividendYearLimit(
            List<InvestmentAnalysisOutput.DividendPoint> points,
            Integer annualLimit
    ) {
        long limit = InvestmentAnalysisNumberUtils.normalizeLimit(annualLimit);
        if (limit == Long.MAX_VALUE) {
            return points;
        }
        Set<Integer> years = points.stream()
                .map(InvestmentAnalysisNumberUtils::extractDividendYear)
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .limit(limit)
                .collect(java.util.stream.Collectors.toSet());
        return points.stream()
                .filter(p -> {
                    Integer y = InvestmentAnalysisNumberUtils.extractDividendYear(p);
                    return y != null && years.contains(y);
                })
                .sorted(Comparator.comparing(
                        InvestmentAnalysisNumberUtils::extractDividendDateForSort,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .toList();
    }
}
