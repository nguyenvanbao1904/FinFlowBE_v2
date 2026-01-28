package com.finflow.backend.modules.identity.usecase;

import com.finflow.backend.modules.identity.internal.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerifyOtpUseCase {
    private final OtpService otpService;

    public boolean execute(String email, String otp) {
        return otpService.verifyOtp(email, otp);
    }
}
