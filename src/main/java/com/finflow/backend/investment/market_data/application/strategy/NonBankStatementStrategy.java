package com.finflow.backend.investment.market_data.application.strategy;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentFinancialPointMapper;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.domain.entity.NonBankBalanceSheet;
import com.finflow.backend.investment.market_data.domain.entity.NonBankIncomeStatement;
import com.finflow.backend.investment.market_data.application.dto.InvestmentAnalysisOutput;

import java.util.*;

import static com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisNumberUtils.*;

public class NonBankStatementStrategy {
    private final InvestmentFinancialPointMapper pointMapper;

    public NonBankStatementStrategy(InvestmentFinancialPointMapper pointMapper) {
        this.pointMapper = pointMapper;
    }

    public List<InvestmentAnalysisOutput.NonBankFinancialPoint> buildPoints(
            List<NonBankBalanceSheet> balances,
            List<NonBankIncomeStatement> incomes,
            List<FinancialIndicator> indicators,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        Map<Integer, NonBankBalanceSheet> balByYear = keepLatestQuarterByYear(
                balances, NonBankBalanceSheet::getYear, NonBankBalanceSheet::getQuarter);
        Map<Integer, FinancialIndicator> indByYear = keepLatestQuarterByYear(
                indicators, FinancialIndicator::getYear, FinancialIndicator::getQuarter);

        Map<Integer, List<NonBankIncomeStatement>> incGrouped = new HashMap<>();
        for (NonBankIncomeStatement inc : incomes) {
            incGrouped.computeIfAbsent(inc.getYear(), k -> new ArrayList<>()).add(inc);
        }

        Set<Integer> allYears = new HashSet<>();
        allYears.addAll(balByYear.keySet());
        allYears.addAll(indByYear.keySet());
        allYears.addAll(incGrouped.keySet());
        List<Integer> years = allYears.stream().sorted().toList();

        // --- Quarterly lookup maps for per-metric YoY ---
        Map<String, Double> qProfitMap = new HashMap<>();
        Map<String, Double> qRevenueMap = new HashMap<>();
        Map<String, Double> qInventoryMap = new HashMap<>();

        for (NonBankIncomeStatement inc : incomes) {
            String key = inc.getYear() + "-" + inc.getQuarter();
            Double pat = toDouble(inc.getProfitAfterTax());
            if (pat != null) qProfitMap.put(key, pat);
            Double rev = toDouble(inc.getNetRevenue());
            if (rev != null) qRevenueMap.put(key, rev);
        }
        for (NonBankBalanceSheet bs : balances) {
            String key = bs.getYear() + "-" + bs.getQuarter();
            Double inv = toDouble(bs.getInventories());
            if (inv != null) qInventoryMap.put(key, inv);
        }

        // --- Annual aggregate maps ---
        Map<Integer, Double> annualProfitMap = new HashMap<>();
        Map<Integer, Double> annualRevenueMap = new HashMap<>();
        Map<Integer, Double> annualInventoryMap = new HashMap<>();
        Map<Integer, Integer> annualQuarterCountMap = new HashMap<>();

        for (Integer year : years) {
            List<NonBankIncomeStatement> quarterIncomes = incGrouped.getOrDefault(year, List.of());
            int qCount = (int) quarterIncomes.stream()
                    .filter(qi -> qi.getProfitAfterTax() != null)
                    .count();
            annualQuarterCountMap.put(year, qCount);

            Double profit = sumBigDecimals(quarterIncomes, NonBankIncomeStatement::getProfitAfterTax);
            if (profit != null) annualProfitMap.put(year, profit);

            Double revenue = sumBigDecimals(quarterIncomes, NonBankIncomeStatement::getNetRevenue);
            if (revenue != null) annualRevenueMap.put(year, revenue);

            // Inventories — balance sheet snapshot, use latest quarter
            NonBankBalanceSheet b = balByYear.get(year);
            if (b != null) {
                Double inv = toDouble(b.getInventories());
                if (inv != null) annualInventoryMap.put(year, inv);
            }
        }

        // --- Build points ---
        List<InvestmentAnalysisOutput.NonBankFinancialPoint> points = new ArrayList<>();
        for (Integer year : years) {
            NonBankBalanceSheet b = balByYear.get(year);
            FinancialIndicator f = indByYear.get(year);
            List<NonBankIncomeStatement> quarterIncomes = incGrouped.getOrDefault(year, List.of());
            int quarterCount = quarterIncomes.size();

            Double annualNetRevenue = sumBigDecimals(quarterIncomes, NonBankIncomeStatement::getNetRevenue);
            Double annualProfit = sumBigDecimals(quarterIncomes, NonBankIncomeStatement::getProfitAfterTax);
            Double annualTotalRevenue = sumBigDecimals(quarterIncomes, NonBankIncomeStatement::getTotalRevenue);
            Double annualGrossProfit = sumBigDecimals(quarterIncomes, NonBankIncomeStatement::getGrossProfit);
            Double annualCOGS = sumBigDecimals(quarterIncomes, NonBankIncomeStatement::getCostOfGoodsSold);
            Double annualSelling = sumBigDecimals(quarterIncomes, NonBankIncomeStatement::getSellingExpense);
            Double annualManaging = sumBigDecimals(quarterIncomes, NonBankIncomeStatement::getManagingExpense);

            Double grossMargin = f == null ? null : toDouble(f.getLng());
            Double netMargin = f == null ? null : toDouble(f.getLnr());

            // Annual YoY — flow metrics require both years have 4 quarters
            int prevQCount = annualQuarterCountMap.getOrDefault(year - 1, 0);
            boolean fullBothYears = quarterCount == 4 && prevQCount == 4;

            Double annualYoY = fullBothYears ? computeYoY(annualProfit, annualProfitMap.get(year - 1)) : null;
            Double annualYoYRevenue = fullBothYears ? computeYoY(annualNetRevenue, annualRevenueMap.get(year - 1)) : null;
            // Inventories — balance sheet snapshot, no quarterCount guard
            Double annualYoYInventory = computeYoY(annualInventoryMap.get(year), annualInventoryMap.get(year - 1));

            points.add(makeNonBankPoint(year, 0, quarterCount, annualYoY,
                    annualYoYRevenue, annualYoYInventory,
                    b, f, annualNetRevenue, annualProfit, grossMargin, netMargin,
                    annualTotalRevenue, annualGrossProfit, annualCOGS, annualSelling, annualManaging));

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

                String prevKey = (year - 1) + "-" + qi.getQuarter();
                Double qYoY = computeYoY(qProfit, qProfitMap.get(prevKey));
                Double qYoYRevenue = computeYoY(qNetRevenue, qRevenueMap.get(prevKey));
                Double qYoYInventory = computeYoY(toDouble(qb.getInventories()), qInventoryMap.get(prevKey));

                points.add(makeNonBankPoint(year, qi.getQuarter(), 1, qYoY,
                        qYoYRevenue, qYoYInventory,
                        qb, qf, qNetRevenue, qProfit, qGross, qNet,
                        toDouble(qi.getTotalRevenue()), toDouble(qi.getGrossProfit()),
                        toDouble(qi.getCostOfGoodsSold()), toDouble(qi.getSellingExpense()),
                        toDouble(qi.getManagingExpense())));
            }
        }

        return applyFinancialLimitsNonBank(points, annualLimit, quarterlyLimit);
    }

    private List<InvestmentAnalysisOutput.NonBankFinancialPoint> applyFinancialLimitsNonBank(
            List<InvestmentAnalysisOutput.NonBankFinancialPoint> points,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        List<InvestmentAnalysisOutput.NonBankFinancialPoint> annual = points.stream()
                .filter(p -> p.quarter() != null && p.quarter() == 0)
                .sorted(Comparator.comparing(InvestmentAnalysisOutput.NonBankFinancialPoint::year, Comparator.reverseOrder()))
                .limit(normalizeLimit(annualLimit))
                .toList();

        List<InvestmentAnalysisOutput.NonBankFinancialPoint> quarterly = points.stream()
                .filter(p -> p.quarter() != null && p.quarter() > 0)
                .sorted(
                        Comparator.comparing(InvestmentAnalysisOutput.NonBankFinancialPoint::year, Comparator.reverseOrder())
                                .thenComparing(InvestmentAnalysisOutput.NonBankFinancialPoint::quarter, Comparator.reverseOrder())
                )
                .limit(normalizeLimit(quarterlyLimit))
                .toList();

        List<InvestmentAnalysisOutput.NonBankFinancialPoint> merged = new ArrayList<>(annual.size() + quarterly.size());
        merged.addAll(annual);
        merged.addAll(quarterly);
        merged.sort(
                Comparator.comparing(InvestmentAnalysisOutput.NonBankFinancialPoint::year, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(InvestmentAnalysisOutput.NonBankFinancialPoint::quarter, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        return merged;
    }

    private InvestmentAnalysisOutput.NonBankFinancialPoint makeNonBankPoint(
            Integer year,
            Integer quarter,
            Integer quarterCount,
            Double yoyGrowth,
            Double yoyNetRevenue,
            Double yoyInventories,
            NonBankBalanceSheet b,
            FinancialIndicator f,
            Double netRevenue,
            Double profit,
            Double grossMargin,
            Double netMargin,
            Double totalRevenue,
            Double grossProfit,
            Double costOfGoodsSold,
            Double sellingExpense,
            Double managingExpense
    ) {
        return pointMapper.toNonBankFinancialPoint(
                year,
                quarter,
                quarterCount,
                yoyGrowth,
                yoyNetRevenue,
                yoyInventories,
                // Balance sheet — assets
                b == null ? null : toDouble(b.getCashAndCashEquivalents()),
                b == null ? null : toDouble(b.getShortTermInvestments()),
                b == null ? null : toDouble(b.getShortTermReceivables()),
                b == null ? null : toDouble(b.getInventories()),
                b == null ? null : toDouble(b.getFixedAssets()),
                b == null ? null : toDouble(b.getLongTermReceivables()),
                b == null ? null : toDouble(b.getTotalAssets()),
                b == null ? null : toDouble(b.getInProgressLongTermAsset()),
                // Balance sheet — liabilities & equity
                b == null ? null : toDouble(b.getEquity()),
                b == null ? null : toDouble(b.getShortTermBorrowings()),
                b == null ? null : toDouble(b.getLongTermBorrowings()),
                b == null ? null : toDouble(b.getAdvancesFromCustomers()),
                b == null ? null : toDouble(b.getTotalCapital()),
                b == null ? null : toDouble(b.getTotalLiabilities()),
                b == null ? null : toDouble(b.getConvertibleBond()),
                // Indicators
                f == null ? null : toDouble(f.getRoe()),
                f == null ? null : toDouble(f.getRoa()),
                grossMargin,
                netMargin,
                f == null ? null : toDouble(f.getPe()),
                f == null ? null : toDouble(f.getPb()),
                f == null ? null : toDouble(f.getEps()),
                f == null ? null : toDouble(f.getBvps()),
                f == null ? null : toDouble(f.getSaleGrowth()),
                f == null ? null : toDouble(f.getProfitGrowth()),
                f == null ? null : toDouble(f.getCurrentRatio()),
                f == null ? null : toDouble(f.getTotalDebtOverEquity()),
                f == null ? null : toDouble(f.getEvOverEbitda()),
                f == null ? null : toDouble(f.getInventoryTurnover()),
                f == null ? null : toDouble(f.getPayoutRatio()),
                f == null ? null : toDouble(f.getCashDividend()),
                f == null ? null : toDouble(f.getShareAtPeriodEnd()),
                // Income statement
                netRevenue,
                profit,
                totalRevenue,
                grossProfit,
                costOfGoodsSold,
                sellingExpense,
                managingExpense
        );
    }
}
