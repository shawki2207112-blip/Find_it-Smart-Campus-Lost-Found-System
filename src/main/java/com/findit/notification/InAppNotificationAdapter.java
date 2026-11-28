package com.findit.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Pattern: Adapter - adapts the persistence layer (NotificationRepository.save) to the {@link NotificationSender}
 * interface. Active when findit.notification.channel=in-app (the default).
 */
@Component
@ConditionalOnProperty(name = "findit.notification.channel", havingValue = "in-app", matchIfMissing = true)
public class InAppNotificationAdapter implements NotificationSender {

    private final NotificationRepository repository;

    public InAppNotificationAdapter(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void send(Notification notification) {
        repository.save(notification);
    }
}
