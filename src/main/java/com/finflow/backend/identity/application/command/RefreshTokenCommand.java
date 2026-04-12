package com.finflow.backend.identity.application.command;

/**
 * Command for refreshing an expired access token using a refresh token.
 */
public record RefreshTokenCommand(
        String refreshToken
) {}
