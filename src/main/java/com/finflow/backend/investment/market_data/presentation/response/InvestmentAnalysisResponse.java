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
            /** Tổng tài sản theo BCTC (cột total_assets); dùng để tính phần "khác" = total − các khoản chi tiết đã có. */
            Double totalAssets,
            Double equity,
            Double shortTermBorrowings,
            Double longTermBorrowings,
            Double advancesFromCustomers,
            /** Tổng nguồn vốn theo BCTC (cột total_capital); dùng tính phần khác = total − các khoản chi tiết. */
            Double totalCapital,
            Double roe,
            Double roa,
            Double netRevenue,
            Double profitAfterTax,
            /** Biên LN gộp (%) — `gross_margin` / lng trong DB; không suy từ LNST/DT. */
            Double grossMargin,
            /** Biên LN ròng (%) — `net_margin` / lnr; có thể suy từ LNST/DT khi thiếu trong DB. */
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
