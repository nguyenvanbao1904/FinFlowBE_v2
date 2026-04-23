package com.finflow.backend.identity.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckUserExistenceRequest {
    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String username;
}
