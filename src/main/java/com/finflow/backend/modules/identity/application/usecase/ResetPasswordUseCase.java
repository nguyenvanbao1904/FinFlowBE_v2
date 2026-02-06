package com.finflow.backend.modules.identity.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import com.finflow.backend.modules.identity.presentation.request.ResetPasswordRequest;
import com.finflow.backend.modules.identity.exception.IdentityErrorCode;
import com.finflow.backend.modules.identity.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @Transactional
    public void execute(ResetPasswordRequest request, String token) {
        // 1. Validate Passwords Match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(IdentityErrorCode.INVALID_CREDENTIALS); // Reuse or simpler error
        }

        // 2. Validate Token
        String email = validateTokenAndGetEmail(token);
        
        // 3. Get User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(IdentityErrorCode.USER_NOT_FOUND));

        // 4. Update Password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user); // JPA implicit save, but explicit is fine
        
        log.info("Password reset successfully for user: {}", email);
    }
    
    private String validateTokenAndGetEmail(String token) {
        try {
            org.springframework.security.oauth2.jwt.Jwt jwt = jwtDecoder.decode(token);
            
            // Validate Token Type
            String type = jwt.getClaimAsString("type");
            if (!"RESET_PASSWORD_TOKEN".equals(type)) {
                log.warn("Invalid token type for reset password: {}", type);
                throw new AppException(IdentityErrorCode.INVALID_TOKEN);
            }

            return jwt.getSubject();
        } catch (Exception e) {
            log.warn("Reset password token validation failed: {}", e.getMessage());
            throw new AppException(IdentityErrorCode.INVALID_TOKEN);
        }
    }
}
