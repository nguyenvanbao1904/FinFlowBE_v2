package com.finflow.backend.identity.application.port.in;
import com.finflow.backend.identity.application.command.SendOtpCommand;

public interface SendOtpPort {
    void execute(SendOtpCommand command);
}
