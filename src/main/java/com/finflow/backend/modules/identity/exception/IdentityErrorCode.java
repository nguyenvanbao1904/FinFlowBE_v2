package com.finflow.backend.modules.identity.exception;

import com.finflow.backend.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum IdentityErrorCode implements ErrorCode {
    // User related errors (1001-1009)
    USER_EXISTED(1001, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1002, "User not found", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(1003, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    
    // Authentication errors (1010-1019)
    INVALID_TOKEN(1010, "Token invalid or expired", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(1011, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    
    // Registration errors (1020-1029)
    EMAIL_ALREADY_EXISTS(1020, "Email is already in use", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS(1021, "Username is already taken", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1022, "Role not found", HttpStatus.NOT_FOUND),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    IdentityErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}