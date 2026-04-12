package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.RegisterCommand;

public interface RegisterUserPort {
    void execute(RegisterCommand command);
}
