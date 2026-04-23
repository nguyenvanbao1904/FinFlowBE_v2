package com.finflow.backend.identity.infrastructure.adapter;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.identity.application.port.out.TokenServicePort;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenServicePort {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Override
    public String generateToken(String subject, String scope, long expirySeconds, String type) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(expirySeconds, ChronoUnit.SECONDS))
                .subject(subject)
                .claim("scope", scope)
                .claim("type", type)
                .id(UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public DecodedToken decodeToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return new DecodedToken(
                    jwt.getSubject(),
                    jwt.getClaimAsString("type"),
                    jwt.getClaims()
            );
        } catch (JwtException e) {
            throw new AppException(IdentityErrorCode.INVALID_TOKEN);
        }
    }
}
