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
        @NotNull(message = "REQUIRED_FIELD")
        UUID id,

        UUID parentId,

        @NotBlank(message = "REQUIRED_FIELD")
        String nameVi,

        @NotNull(message = "REQUIRED_FIELD")
        Integer level,

        String icbCode,

        String detailLabel
) {
}
