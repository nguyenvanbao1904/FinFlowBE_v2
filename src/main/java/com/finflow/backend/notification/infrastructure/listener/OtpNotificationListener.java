package com.finflow.backend.notification.infrastructure.listener;

import com.finflow.backend.identity.api.OtpRequestedEvent;
import com.finflow.backend.notification.MdcCorrelationSupport;
import com.finflow.backend.notification.application.port.out.EmailNotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpNotificationListener {

    private final EmailNotificationPort emailNotificationPort;

    @Async
    @EventListener
    public void handleOtpRequested(OtpRequestedEvent event) {
        MdcCorrelationSupport.run(event.getCorrelationId(), () -> {
            log.debug("Received OTP request event for: {}", event.getEmail());
            String subject = "FinFlow Verification Code";
            String text = "Your verification code is: " + event.getOtpCode() + "\n\nThis code expires in 5 minutes.";
            emailNotificationPort.sendSimpleMessage(event.getEmail(), subject, text);
        });
    }
}
