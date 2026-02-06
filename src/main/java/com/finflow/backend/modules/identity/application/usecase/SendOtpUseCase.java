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

    public void execute(String email, OtpPurpose purpose) {
        boolean emailExists = userRepository.existsByEmail(email);
        
        if (purpose == OtpPurpose.REGISTER && emailExists) {
            throw new AppException(IdentityErrorCode.EMAIL_ALREADY_EXISTS);
        } else if (purpose == OtpPurpose.RESET_PASSWORD && !emailExists) {
            throw new AppException(IdentityErrorCode.USER_NOT_FOUND); 
        }

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
