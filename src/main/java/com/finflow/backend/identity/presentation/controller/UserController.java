package com.finflow.backend.identity.presentation.controller;

import com.finflow.backend.identity.application.command.UpdateProfileCommand;
import com.finflow.backend.identity.presentation.mapper.IdentityPresentationMapper;
import com.finflow.backend.identity.presentation.response.UserResponse;
import com.finflow.backend.identity.presentation.request.UpdateProfileRequest;
import com.finflow.backend.identity.application.port.in.GetProfilePort;
import com.finflow.backend.identity.application.port.in.UpdateProfilePort;
import com.finflow.backend.identity.application.query.GetProfileQuery;
import com.finflow.backend.common.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final IdentityPresentationMapper mapper;

    @Operation(summary = "Get current user profile")
    @GetMapping("/my-profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Get profile request received for userId: {}", userId);

        // 2. Delegate to UseCase
        var response = getProfilePort.execute(new GetProfileQuery(userId));

        log.info("Profile retrieved successfully for userId: {}", userId);
        return ResponseEntity.ok(mapper.toUserResponse(response));
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/my-profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request) {
        String userId = jwt.getSubject();
        var output = updateProfilePort.execute(
                new UpdateProfileCommand(userId, request.getFirstName(),
                        request.getLastName(), request.getDob()));
        return ResponseEntity.ok(mapper.toUserResponse(output));
    }
}
