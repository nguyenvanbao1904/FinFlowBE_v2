package com.finflow.backend.modules.identity.presentation.controller;

import com.finflow.backend.modules.identity.presentation.request.*;
import com.finflow.backend.modules.identity.presentation.response.*;
import com.finflow.backend.modules.identity.application.usecase.*;
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

   
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.getUsername());
        AuthResponse response = loginUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request received");
        AuthResponse response = refreshTokenUseCase.execute(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody @Valid GoogleLoginRequest request) {
        log.info("Google login request received");
        AuthResponse response = googleLoginUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<MessageResponse> sendOtp(@RequestBody @Valid SendOtpRequest request) {
        log.info("Send OTP request for: {} with purpose: {}", request.getEmail(), request.getPurpose());
        sendOtpUseCase.execute(request.getEmail(), request.getPurpose());
        return ResponseEntity.ok(new MessageResponse("OTP sent successfully"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@RequestBody @Valid VerifyOtpRequest request) {
        log.info("Verify OTP request for: {} with purpose: {}", request.getEmail(), request.getPurpose());
        VerifyOtpResponse response = verifyOtpUseCase.execute(request.getEmail(), request.getOtp(), request.getPurpose());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request,
            @RequestHeader("X-Reset-Token") String token
    ) {
        log.info("Reset password request received");
        resetPasswordUseCase.execute(request, token);
        return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
    }

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
    
    @PostMapping("/check-user-existence")
    public ResponseEntity<CheckUserExistenceResponse> checkUserExistence(@RequestBody @Valid CheckUserExistenceRequest request) {
        log.info("Check user existence request received for email: {}", request.getEmail());
        CheckUserExistenceResponse response = checkUserExistenceUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/toggle-biometric")
    public ResponseEntity<MessageResponse> toggleBiometric(
            @RequestBody @Valid ToggleBiometricRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        
        log.info("Toggle biometric request for user: {}", username);
        toggleBiometricUseCase.execute(username, request);
        
        return ResponseEntity.ok(new MessageResponse(
            request.getEnabled() 
                ? "Biometric authentication enabled successfully" 
                : "Biometric authentication disabled successfully"
        ));
    }
}