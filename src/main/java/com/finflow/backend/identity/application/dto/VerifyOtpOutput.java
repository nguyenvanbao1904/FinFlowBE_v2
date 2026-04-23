package com.finflow.backend.identity.application.dto;

import lombok.Builder;

@Builder
public record VerifyOtpOutput(
        String message,
        String registrationToken
) {}
