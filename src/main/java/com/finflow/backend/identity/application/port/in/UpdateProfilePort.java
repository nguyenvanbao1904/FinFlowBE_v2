package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.UpdateProfileCommand;
import com.finflow.backend.identity.application.dto.UserOutput;

public interface UpdateProfilePort {
    UserOutput execute(UpdateProfileCommand command);
}
