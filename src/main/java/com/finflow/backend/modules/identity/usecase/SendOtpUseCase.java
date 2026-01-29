package com.finflow.backend.modules.identity.usecase;

import com.finflow.backend.modules.identity.internal.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendOtpUseCase {
    private final OtpService otpService;

    public void execute(String email, com.finflow.backend.modules.identity.domain.OtpPurpose purpose) {
        // Can add more validation here (e.g. check if email already registered if needed, 
        // but for registration flow we might want to allow it or check existed)
        // Usually for registration, we check if user exists first to prevent spam or enumeration? 
        // User asked for "100% verified from beginning".
        // If email exists, maybe we shouldn't send OTP for REGISTRATION.
        // But for Forgot Password we should.
        // Let's keep it simple: just send.
        otpService.generateAndSendOtp(email, purpose);
    }
}
