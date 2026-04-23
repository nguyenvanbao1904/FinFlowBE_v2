package com.finflow.backend.investment.portfolio.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTradeTransactionRequest {

    @NotBlank(message = "TRADE_TYPE_REQUIRED")
    String tradeType;

    // BUY/SELL/DIVIDEND
    String symbol;
    BigDecimal quantity;
    BigDecimal price;

    // DEPOSIT/WITHDRAW (nạp/rút)
    BigDecimal amount;

    // percent fields: e.g. 0.1 means 0.1% (tax for SELL defaults to 0.1%)
    BigDecimal feePercent;
    BigDecimal taxPercent;

    /**
     * ISO8601 string (e.g. 2026-03-05T18:09:41.830+07:00).
     * If null/blank, use now().
     */
    String transactionDate;
}

