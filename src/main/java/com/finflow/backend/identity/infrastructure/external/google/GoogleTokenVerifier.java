package com.finflow.backend.identity.infrastructure.external.google;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Slf4j
@Component
public class GoogleTokenVerifier {

    @Value("${app.security.google.client-id}")
    private String clientId;

    public GoogleIdToken.Payload verify(String idTokenString) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            }
            log.warn("Google ID token verification returned null");
            throw new AppException(IdentityErrorCode.INVALID_TOKEN);
        } catch (GeneralSecurityException | IOException e) {
            log.warn("Google ID token verification failed: {}", e.getMessage());
            throw new AppException(IdentityErrorCode.INVALID_TOKEN);
        }
    }
}
