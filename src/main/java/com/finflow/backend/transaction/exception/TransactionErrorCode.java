package com.finflow.backend.transaction.exception;

import com.finflow.backend.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum TransactionErrorCode implements ErrorCode {
    
    // Transaction Domain Errors -> 3001-3099
    CATEGORY_NOT_FOUND(3001, "Category not found", HttpStatus.NOT_FOUND),
    TRANSACTION_NOT_FOUND(3002, "Transaction not found", HttpStatus.NOT_FOUND),
    INVALID_AMOUNT(3003, "Transaction amount must be greater than zero", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_ACCESS(3004, "You do not have permission to update this transaction", HttpStatus.FORBIDDEN),
    TRANSACTION_TEXT_REQUIRED(3005, "Transaction text is required", HttpStatus.BAD_REQUEST),
    ;

    TransactionErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
