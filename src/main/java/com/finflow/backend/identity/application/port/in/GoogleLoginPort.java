package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.GoogleLoginCommand;
import com.finflow.backend.identity.presentation.response.AuthResponse;

public interface GoogleLoginPort {
    AuthResponse execute(GoogleLoginCommand command);
}
