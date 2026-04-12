package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.RefreshTokenCommand;
import com.finflow.backend.identity.presentation.response.AuthResponse;

public interface RefreshTokenPort {
    AuthResponse execute(RefreshTokenCommand command);
}
