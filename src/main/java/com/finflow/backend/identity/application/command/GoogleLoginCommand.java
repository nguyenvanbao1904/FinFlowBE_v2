package com.finflow.backend.identity.application.command;

/**
 * Command for authenticating via Google ID token.
 */
public record GoogleLoginCommand(
        String idToken
) {}
