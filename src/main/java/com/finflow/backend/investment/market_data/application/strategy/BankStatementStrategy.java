package com.finflow.backend.investment.market_data.application.strategy;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentFinancialPointMapper;
import com.finflow.backend.investment.market_data.domain.entity.BankBalanceSheet;
import com.finflow.backend.investment.market_data.domain.entity.BankIncomeStatement;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.application.dto.InvestmentAnalysisOutput;

import java.util.*;

import static com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisNumberUtils.keepLatestQuarterByYear;
import static com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisNumberUtils.normalizeLimit;
import static com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisNumberUtils.sumBigDecimals;
import static com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisNumberUtils.toDouble;

public class BankStatementStrategy {
    private final InvestmentFinancialPointMapper pointMapper;

    public BankStatementStrategy(InvestmentFinancialPointMapper pointMapper) {
        this.pointMapper = pointMapper;
    }

    public List<InvestmentAnalysisOutput.BankFinancialPoint> buildPoints(
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

        List<InvestmentAnalysisOutput.BankFinancialPoint> points = new ArrayList<>();
        for (Integer year : years) {
            BankBalanceSheet b = balByYear.get(year);
            FinancialIndicator f = indByYear.get(year);
            List<BankIncomeStatement> quarterIncomes = incGrouped.getOrDefault(year, List.of());

            Double annualNetInterest = sumBigDecimals(quarterIncomes, BankIncomeStatement::getNetInterestIncome);
            Double annualFee = sumBigDecimals(quarterIncomes, BankIncomeStatement::getNetFeeAndCommissionIncome);
            Double annualOther = sumBigDecimals(quarterIncomes, BankIncomeStatement::getNetOtherIncomeOrExpenses);
            Double annualProfit = sumBigDecimals(quarterIncomes, BankIncomeStatement::getProfitAfterTax);
            Double annualInterestExpense = sumBigDecimals(quarterIncomes, BankIncomeStatement::getInterestExpense);
            Double annualTOI = sumBigDecimals(quarterIncomes, BankIncomeStatement::getTotalOperatingIncome);
            Double annualTOE = sumBigDecimals(quarterIncomes, BankIncomeStatement::getTotalOperatingExpense);
            Double annualProvision = sumBigDecimals(quarterIncomes, BankIncomeStatement::getCreditRiskProvisionsExpense);
            Double annualInterestIncome = sumBigDecimals(quarterIncomes, BankIncomeStatement::getInterestAndSimilarIncome);

            points.add(makeBankPoint(year, 0, b, f,
                    annualNetInterest, annualFee, annualOther, annualProfit, annualInterestExpense,
                    annualTOI, annualTOE, annualProvision, annualInterestIncome));

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
                        toDouble(qi.getInterestExpense()),
                        toDouble(qi.getTotalOperatingIncome()),
                        toDouble(qi.getTotalOperatingExpense()),
                        toDouble(qi.getCreditRiskProvisionsExpense()),
                        toDouble(qi.getInterestAndSimilarIncome())));
            }
        }

        return applyFinancialLimitsBank(points, annualLimit, quarterlyLimit);
    }

    private List<InvestmentAnalysisOutput.BankFinancialPoint> applyFinancialLimitsBank(
            List<InvestmentAnalysisOutput.BankFinancialPoint> points,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        List<InvestmentAnalysisOutput.BankFinancialPoint> annual = points.stream()
                .filter(p -> p.quarter() != null && p.quarter() == 0)
                .sorted(Comparator.comparing(InvestmentAnalysisOutput.BankFinancialPoint::year, Comparator.reverseOrder()))
                .limit(normalizeLimit(annualLimit))
                .toList();

        List<InvestmentAnalysisOutput.BankFinancialPoint> quarterly = points.stream()
                .filter(p -> p.quarter() != null && p.quarter() > 0)
                .sorted(
                        Comparator.comparing(InvestmentAnalysisOutput.BankFinancialPoint::year, Comparator.reverseOrder())
                                .thenComparing(InvestmentAnalysisOutput.BankFinancialPoint::quarter, Comparator.reverseOrder())
                )
                .limit(normalizeLimit(quarterlyLimit))
                .toList();

        List<InvestmentAnalysisOutput.BankFinancialPoint> merged = new ArrayList<>(annual.size() + quarterly.size());
        merged.addAll(annual);
        merged.addAll(quarterly);
        merged.sort(
                Comparator.comparing(InvestmentAnalysisOutput.BankFinancialPoint::year, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(InvestmentAnalysisOutput.BankFinancialPoint::quarter, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        return merged;
    }

    private InvestmentAnalysisOutput.BankFinancialPoint makeBankPoint(
            Integer year,
            Integer quarter,
            BankBalanceSheet b,
            FinancialIndicator f,
            Double netInterest,
            Double fee,
            Double other,
            Double profit,
            Double interestExpense,
            Double totalOperatingIncome,
            Double totalOperatingExpense,
            Double creditRiskProvisionsExpense,
            Double interestAndSimilarIncome
    ) {
        return pointMapper.toBankFinancialPoint(
                year,
                quarter,
                // Balance sheet — assets
                b == null ? null : toDouble(b.getCashAndCashEquivalents()),
                b == null ? null : toDouble(b.getBalancesWithSbv()),
                b == null ? null : toDouble(b.getInterbankPlacementsAndLoans()),
                b == null ? null : toDouble(b.getTradingSecurities()),
                b == null ? null : toDouble(b.getInvestmentSecurities()),
                b == null ? null : toDouble(b.getLoansToCustomers()),
                null, // shortTermLoans
                null, // mediumLongTermLoans
                null, // personalLoans
                null, // corporateLoans
                // Balance sheet — liabilities & equity
                b == null ? null : toDouble(b.getGovAndSbvDebt()),
                b == null ? null : toDouble(b.getDepositsFromCustomers()),
                b == null ? null : toDouble(b.getConvertibleAndOtherPapers()),
                b == null ? null : toDouble(b.getTotalEquity()),
                b == null ? null : toDouble(b.getDepositsBorrowingsOthers()),
                b == null ? null : toDouble(b.getTotalLiabilities()),
                b == null ? null : toDouble(b.getTotalEquity()),
                b == null ? null : toDouble(b.getIssuingValuablePaper()),
                // Balance sheet — loan quality
                b == null ? null : toDouble(b.getCustomerLoan()),
                b == null ? null : toDouble(b.getStandardDebt()),
                b == null ? null : toDouble(b.getWatchlistDebt()),
                b == null ? null : toDouble(b.getSubstandardDebt()),
                b == null ? null : toDouble(b.getDoubtfulDebt()),
                b == null ? null : toDouble(b.getBadDebt()),
                b == null ? null : toDouble(b.getProvisionForCustomerLoanLoss()),
                // Indicators
                f == null ? null : toDouble(f.getRoe()),
                f == null ? null : toDouble(f.getRoa()),
                f == null ? null : toDouble(f.getNim()),
                f == null ? null : toDouble(f.getYoea()),
                f == null ? null : toDouble(f.getCof()),
                f == null ? null : toDouble(f.getCir()),
                f == null ? null : toDouble(f.getLdr()),
                f == null ? null : toDouble(f.getNplToLoan()),
                f == null ? null : toDouble(f.getLoanlossReservesToNPL()),
                f == null ? null : toDouble(f.getPe()),
                f == null ? null : toDouble(f.getPb()),
                f == null ? null : toDouble(f.getEps()),
                f == null ? null : toDouble(f.getBvps()),
                f == null ? null : toDouble(f.getSaleGrowth()),
                f == null ? null : toDouble(f.getProfitGrowth()),
                f == null ? null : toDouble(f.getPayoutRatio()),
                f == null ? null : toDouble(f.getCashDividend()),
                f == null ? null : toDouble(f.getShareAtPeriodEnd()),
                // Income statement
                netInterest,
                fee,
                other,
                profit,
                interestExpense,
                totalOperatingIncome,
                totalOperatingExpense,
                creditRiskProvisionsExpense,
                interestAndSimilarIncome
        );
    }
}
