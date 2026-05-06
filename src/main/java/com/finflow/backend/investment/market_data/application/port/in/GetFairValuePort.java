package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.response.FairValueResponse;

public interface GetFairValuePort {
    FairValueResponse execute(String symbol, Integer targetYear);
}
