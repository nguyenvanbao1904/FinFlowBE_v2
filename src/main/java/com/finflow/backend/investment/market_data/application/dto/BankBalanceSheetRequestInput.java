package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BankBalanceSheetRequestInput(
        String companyId,
        Integer year,
        Integer quarter,
        BigDecimal cashAndEquivalents,
        BigDecimal depositsAtSBV,
        BigDecimal interbankPlacements,
        BigDecimal tradingSecurities,
        BigDecimal investmentSecurities,
        BigDecimal customerLoans,
        BigDecimal shortTermLoans,
        BigDecimal mediumLongTermLoans,
        BigDecimal personalLoans,
        BigDecimal corporateLoans,
        BigDecimal sbvBorrowings,
        BigDecimal customerDeposits,
        BigDecimal valuablePapers,
        BigDecimal equity,
        BigDecimal depositsBorrowingsOthers,
        BigDecimal totalLiabilities
) {}
