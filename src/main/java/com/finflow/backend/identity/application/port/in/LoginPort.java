package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.LoginCommand;
import com.finflow.backend.identity.application.dto.AuthOutput;

public interface LoginPort {
    AuthOutput execute(LoginCommand command);
}
