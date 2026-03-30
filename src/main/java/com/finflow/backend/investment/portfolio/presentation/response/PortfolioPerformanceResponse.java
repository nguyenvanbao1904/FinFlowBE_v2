package com.finflow.backend.investment.portfolio.presentation.response;

import java.time.LocalDate;
import java.util.List;

public record PortfolioPerformanceResponse(
        LocalDate rangeStart,
        LocalDate rangeEnd,
        String benchmarkCode,
        List<PerformanceSeriesPointResponse> portfolioPoints,
        List<PerformanceSeriesPointResponse> benchmarkPoints
) {}
