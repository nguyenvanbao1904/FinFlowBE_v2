package com.finflow.backend.modules.notification.application.listener;

import com.finflow.backend.modules.identity.application.event.OtpRequestedEvent;
import com.finflow.backend.modules.notification.infrastructure.mail.EmailService;
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
