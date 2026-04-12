package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.presentation.request.CheckUserExistenceRequest;
import com.finflow.backend.identity.presentation.response.CheckUserExistenceResponse;

public interface CheckUserExistencePort {
    CheckUserExistenceResponse execute(CheckUserExistenceRequest request);
}
