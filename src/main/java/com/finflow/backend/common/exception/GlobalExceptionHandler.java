package com.finflow.backend.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.finflow.backend.common.constants.ValidationConstants;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. Xử lý Exception Business (AppException)
    @ExceptionHandler(value = AppException.class)
    ProblemDetail handlingAppException(AppException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        log.warn("Business error: {}", errorCode.getMessage());
        return toProblemDetail(errorCode, exception.getMessage(), request);
    }

    // 2. Xử lý Validation (VD: @Size, @NotNull)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ProblemDetail handlingValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String enumKey = exception.getFieldError() != null ? exception.getFieldError().getDefaultMessage() : "INVALID_KEY";

        ErrorCode errorCode = CommonErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        try {
            // 1. Try to find ErrorCode from known Enums
            try {
                errorCode = com.finflow.backend.modules.identity.exception.IdentityErrorCode.valueOf(enumKey);
            } catch (IllegalArgumentException e1) {
                try {
                    errorCode = CommonErrorCode.valueOf(enumKey);
                } catch (IllegalArgumentException e2) {
                    // Not found in both, keep INVALID_KEY
                }
            }

            // 2. Extract attributes (min, max, etc.) from ConstraintViolation
            var validationError = exception.getBindingResult().getAllErrors().getFirst();
            if (validationError.contains(jakarta.validation.ConstraintViolation.class)) {
                 var constraintViolation = validationError.unwrap(jakarta.validation.ConstraintViolation.class);
                 attributes = constraintViolation.getConstraintDescriptor().getAttributes();
            }

        } catch (Exception e) {
            // If any lookup fails
        }

        // 3. Map attributes to message placeholders
        String message = Objects.nonNull(attributes)
                ? mapAttribute(errorCode.getMessage(), attributes)
                : errorCode.getMessage();

        return toProblemDetail(errorCode, message, request);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(ValidationConstants.MIN_ATTRIBUTE));
        
        // Simple replacement for {min} - can be extended for other attributes
        return message.replace("{" + ValidationConstants.MIN_ATTRIBUTE + "}", minValue);
    }

    // 2.5. Xử lý Spring Security BadCredentialsException
    // (Username/Password sai - thrown từ Security filters)
    @ExceptionHandler(value = BadCredentialsException.class)
    ProblemDetail handlingBadCredentials(BadCredentialsException exception, HttpServletRequest request) {
        String userMessage = "Invalid username or password";
        log.warn("Bad credentials - returning: {}", userMessage);

        return toProblemDetail(
                CommonErrorCode.UNAUTHENTICATED,
                userMessage,
                request
        );
    }

    // 3. Xử lý lỗi hệ thống không mong muốn (Fallback)
    @ExceptionHandler(value = Exception.class)
    ProblemDetail handlingRuntimeException(Exception exception, HttpServletRequest request) {
        log.error("Exception: ", exception);

        return toProblemDetail(
                CommonErrorCode.UNCATEGORIZED_EXCEPTION,
                exception.getMessage(),
                request
        );
    }

    // --- Helpers ---
    private ProblemDetail toProblemDetail(ErrorCode errorCode, String detail, HttpServletRequest request) {
        HttpStatusCode status = errorCode.getStatusCode();
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail != null ? detail : errorCode.getMessage());
        pd.setTitle(errorCode.getMessage());
        pd.setType(URI.create("/api/error/" + errorCode.getCode()));
        pd.setProperty("code", errorCode.getCode());
        if (request != null) {
            pd.setProperty("instance", request.getRequestURI());
        }
        return pd;
    }
}