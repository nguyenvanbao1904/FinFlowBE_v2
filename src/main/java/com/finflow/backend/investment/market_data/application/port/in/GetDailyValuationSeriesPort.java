package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;

import java.util.List;

public interface GetDailyValuationSeriesPort {

    List<InvestmentAnalysisResponse.DailyValuationPoint> execute(String symbol, String startDate, String endDate);
}
