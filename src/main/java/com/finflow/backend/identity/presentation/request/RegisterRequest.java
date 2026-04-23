package com.finflow.backend.identity.presentation.request;

import com.finflow.backend.identity.domain.constant.IdentityValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @NotBlank(message = "USERNAME_INVALID")
    @Size(min = IdentityValidationConstants.USERNAME_MIN_LENGTH, message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "PASSWORD_INVALID")
    @Size(min = IdentityValidationConstants.PASSWORD_MIN_LENGTH, message = "PASSWORD_INVALID")
    String password;

    @NotBlank(message = "EMAIL_INVALID")
    @jakarta.validation.constraints.Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "EMAIL_INVALID")
    String email;

    String firstName;
    String lastName;
    LocalDate dob;
}