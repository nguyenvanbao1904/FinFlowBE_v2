package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CompanyRequestDTO(
        @NotBlank(message = "SYSTEM_ERROR")
        String id,

        @NotBlank(message = "SYSTEM_ERROR")
        String exchange,

        String industry,
        
        String companyName,

        @NotBlank(message = "SYSTEM_ERROR")
        String companyType // BANK or NON_BANK
) {
}
