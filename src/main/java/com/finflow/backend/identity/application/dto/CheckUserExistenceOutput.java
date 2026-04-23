package com.finflow.backend.identity.application.dto;

import lombok.Builder;

@Builder
public record CheckUserExistenceOutput(
        boolean exists,
        Boolean isActive,
        Boolean hasPassword,
        Boolean isDeleted
) {}
