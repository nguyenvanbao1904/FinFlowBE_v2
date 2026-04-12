package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.ChangePasswordCommand;

public interface ChangePasswordPort {
    void execute(ChangePasswordCommand command);
}
