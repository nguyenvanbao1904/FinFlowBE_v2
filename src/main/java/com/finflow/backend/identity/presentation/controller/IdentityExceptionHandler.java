package com.finflow.backend.identity.presentation.controller;

import com.finflow.backend.common.exception.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;

@ControllerAdvice
@Slf4j
public class IdentityExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail handlingBadCredentials(BadCredentialsException exception, HttpServletRequest request) {
        String userMessage = "Invalid username or password";
        log.warn("Bad credentials - returning: {}", userMessage);

        var errorCode = CommonErrorCode.UNAUTHENTICATED;
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(errorCode.getStatusCode(), userMessage);
        pd.setTitle(errorCode.getMessage());
        pd.setType(URI.create("/api/error/" + errorCode.getCode()));
        pd.setProperty("code", errorCode.getCode());
        if (request != null) {
            pd.setProperty("instance", request.getRequestURI());
        }
        return pd;
    }
}
