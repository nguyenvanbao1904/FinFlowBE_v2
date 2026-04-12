package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.presentation.response.UserResponse;

public interface GetProfilePort {
    UserResponse execute(String userId);
}
