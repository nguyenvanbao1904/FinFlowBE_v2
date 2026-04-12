package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.domain.enums.OtpPurpose;
import com.finflow.backend.identity.presentation.response.VerifyOtpResponse;

public interface VerifyOtpPort {
    VerifyOtpResponse execute(String email, String otp, OtpPurpose purpose);
}
