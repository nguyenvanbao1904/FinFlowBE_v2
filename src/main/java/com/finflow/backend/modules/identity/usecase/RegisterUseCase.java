package com.finflow.backend.modules.identity.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.modules.identity.dto.RegisterRequest;
import com.finflow.backend.modules.identity.exception.IdentityErrorCode;
import com.finflow.backend.modules.identity.domain.entity.Role;
import com.finflow.backend.modules.identity.domain.entity.User;
import com.finflow.backend.modules.identity.domain.repository.RoleRepository;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Use Case: Register New User
 * 
 * WHY UseCase? ✅ VALID UseCase theo FEATURE_DEVELOPMENT_GUIDE:
 * - Orchestrates 2 repositories: UserRepository + RoleRepository
 * - Complex business logic: Validation (unique username/email) + Password hashing + Role assignment
 * - Transaction spanning multiple operations
 * 
 * Responsibilities:
 * 1. Validate username and email uniqueness
 * 2. Hash password securely
 * 3. Assign default USER role (create if not exists)
 * 4. Create and save user account
 * 
 * Pattern: Controller → UseCase → Multiple Repositories
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    /**
     * Execute user registration
     * 
     * @param request RegisterRequest with user details
     * @throws AppException if username or email already exists
     */
    @Transactional
    public void execute(RegisterRequest request, String registrationToken) {
        log.info("Executing register use case for user: {}", request.getUsername());

        // 1. Validate username is unique
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username {} already exists", request.getUsername());
            throw new AppException(IdentityErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 2. Validate email is unique
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email {} already in use", request.getEmail());
            throw new AppException(IdentityErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 3. Verify Registration Token (Stateless)
        validateRegistrationToken(registrationToken, request.getEmail());
        
        // 4. Get or create default USER role
        Role userRole = roleRepository.findById("ROLE_USER")
                .orElseThrow(() -> new AppException(IdentityErrorCode.ROLE_NOT_FOUND));

        // 5. Create user entity
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .roles(new HashSet<>(Set.of(userRole)))
                .isActive(true)
                .accountVerified(true)
                .build();

        // 6. Save to database
        userRepository.save(newUser);
        
        log.info("User {} registered successfully", request.getUsername());
    }

    private void validateRegistrationToken(String token, String email) {
        try {
            org.springframework.security.oauth2.jwt.Jwt jwt = jwtDecoder.decode(token);
            
            // Validate Token Type
            String type = jwt.getClaimAsString("type");
            if (!"REGISTRATION_TOKEN".equals(type)) {
                log.warn("Invalid token type: {}", type);
                throw new AppException(IdentityErrorCode.INVALID_TOKEN);
            }

            // Validate Email (Subject)
            String subject = jwt.getSubject();
            if (!email.equals(subject)) {
                log.warn("Token subject {} does not match email {}", subject, email);
                throw new AppException(IdentityErrorCode.INVALID_TOKEN);
            }
            
            // Expiry is checked automatically by jwtDecoder
            
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            throw new AppException(IdentityErrorCode.INVALID_TOKEN);
        }
    }
}
