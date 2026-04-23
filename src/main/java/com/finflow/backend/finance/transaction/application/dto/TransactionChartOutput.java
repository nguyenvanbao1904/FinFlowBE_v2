package com.finflow.backend.finance.transaction.application.dto;

import java.util.List;

public record TransactionChartOutput(
        List<ChartPointOutput> dataPoints,
        String periodLabel,
        boolean hasNext
) {
    public record ChartPointOutput(
            String label,
            double income,
            double expense
    ) {}
}
