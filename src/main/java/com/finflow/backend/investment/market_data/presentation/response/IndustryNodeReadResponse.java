package com.finflow.backend.investment.market_data.presentation.response;

import java.util.UUID;

public record IndustryNodeReadResponse(
        UUID id,
        UUID parentId,
        Integer level,
        String nameVi,
        String icbCode,
        String detailLabel
) {
}
