package com.finflow.backend.finance.transaction.exception;

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
    UNAUTHORIZED_ACCESS(3004, "You do not have permission to modify this transaction", HttpStatus.FORBIDDEN),
    TRANSACTION_TEXT_REQUIRED(3005, "Transaction text is required", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE(3006, "Insufficient account balance for this expense", HttpStatus.BAD_REQUEST),
    CATEGORY_IN_USE(3007, "Category is in use by transactions or budgets", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_OWNED(3008, "You can only edit or delete your own categories", HttpStatus.FORBIDDEN),
    INVALID_TRANSACTION_DATE(3009, "Transaction date format is invalid (expected ISO 8601 with timezone, e.g. 2026-03-05T18:00:00+07:00)", HttpStatus.BAD_REQUEST),
    INVALID_CHART_RANGE(3030, "Invalid chart range. Accepted values: WEEK, MONTH, THREE_MONTHS, SIX_MONTHS, YEAR", HttpStatus.BAD_REQUEST),
    INVALID_TRANSACTION_TYPE(3031, "Invalid transaction type. Accepted values: INCOME, EXPENSE, SAVING", HttpStatus.BAD_REQUEST),
    INVALID_CATEGORY_TYPE(3032, "Invalid category type. Accepted values: INCOME, EXPENSE, SAVING", HttpStatus.BAD_REQUEST),

    // @NotNull / @NotBlank field validation -> 3010-3019
    TRANSACTION_TYPE_REQUIRED(3010, "Transaction type is required", HttpStatus.BAD_REQUEST),
    TRANSACTION_CATEGORY_REQUIRED(3011, "Category is required", HttpStatus.BAD_REQUEST),
    TRANSACTION_ACCOUNT_REQUIRED(3012, "Wealth account is required", HttpStatus.BAD_REQUEST),
    TRANSACTION_DATE_REQUIRED(3013, "Transaction date is required", HttpStatus.BAD_REQUEST),
    TRANSACTION_AMOUNT_REQUIRED(3014, "Transaction amount is required", HttpStatus.BAD_REQUEST),
    CATEGORY_NAME_REQUIRED(3015, "Category name is required", HttpStatus.BAD_REQUEST),
    CATEGORY_TYPE_REQUIRED(3016, "Category type is required", HttpStatus.BAD_REQUEST),

    // Analytics upstream / integration -> 3020-3029
    ANALYTICS_UPSTREAM_ERROR(3020, "Analytics AI service is temporarily unavailable", HttpStatus.BAD_GATEWAY),
    AI_PREFILL_UPSTREAM_ERROR(3021, "Transaction prefill AI service is temporarily unavailable", HttpStatus.BAD_GATEWAY),

    // Cross-module boundary errors (local copies) -> 3040-3049
    WEALTH_ACCOUNT_NOT_FOUND(3040, "Wealth account not found", HttpStatus.NOT_FOUND),
    WEALTH_ACCOUNT_NOT_ELIGIBLE(3041, "Wealth account is not eligible for transactions", HttpStatus.BAD_REQUEST),
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
