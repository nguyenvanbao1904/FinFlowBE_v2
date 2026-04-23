package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.query.GetInvestmentFullAnalysisQuery;

import com.finflow.backend.investment.market_data.application.dto.InvestmentAnalysisOutput;

public interface GetInvestmentFullAnalysisPort {

    InvestmentAnalysisOutput execute(GetInvestmentFullAnalysisQuery query);
}
