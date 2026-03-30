package com.finflow.backend.investment.market_data.exception;

import com.finflow.backend.common.exception.ErrorCode;
import com.finflow.backend.common.exception.ErrorCodeResolver;
import org.springframework.stereotype.Component;

@Component
public class MarketDataErrorCodeResolver implements ErrorCodeResolver {

    @Override
    public ErrorCode resolve(String key) {
        try {
            return MarketDataErrorCode.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
