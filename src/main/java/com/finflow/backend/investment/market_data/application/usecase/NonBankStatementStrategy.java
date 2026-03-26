package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.domain.entity.NonBankBalanceSheet;
import com.finflow.backend.investment.market_data.domain.entity.NonBankIncomeStatement;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;

import java.util.*;

import static com.finflow.backend.investment.market_data.application.usecase.InvestmentAnalysisNumberUtils.keepLatestQuarterByYear;
import static com.finflow.backend.investment.market_data.application.usecase.InvestmentAnalysisNumberUtils.normalizeLimit;
import static com.finflow.backend.investment.market_data.application.usecase.InvestmentAnalysisNumberUtils.sumBigDecimals;
import static com.finflow.backend.investment.market_data.application.usecase.InvestmentAnalysisNumberUtils.toDouble;

/**
 * Strategy for building NON_BANK financial series points.
 * Pure transformation: no repository access.
 */
class NonBankStatementStrategy {
    private final InvestmentFinancialPointMapper pointMapper;

    NonBankStatementStrategy(InvestmentFinancialPointMapper pointMapper) {
        this.pointMapper = pointMapper;
    }

    List<InvestmentAnalysisResponse.NonBankFinancialPoint> buildPoints(
            List<NonBankBalanceSheet> balances,
            List<NonBankIncomeStatement> incomes,
            List<FinancialIndicator> indicators,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        Map<Integer, NonBankBalanceSheet> balByYear = keepLatestQuarterByYear(
                balances,
                NonBankBalanceSheet::getYear,
                NonBankBalanceSheet::getQuarter
        );
        Map<Integer, FinancialIndicator> indByYear = keepLatestQuarterByYear(
                indicators,
                FinancialIndicator::getYear,
                FinancialIndicator::getQuarter
        );

        Map<Integer, List<NonBankIncomeStatement>> incGrouped = new HashMap<>();
        for (NonBankIncomeStatement inc : incomes) {
            incGrouped.computeIfAbsent(inc.getYear(), k -> new ArrayList<>()).add(inc);
        }

        Set<Integer> allYears = new HashSet<>();
        allYears.addAll(balByYear.keySet());
        allYears.addAll(indByYear.keySet());
        allYears.addAll(incGrouped.keySet());
        List<Integer> years = allYears.stream().sorted().toList();

        List<InvestmentAnalysisResponse.NonBankFinancialPoint> points = new ArrayList<>();
        for (Integer year : years) {
            NonBankBalanceSheet b = balByYear.get(year);
            FinancialIndicator f = indByYear.get(year);
            List<NonBankIncomeStatement> quarterIncomes = incGrouped.getOrDefault(year, List.of());

            Double annualNetRevenue = sumBigDecimals(quarterIncomes, NonBankIncomeStatement::getNetRevenue);
            Double annualProfit = sumBigDecimals(quarterIncomes, NonBankIncomeStatement::getProfitAfterTax);
            Double annualTotalRevenue = sumBigDecimals(quarterIncomes, NonBankIncomeStatement::getTotalRevenue);
            Double grossMargin = f == null ? null : toDouble(f.getLng());
            Double netMargin = f == null ? null : toDouble(f.getLnr());
            if (netMargin == null && annualNetRevenue != null && annualProfit != null && annualNetRevenue > 0) {
                netMargin = annualProfit / annualNetRevenue * 100.0;
            }

            points.add(makeNonBankPoint(
                    year,
                    0,
                    b,
                    f,
                    annualNetRevenue,
                    annualProfit,
                    grossMargin,
                    netMargin,
                    annualTotalRevenue
            ));

            for (NonBankIncomeStatement qi : quarterIncomes) {
                NonBankBalanceSheet qb = balances.stream()
                        .filter(bs -> bs.getYear() == year && bs.getQuarter() == qi.getQuarter())
                        .findFirst().orElse(b);

                FinancialIndicator qf = indicators.stream()
                        .filter(ind -> ind.getYear() == year && ind.getQuarter() == qi.getQuarter())
                        .findFirst().orElse(null);

                Double qNetRevenue = toDouble(qi.getNetRevenue());
                Double qProfit = toDouble(qi.getProfitAfterTax());
                Double qGross = qf == null ? null : toDouble(qf.getLng());
                Double qNet = qf == null ? null : toDouble(qf.getLnr());
                if (qNet == null && qNetRevenue != null && qProfit != null && qNetRevenue > 0) {
                    qNet = qProfit / qNetRevenue * 100.0;
                }

                points.add(makeNonBankPoint(
                        year,
                        qi.getQuarter(),
                        qb,
                        qf,
                        qNetRevenue,
                        qProfit,
                        qGross,
                        qNet,
                        toDouble(qi.getTotalRevenue())
                ));
            }
        }

        return applyFinancialLimitsNonBank(points, annualLimit, quarterlyLimit);
    }

    private List<InvestmentAnalysisResponse.NonBankFinancialPoint> applyFinancialLimitsNonBank(
            List<InvestmentAnalysisResponse.NonBankFinancialPoint> points,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        List<InvestmentAnalysisResponse.NonBankFinancialPoint> annual = points.stream()
                .filter(p -> p.quarter() != null && p.quarter() == 0)
                .sorted(Comparator.comparing(InvestmentAnalysisResponse.NonBankFinancialPoint::year, Comparator.reverseOrder()))
                .limit(normalizeLimit(annualLimit))
                .toList();

        List<InvestmentAnalysisResponse.NonBankFinancialPoint> quarterly = points.stream()
                .filter(p -> p.quarter() != null && p.quarter() > 0)
                .sorted(
                        Comparator.comparing(InvestmentAnalysisResponse.NonBankFinancialPoint::year, Comparator.reverseOrder())
                                .thenComparing(InvestmentAnalysisResponse.NonBankFinancialPoint::quarter, Comparator.reverseOrder())
                )
                .limit(normalizeLimit(quarterlyLimit))
                .toList();

        List<InvestmentAnalysisResponse.NonBankFinancialPoint> merged = new ArrayList<>(annual.size() + quarterly.size());
        merged.addAll(annual);
        merged.addAll(quarterly);
        merged.sort(
                Comparator.comparing(InvestmentAnalysisResponse.NonBankFinancialPoint::year, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(InvestmentAnalysisResponse.NonBankFinancialPoint::quarter, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        return merged;
    }

    private InvestmentAnalysisResponse.NonBankFinancialPoint makeNonBankPoint(
            Integer year,
            Integer quarter,
            NonBankBalanceSheet b,
            FinancialIndicator f,
            Double netRevenue,
            Double profit,
            Double grossMargin,
            Double netMargin,
            Double totalRevenue
    ) {
        return pointMapper.toNonBankFinancialPoint(
                year,
                quarter,
                b == null ? null : toDouble(b.getCashAndCashEquivalents()),
                b == null ? null : toDouble(b.getShortTermInvestments()),
                b == null ? null : toDouble(b.getShortTermReceivables()),
                b == null ? null : toDouble(b.getInventories()),
                b == null ? null : toDouble(b.getFixedAssets()),
                b == null ? null : toDouble(b.getLongTermReceivables()),
                b == null ? null : toDouble(b.getTotalAssets()),
                b == null ? null : toDouble(b.getEquity()),
                b == null ? null : toDouble(b.getShortTermBorrowings()),
                b == null ? null : toDouble(b.getLongTermBorrowings()),
                b == null ? null : toDouble(b.getAdvancesFromCustomers()),
                b == null ? null : toDouble(b.getTotalCapital()),
                f == null ? null : toDouble(f.getRoe()),
                f == null ? null : toDouble(f.getRoa()),
                netRevenue,
                profit,
                grossMargin,
                netMargin,
                b == null ? null : toDouble(b.getTotalLiabilities()),
                totalRevenue
        );
    }
}

