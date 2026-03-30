package com.finflow.backend.investment.market_data.exception;

import com.finflow.backend.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum MarketDataErrorCode implements ErrorCode {

    // Market data (investment market analysis) errors -> 6001-6099
    INVALID_SYMBOL(6001, "Stock symbol is required", HttpStatus.BAD_REQUEST),
    COMPANY_NOT_FOUND(6002, "Company not found", HttpStatus.NOT_FOUND),
    INVALID_ISO_DATE(6003, "Invalid date format (expected yyyy-MM-dd)", HttpStatus.BAD_REQUEST),
    DAILY_VALUATION_RANGE_TOO_LONG(
            6004,
            "Daily valuation range exceeds maximum allowed (2010-01-01 through today)",
            HttpStatus.BAD_REQUEST),
    DAILY_VALUATION_START_BEFORE_MIN(
            6005,
            "startDate must be on or after 2010-01-01",
            HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    MarketDataErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

