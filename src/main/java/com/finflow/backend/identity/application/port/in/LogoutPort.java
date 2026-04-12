package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.LogoutCommand;

public interface LogoutPort {
    void execute(LogoutCommand command);
}
