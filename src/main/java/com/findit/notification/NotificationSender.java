package com.findit.notification;

/**
 * Pattern: Adapter (target interface) - the only notification-delivery API business code depends on.
 * Implementations adapt different delivery mechanisms to it; the active one is chosen by findit.notification.channel.
 */
public interface NotificationSender {

    void send(Notification notification);
}
