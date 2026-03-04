package com.finflow.backend.common.exception;

/**
 * SPI để các module (như identity, order, v.v.) đăng ký resolver
 * ánh xạ từ enum key (message của @Valid) sang ErrorCode cụ thể.
 */
public interface ErrorCodeResolver {

    /**
     * @param key enum name, ví dụ "USERNAME_INVALID"
     * @return ErrorCode tương ứng, hoặc null nếu resolver không biết key này
     */
    ErrorCode resolve(String key);
}

