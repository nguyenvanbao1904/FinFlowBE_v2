package com.finflow.backend.investment.market_data.application.dto;

import java.util.UUID;

public record IndustryNodeReadOutput(
        UUID id,
        UUID parentId,
        Integer level,
        String nameVi,
        String icbCode,
        String detailLabel
) {}
