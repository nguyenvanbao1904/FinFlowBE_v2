package com.finflow.backend.modules.identity.infrastructure.configuration;

public final class TokenConfig {
    
    private TokenConfig() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * Access token expiration time in seconds
     */
    public static final long ACCESS_TOKEN_EXPIRY_SECONDS = 3600;
    
    /**
     * Refresh token expiration time in seconds
     */
    public static final long REFRESH_TOKEN_EXPIRY_SECONDS = 7 * 24 * 3600;
}
