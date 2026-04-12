package com.finflow.backend.identity.infrastructure.configuration;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.common.exception.CommonErrorCode;
import com.finflow.backend.identity.domain.repository.InvalidatedTokenRepository;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final InternalApiKeyFilter internalApiKeyFilter;

    // Prefer config property: jwt.signerKey=${FINFLOW_JWTSIGNERKEY}
    // Fallback: FINFLOW_JWTSIGNERKEY from env/system properties.
    @Value("${jwt.signerKey:}")
    private String jwtSignerKey;

    // --- 1. KEY MANAGEMENT (RSA) ---
    @Bean
    public KeyPair keyPair() {
        String signerKey = jwtSignerKey;
        if (signerKey == null || signerKey.isBlank()) {
            signerKey = System.getProperty("FINFLOW_JWTSIGNERKEY");
        }
        if (signerKey == null || signerKey.isBlank()) {
            signerKey = System.getenv("FINFLOW_JWTSIGNERKEY");
        }
        if (signerKey == null || signerKey.isBlank()) {
            throw new AppException(CommonErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        byte[] seed = toSeedBytes(signerKey);
        SecureRandom secureRandom;
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (Exception ignored) {
            secureRandom = new SecureRandom();
        }
        secureRandom.setSeed(seed);

        try {
            var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            // Deterministic primes => stable JWT keys across restarts (dev simplification).
            keyPairGenerator.initialize(2048, secureRandom);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new AppException(CommonErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyPair keyPair) {
        JWK jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyPair keyPair) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();

        // Định nghĩa Validator check blacklist
        OAuth2TokenValidator<Jwt> withBlacklist = token -> {
            String jti = token.getId(); // Lấy ID của token đang gửi lên
            if (invalidatedTokenRepository.existsById(jti)) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("token_blacklisted", "Token has been invalidated", null)
                );
            }
            return OAuth2TokenValidatorResult.success();
        };

        // Kết hợp: Check Hạn sử dụng (mặc định) + Check Blacklist (vừa viết)
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                withBlacklist
        ));

        return jwtDecoder;
    }

    private static byte[] toSeedBytes(String signerKey) {
        String raw = signerKey.trim();
        // If it looks like hex, decode it. Otherwise use raw UTF-8 bytes.
        if (raw.length() % 2 == 0 && raw.matches("[0-9a-fA-F]+")) {
            int len = raw.length() / 2;
            byte[] out = new byte[len];
            for (int i = 0; i < len; i++) {
                int idx = i * 2;
                out[i] = (byte) Integer.parseInt(raw.substring(idx, idx + 2), 16);
            }
            return out;
        }
        return raw.getBytes(StandardCharsets.UTF_8);
    }

    // --- 2. AUTHENTICATION MANAGER ---
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/register",
        "/api/auth/login",
        "/api/auth/refresh",
        "/api/auth/google",
        "/api/auth/send-otp",
        "/api/auth/verify-otp",
        "/api/auth/reset-password",
        "/api/auth/check-user-existence",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    // --- 3. FILTER CHAIN (Quy định đường đi của Request) ---
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Vì dùng Token nên không cần Session (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public Endpoints
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers("/api/internal/**").permitAll()
                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )
                // Kích hoạt tính năng OAuth2 Resource Server (Tự động check Token)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
        grantedAuthoritiesConverter.setAuthorityPrefix(""); // Bỏ SCOPE_ prefix tự động

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}