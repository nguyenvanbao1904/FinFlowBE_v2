package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.command.UpdateProfileCommand;
import com.finflow.backend.identity.presentation.response.UserResponse;

public interface UpdateProfilePort {
    UserResponse execute(UpdateProfileCommand command);
}
