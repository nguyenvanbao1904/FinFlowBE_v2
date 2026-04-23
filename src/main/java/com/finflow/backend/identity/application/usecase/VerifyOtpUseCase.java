package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.command.VerifyOtpCommand;
import com.finflow.backend.identity.application.port.in.VerifyOtpPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.identity.application.dto.OtpData;
import com.finflow.backend.identity.application.dto.VerifyOtpOutput;
import com.finflow.backend.identity.application.port.out.OtpStorePort;
import com.finflow.backend.identity.domain.enums.OtpPurpose;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.finflow.backend.identity.application.port.out.TokenServicePort;
import com.finflow.backend.identity.domain.constant.IdentityConstants;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerifyOtpUseCase implements VerifyOtpPort {
    private final OtpStorePort otpStorePort;
    private final TokenServicePort tokenServicePort;

    @Override
    public VerifyOtpOutput execute(VerifyOtpCommand request) {
        String email = request.email();
        String code = request.otp();
        OtpPurpose purpose = request.purpose();
        OtpData data = otpStorePort.getOtp(email);
        
        if (data == null) {
            log.warn("OTP not found or expired");
            throw new AppException(IdentityErrorCode.INVALID_OTP);
        }

        if (data.purpose() != purpose) {
            log.warn("OTP purpose mismatch");
            throw new AppException(IdentityErrorCode.INVALID_OTP);
        }

        if (data.expiryTime().isBefore(LocalDateTime.now())) {
            otpStorePort.deleteOtp(email);
            log.warn("OTP expired");
            throw new AppException(IdentityErrorCode.INVALID_OTP);
        }

        if (data.code().equals(code)) {
            otpStorePort.deleteOtp(email);
            log.debug("OTP verified successfully");
            
            String tokenType;
            if (purpose == OtpPurpose.REGISTER) {
                tokenType = IdentityConstants.TOKEN_TYPE_REGISTRATION;
            } else if (purpose == OtpPurpose.RESET_PASSWORD) {
                tokenType = IdentityConstants.TOKEN_TYPE_RESET_PASSWORD;
            } else if (purpose == OtpPurpose.DELETE_ACCOUNT) {
                tokenType = IdentityConstants.TOKEN_TYPE_DELETE_ACCOUNT;
            } else if (purpose == OtpPurpose.RESET_PIN) {
                tokenType = IdentityConstants.TOKEN_TYPE_RESET_PIN;
            } else {
                throw new AppException(IdentityErrorCode.INVALID_OTP);
            }
                
            String token = tokenServicePort.generateToken(email, "", 300, tokenType);
            
            return VerifyOtpOutput.builder()
                .message("OTP Verified Successfully")
                .registrationToken(token)
                .build();
        }
        
        throw new AppException(IdentityErrorCode.INVALID_OTP);
    }
}
