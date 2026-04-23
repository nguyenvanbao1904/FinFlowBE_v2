package com.finflow.backend.investment.portfolio.application.dto;

import java.util.List;

/**
 * Application-layer output for portfolio health computation.
 */
public record PortfolioHealthOutput(
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
