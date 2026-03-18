package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record NonBankBalanceSheetRequestDTO(
    @NotBlank String companyId,
    @NotNull Integer year,
    @NotNull Integer quarter,
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
    BigDecimal advancesFromCustomers
) {}
