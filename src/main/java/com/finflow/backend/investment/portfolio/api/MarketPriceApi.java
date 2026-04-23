package com.finflow.backend.investment.portfolio.api;

import java.util.List;
import java.util.Map;

public interface MarketPriceApi {

    Map<String, MarketPriceQuote> getClosePrices(List<String> symbols);
}
