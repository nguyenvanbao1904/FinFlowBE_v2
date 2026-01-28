package com.finflow.backend.modules.identity.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthResponse {
    String token;
    String refreshToken;
    String type;
    Long expiresIn;
    String username;
    String email;
    // Sau này có thể thêm role, avatar...
}