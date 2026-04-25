package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record NonBankBalanceSheetRequestDTO(
    @NotBlank(message = "REQUIRED_FIELD") String companyId,
    @NotNull(message = "REQUIRED_FIELD") Integer year,
    @NotNull(message = "REQUIRED_FIELD") Integer quarter,
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
