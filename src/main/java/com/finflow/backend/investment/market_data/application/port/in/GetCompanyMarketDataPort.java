package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.query.GetCompanyMarketDataQuery;

import com.finflow.backend.investment.market_data.application.dto.CompanyMarketDataOutput;

public interface GetCompanyMarketDataPort {

    CompanyMarketDataOutput execute(GetCompanyMarketDataQuery query);
}
