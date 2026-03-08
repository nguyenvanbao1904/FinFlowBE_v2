package com.finflow.backend.identity.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    @NotBlank(message = "TOKEN_REQUIRED")
    private String idToken;
}
