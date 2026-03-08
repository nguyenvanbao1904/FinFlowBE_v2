package com.finflow.backend.identity.exception;

import com.finflow.backend.common.exception.ErrorCode;
import com.finflow.backend.common.exception.ErrorCodeResolver;
import org.springframework.stereotype.Component;

@Component
public class IdentityErrorCodeResolver implements ErrorCodeResolver {

    @Override
    public ErrorCode resolve(String key) {
        try {
            return IdentityErrorCode.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

