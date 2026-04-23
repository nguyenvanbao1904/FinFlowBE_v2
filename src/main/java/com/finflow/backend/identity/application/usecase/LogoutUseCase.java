package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.port.in.LogoutPort;

import com.finflow.backend.identity.application.command.LogoutCommand;
import com.finflow.backend.identity.application.port.out.TokenServicePort;
import com.finflow.backend.identity.domain.entity.InvalidatedToken;
import com.finflow.backend.identity.domain.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogoutUseCase implements LogoutPort {

    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final TokenServicePort tokenServicePort;

    @Transactional
    @Override
    public void execute(LogoutCommand command) {
        log.info("Executing logout use case");

        try {
            TokenServicePort.DecodedToken decoded = tokenServicePort.decodeToken(command.accessToken());
            String jti = decoded.claims().get("jti") == null ? null : decoded.claims().get("jti").toString();
            Date expiryTime = extractExpiry(decoded);
            if (jti == null || expiryTime == null) {
                log.warn("Could not extract jti/exp from token during logout");
                return;
            }

            // 3. Add to invalidated tokens (blacklist)
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jti)
                    .expiryTime(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
            
            log.info("Token ID {} has been invalidated successfully", jti);

        } catch (Exception e) {
            log.error("Failed to parse token during logout", e);
            // Note: We don't throw exception to ensure client always gets 200 OK
            // This is a design choice - invalid tokens are effectively "logged out"
        }
    }

    private Date extractExpiry(TokenServicePort.DecodedToken decoded) {
        Object exp = decoded.claims().get("exp");
        if (exp instanceof Date date) {
            return date;
        }
        if (exp instanceof Number number) {
            return new Date(number.longValue() * 1000);
        }
        if (exp instanceof java.time.Instant instant) {
            return Date.from(instant);
        }
        return null;
    }
}
