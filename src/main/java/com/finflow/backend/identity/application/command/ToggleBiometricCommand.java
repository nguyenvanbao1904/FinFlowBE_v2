package com.finflow.backend.identity.application.command;

/**
 * Command for enabling or disabling biometric authentication.
 */
public record ToggleBiometricCommand(
        String userId,
        Boolean enabled
) {}
