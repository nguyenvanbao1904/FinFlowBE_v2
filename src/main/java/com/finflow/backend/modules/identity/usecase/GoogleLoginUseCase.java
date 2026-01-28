package com.finflow.backend.modules.identity.usecase;

import com.finflow.backend.modules.identity.dto.AuthResponse;
import com.finflow.backend.modules.identity.dto.GoogleLoginRequest;
import com.finflow.backend.modules.identity.domain.entity.Role;
import com.finflow.backend.modules.identity.domain.entity.User;
import com.finflow.backend.modules.identity.domain.repository.RoleRepository;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import com.finflow.backend.modules.identity.internal.security.GoogleTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections; // Import Collections
import java.util.HashSet;     // Import HashSet
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GoogleLoginUseCase {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtEncoder jwtEncoder;

    @Transactional
    public AuthResponse execute(GoogleLoginRequest request) {
        // 1. Verify Google Token
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.getIdToken());
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        
    // 2. Find or Create User
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(email, payload));

        // 3. Generate Tokens
        // Convert Set<Role> to space-separated String for scope
        String scope = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(" "));

        String accessToken = generateToken(user.getId(), user.getUsername(), scope, 3600, "access");
        String refreshToken = generateToken(user.getId(), user.getUsername(), scope, 7 * 24 * 3600, "refresh");

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600L)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    private User createNewUser(String email, GoogleIdToken.Payload payload) {
        // Fetch default role
        Role userRole = roleRepository.findById("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));

        User user = new User();
        user.setEmail(email);
        user.setUsername(email); // Use email as username for Google users
        user.setPassword(UUID.randomUUID().toString()); // strong random password
        user.setRoles(new HashSet<>(Collections.singletonList(userRole))); // Use Set<Role>
        
        // Populate names if available
        if (payload.get("given_name") != null) {
            user.setFirstName((String) payload.get("given_name"));
        }
        if (payload.get("family_name") != null) {
            user.setLastName((String) payload.get("family_name"));
        }
        
        return userRepository.save(user);
    }
    
    private String generateToken(String subject, String username, String scope, long expirySeconds, String type) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(expirySeconds, ChronoUnit.SECONDS))
                .subject(username)
                .claim("scope", scope)
                .claim("type", type)
                .id(UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
