package com.finflow.backend.identity.application.dto;

import lombok.Builder;

@Builder
public record AuthOutput(
        String token,
        String refreshToken,
        String type,
        Long expiresIn,
        Long refreshTokenExpiresIn,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean isReactivated
) {}
