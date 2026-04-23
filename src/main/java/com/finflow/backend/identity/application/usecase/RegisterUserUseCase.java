package com.finflow.backend.identity.application.usecase;
import com.finflow.backend.identity.application.port.out.TokenServicePort;
import com.finflow.backend.identity.domain.constant.IdentityConstants;

import com.finflow.backend.identity.application.port.in.RegisterUserPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.identity.application.command.RegisterCommand;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import com.finflow.backend.identity.domain.entity.Role;
import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.RoleRepository;
import com.finflow.backend.identity.domain.repository.UserRepository;
import com.finflow.backend.identity.domain.enums.AuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.finflow.backend.identity.application.port.out.PasswordEncoderPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegisterUserUseCase implements RegisterUserPort {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenServicePort tokenServicePort;

    @Transactional
    @Override
    public void execute(RegisterCommand command) {
        String registrationToken = command.registrationToken();
        log.debug("Executing register use case");

        // 1. Validate username is unique
        if (userRepository.existsByUsername(command.username())) {
            log.warn("Registration failed: Username already exists");
            throw new AppException(IdentityErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 2. Validate email is unique
        if (userRepository.existsByEmail(command.email())) {
            log.warn("Registration failed: Email already in use");
            throw new AppException(IdentityErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 3. Verify Registration Token (Stateless)
        validateRegistrationToken(registrationToken, command.email());
        
        // 4. Get or create default USER role
        Role userRole = roleRepository.findById(IdentityConstants.ROLE_USER)
                .orElseThrow(() -> new AppException(IdentityErrorCode.ROLE_NOT_FOUND));

        // 5. Create user entity
        User newUser = new User();
        newUser.setUsername(command.username());
        newUser.setEmail(command.email());
        newUser.setPassword(passwordEncoder.encode(command.password()));
        newUser.setFirstName(command.firstName());
        newUser.setLastName(command.lastName());
        newUser.setDob(command.dob());
        newUser.setRoles(new HashSet<>(Set.of(userRole)));
        newUser.setProvider(AuthProvider.LOCAL);
        newUser.setIsActive(true);
        newUser.setAccountVerified(true);

        // 6. Save to database
        userRepository.save(newUser);
        
        log.info("User registered successfully");
    }

    private void validateRegistrationToken(String token, String email) {
        try {
            TokenServicePort.DecodedToken decoded = tokenServicePort.decodeToken(token);
            
            // Validate Token Type
            String type = decoded.type();
            if (!IdentityConstants.TOKEN_TYPE_REGISTRATION.equals(type)) {
                log.warn("Invalid token type: {}", type);
                throw new AppException(IdentityErrorCode.INVALID_TOKEN);
            }

            // Validate Email (Subject)
            String subject = decoded.subject();
            if (!email.equals(subject)) {
                log.warn("Token subject does not match registration email");
                throw new AppException(IdentityErrorCode.INVALID_TOKEN);
            }
            
            // Expiry is checked automatically by jwtDecoder
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            throw new AppException(IdentityErrorCode.INVALID_TOKEN);
        }
    }
}
