package com.finflow.backend.identity.presentation.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToggleBiometricRequest {
    @NotNull(message = "BIOMETRIC_TOGGLE_REQUIRED")
    Boolean enabled;
}
