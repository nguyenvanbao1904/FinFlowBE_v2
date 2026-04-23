package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

@Builder
public record CompanyIndustryOutput(
        String symbol,
        String industryLabel
) {}
