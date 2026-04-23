package com.finflow.backend.finance.budget.exception;

import com.finflow.backend.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum BudgetErrorCode implements ErrorCode {

    // Budget Domain Errors -> 3101-3199
    BUDGET_NOT_FOUND(3101, "Budget not found", HttpStatus.NOT_FOUND),
    BUDGET_INVALID_DATE_RANGE(3102, "Budget startDate must be on or before endDate", HttpStatus.BAD_REQUEST),
    BUDGET_END_DATE_IN_PAST(3103, "End date cannot be before today", HttpStatus.BAD_REQUEST),
    BUDGET_CATEGORY_MUST_BE_EXPENSE(3104, "Budget category must be an expense category", HttpStatus.BAD_REQUEST),
    BUDGET_CATEGORY_NOT_FOUND(3105, "Budget category not found", HttpStatus.NOT_FOUND);

    BudgetErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}

