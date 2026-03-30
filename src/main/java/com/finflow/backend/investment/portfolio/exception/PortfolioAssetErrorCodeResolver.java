package com.finflow.backend.investment.portfolio.exception;

import com.finflow.backend.common.exception.ErrorCode;
import com.finflow.backend.common.exception.ErrorCodeResolver;
import org.springframework.stereotype.Component;

@Component
public class PortfolioAssetErrorCodeResolver implements ErrorCodeResolver {

    @Override
    public ErrorCode resolve(String key) {
        try {
            return PortfolioAssetErrorCode.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
