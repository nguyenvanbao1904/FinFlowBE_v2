package com.finflow.backend.identity.domain.constant;

/**
 * Domain-level constants shared across the identity module.
 */
public final class IdentityConstants {

    private IdentityConstants() {
        // utility class
    }

    // ── Token types ────────────────────────────────────────────────────────────

    /** Token type value embedded in access JWTs. */
    public static final String TOKEN_TYPE_ACCESS = "access";

    /** Token type value embedded in refresh JWTs. */
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    /** Token type value embedded in OTP verification / registration JWTs. */
    public static final String TOKEN_TYPE_REGISTRATION = "REGISTRATION_TOKEN";

    /** Token type value embedded in password-reset JWTs. */
    public static final String TOKEN_TYPE_RESET_PASSWORD = "RESET_PASSWORD_TOKEN";

    /** Token type value embedded in account-deletion confirmation JWTs. */
    public static final String TOKEN_TYPE_DELETE_ACCOUNT = "DELETE_ACCOUNT_TOKEN";

    /** Token type value embedded in PIN-reset JWTs. */
    public static final String TOKEN_TYPE_RESET_PIN = "RESET_PIN_TOKEN";

    // ── Role IDs ───────────────────────────────────────────────────────────────

    /** Default role assigned to every new user. */
    public static final String ROLE_USER = "ROLE_USER";

    /** Administrator role. */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
}
