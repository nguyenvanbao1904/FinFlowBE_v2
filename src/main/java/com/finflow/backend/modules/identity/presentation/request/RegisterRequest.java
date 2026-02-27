package com.finflow.backend.modules.identity.presentation.request;

import com.finflow.backend.common.constants.ValidationConstants;
import jakarta.validation.constraints.Email;
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
    @Size(min = ValidationConstants.USERNAME_MIN_LENGTH, message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "PASSWORD_INVALID")
    @Size(min = ValidationConstants.PASSWORD_MIN_LENGTH, message = "PASSWORD_INVALID")
    String password;

    @NotBlank(message = "EMAIL_INVALID") // Creating a new code or reusing INVALID_KEY if needed. Reusing existing pattern if possible or adding new. Let's check ErrorCode.
    @jakarta.validation.constraints.Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "EMAIL_INVALID")
    String email;

    String firstName;
    String lastName;
    LocalDate dob;
}