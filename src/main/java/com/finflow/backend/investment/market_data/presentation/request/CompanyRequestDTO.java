package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CompanyRequestDTO(
        @NotBlank(message = "SYSTEM_ERROR")
        String id,

        @NotBlank(message = "SYSTEM_ERROR")
        String exchange,

        /** UUID nút ngành (nút lá) — ưu tiên hơn {@code industryIcbCode}. */
        String industryNodeId,

        /** Mã ICB trên hồ sơ công ty — backend resolve → {@code industry_node_id} sau khi đã sync cây. */
        String industryIcbCode,
        
        String companyName,

        String description,

        @NotBlank(message = "SYSTEM_ERROR")
        String companyType // BANK or NON_BANK
) {
}
