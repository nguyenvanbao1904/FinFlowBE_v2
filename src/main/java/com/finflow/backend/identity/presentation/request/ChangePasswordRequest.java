package com.finflow.backend.identity.presentation.request;

import com.finflow.backend.common.constants.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String oldPassword;

    @NotBlank(message = "PASSWORD_INVALID")
    @Size(min = ValidationConstants.PASSWORD_MIN_LENGTH, message = "PASSWORD_INVALID")
    private String newPassword;
}
