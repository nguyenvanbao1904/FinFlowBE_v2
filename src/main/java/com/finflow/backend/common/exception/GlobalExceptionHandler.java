package com.finflow.backend.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
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
        String enumKey = exception.getFieldError() != null ? exception.getFieldError().getDefaultMessage() : null;

        ErrorCode errorCode = CommonErrorCode.INVALID_KEY;

        try {
            if (enumKey != null) {
                errorCode = CommonErrorCode.valueOf(enumKey);
            }
        } catch (IllegalArgumentException ignored) {
            // fallback giữ CommonErrorCode.INVALID_KEY
        }

        String detail = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return toProblemDetail(errorCode, detail, request);
    }

    // 2.5. Xử lý Spring Security BadCredentialsException
    // (Username/Password sai - thrown từ Security filters)
    @ExceptionHandler(value = BadCredentialsException.class)
    ProblemDetail handlingBadCredentials(BadCredentialsException exception, HttpServletRequest request) {
        log.warn("Bad credentials: {}", exception.getMessage());

        return toProblemDetail(
                CommonErrorCode.UNAUTHENTICATED,
                "Invalid username or password",
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