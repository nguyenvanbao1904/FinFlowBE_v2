package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * Một nút trên cây ngành. {@code id} do crawler sinh (UUID v5 theo mã ICB) để idempotent.
 */
@Builder
public record IndustryNodeRequestDTO(
        @NotNull(message = "SYSTEM_ERROR")
        UUID id,

        UUID parentId,

        @NotBlank(message = "SYSTEM_ERROR")
        String nameVi,

        @NotNull(message = "SYSTEM_ERROR")
        Integer level,

        String icbCode,

        String detailLabel
) {
}
