package com.finflow.backend.identity.presentation.controller;

import com.finflow.backend.identity.application.command.UpdateProfileCommand;
import com.finflow.backend.identity.presentation.response.UserResponse;
import com.finflow.backend.identity.presentation.request.UpdateProfileRequest;
import com.finflow.backend.identity.application.port.in.GetProfilePort;
import com.finflow.backend.identity.application.port.in.UpdateProfilePort;
import com.finflow.backend.common.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@ApiVersion("1")
@Tag(name = "User", description = "User profile APIs")
public class UserController {

    private final GetProfilePort getProfilePort;
    private final UpdateProfilePort updateProfilePort;

    @Operation(summary = "Get current user profile")
    @GetMapping("/my-profile")
    public ResponseEntity<UserResponse> getMyProfile() {
        // 1. Get userId from Security Context
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();
        
        log.info("Get profile request received for userId: {}", userId);

        // 2. Delegate to UseCase
        UserResponse response = getProfilePort.execute(userId);

        log.info("Profile retrieved successfully for userId: {}", userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update current user profile")
    @org.springframework.web.bind.annotation.PutMapping("/my-profile")
    public ResponseEntity<UserResponse> updateProfile(
            @org.springframework.web.bind.annotation.RequestBody UpdateProfileRequest request) {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();
        return ResponseEntity.ok(updateProfilePort.execute(
                new UpdateProfileCommand(userId, request.getFirstName(),
                        request.getLastName(), request.getDob())));
    }
}