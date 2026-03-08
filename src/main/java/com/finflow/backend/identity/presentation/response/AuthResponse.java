package com.finflow.backend.identity.presentation.response;

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
    Long expiresIn;              // Access token expiry in seconds
    Long refreshTokenExpiresIn;  // Refresh token expiry in seconds (e.g. 604800 = 7 days)
    String username;
    String email;
    String firstName;
    String lastName;
    
    @com.fasterxml.jackson.annotation.JsonProperty("isReactivated")
    boolean isReactivated; // Flag to indicate if account was just restored from soft delete
}