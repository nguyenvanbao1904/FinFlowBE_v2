package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;

import java.util.List;

public interface GetInvestmentValuationsPort {

    List<InvestmentAnalysisResponse.ValuationPoint> execute(
            String symbol,
            Integer annualLimit,
            String startDate,
            String endDate,
            Boolean showQuarterly
    );
}
