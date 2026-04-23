package com.finflow.backend.investment.market_data.api;

import java.util.Collection;
import java.util.List;

/**
 * Public read contract exposed by market_data submodule.
 */
public interface MarketIndicatorReadApi {
    List<MarketIndicatorData> findAllByCompanyIds(Collection<String> companyIds);
}
