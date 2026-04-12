package com.finflow.backend.identity.application.command;

/**
 * Command for changing the current user's password.
 */
public record ChangePasswordCommand(
        String userId,
        String oldPassword,
        String newPassword
) {}
