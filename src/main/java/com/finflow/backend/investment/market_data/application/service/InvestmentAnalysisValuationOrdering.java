package com.finflow.backend.investment.market_data.application.service;

import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;

import java.util.Comparator;

/**
 * Shared chronological ordering for valuation points (used by full analysis and partial valuation endpoints).
 */
public final class InvestmentAnalysisValuationOrdering {

    private InvestmentAnalysisValuationOrdering() {
    }

    public static final Comparator<InvestmentAnalysisResponse.ValuationPoint> VALUATION_ASC =
            Comparator.comparing(InvestmentAnalysisResponse.ValuationPoint::year, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(InvestmentAnalysisResponse.ValuationPoint::quarter, Comparator.nullsLast(Comparator.naturalOrder()));
}
