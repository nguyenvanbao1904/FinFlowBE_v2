package com.finflow.backend.identity.presentation.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckUserExistenceRequest {
    private String email;
    private String username;
}
