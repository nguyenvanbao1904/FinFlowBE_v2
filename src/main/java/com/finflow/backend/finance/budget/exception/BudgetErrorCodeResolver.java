package com.finflow.backend.finance.budget.exception;

import com.finflow.backend.common.exception.ErrorCode;
import com.finflow.backend.common.exception.ErrorCodeResolver;
import org.springframework.stereotype.Component;

@Component
public class BudgetErrorCodeResolver implements ErrorCodeResolver {

    @Override
    public ErrorCode resolve(String key) {
        try {
            return BudgetErrorCode.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

