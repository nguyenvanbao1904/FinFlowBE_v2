package com.finflow.backend.identity.presentation.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountRequest {
    @Size(min = 8, max = 128)
    private String password;
}
