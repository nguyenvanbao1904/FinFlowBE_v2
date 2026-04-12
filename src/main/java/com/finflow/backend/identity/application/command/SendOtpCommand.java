package com.finflow.backend.identity.application.command;

import com.finflow.backend.identity.domain.enums.OtpPurpose;

/**
 * Command for sending an OTP code to a user's email.
 */
public record SendOtpCommand(
        String email,
        OtpPurpose purpose
) {}
