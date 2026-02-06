package com.finflow.backend.modules.identity.presentation.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToggleBiometricRequest {
    @NotNull(message = "enabled field is required")
    Boolean enabled;
}
