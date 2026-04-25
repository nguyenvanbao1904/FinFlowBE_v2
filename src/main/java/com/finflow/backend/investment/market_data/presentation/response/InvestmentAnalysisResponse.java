package com.finflow.backend.investment.market_data.presentation.response;

import java.util.List;

public record InvestmentAnalysisResponse(
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
            /** PE theo giá VPS gần nhất / EPS (TTM 4 quý gần nhất). */
            Double livePe,
            /** PB theo giá VPS gần nhất / BVPS (BCTC gần nhất). */
            Double livePb,
            /** P/S theo giá VPS / (mẫu TTM trên CP; NH = NII+phí+khác). */
            Double livePs,
            /** Giá VND dùng cho bội số live (VPS). */
            Double livePriceVnd,
            /** CLOSE (nguồn giá VPS). */
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

    /** Chuỗi P/E–P/B–P/S theo ngày giao dịch (Finfo + BCTC). */
    public record DailyValuationPoint(
            String date,
            Double pe,
            Double pb,
            Double ps
    ) {}

    public record FinancialSeries(
            String companyType,
            List<BankFinancialPoint> bank,
            List<NonBankFinancialPoint> nonBank,
            List<CashFlowPoint> cashFlows
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
            Double depositsBorrowingsOthers,
            Double totalLiabilities,
            Double totalEquity,
            Double issuingValuablePaper,
            Double customerLoan,
            Double standardDebt,
            Double watchlistDebt,
            Double substandardDebt,
            Double doubtfulDebt,
            Double badDebt,
            Double provisionForCustomerLoanLoss,
            Double roe,
            Double roa,
            Double nim,
            Double yoea,
            Double cof,
            Double cir,
            Double ldr,
            Double nplToLoan,
            Double loanlossReservesToNPL,
            Double pe,
            Double pb,
            Double eps,
            Double bvps,
            Double saleGrowth,
            Double profitGrowth,
            Double payoutRatio,
            Double cashDividend,
            Double shareAtPeriodEnd,
            Double netInterestIncome,
            Double feeAndCommissionIncome,
            Double otherIncome,
            Double profitAfterTax,
            Double interestExpense,
            Double totalOperatingIncome,
            Double totalOperatingExpense,
            Double creditRiskProvisionsExpense,
            Double interestAndSimilarIncome
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
            Double inProgressLongTermAsset,
            Double equity,
            Double shortTermBorrowings,
            Double longTermBorrowings,
            Double advancesFromCustomers,
            Double totalCapital,
            Double totalLiabilities,
            Double convertibleBond,
            Double roe,
            Double roa,
            Double grossMargin,
            Double netMargin,
            Double pe,
            Double pb,
            Double eps,
            Double bvps,
            Double saleGrowth,
            Double profitGrowth,
            Double currentRatio,
            Double totalDebtOverEquity,
            Double evOverEbitda,
            Double inventoryTurnover,
            Double payoutRatio,
            Double cashDividend,
            Double shareAtPeriodEnd,
            Double netRevenue,
            Double profitAfterTax,
            Double totalRevenue,
            Double grossProfit,
            Double costOfGoodsSold,
            Double sellingExpense,
            Double managingExpense
    ) {}

    public record CashFlowPoint(
            Integer year,
            Integer quarter,
            Double operatingCashflow,
            Double investingCashflow,
            Double financingCashflow
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
