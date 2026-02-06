package com.finflow.backend.modules.identity.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.modules.identity.infrastructure.configuration.TokenConfig;
import com.finflow.backend.modules.identity.presentation.response.AuthResponse;
import com.finflow.backend.modules.identity.exception.IdentityErrorCode;
import com.finflow.backend.modules.identity.domain.entity.InvalidatedToken;
import com.finflow.backend.modules.identity.domain.entity.Role;
import com.finflow.backend.modules.identity.domain.repository.InvalidatedTokenRepository;
import com.finflow.backend.modules.identity.domain.entity.User;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenUseCase {

    private final JwtDecoder jwtDecoder;
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    public AuthResponse execute(String refreshToken) {
        log.info("Executing refresh token use case");

        Jwt jwt;
        try {
            // Validate signature, expiry, and blacklist (via SecurityConfig validator)
            jwt = jwtDecoder.decode(refreshToken);
        } catch (Exception ex) {
            log.warn("Refresh token invalid: {}", ex.getMessage());
            throw new AppException(IdentityErrorCode.INVALID_TOKEN);
        }

        // Enforce token type
        String type = jwt.getClaimAsString("type");
        if (!"refresh".equals(type)) {
            log.warn("Token type is not refresh");
            throw new AppException(IdentityErrorCode.INVALID_TOKEN);
        }

        String username = jwt.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(IdentityErrorCode.USER_NOT_FOUND));

        String scope = buildScope(user);

        // Rotate refresh token: blacklist old token
        blacklistToken(jwt);

        String newAccessToken = generateToken(username, scope, TokenConfig.ACCESS_TOKEN_EXPIRY_SECONDS, "access");
        String newRefreshToken = generateToken(username, scope, TokenConfig.REFRESH_TOKEN_EXPIRY_SECONDS, "refresh");

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
                .collect(Collectors.joining(" "));
    }

    private void blacklistToken(Jwt jwt) {
        String jti = jwt.getId();
        Instant expiry = jwt.getExpiresAt();
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

    private String generateToken(String subject, String scope, long expirySeconds, String type) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(expirySeconds, ChronoUnit.SECONDS))
                .subject(subject)
                .claim("scope", scope)
                .claim("type", type)
                .id(UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}

