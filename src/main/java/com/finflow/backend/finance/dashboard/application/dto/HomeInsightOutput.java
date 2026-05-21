package com.finflow.backend.finance.dashboard.application.dto;

import java.util.List;

public record HomeInsightOutput(
        String title,
        String message,
        List<String> warnings,
        boolean cached
) {
}
