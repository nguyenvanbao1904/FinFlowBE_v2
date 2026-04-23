package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.RefreshTokenCommand;
import com.finflow.backend.identity.application.dto.AuthOutput;

public interface RefreshTokenPort {
    AuthOutput execute(RefreshTokenCommand command);
}
