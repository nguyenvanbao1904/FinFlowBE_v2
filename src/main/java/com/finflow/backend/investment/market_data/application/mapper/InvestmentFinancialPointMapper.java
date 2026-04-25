package com.finflow.backend.investment.market_data.application.mapper;

import com.finflow.backend.investment.market_data.application.dto.InvestmentAnalysisOutput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InvestmentFinancialPointMapper {

    InvestmentAnalysisOutput.BankFinancialPoint toBankFinancialPoint(
            Integer year,
            Integer quarter,
            // Balance sheet — assets
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
            // Balance sheet — liabilities & equity
            Double sbvBorrowings,
            Double customerDeposits,
            Double valuablePapers,
            Double equity,
            Double depositsBorrowingsOthers,
            Double totalLiabilities,
            Double totalEquity,
            Double issuingValuablePaper,
            // Balance sheet — loan quality
            Double customerLoan,
            Double standardDebt,
            Double watchlistDebt,
            Double substandardDebt,
            Double doubtfulDebt,
            Double badDebt,
            Double provisionForCustomerLoanLoss,
            // Indicators
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
            // Income statement
            Double netInterestIncome,
            Double feeAndCommissionIncome,
            Double otherIncome,
            Double profitAfterTax,
            Double interestExpense,
            Double totalOperatingIncome,
            Double totalOperatingExpense,
            Double creditRiskProvisionsExpense,
            Double interestAndSimilarIncome
    );

    InvestmentAnalysisOutput.NonBankFinancialPoint toNonBankFinancialPoint(
            Integer year,
            Integer quarter,
            // Balance sheet — assets
            Double cashAndEquivalents,
            Double shortTermInvestments,
            Double shortTermReceivables,
            Double inventories,
            Double fixedAssets,
            Double longTermReceivables,
            Double totalAssets,
            Double inProgressLongTermAsset,
            // Balance sheet — liabilities & equity
            Double equity,
            Double shortTermBorrowings,
            Double longTermBorrowings,
            Double advancesFromCustomers,
            Double totalCapital,
            Double totalLiabilities,
            Double convertibleBond,
            // Indicators
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
            // Income statement
            Double netRevenue,
            Double profitAfterTax,
            Double totalRevenue,
            Double grossProfit,
            Double costOfGoodsSold,
            Double sellingExpense,
            Double managingExpense
    );
}
