package com.finflow.backend.identity.application.query;

public record CheckUserExistenceQuery(
        String email,
        String username
) {}
