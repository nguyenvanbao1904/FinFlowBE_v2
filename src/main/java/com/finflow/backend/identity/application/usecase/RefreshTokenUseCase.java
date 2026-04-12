package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.port.in.RefreshTokenPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.identity.infrastructure.configuration.TokenConfig;
import com.finflow.backend.identity.presentation.response.AuthResponse;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import com.finflow.backend.identity.domain.entity.InvalidatedToken;
import com.finflow.backend.identity.domain.entity.Role;
import com.finflow.backend.identity.domain.repository.InvalidatedTokenRepository;
import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.UserRepository;
import com.finflow.backend.identity.application.command.RefreshTokenCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;


import com.finflow.backend.identity.application.port.out.TokenServicePort;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenUseCase implements RefreshTokenPort {

    private final TokenServicePort tokenServicePort;
    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    public AuthResponse execute(RefreshTokenCommand command) {
        log.info("Executing refresh token use case");

        TokenServicePort.DecodedToken decoded;
        try {
            // Validate signature, expiry, and blacklist (via SecurityConfig validator)
            decoded = tokenServicePort.decodeToken(command.refreshToken());
        } catch (Exception ex) {
            log.warn("Refresh token invalid: {}", ex.getMessage());
            throw new AppException(IdentityErrorCode.INVALID_TOKEN);
        }

        // Enforce token type
        String type = decoded.type();
        if (!"refresh".equals(type)) {
            log.warn("Token type is not refresh");
            throw new AppException(IdentityErrorCode.INVALID_TOKEN);
        }

        String userId = decoded.subject();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(IdentityErrorCode.USER_NOT_FOUND));

        String scope = buildScope(user);

        // Rotate refresh token: blacklist old token
        blacklistToken(decoded);

        String newAccessToken = tokenServicePort.generateToken(userId, scope, TokenConfig.ACCESS_TOKEN_EXPIRY_SECONDS, "access");
        String newRefreshToken = tokenServicePort.generateToken(userId, scope, TokenConfig.REFRESH_TOKEN_EXPIRY_SECONDS, "refresh");

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .type("Bearer")
                .expiresIn(TokenConfig.ACCESS_TOKEN_EXPIRY_SECONDS)
                .refreshTokenExpiresIn(TokenConfig.REFRESH_TOKEN_EXPIRY_SECONDS)
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    private String buildScope(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .map(this::normalizeRole)
                .collect(Collectors.joining(" "));
    }

    private void blacklistToken(TokenServicePort.DecodedToken decoded) {
        Object jtiObj = decoded.claims().get("jti");
        Object expObj = decoded.claims().get("exp");
        
        String jti = jtiObj != null ? jtiObj.toString() : null;
        Instant expiry = null;
        if (expObj instanceof Instant) expiry = (Instant) expObj;
        else if (expObj instanceof Date) expiry = ((Date) expObj).toInstant();

        if (jti != null && expiry != null) {
            invalidatedTokenRepository.save(
                    InvalidatedToken.builder()
                            .id(jti)
                            .expiryTime(Date.from(expiry))
                            .build()
            );
            log.info("Refresh token jti {} blacklisted until {}", jti, expiry);
        }
    }

    private String normalizeRole(String role) {
        if (role == null) return "";
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
}
