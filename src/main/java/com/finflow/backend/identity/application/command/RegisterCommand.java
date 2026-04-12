package com.finflow.backend.identity.application.command;

import java.time.LocalDate;

/**
 * Command for registering a new user account.
 * Decouples the application layer from the presentation layer's request DTO.
 */
public record RegisterCommand(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        LocalDate dob,
        String registrationToken
) {}
