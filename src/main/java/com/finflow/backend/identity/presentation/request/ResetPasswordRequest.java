package com.finflow.backend.identity.presentation.request;

import com.finflow.backend.identity.domain.constant.IdentityValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "PASSWORD_INVALID")
    @Size(min = IdentityValidationConstants.PASSWORD_MIN_LENGTH, message = "PASSWORD_INVALID")
    private String password;

    @NotBlank(message = "CONFIRM_PASSWORD_REQUIRED")
    private String confirmPassword;
}
