package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record BankFinancialIndicatorRequestDTO(
        @NotBlank(message = "REQUIRED_FIELD") String companyId,
        @NotNull(message = "REQUIRED_FIELD") Integer year,
        @NotNull(message = "REQUIRED_FIELD") Integer quarter,
        BigDecimal pe,
        BigDecimal pb,
        BigDecimal ps,
        BigDecimal roe,
        BigDecimal roa,
        BigDecimal eps,
        BigDecimal bvps,
        BigDecimal cplh,
        BigDecimal saleGrowth,
        BigDecimal profitGrowth,
        BigDecimal currentRatio,
        BigDecimal totalDebtOverEquity,
        BigDecimal evOverEbitda,
        BigDecimal inventoryTurnover,
        BigDecimal payoutRatio,
        BigDecimal cashDividend,
        BigDecimal shareAtPeriodEnd,
        BigDecimal nim,
        BigDecimal yoea,
        BigDecimal cof,
        BigDecimal cir,
        BigDecimal ldr,
        BigDecimal nplToLoan,
        BigDecimal loanlossReservesToNPL
) {}
