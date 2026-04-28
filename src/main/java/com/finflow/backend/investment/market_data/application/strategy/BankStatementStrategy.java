package com.finflow.backend.investment.market_data.application.strategy;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentFinancialPointMapper;
import com.finflow.backend.investment.market_data.domain.entity.BankBalanceSheet;
import com.finflow.backend.investment.market_data.domain.entity.BankIncomeStatement;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.application.dto.InvestmentAnalysisOutput;

import java.util.*;

import static com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisNumberUtils.*;

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

        // --- Quarterly lookup maps for per-metric YoY ---
        Map<String, Double> qProfitMap = new HashMap<>();
        Map<String, Double> qCustomerLoanMap = new HashMap<>();
        Map<String, Double> qToiMap = new HashMap<>();
        Map<String, Double> qNplMap = new HashMap<>();

        for (BankIncomeStatement inc : incomes) {
            String key = inc.getYear() + "-" + inc.getQuarter();
            Double pat = toDouble(inc.getProfitAfterTax());
            if (pat != null) qProfitMap.put(key, pat);
            Double toi = sumNullableDoubles(
                    toDouble(inc.getNetInterestIncome()),
                    toDouble(inc.getNetFeeAndCommissionIncome()),
                    toDouble(inc.getNetOtherIncomeOrExpenses()));
            if (toi != null) qToiMap.put(key, toi);
        }
        for (BankBalanceSheet bs : balances) {
            String key = bs.getYear() + "-" + bs.getQuarter();
            Double cl = toDouble(bs.getCustomerLoan());
            if (cl != null) qCustomerLoanMap.put(key, cl);
            Double npl = sumNullableDoubles(
                    toDouble(bs.getSubstandardDebt()),
                    toDouble(bs.getDoubtfulDebt()),
                    toDouble(bs.getBadDebt()));
            if (npl != null) qNplMap.put(key, npl);
        }

        // --- Annual aggregate maps ---
        Map<Integer, Double> annualProfitMap = new HashMap<>();
        Map<Integer, Double> annualToiMap = new HashMap<>();
        Map<Integer, Double> annualCustomerLoanMap = new HashMap<>();
        Map<Integer, Double> annualNplMap = new HashMap<>();
        Map<Integer, Integer> annualQuarterCountMap = new HashMap<>();

        for (Integer year : years) {
            List<BankIncomeStatement> quarterIncomes = incGrouped.getOrDefault(year, List.of());
            int qCount = (int) quarterIncomes.stream()
                    .filter(qi -> qi.getProfitAfterTax() != null)
                    .count();
            annualQuarterCountMap.put(year, qCount);

            Double profit = sumBigDecimals(quarterIncomes, BankIncomeStatement::getProfitAfterTax);
            if (profit != null) annualProfitMap.put(year, profit);

            Double toi = sumNullableDoubles(
                    sumBigDecimals(quarterIncomes, BankIncomeStatement::getNetInterestIncome),
                    sumBigDecimals(quarterIncomes, BankIncomeStatement::getNetFeeAndCommissionIncome),
                    sumBigDecimals(quarterIncomes, BankIncomeStatement::getNetOtherIncomeOrExpenses));
            if (toi != null) annualToiMap.put(year, toi);

            BankBalanceSheet b = balByYear.get(year);
            if (b != null) {
                Double cl = toDouble(b.getCustomerLoan());
                if (cl != null) annualCustomerLoanMap.put(year, cl);
                Double npl = sumNullableDoubles(
                        toDouble(b.getSubstandardDebt()),
                        toDouble(b.getDoubtfulDebt()),
                        toDouble(b.getBadDebt()));
                if (npl != null) annualNplMap.put(year, npl);
            }
        }

        // --- Build points ---
        List<InvestmentAnalysisOutput.BankFinancialPoint> points = new ArrayList<>();
        for (Integer year : years) {
            BankBalanceSheet b = balByYear.get(year);
            FinancialIndicator f = indByYear.get(year);
            List<BankIncomeStatement> quarterIncomes = incGrouped.getOrDefault(year, List.of());
            int quarterCount = quarterIncomes.size();

            Double annualNetInterest = sumBigDecimals(quarterIncomes, BankIncomeStatement::getNetInterestIncome);
            Double annualFee = sumBigDecimals(quarterIncomes, BankIncomeStatement::getNetFeeAndCommissionIncome);
            Double annualOther = sumBigDecimals(quarterIncomes, BankIncomeStatement::getNetOtherIncomeOrExpenses);
            Double annualProfit = sumBigDecimals(quarterIncomes, BankIncomeStatement::getProfitAfterTax);
            Double annualInterestExpense = sumBigDecimals(quarterIncomes, BankIncomeStatement::getInterestExpense);
            Double annualTOI = sumBigDecimals(quarterIncomes, BankIncomeStatement::getTotalOperatingIncome);
            Double annualTOE = sumBigDecimals(quarterIncomes, BankIncomeStatement::getTotalOperatingExpense);
            Double annualProvision = sumBigDecimals(quarterIncomes, BankIncomeStatement::getCreditRiskProvisionsExpense);
            Double annualInterestIncome = sumBigDecimals(quarterIncomes, BankIncomeStatement::getInterestAndSimilarIncome);

            // Annual YoY — flow metrics require both years have 4 quarters
            int prevQCount = annualQuarterCountMap.getOrDefault(year - 1, 0);
            boolean fullBothYears = quarterCount == 4 && prevQCount == 4;

            Double annualYoY = fullBothYears ? computeYoY(annualProfit, annualProfitMap.get(year - 1)) : null;
            Double annualYoYToi = fullBothYears ? computeYoY(annualToiMap.get(year), annualToiMap.get(year - 1)) : null;
            // Balance-sheet snapshots — no quarterCount guard
            Double annualYoYCustomerLoan = computeYoY(annualCustomerLoanMap.get(year), annualCustomerLoanMap.get(year - 1));
            Double annualYoYNpl = computeYoY(annualNplMap.get(year), annualNplMap.get(year - 1));

            // Computed aggregates
            Double totalAssetsVal = computeBankTotalAssets(b);
            Double nplVal = computeBankNpl(b);

            points.add(makeBankPoint(year, 0, quarterCount, annualYoY,
                    totalAssetsVal, nplVal, annualYoYCustomerLoan, annualYoYToi, annualYoYNpl,
                    b, f, annualNetInterest, annualFee, annualOther, annualProfit,
                    annualInterestExpense, annualTOI, annualTOE, annualProvision, annualInterestIncome));

            for (BankIncomeStatement qi : quarterIncomes) {
                BankBalanceSheet qb = balances.stream()
                        .filter(bs -> bs.getYear() == year && bs.getQuarter() == qi.getQuarter())
                        .findFirst().orElse(b);

                FinancialIndicator qf = indicators.stream()
                        .filter(ind -> ind.getYear() == year && ind.getQuarter() == qi.getQuarter())
                        .findFirst().orElse(null);

                String prevKey = (year - 1) + "-" + qi.getQuarter();
                Double qYoY = computeYoY(toDouble(qi.getProfitAfterTax()), qProfitMap.get(prevKey));
                Double qYoYCustomerLoan = computeYoY(toDouble(qb.getCustomerLoan()), qCustomerLoanMap.get(prevKey));
                Double qToiCur = sumNullableDoubles(
                        toDouble(qi.getNetInterestIncome()),
                        toDouble(qi.getNetFeeAndCommissionIncome()),
                        toDouble(qi.getNetOtherIncomeOrExpenses()));
                Double qYoYToi = computeYoY(qToiCur, qToiMap.get(prevKey));
                Double qNplCur = computeBankNpl(qb);
                Double qYoYNpl = computeYoY(qNplCur, qNplMap.get(prevKey));

                points.add(makeBankPoint(year, qi.getQuarter(), 1, qYoY,
                        computeBankTotalAssets(qb), qNplCur, qYoYCustomerLoan, qYoYToi, qYoYNpl,
                        qb, qf,
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

    private Double computeBankTotalAssets(BankBalanceSheet b) {
        if (b == null) return null;
        return sumNullableDoubles(
                toDouble(b.getCashAndCashEquivalents()),
                toDouble(b.getBalancesWithSbv()),
                toDouble(b.getInterbankPlacementsAndLoans()),
                toDouble(b.getTradingSecurities()),
                toDouble(b.getInvestmentSecurities()),
                toDouble(b.getLoansToCustomers()));
    }

    private Double computeBankNpl(BankBalanceSheet b) {
        if (b == null) return null;
        return sumNullableDoubles(
                toDouble(b.getSubstandardDebt()),
                toDouble(b.getDoubtfulDebt()),
                toDouble(b.getBadDebt()));
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
            Integer quarterCount,
            Double yoyGrowth,
            Double totalAssets,
            Double npl,
            Double yoyCustomerLoan,
            Double yoyTotalOperatingIncome,
            Double yoyNpl,
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
                quarterCount,
                yoyGrowth,
                totalAssets,
                npl,
                yoyCustomerLoan,
                yoyTotalOperatingIncome,
                yoyNpl,
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
