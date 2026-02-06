package com.finflow.backend.common.config;

import com.finflow.backend.common.versioning.ApiVersionRequestMappingHandlerMapping;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class VersioningWebMvcRegistrations extends DelegatingWebMvcConfiguration {
    @Override
    @NullMarked
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new ApiVersionRequestMappingHandlerMapping();
    }
}

