package com.finflow.backend.identity.application.port.in;
import com.finflow.backend.identity.application.command.VerifyOtpCommand;

import com.finflow.backend.identity.application.dto.VerifyOtpOutput;

public interface VerifyOtpPort {
    VerifyOtpOutput execute(VerifyOtpCommand command);
}
