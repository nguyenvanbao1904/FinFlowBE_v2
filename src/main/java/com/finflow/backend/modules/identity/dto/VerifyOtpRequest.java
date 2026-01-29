package com.finflow.backend.modules.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {
    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Email invalid format")
    private String email;

    @NotBlank(message = "OTP is required")
    private String otp;

    @jakarta.validation.constraints.NotNull(message = "Purpose is required")
    private com.finflow.backend.modules.identity.domain.OtpPurpose purpose;
}
