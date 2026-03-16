package com.finflow.backend.identity.presentation.controller;

import com.finflow.backend.identity.presentation.request.*;
import com.finflow.backend.identity.presentation.response.*;
import com.finflow.backend.identity.application.usecase.*;
import com.finflow.backend.common.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@ApiVersion("1")
@Tag(name = "Auth", description = "Authentication and account management APIs")
public class AuthController {

    // Inject Use Cases instead of Services
    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final GoogleLoginUseCase googleLoginUseCase;
    private final SendOtpUseCase sendOtpUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final CheckUserExistenceUseCase checkUserExistenceUseCase;
    private final ToggleBiometricUseCase toggleBiometricUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;

    @Operation(summary = "Register a new user (requires X-Registration-Token)")
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(
            @RequestBody @Valid RegisterRequest request,
            @RequestHeader("X-Registration-Token") String registrationToken
    ) {
        log.info("Register request received for username: {}", request.getUsername());
        registerUseCase.execute(request, registrationToken);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("User registered successfully!"));
    }

    @Operation(summary = "Login with username and password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.getUsername());
        AuthResponse response = loginUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request received");
        AuthResponse response = refreshTokenUseCase.execute(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Login with Google ID token")
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody @Valid GoogleLoginRequest request) {
        log.info("Google login request received");
        AuthResponse response = googleLoginUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Send OTP to email (for reset password or verification)")
    @PostMapping("/send-otp")
    public ResponseEntity<MessageResponse> sendOtp(@RequestBody @Valid SendOtpRequest request) {
        log.info("Send OTP request for: {} with purpose: {}", request.getEmail(), request.getPurpose());
        sendOtpUseCase.execute(request.getEmail(), request.getPurpose());
        return ResponseEntity.ok(new MessageResponse("OTP sent successfully"));
    }

    @Operation(summary = "Verify OTP and get reset token or verification result")
    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@RequestBody @Valid VerifyOtpRequest request) {
        log.info("Verify OTP request for: {} with purpose: {}", request.getEmail(), request.getPurpose());
        VerifyOtpResponse response = verifyOtpUseCase.execute(request.getEmail(), request.getOtp(), request.getPurpose());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reset password with token from verify-otp (requires X-Reset-Token)")
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request,
            @RequestHeader("X-Reset-Token") String token
    ) {
        log.info("Reset password request received");
        resetPasswordUseCase.execute(request, token);
        return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
    }

    @Operation(summary = "Logout and invalidate refresh token")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        log.info("Logout request received");

        // 1. Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            // 2. Remove "Bearer " prefix (first 7 characters)
            String token = authHeader.substring(7);

            // 3. Execute logout use case
            logoutUseCase.execute(token);
        }

        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Check if user exists by email (e.g. for forgot password)")
    @PostMapping("/check-user-existence")
    public ResponseEntity<CheckUserExistenceResponse> checkUserExistence(@RequestBody @Valid CheckUserExistenceRequest request) {
        log.info("Check user existence request received for email: {}", request.getEmail());
        CheckUserExistenceResponse response = checkUserExistenceUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Enable or disable biometric authentication for current user")
    @PostMapping("/toggle-biometric")
    public ResponseEntity<MessageResponse> toggleBiometric(
            @RequestBody @Valid ToggleBiometricRequest request
    ) {
        String userId = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        
        log.info("Toggle biometric request for userId: {}", userId);
        toggleBiometricUseCase.execute(userId, request);
        
        return ResponseEntity.ok(new MessageResponse(
            request.getEnabled() 
                ? "Biometric authentication enabled successfully" 
                : "Biometric authentication disabled successfully"
        ));
    }
    @Operation(summary = "Change password for authenticated user")
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        String userId = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        
        log.info("Change password request for userId: {}", userId);
        changePasswordUseCase.execute(userId, request);
        
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    @Operation(summary = "Soft delete current user account")
    @DeleteMapping("/delete-account")
    public ResponseEntity<MessageResponse> deleteAccount(@RequestBody(required = false) DeleteAccountRequest request) {
        String userId = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        
        // Handle null request body (if frontend sends nothing for google users, though standard is sending empty json)
        if (request == null) {
            request = new DeleteAccountRequest();
        }

        log.info("Delete account request for userId: {}", userId);
        deleteAccountUseCase.execute(userId, request);
        
        return ResponseEntity.ok(new MessageResponse("Account deleted successfully"));
    }
}