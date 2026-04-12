package com.finflow.backend.investment.market_data.presentation.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CompanyMarketDataResponse(
        String symbol,
        String companyType,
        List<String> includedSections,
        CompanyData company,
        List<ShareholderData> shareholders,
        List<DividendData> dividends,
        List<FinancialIndicatorData> financialIndicators,
        List<BankBalanceSheetData> bankBalanceSheets,
        List<NonBankBalanceSheetData> nonBankBalanceSheets,
        List<BankIncomeStatementData> bankIncomeStatements,
        List<NonBankIncomeStatementData> nonBankIncomeStatements
) {
    public record CompanyData(
            String id,
            String exchange,
            String companyName,
            String description,
            String companyType,
            UUID industryNodeId,
            UUID industryParentId,
            Integer industryLevel,
            String industryIcbCode,
            String industryNameVi,
            String industryDetailLabel
    ) {}

    public record ShareholderData(
            UUID id,
            String companyId,
            String shareholderName,
            Long quantity,
            BigDecimal shareOwnPercent,
            LocalDate updateDate
    ) {}

    public record DividendData(
            UUID id,
            String companyId,
            String eventTitle,
            String eventType,
            String ratio,
            BigDecimal value,
            LocalDate recordDate,
            LocalDate exrightDate,
            LocalDate issueDate
    ) {}

    public record FinancialIndicatorData(
            UUID id,
            String companyId,
            Integer year,
            Integer quarter,
            BigDecimal pe,
            BigDecimal pb,
            BigDecimal ps,
            BigDecimal roe,
            BigDecimal roa,
            BigDecimal eps,
            BigDecimal bvps,
            BigDecimal lng,
            BigDecimal lnr,
            BigDecimal cplh
    ) {}

    public record BankBalanceSheetData(
            UUID id,
            String companyId,
            Integer year,
            Integer quarter,
            BigDecimal cashAndCashEquivalents,
            BigDecimal totalAssets,
            BigDecimal equity,
            BigDecimal totalCapital,
            BigDecimal balancesWithSbv,
            BigDecimal interbankPlacementsAndLoans,
            BigDecimal tradingSecurities,
            BigDecimal investmentSecurities,
            BigDecimal loansToCustomers,
            BigDecimal govAndSbvDebt,
            BigDecimal depositsBorrowingsOthers,
            BigDecimal depositsFromCustomers,
            BigDecimal convertibleAndOtherPapers,
            BigDecimal totalLiabilities
    ) {}

    public record NonBankBalanceSheetData(
            UUID id,
            String companyId,
            Integer year,
            Integer quarter,
            BigDecimal cashAndCashEquivalents,
            BigDecimal totalAssets,
            BigDecimal equity,
            BigDecimal totalCapital,
            BigDecimal shortTermInvestments,
            BigDecimal shortTermReceivables,
            BigDecimal longTermReceivables,
            BigDecimal inventories,
            BigDecimal fixedAssets,
            BigDecimal shortTermBorrowings,
            BigDecimal longTermBorrowings,
            BigDecimal advancesFromCustomers,
            BigDecimal totalLiabilities
    ) {}

    public record BankIncomeStatementData(
            UUID id,
            String companyId,
            Integer year,
            Integer quarter,
            BigDecimal profitAfterTax,
            BigDecimal interestExpense,
            BigDecimal netInterestIncome,
            BigDecimal netFeeAndCommissionIncome,
            BigDecimal netOtherIncomeOrExpenses,
            BigDecimal netProfit
    ) {}

    public record NonBankIncomeStatementData(
            UUID id,
            String companyId,
            Integer year,
            Integer quarter,
            BigDecimal profitAfterTax,
            BigDecimal netRevenue,
            BigDecimal totalRevenue,
            BigDecimal netProfit
    ) {}
}
