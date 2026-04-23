package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.GoogleLoginCommand;
import com.finflow.backend.identity.application.dto.AuthOutput;

public interface GoogleLoginPort {
    AuthOutput execute(GoogleLoginCommand command);
}
