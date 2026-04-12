package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.ToggleBiometricCommand;

public interface ToggleBiometricPort {
    void execute(ToggleBiometricCommand command);
}
