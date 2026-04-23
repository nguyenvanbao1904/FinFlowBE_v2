package com.finflow.backend.identity.application.command;

import com.finflow.backend.identity.domain.enums.OtpPurpose;

public record SendOtpCommand(
        String email,
        OtpPurpose purpose
) {}
