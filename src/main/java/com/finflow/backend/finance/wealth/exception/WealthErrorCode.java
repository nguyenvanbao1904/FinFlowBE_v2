package com.finflow.backend.finance.wealth.exception;

import com.finflow.backend.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum WealthErrorCode implements ErrorCode {

    WEALTH_ACCOUNT_NOT_FOUND(4002, "Wealth account not found", HttpStatus.NOT_FOUND),
    WEALTH_ACCOUNT_TYPE_NOT_FOUND(4003, "Wealth account type not found", HttpStatus.NOT_FOUND),
    WEALTH_ACCOUNT_NAME_BLANK(4004, "Wealth account name is required", HttpStatus.BAD_REQUEST),
    WEALTH_ACCOUNT_TYPE_REQUIRED(4005, "Wealth account type is required", HttpStatus.BAD_REQUEST),
    WEALTH_ACCOUNT_BALANCE_REQUIRED(4006, "Balance is required", HttpStatus.BAD_REQUEST),
    WEALTH_ACCOUNT_NOT_TRANSACTION_ELIGIBLE(4007, "Wealth account type cannot be used for transactions", HttpStatus.BAD_REQUEST),
    BALANCE_NEGATIVE_FOR_NON_DEBT_TYPE(4008, "Negative balance is only allowed for debt wealth account types", HttpStatus.BAD_REQUEST),
    WEALTH_ACCOUNT_HAS_TRANSACTIONS(4009, "Cannot delete wealth account that has transactions. Reassign or delete the transactions first.", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    WealthErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
