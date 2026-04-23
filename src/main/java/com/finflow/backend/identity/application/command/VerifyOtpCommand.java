package com.finflow.backend.identity.application.command;

import com.finflow.backend.identity.domain.enums.OtpPurpose;

public record VerifyOtpCommand(
        String email,
        String otp,
        OtpPurpose purpose
) {}
