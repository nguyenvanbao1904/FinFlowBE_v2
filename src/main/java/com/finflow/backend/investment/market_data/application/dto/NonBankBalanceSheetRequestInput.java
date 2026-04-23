package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record NonBankBalanceSheetRequestInput(
        String companyId,
        Integer year,
        Integer quarter,
        BigDecimal cashAndEquivalents,
        BigDecimal shortTermInvestments,
        BigDecimal shortTermReceivables,
        BigDecimal inventories,
        BigDecimal fixedAssets,
        BigDecimal longTermReceivables,
        BigDecimal totalAssets,
        BigDecimal equity,
        BigDecimal shortTermBorrowings,
        BigDecimal longTermBorrowings,
        BigDecimal advancesFromCustomers,
        BigDecimal totalCapital,
        BigDecimal totalLiabilities
) {}
