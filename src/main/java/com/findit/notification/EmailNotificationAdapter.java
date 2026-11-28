package com.findit.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Pattern: Adapter - wraps the {@link EmailClient} (incompatible API) and presents it as a {@link NotificationSender}.
 * Active when findit.notification.channel=email. It only logs the mail; it does not store an in-app row.
 */
@Component
@ConditionalOnProperty(name = "findit.notification.channel", havingValue = "email")
public class EmailNotificationAdapter implements NotificationSender {

    private final EmailClient emailClient;

    public EmailNotificationAdapter(EmailClient emailClient) {
        this.emailClient = emailClient;
    }

    @Override
    public void send(Notification notification) {
        String subject = "FindIt: " + notification.getType();
        emailClient.sendMail(notification.getRecipient().getEmail(), subject, notification.getMessage());
    }
}
