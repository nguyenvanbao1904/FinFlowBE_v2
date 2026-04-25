package com.finflow.backend.identity.infrastructure.configuration;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.common.exception.CommonErrorCode;
import com.finflow.backend.identity.application.port.out.TokenBlacklistCheckPort;
import com.finflow.backend.identity.infrastructure.security.CustomUserDetailsService;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklistCheckPort tokenBlacklistCheckPort;
    private final InternalApiKeyFilter internalApiKeyFilter;

    /**
     * PEM-encoded RSA private key (PKCS#8). Injected from environment variable
     * FINFLOW_JWT_PRIVATE_KEY. If absent, a random dev-only key pair is generated.
     */
    @Value("${FINFLOW_JWT_PRIVATE_KEY:}")
    private String privateKeyPem;

    /**
     * PEM-encoded RSA public key (X.509/SubjectPublicKeyInfo). Injected from
     * environment variable FINFLOW_JWT_PUBLIC_KEY.
     */
    @Value("${FINFLOW_JWT_PUBLIC_KEY:}")
    private String publicKeyPem;

    // --- 1. KEY MANAGEMENT (RSA) ---
    @Bean
    public KeyPair keyPair() {
        boolean hasPrivate = privateKeyPem != null && !privateKeyPem.isBlank();
        boolean hasPublic  = publicKeyPem  != null && !publicKeyPem.isBlank();

        if (hasPrivate && hasPublic) {
            return loadKeyPairFromPem(privateKeyPem, publicKeyPem);
        }

        // Dev fallback: generate a fresh random key pair each startup.
        // WARNING: tokens will be invalidated on every restart. Never use in production.
        log.warn("*** [SECURITY] FINFLOW_JWT_PRIVATE_KEY / FINFLOW_JWT_PUBLIC_KEY are not set. " +
                 "Generating a random RSA key pair for DEV MODE. " +
                 "All existing JWT tokens will be invalidated on restart. " +
                 "Set these environment variables before deploying to production. ***");
        try {
            var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);  // uses platform SecureRandom — non-deterministic
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new AppException(CommonErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Parse PEM strings into a {@link KeyPair}.
     * Expected formats:
     * <ul>
     *   <li>Private key: PKCS#8 PEM (-----BEGIN PRIVATE KEY-----)</li>
     *   <li>Public key:  X.509 SubjectPublicKeyInfo PEM (-----BEGIN PUBLIC KEY-----)</li>
     * </ul>
     */
    private static KeyPair loadKeyPairFromPem(String privatePem, String publicPem) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");

            byte[] privateBytes = decodePem(privatePem);
            RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));

            byte[] publicBytes = decodePem(publicPem);
            RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(publicBytes));

            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new AppException(CommonErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /** Strip PEM headers/footers and Base64-decode to raw DER bytes. */
    private static byte[] decodePem(String pem) {
        String stripped = pem
                .replace("\\n", "\n")
                .replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s+", "");
        return Base64.getDecoder().decode(stripped);
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
            if (tokenBlacklistCheckPort.isBlacklisted(jti)) {
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
        "/swagger-ui.html",
        "/actuator/**"
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
                        .requestMatchers("/api/internal/**").hasRole("INTERNAL_SERVICE")
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