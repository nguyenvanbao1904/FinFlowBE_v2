package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;

public interface GetInvestmentFinancialSeriesPort {

    InvestmentAnalysisResponse.FinancialSeries execute(String symbol, Integer annualLimit, Integer quarterlyLimit);
}
