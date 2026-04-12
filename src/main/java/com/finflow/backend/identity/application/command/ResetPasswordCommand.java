package com.finflow.backend.identity.application.command;

/**
 * Command for resetting a user's password using a reset token.
 */
public record ResetPasswordCommand(
        String newPassword,
        String confirmPassword,
        String resetToken
) {}
