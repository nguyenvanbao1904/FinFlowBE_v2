package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CompanyRequestDTO(
        @NotBlank(message = "REQUIRED_FIELD")
        String id,

        @NotBlank(message = "REQUIRED_FIELD")
        String exchange,

        /** UUID nút ngành (nút lá) — ưu tiên hơn {@code industryIcbCode}. */
        String industryNodeId,

        /** Mã ICB trên hồ sơ công ty — backend resolve → {@code industry_node_id} sau khi đã sync cây. */
        String industryIcbCode,
        
        String companyName,

        String description,

        @NotBlank(message = "REQUIRED_FIELD")
        String companyType // BANK or NON_BANK
) {
}
