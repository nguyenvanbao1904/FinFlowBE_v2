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
    PORTFOLIO_NOT_FOUND(5002, "Portfolio not found", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    PortfolioErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

