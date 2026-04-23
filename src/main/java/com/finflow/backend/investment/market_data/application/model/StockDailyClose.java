package com.finflow.backend.investment.market_data.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Daily closing price of a stock symbol.
 * Application-layer model — independent of any infrastructure SDK.
 */
public record StockDailyClose(LocalDate date, BigDecimal closeVnd) {}
