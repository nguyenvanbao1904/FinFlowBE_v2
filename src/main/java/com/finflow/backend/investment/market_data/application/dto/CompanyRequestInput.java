package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

@Builder
public record CompanyRequestInput(
        String id,
        String exchange,
        String industryNodeId,
        String industryIcbCode,
        String companyName,
        String description,
        String companyType
) {}
