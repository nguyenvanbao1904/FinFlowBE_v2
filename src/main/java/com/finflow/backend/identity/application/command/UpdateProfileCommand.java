package com.finflow.backend.identity.application.command;

import java.time.LocalDate;

/**
 * Command for updating the current user's profile information.
 */
public record UpdateProfileCommand(
        String userId,
        String firstName,
        String lastName,
        LocalDate dob
) {}
