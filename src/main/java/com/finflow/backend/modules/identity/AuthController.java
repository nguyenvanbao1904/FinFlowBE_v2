package com.finflow.backend.modules.identity;

import com.finflow.backend.modules.identity.dto.*;
import com.finflow.backend.modules.identity.dto.SendOtpRequest;
import com.finflow.backend.modules.identity.dto.VerifyOtpRequest;
import com.finflow.backend.modules.identity.usecase.LoginUseCase;
import com.finflow.backend.modules.identity.usecase.LogoutUseCase;
import com.finflow.backend.modules.identity.usecase.RefreshTokenUseCase;
import com.finflow.backend.modules.identity.usecase.RegisterUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Authentication Controller
 * 
 * Handles HTTP requests for authentication operations.
 * Delegates complex business logic to UseCases.
 * 
 * Pattern Decision (theo FEATURE_DEVELOPMENT_GUIDE):
 * - Login: UseCase ✅ (orchestrate AuthManager + JWT + UserRepo)
 * - Register: UseCase ✅ (orchestrate 2 repos + validation + hashing)
 * - Logout: UseCase ✅ (JWT parsing + blacklist logic)
 * 
 * Controller responsibilities:
 * - HTTP request/response handling
 * - Request validation (@Valid)
 * - Delegate to UseCases for business logic
 * - Return appropriate HTTP status codes
 * 
 * Pattern: Controller → Use Case → Repository(s)
 */
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
    private final com.finflow.backend.modules.identity.usecase.GoogleLoginUseCase googleLoginUseCase;
    private final com.finflow.backend.modules.identity.usecase.SendOtpUseCase sendOtpUseCase;
    private final com.finflow.backend.modules.identity.usecase.VerifyOtpUseCase verifyOtpUseCase;
    private final com.finflow.backend.modules.identity.usecase.ResetPasswordUseCase resetPasswordUseCase;
    private final com.finflow.backend.modules.identity.usecase.CheckUserExistenceUseCase checkUserExistenceUseCase;

    /**
     * Register new user account
     * 
     * @param request RegisterRequest with user details
     * @return Success message
     */
    /**
     * Register new user account
     * 
     * @param request RegisterRequest with user details
     * @return Success message
     */
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

    /**
     * Login user and return JWT token
     * 
     * @param request LoginRequest with credentials
     * @return AuthResponse with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.getUsername());
        AuthResponse response = loginUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using refresh token
     *
     * @param request RefreshTokenRequest with refresh token
     * @return AuthResponse with new tokens
     */
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
            @RequestBody @Valid com.finflow.backend.modules.identity.dto.ResetPasswordRequest request,
            @RequestHeader("X-Reset-Token") String token
    ) {
        log.info("Reset password request received");
        resetPasswordUseCase.execute(request, token);
        return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
    }

    /**
     * Logout user by invalidating JWT token
     * 
     * @param request HttpServletRequest to extract token from header
     * @return Empty response
     */
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
}