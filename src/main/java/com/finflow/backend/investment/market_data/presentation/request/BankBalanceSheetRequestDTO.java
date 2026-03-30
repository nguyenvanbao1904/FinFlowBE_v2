package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record BankBalanceSheetRequestDTO(
    @NotBlank(message = "REQUIRED_FIELD") String companyId,
    @NotNull(message = "REQUIRED_FIELD") Integer year,
    @NotNull(message = "REQUIRED_FIELD") Integer quarter,
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

    // --- NỢ CHI TIẾT ---
    BigDecimal totalLiabilities
) {}
