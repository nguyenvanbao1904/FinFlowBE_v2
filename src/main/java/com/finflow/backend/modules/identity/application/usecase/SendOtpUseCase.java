package com.finflow.backend.modules.identity.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.common.redis.RedisService;
import com.finflow.backend.modules.identity.application.event.OtpRequestedEvent;
import com.finflow.backend.modules.identity.domain.enums.OtpPurpose;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import com.finflow.backend.modules.identity.exception.IdentityErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendOtpUseCase {
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final RedisService redisService;
    
    private static final SecureRandom random = new SecureRandom();
    private static final int EXPIRATION_MINUTES = 5;
    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String OTP_RATE_KEY_PREFIX = "otp:rate:";
    private static final long RATE_LIMIT_SECONDS = 60;

    public void execute(String email, OtpPurpose purpose) {
        if (purpose == OtpPurpose.REGISTER) {
            boolean emailExists = userRepository.existsByEmail(email);
            if (emailExists) {
                throw new AppException(IdentityErrorCode.EMAIL_ALREADY_EXISTS);
            }
        } else if (purpose == OtpPurpose.RESET_PASSWORD) {
            // For password reset, validate user exists, is active, and has password
            var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(IdentityErrorCode.USER_NOT_FOUND));
            
            // Check if account is soft-deleted
            if (!user.getIsActive() || user.getDeletedAt() != null) {
                throw new AppException(IdentityErrorCode.ACCOUNT_DELETED);
            }
            
            // Check if user has a password (OAuth users don't have password)
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                throw new AppException(IdentityErrorCode.NO_PASSWORD_SET);
            }
        } else {
            // For other purposes (DELETE_ACCOUNT, RESET_PIN), just check user exists
            boolean emailExists = userRepository.existsByEmail(email);
            if (!emailExists) {
                throw new AppException(IdentityErrorCode.USER_NOT_FOUND);
            }
        }

        // Rate limit per email
        String rateKey = OTP_RATE_KEY_PREFIX + email;
        if (redisService.exists(rateKey)) {
            throw new AppException(IdentityErrorCode.OTP_RATE_LIMITED);
        }
        // set rate-limit marker
        redisService.set(rateKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        String otp = String.format("%06d", random.nextInt(999999));
        
        String redisKey = OTP_KEY_PREFIX + email;
        OtpData otpData = new OtpData(otp, LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES), purpose);
        
        redisService.set(redisKey, otpData, EXPIRATION_MINUTES, TimeUnit.MINUTES);
        
        log.info("Stored OTP in Redis for: {} with TTL: {} minutes", email, EXPIRATION_MINUTES);
        log.info("Publishing OTP event for: {}", email);
        
        eventPublisher.publishEvent(new OtpRequestedEvent(email, otp));
    }
    
    public record OtpData(
        String code, 
        LocalDateTime expiryTime, 
        OtpPurpose purpose
    ) {}
}
