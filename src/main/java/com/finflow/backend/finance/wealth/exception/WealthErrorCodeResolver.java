package com.finflow.backend.finance.wealth.exception;

import com.finflow.backend.common.exception.ErrorCode;
import com.finflow.backend.common.exception.ErrorCodeResolver;
import org.springframework.stereotype.Component;

@Component
public class WealthErrorCodeResolver implements ErrorCodeResolver {

    @Override
    public ErrorCode resolve(String key) {
        try {
            return WealthErrorCode.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
