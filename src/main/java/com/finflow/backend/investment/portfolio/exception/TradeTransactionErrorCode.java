package com.finflow.backend.investment.portfolio.exception;

import com.finflow.backend.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum TradeTransactionErrorCode implements ErrorCode {

    // Trade Transaction Domain Errors -> 5201-5299
    TRADE_TYPE_REQUIRED(5201, "Trade type is required", HttpStatus.BAD_REQUEST),

    TRADE_SYMBOL_REQUIRED(5202, "Trade symbol is required", HttpStatus.BAD_REQUEST),
    TRADE_QUANTITY_REQUIRED(5203, "Trade quantity is required", HttpStatus.BAD_REQUEST),
    TRADE_PRICE_REQUIRED(5204, "Trade price is required", HttpStatus.BAD_REQUEST),

    TRADE_AMOUNT_REQUIRED(5205, "Trade amount is required", HttpStatus.BAD_REQUEST),
    INVALID_TRADE_QUANTITY_NON_POSITIVE(5206, "Trade quantity must be positive", HttpStatus.BAD_REQUEST),
    INVALID_TRADE_PRICE_NEGATIVE(5207, "Trade price must be non-negative", HttpStatus.BAD_REQUEST),
    INVALID_TRADE_AMOUNT_NON_POSITIVE(5208, "Trade amount must be positive", HttpStatus.BAD_REQUEST),
    INVALID_TRADE_QUANTITY_MUST_BE_WHOLE_NUMBER(5211, "Trade quantity must be a whole number", HttpStatus.BAD_REQUEST),

    PORTFOLIO_CASH_BALANCE_INSUFFICIENT(5209, "Portfolio cash balance is insufficient", HttpStatus.BAD_REQUEST),
    PORTFOLIO_ASSET_QUANTITY_INSUFFICIENT(5210, "Portfolio asset quantity is insufficient", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    TradeTransactionErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

