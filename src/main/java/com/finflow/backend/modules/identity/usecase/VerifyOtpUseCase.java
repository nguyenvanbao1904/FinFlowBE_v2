package com.finflow.backend.modules.identity.usecase;

import com.finflow.backend.modules.identity.internal.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerifyOtpUseCase {
    private final OtpService otpService;

    public com.finflow.backend.modules.identity.dto.VerifyOtpResponse execute(String email, String otp, com.finflow.backend.modules.identity.domain.OtpPurpose purpose) {
        return otpService.verifyOtp(email, otp, purpose);
    }
}
