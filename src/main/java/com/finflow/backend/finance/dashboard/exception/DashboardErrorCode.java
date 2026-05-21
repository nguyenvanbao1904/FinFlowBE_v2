package com.finflow.backend.finance.dashboard.exception;

import com.finflow.backend.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum DashboardErrorCode implements ErrorCode {
    HOME_INSIGHT_UPSTREAM_ERROR(8001, "Home insight AI service is temporarily unavailable", HttpStatus.BAD_GATEWAY);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    DashboardErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
