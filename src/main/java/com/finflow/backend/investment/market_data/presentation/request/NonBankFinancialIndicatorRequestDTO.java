package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record NonBankFinancialIndicatorRequestDTO(
        @NotBlank String companyId,
        @NotNull Integer year,
        @NotNull Integer quarter,
        BigDecimal pe,
        BigDecimal pb,
        BigDecimal ps,
        BigDecimal roe,
        BigDecimal roa,
        BigDecimal eps,
        BigDecimal bvps,
        BigDecimal cplh,
        BigDecimal lng,
        BigDecimal lnr
) {}
