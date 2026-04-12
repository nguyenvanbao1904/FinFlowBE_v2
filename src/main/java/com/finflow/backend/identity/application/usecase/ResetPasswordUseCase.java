package com.finflow.backend.identity.application.usecase;
import com.finflow.backend.identity.application.port.out.TokenServicePort;

import com.finflow.backend.identity.application.port.in.ResetPasswordPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.identity.domain.repository.UserRepository;
import com.finflow.backend.identity.application.command.ResetPasswordCommand;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import com.finflow.backend.identity.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.finflow.backend.identity.application.port.out.PasswordEncoderPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordUseCase implements ResetPasswordPort {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenServicePort tokenServicePort;

    @Transactional
    @Override
    public void execute(ResetPasswordCommand command) {
        // 1. Validate Passwords Match
        if (!command.newPassword().equals(command.confirmPassword())) {
            throw new AppException(IdentityErrorCode.INVALID_CREDENTIALS);
        }

        // 2. Validate Token
        String email = validateTokenAndGetEmail(command.resetToken());
        
        // 3. Get User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(IdentityErrorCode.USER_NOT_FOUND));

        // 4. Update Password
        user.setPassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);
        
        log.info("Password reset successfully for user: {}", email);
    }
    
    private String validateTokenAndGetEmail(String token) {
        try {
            TokenServicePort.DecodedToken decoded = tokenServicePort.decodeToken(token);
            
            // Validate Token Type
            String type = decoded.type();
            if (!"RESET_PASSWORD_TOKEN".equals(type)) {
                log.warn("Invalid token type for reset password: {}", type);
                throw new AppException(IdentityErrorCode.INVALID_TOKEN);
            }

            return decoded.subject();
        } catch (Exception e) {
            log.warn("Reset password token validation failed: {}", e.getMessage());
            throw new AppException(IdentityErrorCode.INVALID_TOKEN);
        }
    }
}
