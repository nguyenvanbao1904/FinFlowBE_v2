package com.finflow.backend.ai_chat.exception;

import com.finflow.backend.common.exception.ErrorCode;
import com.finflow.backend.common.exception.ErrorCodeResolver;
import org.springframework.stereotype.Component;

@Component
public class ChatErrorCodeResolver implements ErrorCodeResolver {
    @Override
    public ErrorCode resolve(String key) {
        try {
            return ChatErrorCode.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
