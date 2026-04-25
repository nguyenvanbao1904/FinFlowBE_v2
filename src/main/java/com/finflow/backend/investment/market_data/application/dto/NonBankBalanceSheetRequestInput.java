package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record NonBankBalanceSheetRequestInput(
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
        BigDecimal totalLiabilities,
        BigDecimal inProgressLongTermAsset,
        BigDecimal convertibleBond
) {}
