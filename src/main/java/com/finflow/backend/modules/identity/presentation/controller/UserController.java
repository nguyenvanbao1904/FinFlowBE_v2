package com.finflow.backend.modules.identity.presentation.controller;

import com.finflow.backend.modules.identity.presentation.response.UserResponse;
import com.finflow.backend.modules.identity.presentation.request.UpdateProfileRequest;
import com.finflow.backend.modules.identity.application.usecase.GetProfileUseCase;
import com.finflow.backend.modules.identity.application.usecase.UpdateProfileUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;

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
    public ResponseEntity<UserResponse> updateProfile(@org.springframework.web.bind.annotation.RequestBody UpdateProfileRequest request) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        return ResponseEntity.ok(updateProfileUseCase.execute(username, request));
    }
}