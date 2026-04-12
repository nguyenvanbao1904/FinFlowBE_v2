package com.finflow.backend.investment.portfolio.application.result;

import java.util.List;

/**
 * Application-layer result for portfolio health computation.
 * Framework-free; mapped to HTTP response by the web adapter (controller).
 */
public record PortfolioHealthResult(
        int latestYear,
        int latestQuarter,
        CurrentSnapshot current,
        List<HistoryPoint> history
) {

    public record CurrentSnapshot(
            double totalValueClose,
            double stockValueClose,
            double cashBalance,
            Double pe,
            Double pb,
            Double ps,
            String priceType
    ) {}

    public record HistoryPoint(
            int year,
            int quarter,
            Double pe,
            Double pb,
            Double ps,
            Double roe,
            Double roa,
            double coverage
    ) {}
}
