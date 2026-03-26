package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record BankBalanceSheetRequestDTO(
    @NotBlank String companyId,
    @NotNull Integer year,
    @NotNull Integer quarter,
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
