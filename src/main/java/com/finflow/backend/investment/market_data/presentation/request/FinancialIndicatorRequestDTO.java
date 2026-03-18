package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FinancialIndicatorRequestDTO(
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
    BigDecimal lng,
    BigDecimal lnr,
    BigDecimal cplh
) {}
