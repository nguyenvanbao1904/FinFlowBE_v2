package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.port.in.LoginPort;

import com.finflow.backend.identity.application.command.LoginCommand;
import com.finflow.backend.identity.application.dto.AuthOutput;
import com.finflow.backend.identity.application.model.TokenLifetimePolicy;
import com.finflow.backend.identity.application.port.out.CredentialAuthenticationPort;
import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.finflow.backend.identity.application.port.out.TokenServicePort;
import com.finflow.backend.identity.domain.constant.IdentityConstants;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginUseCase implements LoginPort {

    private final CredentialAuthenticationPort credentialAuthenticationPort;
    private final TokenServicePort tokenServicePort;
    private final UserRepository userRepository;

   
    @Transactional
    @Override
    public AuthOutput execute(LoginCommand command) {
        log.debug("Executing login use case for user: {}", command.username());

        CredentialAuthenticationPort.AuthenticatedPrincipal authenticatedPrincipal;
        boolean isReactivated = false;
        try {
            authenticatedPrincipal = credentialAuthenticationPort.authenticate(command.username(), command.password());
        } catch (CredentialAuthenticationPort.AccountLockedException e) {
            log.warn("Account is locked (possibly soft-deleted). Checking for reactivation...");
            // Handle reactivation logic if user is soft-deleted
            User user = userRepository.findByUsername(command.username())
                    .or(() -> userRepository.findByEmail(command.username()))
                    .orElseThrow(() -> e); // Should not happen if locked

            if (user.getDeletedAt() != null) {
                log.info("Soft-deleted user reactivating account");
                user.setIsActive(true);
                user.setDeletedAt(null);
                user.setLastLogin(java.time.LocalDateTime.now());
                userRepository.save(user);
                isReactivated = true;

                // Retry authentication
                authenticatedPrincipal = credentialAuthenticationPort.authenticate(command.username(), command.password());
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
                authenticatedPrincipal.scope(),
                TokenLifetimePolicy.ACCESS_TOKEN_EXPIRY_SECONDS,
                IdentityConstants.TOKEN_TYPE_ACCESS
        );
        String refreshToken = tokenServicePort.generateToken(
                user.getId(),
                authenticatedPrincipal.scope(),
                TokenLifetimePolicy.REFRESH_TOKEN_EXPIRY_SECONDS,
                IdentityConstants.TOKEN_TYPE_REFRESH
        );

        // 4. Build and return response
        AuthOutput response = AuthOutput.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(TokenLifetimePolicy.ACCESS_TOKEN_EXPIRY_SECONDS)
                .refreshTokenExpiresIn(TokenLifetimePolicy.REFRESH_TOKEN_EXPIRY_SECONDS)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isReactivated(isReactivated) // Set the flag
                .build();

        log.info("Login successful");
        return response;
    }
}
