package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.response.CompanyMarketDataResponse;

import java.util.List;

public interface GetCompanyMarketDataPort {

    CompanyMarketDataResponse execute(
            String symbol,
            List<String> includes,
            Integer annualLimit,
            Integer quarterlyLimit
    );
}
