package com.finflow.backend.investment.market_data.application.mapper;

import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Centralize DTO construction for financial-series points.
 * <p>
 * Strategy classes compute the numbers; this mapper creates the response DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InvestmentFinancialPointMapper {

    InvestmentAnalysisResponse.BankFinancialPoint toBankFinancialPoint(
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
    );

    InvestmentAnalysisResponse.NonBankFinancialPoint toNonBankFinancialPoint(
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
    );
}
