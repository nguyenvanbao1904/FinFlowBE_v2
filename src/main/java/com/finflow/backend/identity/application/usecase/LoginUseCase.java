package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.port.in.LoginPort;

import com.finflow.backend.identity.infrastructure.configuration.TokenConfig;
import com.finflow.backend.identity.application.command.LoginCommand;
import com.finflow.backend.identity.presentation.response.AuthResponse;
import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.finflow.backend.identity.application.port.out.TokenServicePort;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginUseCase implements LoginPort {

    private final AuthenticationManager authenticationManager;
    private final TokenServicePort tokenServicePort;
    private final UserRepository userRepository;

   
    @Override
    public AuthResponse execute(LoginCommand command) {
        log.info("Executing login use case for user: {}", command.username());

        Authentication authentication;
        boolean isReactivated = false;
        try {
            // 1. Authenticate user (will throw AuthenticationException if invalid)
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            command.username(),
                            command.password()
                    )
            );
        } catch (LockedException e) {
            log.warn("Account is locked (possibly soft-deleted). Checking for reactivation...");
            // Handle reactivation logic if user is soft-deleted
            User user = userRepository.findByUsername(command.username())
                    .or(() -> userRepository.findByEmail(command.username()))
                    .orElseThrow(() -> e); // Should not happen if locked

            if (user.getDeletedAt() != null) {
                log.info("User {} is soft-deleted. Reactivating account...", user.getUsername());
                user.setIsActive(true);
                user.setDeletedAt(null);
                user.setLastLogin(java.time.LocalDateTime.now());
                userRepository.save(user);
                isReactivated = true;

                // Retry authentication
                authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                command.username(),
                                command.password()
                        )
                );
            } else {
                throw e; // Locked for other reasons (e.g. admin ban)
            }
        }

        // 3. Get user details from database
        User user = userRepository.findByUsername(command.username())
                .or(() -> userRepository.findByEmail(command.username()))
                .orElseThrow(); // This should never throw since authentication succeeded

        // 2. Generate Access & Refresh Tokens
        String accessToken = tokenServicePort.generateToken(
                user.getId(),
                getScope(authentication),
                TokenConfig.ACCESS_TOKEN_EXPIRY_SECONDS,
                "access"
        );
        String refreshToken = tokenServicePort.generateToken(
                user.getId(),
                getScope(authentication),
                TokenConfig.REFRESH_TOKEN_EXPIRY_SECONDS,
                "refresh"
        );

        // 4. Build and return response
        AuthResponse response = AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(TokenConfig.ACCESS_TOKEN_EXPIRY_SECONDS)
                .refreshTokenExpiresIn(TokenConfig.REFRESH_TOKEN_EXPIRY_SECONDS)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isReactivated(isReactivated) // Set the flag
                .build();

        log.info("Login successful for user: {}", command.username());
        return response;
    }

    /**
     * Build scope string from authorities
     */
    private String getScope(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(this::normalizeAuthorityForToken)
                .collect(Collectors.joining(" "));
    }

    private String normalizeAuthorityForToken(String authority) {
        if (authority == null) {
            return "";
        }
        return authority.startsWith("ROLE_ROLE_")
                ? authority.replaceFirst("ROLE_ROLE_", "ROLE_")
                : authority;
    }
}
