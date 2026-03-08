package com.finflow.backend.transaction.presentation.response;

import java.util.List;

public record TransactionChartResponse(
        List<ChartDataPoint> dataPoints,
        String periodLabel,
        boolean hasNext
) {
    public record ChartDataPoint(
            String label,
            double income,
            double expense
    ) {}
}
