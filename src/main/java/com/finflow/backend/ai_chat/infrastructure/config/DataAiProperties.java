package com.finflow.backend.ai_chat.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "data.ai")
@Getter
@Setter
public class DataAiProperties {
    private String baseUrl = "http://localhost:8001";
    private String internalApiKey = "";
    private int chatTimeoutSeconds = 120;
}
