package com.finflow.backend.finance.dashboard.presentation.response;

import lombok.Builder;

import java.util.List;

@Builder
public record HomeInsightResponse(
        String title,
        String message,
        List<String> warnings,
        boolean cached
) {
}
