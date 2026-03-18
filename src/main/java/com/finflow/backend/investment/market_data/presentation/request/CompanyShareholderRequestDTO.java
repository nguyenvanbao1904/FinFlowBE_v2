package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CompanyShareholderRequestDTO(
        @NotBlank(message = "SYSTEM_ERROR")
        String companyId,

        @NotBlank(message = "SYSTEM_ERROR")
        String shareholderName,

        Long quantity,
        BigDecimal shareOwnPercent,
        LocalDate updateDate
) {
}
