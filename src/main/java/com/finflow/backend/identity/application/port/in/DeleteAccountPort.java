package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.DeleteAccountCommand;

public interface DeleteAccountPort {
    void execute(DeleteAccountCommand command);
}
