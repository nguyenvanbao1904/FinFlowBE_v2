package com.finflow.backend.investment.portfolio.exception;

import com.finflow.backend.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum PortfolioAssetErrorCode implements ErrorCode {

    // Portfolio Asset Domain Errors -> 5101-5199
    PORTFOLIO_ASSET_SYMBOL_BLANK(5101, "Portfolio asset symbol is required", HttpStatus.BAD_REQUEST),
    PORTFOLIO_ASSET_QUANTITY_REQUIRED(5102, "Portfolio asset quantity is required", HttpStatus.BAD_REQUEST),
    PORTFOLIO_ASSET_QUANTITY_MUST_BE_POSITIVE(5103, "Portfolio asset quantity must be positive", HttpStatus.BAD_REQUEST),
    PORTFOLIO_ASSET_QUANTITY_MUST_BE_WHOLE_NUMBER(5106, "Portfolio asset quantity must be a whole number", HttpStatus.BAD_REQUEST),
    PORTFOLIO_ASSET_AVERAGE_PRICE_REQUIRED(5104, "Portfolio asset average price is required", HttpStatus.BAD_REQUEST),
    PORTFOLIO_ASSET_AVERAGE_PRICE_MUST_BE_NON_NEGATIVE(5105, "Portfolio asset average price must be non-negative", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    PortfolioAssetErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

