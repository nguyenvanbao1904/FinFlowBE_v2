package com.finflow.backend.investment.portfolio.exception;

import com.finflow.backend.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ImportPortfolioSnapshotErrorCode implements ErrorCode {

    // Import Portfolio Snapshot Domain Errors -> 5301-5399
    CASH_BALANCE_REQUIRED(5301, "Cash balance is required", HttpStatus.BAD_REQUEST),
    CASH_BALANCE_MUST_BE_NON_NEGATIVE(5302, "Cash balance must be non-negative", HttpStatus.BAD_REQUEST),

    HOLDING_SYMBOL_BLANK(5303, "Holding symbol is required", HttpStatus.BAD_REQUEST),
    HOLDING_QUANTITY_REQUIRED(5304, "Holding quantity is required", HttpStatus.BAD_REQUEST),
    HOLDING_QUANTITY_MUST_BE_NON_NEGATIVE(5305, "Holding quantity must be non-negative", HttpStatus.BAD_REQUEST),
    HOLDING_QUANTITY_MUST_BE_WHOLE_NUMBER(5308, "Holding quantity must be a whole number", HttpStatus.BAD_REQUEST),
    HOLDING_AVERAGE_PRICE_REQUIRED(5306, "Holding average price is required", HttpStatus.BAD_REQUEST),
    HOLDING_AVERAGE_PRICE_MUST_BE_NON_NEGATIVE(5307, "Holding average price must be non-negative", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ImportPortfolioSnapshotErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

