package com.finflow.backend.investment.portfolio.api;

/**
 * Market price of a stock symbol returned by a market data provider.
 * Application-layer model — independent of any infrastructure SDK.
 */
public record MarketPriceQuote(double priceVnd, PriceSource source) {

    public enum PriceSource {
        CLOSE
    }
}
