package com.finflow.backend.identity.application.port.in;

import com.finflow.backend.identity.domain.enums.OtpPurpose;

public interface SendOtpPort {
    void execute(String email, OtpPurpose purpose);
}
