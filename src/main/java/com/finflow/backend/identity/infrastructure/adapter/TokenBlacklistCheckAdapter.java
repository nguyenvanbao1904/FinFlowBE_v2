package com.finflow.backend.identity.infrastructure.adapter;

import com.finflow.backend.identity.application.port.out.TokenBlacklistCheckPort;
import com.finflow.backend.identity.domain.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenBlacklistCheckAdapter implements TokenBlacklistCheckPort {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    public boolean isBlacklisted(String jti) {
        return invalidatedTokenRepository.existsById(jti);
    }
}
