package com.finflow.backend.modules.notification.internal;

import com.finflow.backend.modules.identity.OtpRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpNotificationListener {

    private final EmailService emailService;

    @EventListener
    public void handleOtpRequested(OtpRequestedEvent event) {
        log.info("Received OTP request event for: {}", event.getEmail());
        String subject = "FinFlow Verification Code";
        String text = "Your verification code is: " + event.getOtpCode() + "\n\nThis code expires in 5 minutes.";
        
        emailService.sendSimpleMessage(event.getEmail(), subject, text);
    }
}
