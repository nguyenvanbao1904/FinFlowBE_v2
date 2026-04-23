package com.finflow.backend.investment.market_data.application.dto;

import java.util.List;

public record InvestmentAnalysisOutput(
        Overview overview,
        List<ShareholderPoint> shareholders,
        List<ValuationPoint> valuations,
        FinancialSeries financials,
        List<DividendPoint> dividends
) {
    public record Overview(
            String symbol,
            String companyName,
            String exchange,
            String companyType,
            String industryIcbCode,
            String industryLabel,
            String description,
            Double roe,
            Double roa,
            Double eps,
            Double bvps,
            Double cplh,
            Double currentPE,
            Double medianPE,
            Double meanPE,
            Double currentPB,
            Double medianPB,
            Double meanPB,
            Double currentPS,
            Double medianPS,
            Double meanPS,
            Double livePe,
            Double livePb,
            Double livePs,
            Double livePriceVnd,
            String livePriceSource
    ) {}

    public record ShareholderPoint(
            String name,
            Double percentage,
            Double quantity
    ) {}

    public record ValuationPoint(
            Integer year,
            Integer quarter,
            Double pe,
            Double pb,
            Double ps
    ) {}

    public record DailyValuationPoint(
            String date,
            Double pe,
            Double pb,
            Double ps
    ) {}

    public record FinancialSeries(
            String companyType,
            List<BankFinancialPoint> bank,
            List<NonBankFinancialPoint> nonBank
    ) {}

    public record BankFinancialPoint(
            Integer year,
            Integer quarter,
            Double cashAndEquivalents,
            Double depositsAtSBV,
            Double interbankPlacements,
            Double tradingSecurities,
            Double investmentSecurities,
            Double customerLoans,
            Double shortTermLoans,
            Double mediumLongTermLoans,
            Double personalLoans,
            Double corporateLoans,
            Double sbvBorrowings,
            Double customerDeposits,
            Double valuablePapers,
            Double equity,
            Double roe,
            Double roa,
            Double netInterestIncome,
            Double feeAndCommissionIncome,
            Double otherIncome,
            Double profitAfterTax,
            Double depositsBorrowingsOthers,
            Double totalLiabilities,
            Double interestExpense
    ) {}

    public record NonBankFinancialPoint(
            Integer year,
            Integer quarter,
            Double cashAndEquivalents,
            Double shortTermInvestments,
            Double shortTermReceivables,
            Double inventories,
            Double fixedAssets,
            Double longTermReceivables,
            Double totalAssets,
            Double equity,
            Double shortTermBorrowings,
            Double longTermBorrowings,
            Double advancesFromCustomers,
            Double totalCapital,
            Double roe,
            Double roa,
            Double netRevenue,
            Double profitAfterTax,
            Double grossMargin,
            Double netMargin,
            Double totalLiabilities,
            Double totalRevenue
    ) {}

    public record DividendPoint(
            String eventTitle,
            String eventType,
            String ratio,
            Double value,
            String recordDate,
            String exrightDate,
            String issueDate
    ) {}
}
