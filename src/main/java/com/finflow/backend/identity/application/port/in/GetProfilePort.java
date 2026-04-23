package com.finflow.backend.identity.application.port.in;
import com.finflow.backend.identity.application.query.GetProfileQuery;

import com.finflow.backend.identity.application.dto.UserOutput;

public interface GetProfilePort {
    UserOutput execute(GetProfileQuery query);
}
