package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record IndustryNodeRequestInput(
        UUID id,
        UUID parentId,
        String nameVi,
        Integer level,
        String icbCode,
        String detailLabel
) {}
