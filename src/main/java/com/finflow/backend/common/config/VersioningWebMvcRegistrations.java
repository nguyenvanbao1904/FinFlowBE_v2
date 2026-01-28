package com.finflow.backend.common.config;

import com.finflow.backend.common.versioning.ApiVersionRequestMappingHandlerMapping;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Đăng ký HandlerMapping tùy biến để hỗ trợ @ApiVersion với RequestCondition.
 * Kế thừa DelegatingWebMvcConfiguration để giữ nguyên các cấu hình MVC mặc định.
 */
@Configuration
public class VersioningWebMvcRegistrations extends DelegatingWebMvcConfiguration {
    @Override
    @NullMarked
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new ApiVersionRequestMappingHandlerMapping();
    }
}

