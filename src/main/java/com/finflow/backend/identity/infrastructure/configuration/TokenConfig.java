package com.finflow.backend.identity.infrastructure.configuration;

public final class TokenConfig {
    
    private TokenConfig() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * Access token expiration time in seconds (2 days)
     */
    public static final long ACCESS_TOKEN_EXPIRY_SECONDS = 2 * 24 * 3600;
    
    /**
     * Refresh token expiration time in seconds (7 days)
     */
    public static final long REFRESH_TOKEN_EXPIRY_SECONDS = 7 * 24 * 3600;
}
