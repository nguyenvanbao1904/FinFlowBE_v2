package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CompanyDividendRequestDTO(
        @NotBlank(message = "SYSTEM_ERROR")
        String companyId,

        @NotBlank(message = "SYSTEM_ERROR")
        String eventTitle,

        @NotBlank(message = "SYSTEM_ERROR")
        String eventType, // CASH or STOCK

        String ratio,
        BigDecimal value,
        LocalDate recordDate,
        LocalDate exrightDate,
        LocalDate issueDate
) {
}
