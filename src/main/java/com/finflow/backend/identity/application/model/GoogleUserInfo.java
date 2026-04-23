package com.finflow.backend.identity.application.model;

/**
 * Verified Google user information extracted from a Google ID Token.
 * Application-layer model — does not expose any Google SDK types.
 */
public record GoogleUserInfo(
        String subject,        // Google's unique user ID (sub claim)
        String email,
        boolean emailVerified,
        String givenName,      // nullable
        String familyName      // nullable
) {}
