package com.finflow.backend.investment.portfolio.application.dto;

public record PortfolioMarketBenchmarkOutput(
        String benchmarkCode,
        MetricComparisonOutput pe,
        MetricComparisonOutput pb,
        MetricComparisonOutput ps,
        MetricComparisonOutput roe,
        MetricComparisonOutput roa
) {
    public record MetricComparisonOutput(
            Double portfolio,
            Double benchmark,
            Double deltaPct
    ) {}
}
