package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CompanyShareholderRequestDTO(
        @NotBlank(message = "REQUIRED_FIELD")
        String companyId,

        @NotBlank(message = "REQUIRED_FIELD")
        String shareholderName,

        Long quantity,
        BigDecimal shareOwnPercent,
        LocalDate updateDate
) {
}
