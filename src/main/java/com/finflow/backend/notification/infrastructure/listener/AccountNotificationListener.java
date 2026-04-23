package com.finflow.backend.notification.infrastructure.listener;

import com.finflow.backend.identity.api.AccountHardDeletedEvent;
import com.finflow.backend.identity.api.AccountSoftDeletedEvent;
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
public class AccountNotificationListener {

    private final EmailNotificationPort emailNotificationPort;

    @Async
    @EventListener
    public void handleSoftDelete(AccountSoftDeletedEvent event) {
        MdcCorrelationSupport.run(event.getCorrelationId(), () -> {
            log.debug("Sending soft delete notification to: {}", event.getEmail());
            String subject = "Account Deletion Scheduled - FinFlow";
            String text = "Dear " + event.getUsername() + ",\n\n" +
                    "We received a request to delete your FinFlow account. Your account has been scheduled for permanent deletion in 30 days.\n\n" +
                    "If this was you, no further action is needed.\n" +
                    "If you changed your mind, you can simply log in again within the next 30 days to restore your account instantly.\n\n" +
                    "Best regards,\nFinFlow Team";
            emailNotificationPort.sendSimpleMessage(event.getEmail(), subject, text);
        });
    }

    @Async
    @EventListener
    public void handleHardDelete(AccountHardDeletedEvent event) {
        MdcCorrelationSupport.run(event.getCorrelationId(), () -> {
            log.debug("Sending hard delete notification to: {}", event.getEmail());
            String subject = "Account Permanently Deleted - FinFlow";
            String text = "Dear " + event.getUsername() + ",\n\n" +
                    "Your FinFlow account and all associated data have been permanently deleted as scheduled.\n" +
                    "We are sorry to see you go and hope to welcome you back in the future.\n\n" +
                    "Best regards,\nFinFlow Team";
            emailNotificationPort.sendSimpleMessage(event.getEmail(), subject, text);
        });
    }
}
