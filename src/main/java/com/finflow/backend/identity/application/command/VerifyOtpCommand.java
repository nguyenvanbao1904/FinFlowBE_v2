package com.finflow.backend.identity.application.command;

import com.finflow.backend.identity.domain.enums.OtpPurpose;

/**
 * Command for verifying an OTP code submitted by the user.
 */
public record VerifyOtpCommand(
        String email,
        String otp,
        OtpPurpose purpose
) {}
