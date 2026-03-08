package com.finflow.backend.identity.exception;

    import com.finflow.backend.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum IdentityErrorCode implements ErrorCode {
    // User related errors (1001-1009)
    USER_EXISTED(1001, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1002, "User not found", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1005, "Email is invalid", HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(1006, "Username is required", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1007, "Email is required", HttpStatus.BAD_REQUEST),
    
    // Authentication errors (1010-1019)
    INVALID_TOKEN(1010, "Token invalid or expired", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(1011, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    INVALID_OTP(1017, "Invalid OTP code", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1012, "Invalid password", HttpStatus.BAD_REQUEST),
    OLD_PASSWORD_REQUIRED(1013, "Old password is required", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SAME_AS_OLD(1014, "New password cannot be the same as old password", HttpStatus.BAD_REQUEST),
    CONFIRM_PASSWORD_REQUIRED(1015, "Confirm password is required", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1016, "Password is required", HttpStatus.BAD_REQUEST),
    TOKEN_REQUIRED(1018, "Token is required", HttpStatus.BAD_REQUEST),
    OTP_REQUIRED(1019, "OTP is required", HttpStatus.BAD_REQUEST),
    OTP_PURPOSE_REQUIRED(1020, "OTP purpose is required", HttpStatus.BAD_REQUEST),
    BIOMETRIC_TOGGLE_REQUIRED(1021, "Biometric toggle value is required", HttpStatus.BAD_REQUEST),
    
    // Registration errors (1022-1029)
    EMAIL_ALREADY_EXISTS(1022, "Email is already in use", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS(1023, "Username is already taken", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1024, "Role not found", HttpStatus.NOT_FOUND),
    EMAIL_NOT_VERIFIED(1025, "Email has not been verified", HttpStatus.BAD_REQUEST),
    OTP_RATE_LIMITED(1026, "OTP requests are too frequent, please try again later", HttpStatus.TOO_MANY_REQUESTS),
    
    // Account status errors (1030-1039)
    ACCOUNT_DELETED(1030, "Account has been scheduled for deletion", HttpStatus.FORBIDDEN),
    NO_PASSWORD_SET(1031, "This account uses social login and has no password set", HttpStatus.BAD_REQUEST),
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