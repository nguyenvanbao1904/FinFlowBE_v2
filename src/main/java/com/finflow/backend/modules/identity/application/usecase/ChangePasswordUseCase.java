package com.finflow.backend.modules.identity.application.usecase;

import com.finflow.backend.modules.identity.domain.entity.User;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import com.finflow.backend.modules.identity.exception.IdentityErrorCode;
import com.finflow.backend.modules.identity.presentation.request.ChangePasswordRequest;
import com.finflow.backend.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public void execute(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(IdentityErrorCode.USER_NOT_FOUND));

        // 1. Check if user has a password in DB
        boolean hasPasswordInDb = user.getPassword() != null && !user.getPassword().isEmpty();

        if (hasPasswordInDb) {
            // Case 1: User has password -> oldPassword is MANDATORY and MUST match
            if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
                throw new AppException(IdentityErrorCode.OLD_PASSWORD_REQUIRED);
            }
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new AppException(IdentityErrorCode.INVALID_PASSWORD); 
            }
            
            // Check new != old
            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                throw new AppException(IdentityErrorCode.NEW_PASSWORD_SAME_AS_OLD);
            }
        } 
        // Case 2: User has NO password (OAuth2) -> Allow setting new password without old one.

        // 2. Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
