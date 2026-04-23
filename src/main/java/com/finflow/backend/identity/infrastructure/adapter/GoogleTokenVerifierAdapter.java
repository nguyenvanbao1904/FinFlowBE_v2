package com.finflow.backend.identity.infrastructure.adapter;

import com.finflow.backend.identity.application.model.GoogleUserInfo;
import com.finflow.backend.identity.application.port.out.VerifyGoogleTokenPort;
import com.finflow.backend.identity.infrastructure.external.google.GoogleTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Driven adapter: verifies a Google ID Token and maps the result to {@link GoogleUserInfo}.
 * Ensures that {@code com.google.api.client} types never leak into the application layer.
 */
@Component
@RequiredArgsConstructor
public class GoogleTokenVerifierAdapter implements VerifyGoogleTokenPort {

    private final GoogleTokenVerifier googleTokenVerifier;

    @Override
    public GoogleUserInfo verify(String idTokenString) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(idTokenString);
        return new GoogleUserInfo(
                payload.getSubject(),
                payload.getEmail(),
                Boolean.TRUE.equals(payload.getEmailVerified()),
                (String) payload.get("given_name"),
                (String) payload.get("family_name")
        );
    }
}
