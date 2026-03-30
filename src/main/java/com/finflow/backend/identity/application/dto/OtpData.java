package com.finflow.backend.identity.application.dto;

import com.finflow.backend.identity.domain.enums.OtpPurpose;

import java.time.LocalDateTime;

/**
 * Redis payload for OTP flows. Shared by {@code SendOtpUseCase} and {@code VerifyOtpUseCase}
 * so use cases do not depend on each other's class for nested types (ArchUnit-friendly).
 */
public record OtpData(
        String code,
        LocalDateTime expiryTime,
        OtpPurpose purpose
) {}
