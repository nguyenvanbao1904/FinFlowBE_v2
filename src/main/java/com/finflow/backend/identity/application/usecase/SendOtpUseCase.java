package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.command.SendOtpCommand;
import com.finflow.backend.identity.application.port.in.SendOtpPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.identity.application.dto.OtpData;
import com.finflow.backend.identity.api.OtpRequestedEvent;
import com.finflow.backend.identity.application.port.out.OtpStorePort;
import com.finflow.backend.identity.domain.enums.OtpPurpose;
import com.finflow.backend.identity.domain.repository.UserRepository;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendOtpUseCase implements SendOtpPort {
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final OtpStorePort otpStorePort;

    private static final SecureRandom random = new SecureRandom();
    private static final int EXPIRATION_MINUTES = 5;
    private static final long RATE_LIMIT_SECONDS = 60;

    @Transactional(readOnly = true)
    @Override
    public void execute(SendOtpCommand request) {
        String email = request.email();
        OtpPurpose purpose = request.purpose();
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
        if (otpStorePort.isRateLimited(email)) {
            throw new AppException(IdentityErrorCode.OTP_RATE_LIMITED);
        }
        otpStorePort.markRateLimited(email, RATE_LIMIT_SECONDS);

        String otp = String.format("%06d", random.nextInt(999999));
        
        OtpData otpData = new OtpData(otp, LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES), purpose);
        otpStorePort.saveOtp(email, otpData, EXPIRATION_MINUTES);
        
        log.debug("Stored OTP in Redis for: {} with TTL: {} minutes", email, EXPIRATION_MINUTES);
        log.debug("Publishing OTP event for: {}", email);

        String correlationId = MDC.get("correlationId");
        eventPublisher.publishEvent(
                OtpRequestedEvent.builder()
                        .email(email)
                        .otpCode(otp)
                        .correlationId(correlationId)
                        .build()
        );
    }
}
