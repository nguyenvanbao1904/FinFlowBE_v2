package com.finflow.backend.identity.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountSoftDeletedEvent {
    private String email;
    private String username;
    private String correlationId;
}
