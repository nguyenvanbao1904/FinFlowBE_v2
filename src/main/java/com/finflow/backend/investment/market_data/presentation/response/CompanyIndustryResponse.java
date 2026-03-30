package com.finflow.backend.investment.market_data.presentation.response;

import lombok.Builder;

@Builder
public record CompanyIndustryResponse(
        String symbol,
        String industryLabel
) {
}
