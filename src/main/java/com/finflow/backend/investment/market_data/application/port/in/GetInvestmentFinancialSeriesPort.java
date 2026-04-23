package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.application.dto.InvestmentFinancialSeriesOutput;
import com.finflow.backend.investment.market_data.application.query.GetInvestmentFinancialSeriesQuery;

public interface GetInvestmentFinancialSeriesPort {

    InvestmentFinancialSeriesOutput execute(GetInvestmentFinancialSeriesQuery query);
}
