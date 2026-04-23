package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.port.in.ChangePasswordPort;

import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.UserRepository;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import com.finflow.backend.identity.application.command.ChangePasswordCommand;
import com.finflow.backend.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import com.finflow.backend.identity.application.port.out.PasswordEncoderPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChangePasswordUseCase implements ChangePasswordPort {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Transactional
    @Override
    public void execute(ChangePasswordCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new AppException(IdentityErrorCode.USER_NOT_FOUND));

        // 1. Check if user has a password in DB
        boolean hasPasswordInDb = user.getPassword() != null && !user.getPassword().isEmpty();

        if (hasPasswordInDb) {
            // Case 1: User has password -> oldPassword is MANDATORY and MUST match
            if (command.oldPassword() == null || command.oldPassword().isEmpty()) {
                throw new AppException(IdentityErrorCode.OLD_PASSWORD_REQUIRED);
            }
            if (!passwordEncoder.matches(command.oldPassword(), user.getPassword())) {
                throw new AppException(IdentityErrorCode.INVALID_PASSWORD); 
            }
            
            // Check new != old
            if (passwordEncoder.matches(command.newPassword(), user.getPassword())) {
                throw new AppException(IdentityErrorCode.NEW_PASSWORD_SAME_AS_OLD);
            }
        } 
        // Case 2: User has NO password (OAuth2) -> Allow setting new password without old one.

        // 2. Update password
        user.setPassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);
    }
}
