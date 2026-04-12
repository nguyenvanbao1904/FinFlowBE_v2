package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.LoginCommand;
import com.finflow.backend.identity.presentation.response.AuthResponse;

public interface LoginPort {
    AuthResponse execute(LoginCommand command);
}
