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

    public void generateAndSendOtp(String email) {
        // 1. Generate 6-digit code
        String otp = String.format("%06d", random.nextInt(999999));
        
        // 2. Store with expiration
        otpStorage.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)));
        
        // 3. Publish Event
        log.info("Publishing OTP event for: {}", email);
        eventPublisher.publishEvent(new OtpRequestedEvent(email, otp));
    }

    public boolean verifyOtp(String email, String code) {
        if (!otpStorage.containsKey(email)) {
            return false;
        }
        
        OtpData data = otpStorage.get(email);
        
        // Check expiration
        if (data.expiryTime.isBefore(LocalDateTime.now())) {
            otpStorage.remove(email);
            return false;
        }
        
        // Check match
        if (data.code.equals(code)) {
            otpStorage.remove(email); // Invalidate after use
            return true;
        }
        
        return false;
    }

    private record OtpData(String code, LocalDateTime expiryTime) {}
}
