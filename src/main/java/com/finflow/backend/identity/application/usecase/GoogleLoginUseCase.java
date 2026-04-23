package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.port.in.GoogleLoginPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.identity.application.dto.AuthOutput;
import com.finflow.backend.identity.application.model.GoogleUserInfo;
import com.finflow.backend.identity.application.model.TokenLifetimePolicy;
import com.finflow.backend.identity.application.port.out.VerifyGoogleTokenPort;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import com.finflow.backend.identity.application.command.GoogleLoginCommand;
import com.finflow.backend.identity.domain.entity.Role;
import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.enums.AuthProvider;
import com.finflow.backend.identity.domain.repository.RoleRepository;
import com.finflow.backend.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.finflow.backend.identity.application.port.out.TokenServicePort;
import com.finflow.backend.identity.domain.constant.IdentityConstants;


import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GoogleLoginUseCase implements GoogleLoginPort {

    private final VerifyGoogleTokenPort verifyGoogleTokenPort;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenServicePort tokenServicePort;

    @Transactional
    @Override
    public AuthOutput execute(GoogleLoginCommand command) {
        // 1. Verify Google Token
        GoogleUserInfo userInfo = verifyGoogleTokenPort.verify(command.idToken());
        String email = userInfo.email();

    // 2. Find or Create User
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(email, userInfo));
        
        // 3. Check if account was soft-deleted and restore it
        boolean isReactivated = false;
        if (user.getDeletedAt() != null) {
            user.setIsActive(true);
            user.setDeletedAt(null);
            user.setLastLogin(java.time.LocalDateTime.now());
            userRepository.save(user);
            isReactivated = true;
        }

        // 4. Generate Tokens
        // Convert Set<Role> to space-separated String for scope
        String scope = user.getRoles().stream()
                .map(Role::getName)
                .map(this::normalizeRole)
                .collect(Collectors.joining(" "));

        String accessToken = tokenServicePort.generateToken(user.getId(), scope, TokenLifetimePolicy.ACCESS_TOKEN_EXPIRY_SECONDS, IdentityConstants.TOKEN_TYPE_ACCESS);
        String refreshToken = tokenServicePort.generateToken(user.getId(), scope, TokenLifetimePolicy.REFRESH_TOKEN_EXPIRY_SECONDS, IdentityConstants.TOKEN_TYPE_REFRESH);

        return AuthOutput.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(TokenLifetimePolicy.ACCESS_TOKEN_EXPIRY_SECONDS)
                .refreshTokenExpiresIn(TokenLifetimePolicy.REFRESH_TOKEN_EXPIRY_SECONDS)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .isReactivated(isReactivated)
                .build();
    }

    private User createNewUser(String email, GoogleUserInfo userInfo) {
        // Fetch default role
        Role userRole = roleRepository.findById(IdentityConstants.ROLE_USER)
                .orElseThrow(() -> new AppException(IdentityErrorCode.ROLE_NOT_FOUND));

        User user = new User();
        user.setEmail(email);
        user.setUsername(email); // Use email as username for Google users
        user.setPassword(null); // OAuth2 users have no password
        user.setProvider(AuthProvider.GOOGLE);
        user.setRoles(new HashSet<>(Collections.singletonList(userRole))); // Use Set<Role>
        user.setAccountVerified(userInfo.emailVerified());

        // Populate names if available
        if (userInfo.givenName() != null) {
            user.setFirstName(userInfo.givenName());
        }
        if (userInfo.familyName() != null) {
            user.setLastName(userInfo.familyName());
        }

        return userRepository.save(user);
    }

    private String normalizeRole(String role) {
        if (role == null) return "";
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
}
