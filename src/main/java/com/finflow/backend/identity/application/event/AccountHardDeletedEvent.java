package com.finflow.backend.identity.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountHardDeletedEvent {
    private String email;
    private String username;
    private String correlationId;
}
