package com.finflow.backend.modules.identity.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.common.redis.RedisService;
import com.finflow.backend.modules.identity.application.usecase.SendOtpUseCase.OtpData;
import com.finflow.backend.modules.identity.domain.enums.OtpPurpose;
import com.finflow.backend.modules.identity.exception.IdentityErrorCode;
import com.finflow.backend.modules.identity.presentation.response.VerifyOtpResponse;
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
            throw new AppException(IdentityErrorCode.INVALID_CREDENTIALS);
        }
        
        if (data.purpose() != purpose) {
            log.warn("OTP purpose mismatch for email: {}", email);
            throw new AppException(IdentityErrorCode.INVALID_CREDENTIALS);
        }

        if (data.expiryTime().isBefore(LocalDateTime.now())) {
            redisService.delete(redisKey);
            log.warn("OTP expired for email: {}", email);
            throw new AppException(IdentityErrorCode.INVALID_CREDENTIALS);
        }
        
        if (data.code().equals(code)) {
            redisService.delete(redisKey);
            log.info("OTP verified successfully for email: {}", email);
            
            String tokenType = purpose == OtpPurpose.REGISTER 
                ? "REGISTRATION_TOKEN" 
                : "RESET_PASSWORD_TOKEN";
                
            String token = generateToken(email, tokenType);
            
            return VerifyOtpResponse.builder()
                .message("OTP Verified Successfully")
                .registrationToken(token)
                .build();
        }
        
        throw new AppException(IdentityErrorCode.INVALID_CREDENTIALS);
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
