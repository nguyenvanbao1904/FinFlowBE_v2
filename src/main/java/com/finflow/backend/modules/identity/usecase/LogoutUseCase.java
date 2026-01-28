package com.finflow.backend.modules.identity.usecase;

import com.finflow.backend.modules.identity.domain.entity.InvalidatedToken;
import com.finflow.backend.modules.identity.domain.repository.InvalidatedTokenRepository;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

/**
 * Use Case: Logout User
 * 
 * WHY UseCase? ✅ VALID UseCase theo FEATURE_DEVELOPMENT_GUIDE:
 * - Complex business logic: JWT parsing + Claims extraction + Token blacklisting
 * - Not just a simple repository call
 * - Handles JWT-specific operations (parsing, validation)
 * 
 * Responsibilities:
 * 1. Parse JWT token using Nimbus library
 * 2. Extract token ID (JTI) and expiration
 * 3. Add token to invalidated list (blacklist)
 * 4. Handle parsing errors gracefully
 * 
 * Pattern: Controller → UseCase → Repository
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LogoutUseCase {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    /**
     * Execute logout by invalidating JWT token
     * 
     * @param token Raw JWT token (without "Bearer " prefix)
     * @throws ParseException if token format is invalid
     */
    public void execute(String token) {
        log.info("Executing logout use case");

        try {
            // 1. Parse JWT token
            SignedJWT signedJWT = SignedJWT.parse(token);

            // 2. Extract token ID and expiration
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            // 3. Add to invalidated tokens (blacklist)
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jti)
                    .expiryTime(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
            
            log.info("Token ID {} has been invalidated successfully", jti);

        } catch (ParseException e) {
            log.error("Failed to parse token during logout", e);
            // Note: We don't throw exception to ensure client always gets 200 OK
            // This is a design choice - invalid tokens are effectively "logged out"
        }
    }
}
