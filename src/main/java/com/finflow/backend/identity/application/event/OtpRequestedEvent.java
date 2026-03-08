package com.finflow.backend.identity.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpRequestedEvent {
    private String email;
    private String otpCode;
    private String correlationId;
}
