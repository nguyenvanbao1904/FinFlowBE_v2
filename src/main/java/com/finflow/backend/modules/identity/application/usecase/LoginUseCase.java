package com.finflow.backend.modules.identity.application.usecase;

import com.finflow.backend.modules.identity.infrastructure.configuration.TokenConfig;
import com.finflow.backend.modules.identity.presentation.request.LoginRequest;
import com.finflow.backend.modules.identity.presentation.response.AuthResponse;
import com.finflow.backend.modules.identity.domain.entity.User;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginUseCase {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;

   
    public AuthResponse execute(LoginRequest request) {
        log.info("Executing login use case for user: {}", request.getUsername());

        // 1. Authenticate user (will throw AuthenticationException if invalid)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), 
                        request.getPassword()
                )
        );

        // 2. Generate Access & Refresh Tokens
        String accessToken = generateToken(
                authentication.getName(),
                getScope(authentication),
                TokenConfig.ACCESS_TOKEN_EXPIRY_SECONDS,
                "access"
        );
        String refreshToken = generateToken(
                authentication.getName(),
                getScope(authentication),
                TokenConfig.REFRESH_TOKEN_EXPIRY_SECONDS,
                "refresh"
        );

        // 3. Get user details from database
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(); // This should never throw since authentication succeeded

        // 4. Build and return response
        AuthResponse response = AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(TokenConfig.ACCESS_TOKEN_EXPIRY_SECONDS)
                .refreshTokenExpiresIn(TokenConfig.REFRESH_TOKEN_EXPIRY_SECONDS)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        log.info("Login successful for user: {}", request.getUsername());
        return response;
    }

    /**
     * Build scope string from authorities
     */
    private String getScope(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
    }

    /**
     * Generate JWT token with type and expiry
     */
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
