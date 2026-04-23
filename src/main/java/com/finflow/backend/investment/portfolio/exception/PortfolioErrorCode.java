package com.finflow.backend.investment.portfolio.exception;

import com.finflow.backend.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum PortfolioErrorCode implements ErrorCode {

    // Investment Portfolio Domain Errors -> 5001-5099
    PORTFOLIO_NAME_BLANK(5001, "Portfolio name is required", HttpStatus.BAD_REQUEST),

    // Portfolio not found / ownership
    PORTFOLIO_NOT_FOUND(5002, "Portfolio not found", HttpStatus.NOT_FOUND),

    // Market price errors
    MARKET_PRICE_EMPTY_RESPONSE(5010, "Empty market price response from VPS", HttpStatus.BAD_GATEWAY),
    MARKET_PRICE_PARSE_FAILED(5011, "Failed to parse market price from VPS", HttpStatus.BAD_GATEWAY),
    MARKET_PRICE_MISSING(5012, "Missing close price in VPS payload", HttpStatus.BAD_GATEWAY);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    PortfolioErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

