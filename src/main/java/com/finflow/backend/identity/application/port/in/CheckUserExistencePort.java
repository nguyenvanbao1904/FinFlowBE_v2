package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.application.dto.CheckUserExistenceOutput;
import com.finflow.backend.identity.application.query.CheckUserExistenceQuery;

public interface CheckUserExistencePort {
    CheckUserExistenceOutput execute(CheckUserExistenceQuery query);
}
