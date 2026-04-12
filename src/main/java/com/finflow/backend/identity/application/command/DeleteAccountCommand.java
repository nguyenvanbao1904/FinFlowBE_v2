package com.finflow.backend.identity.application.command;

/**
 * Command for soft-deleting the current user's account.
 */
public record DeleteAccountCommand(
        String userId,
        String password,
        String verificationToken
) {}
