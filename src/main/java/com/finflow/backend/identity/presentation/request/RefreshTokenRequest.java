package com.finflow.backend.identity.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {
    @NotBlank(message = "TOKEN_REQUIRED")
    private String refreshToken;
}

