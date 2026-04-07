package com.finflow.backend.investment.market_data.application.strategy;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentFinancialPointMapper;
import com.finflow.backend.investment.market_data.domain.entity.BankBalanceSheet;
import com.finflow.backend.investment.market_data.domain.entity.BankIncomeStatement;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;

import java.util.*;

import static com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisNumberUtils.keepLatestQuarterByYear;
import static com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisNumberUtils.normalizeLimit;
import static com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisNumberUtils.sumBigDecimals;
import static com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisNumberUtils.toDouble;

/**
 * Strategy for building BANK financial series points.
 * Pure transformation: no repository access.
 */
public class BankStatementStrategy {
    private final InvestmentFinancialPointMapper pointMapper;

    public BankStatementStrategy(InvestmentFinancialPointMapper pointMapper) {
        this.pointMapper = pointMapper;
    }

    public List<InvestmentAnalysisResponse.BankFinancialPoint> buildPoints(
            List<BankBalanceSheet> balances,
            List<BankIncomeStatement> incomes,
            List<FinancialIndicator> indicators,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        Map<Integer, BankBalanceSheet> balByYear =
                keepLatestQuarterByYear(balances, BankBalanceSheet::getYear, BankBalanceSheet::getQuarter);
        Map<Integer, FinancialIndicator> indByYear =
                keepLatestQuarterByYear(indicators, FinancialIndicator::getYear, FinancialIndicator::getQuarter);

        Map<Integer, List<BankIncomeStatement>> incGrouped = new HashMap<>();
        for (BankIncomeStatement inc : incomes) {
            incGrouped.computeIfAbsent(inc.getYear(), k -> new ArrayList<>()).add(inc);
        }

        Set<Integer> allYears = new HashSet<>();
        allYears.addAll(balByYear.keySet());
        allYears.addAll(indByYear.keySet());
        allYears.addAll(incGrouped.keySet());
        List<Integer> years = allYears.stream().sorted().toList();

        List<InvestmentAnalysisResponse.BankFinancialPoint> points = new ArrayList<>();
        for (Integer year : years) {
            BankBalanceSheet b = balByYear.get(year);
            FinancialIndicator f = indByYear.get(year);
            List<BankIncomeStatement> quarterIncomes = incGrouped.getOrDefault(year, List.of());

            Double annualNetInterest = sumBigDecimals(quarterIncomes, BankIncomeStatement::getNetInterestIncome);
            Double annualFee = sumBigDecimals(quarterIncomes, BankIncomeStatement::getNetFeeAndCommissionIncome);
            Double annualOther = sumBigDecimals(quarterIncomes, BankIncomeStatement::getNetOtherIncomeOrExpenses);
            Double annualProfit = sumBigDecimals(quarterIncomes, BankIncomeStatement::getProfitAfterTax);
            Double annualInterestExpense = sumBigDecimals(quarterIncomes, BankIncomeStatement::getInterestExpense);

            points.add(makeBankPoint(year, 0, b, f,
                    annualNetInterest, annualFee, annualOther, annualProfit, annualInterestExpense));

            for (BankIncomeStatement qi : quarterIncomes) {
                BankBalanceSheet qb = balances.stream()
                        .filter(bs -> bs.getYear() == year && bs.getQuarter() == qi.getQuarter())
                        .findFirst().orElse(b);

                FinancialIndicator qf = indicators.stream()
                        .filter(ind -> ind.getYear() == year && ind.getQuarter() == qi.getQuarter())
                        .findFirst().orElse(null);

                points.add(makeBankPoint(year, qi.getQuarter(), qb, qf,
                        toDouble(qi.getNetInterestIncome()),
                        toDouble(qi.getNetFeeAndCommissionIncome()),
                        toDouble(qi.getNetOtherIncomeOrExpenses()),
                        toDouble(qi.getProfitAfterTax()),
                        toDouble(qi.getInterestExpense())));
            }
        }

        return applyFinancialLimitsBank(points, annualLimit, quarterlyLimit);
    }

    private List<InvestmentAnalysisResponse.BankFinancialPoint> applyFinancialLimitsBank(
            List<InvestmentAnalysisResponse.BankFinancialPoint> points,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        List<InvestmentAnalysisResponse.BankFinancialPoint> annual = points.stream()
                .filter(p -> p.quarter() != null && p.quarter() == 0)
                .sorted(Comparator.comparing(InvestmentAnalysisResponse.BankFinancialPoint::year, Comparator.reverseOrder()))
                .limit(normalizeLimit(annualLimit))
                .toList();

        List<InvestmentAnalysisResponse.BankFinancialPoint> quarterly = points.stream()
                .filter(p -> p.quarter() != null && p.quarter() > 0)
                .sorted(
                        Comparator.comparing(InvestmentAnalysisResponse.BankFinancialPoint::year, Comparator.reverseOrder())
                                .thenComparing(InvestmentAnalysisResponse.BankFinancialPoint::quarter, Comparator.reverseOrder())
                )
                .limit(normalizeLimit(quarterlyLimit))
                .toList();

        List<InvestmentAnalysisResponse.BankFinancialPoint> merged = new ArrayList<>(annual.size() + quarterly.size());
        merged.addAll(annual);
        merged.addAll(quarterly);
        merged.sort(
                Comparator.comparing(InvestmentAnalysisResponse.BankFinancialPoint::year, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(InvestmentAnalysisResponse.BankFinancialPoint::quarter, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        return merged;
    }

    private InvestmentAnalysisResponse.BankFinancialPoint makeBankPoint(
            Integer year,
            Integer quarter,
            BankBalanceSheet b,
            FinancialIndicator f,
            Double netInterest,
            Double fee,
            Double other,
            Double profit,
            Double interestExpense
    ) {
        return pointMapper.toBankFinancialPoint(
                year,
                quarter,
                b == null ? null : toDouble(b.getCashAndCashEquivalents()),
                b == null ? null : toDouble(b.getBalancesWithSbv()),
                b == null ? null : toDouble(b.getInterbankPlacementsAndLoans()),
                b == null ? null : toDouble(b.getTradingSecurities()),
                b == null ? null : toDouble(b.getInvestmentSecurities()),
                b == null ? null : toDouble(b.getLoansToCustomers()),
                null,
                null,
                null,
                null,
                b == null ? null : toDouble(b.getGovAndSbvDebt()),
                b == null ? null : toDouble(b.getDepositsFromCustomers()),
                b == null ? null : toDouble(b.getConvertibleAndOtherPapers()),
                b == null ? null : toDouble(b.getEquity()),
                f == null ? null : toDouble(f.getRoe()),
                f == null ? null : toDouble(f.getRoa()),
                netInterest,
                fee,
                other,
                profit,
                b == null ? null : toDouble(b.getDepositsBorrowingsOthers()),
                b == null ? null : toDouble(b.getTotalLiabilities()),
                interestExpense
        );
    }
}
