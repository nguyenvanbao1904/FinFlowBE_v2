package com.finflow.backend.identity.infrastructure.adapter;

import com.finflow.backend.identity.application.port.out.CredentialAuthenticationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpringCredentialAuthenticationAdapter implements CredentialAuthenticationPort {

    private final AuthenticationManager authenticationManager;

    @Override
    public AuthenticatedPrincipal authenticate(String usernameOrEmail, String password) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, password)
            );
            String scope = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(this::normalizeAuthorityForToken)
                    .collect(Collectors.joining(" "));
            return new AuthenticatedPrincipal(scope);
        } catch (LockedException ex) {
            throw new AccountLockedException("Account is locked", ex);
        }
    }

    private String normalizeAuthorityForToken(String authority) {
        if (authority == null) {
            return "";
        }
        return authority.startsWith("ROLE_ROLE_")
                ? authority.replaceFirst("ROLE_ROLE_", "ROLE_")
                : authority;
    }
}
