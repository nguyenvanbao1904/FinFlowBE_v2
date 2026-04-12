package com.finflow.backend.identity.application.command;

/**
 * Command for invalidating a user's JWT access token on logout.
 */
public record LogoutCommand(
        String accessToken
) {}
