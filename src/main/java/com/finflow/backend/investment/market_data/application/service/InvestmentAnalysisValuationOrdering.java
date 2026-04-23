package com.finflow.backend.investment.market_data.application.service;

import com.finflow.backend.investment.market_data.application.dto.InvestmentAnalysisOutput;

import java.util.Comparator;

/**
 * Shared chronological ordering for valuation points (used by full analysis and partial valuation endpoints).
 */
public final class InvestmentAnalysisValuationOrdering {

    private InvestmentAnalysisValuationOrdering() {
    }

    public static final Comparator<InvestmentAnalysisOutput.ValuationPoint> VALUATION_ASC =
            Comparator.comparing(InvestmentAnalysisOutput.ValuationPoint::year, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(InvestmentAnalysisOutput.ValuationPoint::quarter, Comparator.nullsLast(Comparator.naturalOrder()));
}
