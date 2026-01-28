package com.finflow.backend.modules.identity.usecase;

import com.finflow.backend.modules.identity.dto.LoginRequest;
import com.finflow.backend.modules.identity.dto.AuthResponse;
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

/**
 * Use Case: Login User
 * 
 * WHY UseCase? ✅ VALID UseCase theo FEATURE_DEVELOPMENT_GUIDE:
 * - Orchestrates 3 components: AuthenticationManager + JwtEncoder + UserRepository
 * - Complex business logic: Credential validation + JWT generation + User loading
 * - Framework integration: Spring Security AuthenticationManager
 * 
 * Responsibilities:
 * 1. Authenticate credentials (username/password) via AuthenticationManager
 * 2. Generate JWT token with claims and expiration
 * 3. Load user details from database
 * 4. Build and return AuthResponse
 * 
 * Pattern: Controller → UseCase → Multiple Components
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginUseCase {

    private static final long ACCESS_TOKEN_EXPIRY_SECONDS = 3600;          // 1 hour
    private static final long REFRESH_TOKEN_EXPIRY_SECONDS = 7 * 24 * 3600; // 7 days

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;

    /**
     * Execute login flow
     * 
     * @param request LoginRequest containing username and password
     * @return AuthResponse with JWT token and user information
     * @throws org.springframework.security.core.AuthenticationException if credentials invalid
     */
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
                ACCESS_TOKEN_EXPIRY_SECONDS,
                "access"
        );
        String refreshToken = generateToken(
                authentication.getName(),
                getScope(authentication),
                REFRESH_TOKEN_EXPIRY_SECONDS,
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
                .expiresIn(ACCESS_TOKEN_EXPIRY_SECONDS)
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
