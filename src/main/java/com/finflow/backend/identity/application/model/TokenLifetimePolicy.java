package com.finflow.backend.identity.application.model;

/**
 * Token lifetime values owned by application core.
 */
public final class TokenLifetimePolicy {

    private TokenLifetimePolicy() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final long ACCESS_TOKEN_EXPIRY_SECONDS = 2 * 24 * 3600;
    public static final long REFRESH_TOKEN_EXPIRY_SECONDS = 7 * 24 * 3600;
}
