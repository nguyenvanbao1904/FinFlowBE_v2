package com.finflow.backend.common.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.finflow.backend.common.constants.ValidationConstants;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final List<ErrorCodeResolver> errorCodeResolvers;

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
            // 1. Try to resolve via registered module-specific resolvers
            for (ErrorCodeResolver resolver : errorCodeResolvers) {
                ErrorCode resolved = resolver.resolve(enumKey);
                if (resolved != null) {
                    errorCode = resolved;
                    break;
                }
            }

            // 2. Fallback to CommonErrorCode enum if there is a matching constant
            if (errorCode == CommonErrorCode.INVALID_KEY) {
                try {
                    errorCode = CommonErrorCode.valueOf(enumKey);
                } catch (IllegalArgumentException ignored) {
                    // Not found, keep INVALID_KEY
                }
            }

            // 3. Extract attributes (min, max, etc.) from ConstraintViolation
            var validationError = exception.getBindingResult().getAllErrors().getFirst();
            if (validationError.contains(jakarta.validation.ConstraintViolation.class)) {
                 var constraintViolation = validationError.unwrap(jakarta.validation.ConstraintViolation.class);
                 attributes = constraintViolation.getConstraintDescriptor().getAttributes();
            }

        } catch (Exception e) {
            // If any lookup fails
        }

        // 4. Map attributes to message placeholders
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

    // 3. Xử lý lỗi hệ thống không mong muốn (Fallback)
    @ExceptionHandler(value = Exception.class)
    ProblemDetail handlingRuntimeException(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error: {}", exception.getMessage(), exception);

        return toProblemDetail(
                CommonErrorCode.UNCATEGORIZED_EXCEPTION,
                "An unexpected error occurred. Please try again later.",
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