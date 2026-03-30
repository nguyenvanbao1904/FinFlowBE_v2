package com.finflow.backend.investment.portfolio.presentation.response;

/**
 * Portfolio metrics compared against market benchmark (e.g., VNINDEX).
 */
public record PortfolioMarketBenchmarkResponse(
        String benchmarkCode,
        MetricComparison pe,
        MetricComparison pb,
        MetricComparison ps,
        MetricComparison roe,
        MetricComparison roa
) {
    public record MetricComparison(
            Double portfolio,
            Double benchmark,
            Double deltaPct
    ) {}
}

