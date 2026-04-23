package com.finflow.backend.identity.presentation.controller;

import com.finflow.backend.identity.application.command.*;
import com.finflow.backend.identity.application.query.CheckUserExistenceQuery;
import com.finflow.backend.identity.presentation.mapper.IdentityPresentationMapper;
import com.finflow.backend.identity.presentation.request.*;
import com.finflow.backend.identity.presentation.response.*;
import com.finflow.backend.identity.application.port.in.*;
import com.finflow.backend.common.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final RegisterUserPort registerUserPort;
    private final LoginPort loginPort;
    private final LogoutPort logoutPort;
    private final RefreshTokenPort refreshTokenPort;
    private final GoogleLoginPort googleLoginPort;
    private final SendOtpPort sendOtpPort;
    private final VerifyOtpPort verifyOtpPort;
    private final ResetPasswordPort resetPasswordPort;
    private final CheckUserExistencePort checkUserExistencePort;
    private final ToggleBiometricPort toggleBiometricPort;
    private final ChangePasswordPort changePasswordPort;
    private final DeleteAccountPort deleteAccountPort;
    private final IdentityPresentationMapper mapper;

    @Operation(summary = "Register a new user (requires X-Registration-Token)")
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(
            @RequestBody @Valid RegisterRequest request,
            @RequestHeader("X-Registration-Token") String registrationToken
    ) {
        log.debug("Register request received for username: {}", request.getUsername());
        registerUserPort.execute(new RegisterCommand(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getDob(),
                registrationToken
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("User registered successfully!"));
    }

    @Operation(summary = "Login with username and password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login request received for username: {}", request.getUsername());
        var output = loginPort.execute(
                new LoginCommand(request.getUsername(), request.getPassword()));
        return ResponseEntity.ok(mapper.toAuthResponse(output));
    }

    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request received");
        var output = refreshTokenPort.execute(
                new RefreshTokenCommand(request.getRefreshToken()));
        return ResponseEntity.ok(mapper.toAuthResponse(output));
    }

    @Operation(summary = "Login with Google ID token")
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody @Valid GoogleLoginRequest request) {
        log.info("Google login request received");
        var output = googleLoginPort.execute(
                new GoogleLoginCommand(request.getIdToken()));
        return ResponseEntity.ok(mapper.toAuthResponse(output));
    }

    @Operation(summary = "Send OTP to email (for reset password or verification)")
    @PostMapping("/send-otp")
    public ResponseEntity<MessageResponse> sendOtp(@RequestBody @Valid SendOtpRequest request) {
        log.debug("Send OTP request for purpose: {}", request.getPurpose());
        sendOtpPort.execute(new SendOtpCommand(request.getEmail(), request.getPurpose()));
        return ResponseEntity.ok(new MessageResponse("OTP sent successfully"));
    }

    @Operation(summary = "Verify OTP and get reset token or verification result")
    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@RequestBody @Valid VerifyOtpRequest request) {
        log.debug("Verify OTP request for purpose: {}", request.getPurpose());
        var output = verifyOtpPort.execute(
                new VerifyOtpCommand(request.getEmail(), request.getOtp(), request.getPurpose()));
        return ResponseEntity.ok(mapper.toVerifyOtpResponse(output));
    }

    @Operation(summary = "Reset password with token from verify-otp (requires X-Reset-Token)")
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request,
            @RequestHeader("X-Reset-Token") String token
    ) {
        log.info("Reset password request received");
        resetPasswordPort.execute(new ResetPasswordCommand(
                request.getPassword(),
                request.getConfirmPassword(),
                token
        ));
        return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
    }

    @Operation(summary = "Logout and invalidate refresh token")
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        log.info("Logout request received");

        // 1. Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            // 2. Remove "Bearer " prefix (first 7 characters)
            String token = authHeader.substring(7);

            // 3. Execute logout use case
            logoutPort.execute(new LogoutCommand(token));
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Check if user exists by email (e.g. for forgot password)")
    @PostMapping("/check-user-existence")
    public ResponseEntity<CheckUserExistenceResponse> checkUserExistence(
            @RequestBody @Valid CheckUserExistenceRequest request) {
        log.debug("Check user existence request received");
        var output = checkUserExistencePort.execute(
                new CheckUserExistenceQuery(request.getEmail(), request.getUsername()));
        return ResponseEntity.ok(mapper.toCheckUserExistenceResponse(output));
    }

    @Operation(summary = "Enable or disable biometric authentication for current user")
    @PostMapping("/toggle-biometric")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MessageResponse> toggleBiometric(
            @RequestBody @Valid ToggleBiometricRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();

        log.info("Toggle biometric request for userId: {}", userId);
        toggleBiometricPort.execute(new ToggleBiometricCommand(userId, request.getEnabled()));

        return ResponseEntity.ok(new MessageResponse(
            request.getEnabled()
                ? "Biometric authentication enabled successfully"
                : "Biometric authentication disabled successfully"
        ));
    }

    @Operation(summary = "Change password for authenticated user")
    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MessageResponse> changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();

        log.info("Change password request for userId: {}", userId);
        changePasswordPort.execute(new ChangePasswordCommand(
                userId, request.getOldPassword(), request.getNewPassword()));

        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    @Operation(summary = "Soft delete current user account")
    @DeleteMapping("/delete-account")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MessageResponse> deleteAccount(
            @RequestBody(required = false) DeleteAccountRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();

        // Handle null request body (if frontend sends nothing for google users)
        if (request == null) {
            request = new DeleteAccountRequest();
        }

        log.info("Delete account request for userId: {}", userId);
        deleteAccountPort.execute(new DeleteAccountCommand(userId, request.getPassword()));

        return ResponseEntity.ok(new MessageResponse("Account deleted successfully"));
    }
}
