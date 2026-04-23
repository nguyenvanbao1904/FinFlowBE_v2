package com.finflow.backend.investment.portfolio.api;

import java.util.List;
import java.util.Map;

/**
 * Public stock-ratio contract exposed by portfolio submodule.
 */
public interface StockRatiosApi {

    Map<String, Double> getLatestRatios(String code, List<String> itemCodes);
}
