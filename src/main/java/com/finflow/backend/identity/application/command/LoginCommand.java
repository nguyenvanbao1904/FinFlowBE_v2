package com.finflow.backend.identity.application.command;

/**
 * Command for authenticating a user with username and password.
 */
public record LoginCommand(
        String username,
        String password
) {}
