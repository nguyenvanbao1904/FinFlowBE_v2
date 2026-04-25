package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BankBalanceSheetRequestInput(
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
        BigDecimal totalLiabilities,
        BigDecimal customerLoan,
        BigDecimal standardDebt,
        BigDecimal watchlistDebt,
        BigDecimal substandardDebt,
        BigDecimal doubtfulDebt,
        BigDecimal badDebt,
        BigDecimal provisionForCustomerLoanLoss,
        BigDecimal issuingValuablePaper,
        BigDecimal totalEquity
) {}
