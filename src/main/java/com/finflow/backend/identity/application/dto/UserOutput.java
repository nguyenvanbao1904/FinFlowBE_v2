package com.finflow.backend.identity.application.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record UserOutput(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        LocalDate dob,
        Boolean isBiometricEnabled,
        Boolean hasPassword,
        Set<String> roles
) {}
