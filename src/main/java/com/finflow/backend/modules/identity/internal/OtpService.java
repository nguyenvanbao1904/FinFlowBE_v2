package com.finflow.backend.modules.identity.internal;

import com.finflow.backend.modules.identity.OtpRequestedEvent;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    
    // In-memory storage for MVP: Email -> OtpData
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private static final int EXPIRATION_MINUTES = 5;

    private final org.springframework.security.oauth2.jwt.JwtEncoder jwtEncoder;

    public void generateAndSendOtp(String email, com.finflow.backend.modules.identity.domain.OtpPurpose purpose) {
        // Validation based on purpose
        boolean emailExists = userRepository.existsByEmail(email);
        
        if (purpose == com.finflow.backend.modules.identity.domain.OtpPurpose.REGISTER) {
            if (emailExists) {
                throw new com.finflow.backend.common.exception.AppException(com.finflow.backend.modules.identity.exception.IdentityErrorCode.EMAIL_ALREADY_EXISTS);
            }
        } else if (purpose == com.finflow.backend.modules.identity.domain.OtpPurpose.RESET_PASSWORD) {
            if (!emailExists) {
                // Security: Don't reveal email existence? For MVP let's fail.
                // Or better, simulate success efficiently?
                // For now, let's throw USER_NOT_FOUND or generic error.
                throw new com.finflow.backend.common.exception.AppException(com.finflow.backend.modules.identity.exception.IdentityErrorCode.USER_NOT_FOUND); 
            }
        }

        // 1. Generate 6-digit code
        String otp = String.format("%06d", random.nextInt(999999));
        
        // 2. Store with expiration and purpose
        otpStorage.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES), purpose));
        
        // 3. Publish Event
        log.info("Publishing OTP event for: {}", email);
        eventPublisher.publishEvent(new OtpRequestedEvent(email, otp));
    }

    public com.finflow.backend.modules.identity.dto.VerifyOtpResponse verifyOtp(String email, String code, com.finflow.backend.modules.identity.domain.OtpPurpose purpose) {
        if (!otpStorage.containsKey(email)) {
            throw new com.finflow.backend.common.exception.AppException(com.finflow.backend.modules.identity.exception.IdentityErrorCode.INVALID_CREDENTIALS);
        }
        
        OtpData data = otpStorage.get(email);
        
        // Check purpose
        if (data.purpose != purpose) {
             throw new com.finflow.backend.common.exception.AppException(com.finflow.backend.modules.identity.exception.IdentityErrorCode.INVALID_CREDENTIALS);
        }

        // Check expiration
        if (data.expiryTime.isBefore(LocalDateTime.now())) {
            otpStorage.remove(email);
            throw new com.finflow.backend.common.exception.AppException(com.finflow.backend.modules.identity.exception.IdentityErrorCode.INVALID_CREDENTIALS);
        }
        
        // Check match
        if (data.code.equals(code)) {
            otpStorage.remove(email); // Invalidate OTP after use
            
            // Generate Appropriate Token based on Purpose
            String tokenType = purpose == com.finflow.backend.modules.identity.domain.OtpPurpose.REGISTER 
                ? "REGISTRATION_TOKEN" 
                : "RESET_PASSWORD_TOKEN";
                
            String token = generateToken(email, tokenType);
            
            return com.finflow.backend.modules.identity.dto.VerifyOtpResponse.builder()
                .message("OTP Verified Successfully")
                .registrationToken(token) // Reuse field name for simplicity, or we can make it generic
                .build();
        }
        
        throw new com.finflow.backend.common.exception.AppException(com.finflow.backend.modules.identity.exception.IdentityErrorCode.INVALID_CREDENTIALS);
    }

    private String generateToken(String email, String type) {
        java.time.Instant now = java.time.Instant.now();
        org.springframework.security.oauth2.jwt.JwtClaimsSet claims = org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                .issuer("FinFlow")
                .issuedAt(now)
                .expiresAt(now.plus(15, java.time.temporal.ChronoUnit.MINUTES))
                .subject(email)
                .id(java.util.UUID.randomUUID().toString())
                .claim("type", type)
                .build();
        return jwtEncoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(claims)).getTokenValue();
    }

    // Updated Record
    private record OtpData(String code, LocalDateTime expiryTime, com.finflow.backend.modules.identity.domain.OtpPurpose purpose) {}
}
