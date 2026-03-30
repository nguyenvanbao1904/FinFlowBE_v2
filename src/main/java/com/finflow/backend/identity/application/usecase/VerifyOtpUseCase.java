package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.common.redis.RedisService;
import com.finflow.backend.identity.application.dto.OtpData;
import com.finflow.backend.identity.domain.enums.OtpPurpose;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import com.finflow.backend.identity.presentation.response.VerifyOtpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerifyOtpUseCase {
    private final RedisService redisService;
    private final JwtEncoder jwtEncoder;
    
    private static final String OTP_KEY_PREFIX = "otp:";

    public VerifyOtpResponse execute(String email, String code, OtpPurpose purpose) {
        String redisKey = OTP_KEY_PREFIX + email;
        
        OtpData data = redisService.get(redisKey, OtpData.class);
        
        if (data == null) {
            log.warn("OTP not found or expired for email: {}", email);
            throw new AppException(IdentityErrorCode.INVALID_OTP);
        }
        
        if (data.purpose() != purpose) {
            log.warn("OTP purpose mismatch for email: {}", email);
            throw new AppException(IdentityErrorCode.INVALID_OTP);
        }

        if (data.expiryTime().isBefore(LocalDateTime.now())) {
            redisService.delete(redisKey);
            log.warn("OTP expired for email: {}", email);
            throw new AppException(IdentityErrorCode.INVALID_OTP);
        }
        
        if (data.code().equals(code)) {
            redisService.delete(redisKey);
            log.info("OTP verified successfully for email: {}", email);
            
            String tokenType;
            if (purpose == OtpPurpose.REGISTER) {
                tokenType = "REGISTRATION_TOKEN";
            } else if (purpose == OtpPurpose.RESET_PASSWORD) {
                tokenType = "RESET_PASSWORD_TOKEN";
            } else if (purpose == OtpPurpose.DELETE_ACCOUNT) {
                tokenType = "DELETE_ACCOUNT_TOKEN";
            } else if (purpose == OtpPurpose.RESET_PIN) {
                tokenType = "RESET_PIN_TOKEN";
            } else {
                throw new AppException(IdentityErrorCode.INVALID_OTP);
            }
                
            String token = generateToken(email, tokenType);
            
            return VerifyOtpResponse.builder()
                .message("OTP Verified Successfully")
                .registrationToken(token)
                .build();
        }
        
        throw new AppException(IdentityErrorCode.INVALID_OTP);
    }

    private String generateToken(String email, String type) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("FinFlow")
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .subject(email)
                .id(UUID.randomUUID().toString())
                .claim("type", type)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
