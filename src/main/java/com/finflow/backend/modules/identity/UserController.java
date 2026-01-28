package com.finflow.backend.modules.identity;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.modules.identity.dto.UserResponse;
import com.finflow.backend.modules.identity.exception.IdentityErrorCode;
import com.finflow.backend.modules.identity.domain.entity.User;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import com.finflow.backend.modules.identity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

/**
 * User Controller
 * 
 * Handles HTTP requests for user operations.
 * 
 * Pattern: Controller → Repository (for simple CRUD)
 * 
 * Note: No UseCase needed for simple profile retrieval.
 * According to FEATURE_DEVELOPMENT_GUIDE:
 * - Simple queries should call Repository directly
 * - UseCase only when there's complex business logic
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    // Inject UseCase instead of Repository
    private final com.finflow.backend.modules.identity.usecase.GetProfileUseCase getProfileUseCase;
    private final com.finflow.backend.modules.identity.usecase.UpdateProfileUseCase updateProfileUseCase;

    /**
     * Get authenticated user's profile
     * 
     * Requires JWT authentication (configured in SecurityConfig)
     * 
     * Now delegates to GetProfileUseCase for business logic.
     * 
     * @return UserResponse with profile information
     */
    @GetMapping("/my-profile")
    public ResponseEntity<UserResponse> getMyProfile() {
        // 1. Get username from Security Context
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        
        log.info("Get profile request received for user: {}", username);

        // 2. Delegate to UseCase
        UserResponse response = getProfileUseCase.execute(username);

        log.info("Profile retrieved successfully for user: {}", username);
        return ResponseEntity.ok(response);
    }

    @org.springframework.web.bind.annotation.PutMapping("/my-profile")
    public ResponseEntity<UserResponse> updateProfile(@org.springframework.web.bind.annotation.RequestBody com.finflow.backend.modules.identity.dto.UpdateProfileRequest request) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        return ResponseEntity.ok(updateProfileUseCase.execute(username, request));
    }
}