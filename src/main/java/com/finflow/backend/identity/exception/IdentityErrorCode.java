package com.finflow.backend.identity.exception;

import com.finflow.backend.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum IdentityErrorCode implements ErrorCode {
    // User related errors (1100-1109)
    USER_NOT_FOUND(1101, "User not found", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(1102, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1103, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1104, "Email is invalid", HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(1105, "Username is required", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1106, "Email is required", HttpStatus.BAD_REQUEST),

    // Authentication errors (1110-1119)
    INVALID_TOKEN(1110, "Token invalid or expired", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(1111, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    INVALID_OTP(1117, "Invalid OTP code", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1112, "Invalid password", HttpStatus.BAD_REQUEST),
    OLD_PASSWORD_REQUIRED(1113, "Old password is required", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SAME_AS_OLD(1114, "New password cannot be the same as old password", HttpStatus.BAD_REQUEST),
    CONFIRM_PASSWORD_REQUIRED(1115, "Confirm password is required", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1116, "Password is required", HttpStatus.BAD_REQUEST),
    TOKEN_REQUIRED(1118, "Token is required", HttpStatus.BAD_REQUEST),
    OTP_REQUIRED(1119, "OTP is required", HttpStatus.BAD_REQUEST),
    OTP_PURPOSE_REQUIRED(1120, "OTP purpose is required", HttpStatus.BAD_REQUEST),
    BIOMETRIC_TOGGLE_REQUIRED(1121, "Biometric toggle value is required", HttpStatus.BAD_REQUEST),

    // Registration errors (1122-1129)
    EMAIL_ALREADY_EXISTS(1122, "Email is already in use", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS(1123, "Username is already taken", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1124, "Role not found", HttpStatus.NOT_FOUND),
    OTP_RATE_LIMITED(1126, "OTP requests are too frequent, please try again later", HttpStatus.TOO_MANY_REQUESTS),

    // Account status errors (1130-1139)
    ACCOUNT_DELETED(1130, "Account has been scheduled for deletion", HttpStatus.FORBIDDEN),
    NO_PASSWORD_SET(1131, "This account uses social login and has no password set", HttpStatus.BAD_REQUEST),
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