package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.ResetPasswordCommand;

public interface ResetPasswordPort {
    void execute(ResetPasswordCommand command);
}
