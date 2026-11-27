package com.findit.notification;

import com.findit.user.User;
import org.springframework.stereotype.Component;

/**
 * Pattern: Factory - builds the right {@link Notification} (type and message text) for each event type, so no service
 * ever assembles notification messages itself. All services create notifications only through this class
 * (via {@link NotificationService}).
 */
@Component
public class NotificationFactory {

    public Notification create(NotificationType type, User recipient, NotificationContext context) {
        String title = context.itemTitle();
        String message = switch (type) {
            case MATCH_FOUND -> "A possible match was found for \"" + title + "\" (" + context.detail() + ").";
            case CLAIM_SUBMITTED -> "A claim was submitted on your found item \"" + title + "\".";
            case CLAIM_APPROVED -> "Your claim on \"" + title + "\" was approved. Please collect the item from "
                    + "the security office.";
            case CLAIM_REJECTED -> "Your claim on \"" + title + "\" was rejected. Reason: " + context.detail();
            case ITEM_RETURNED -> "\"" + title + "\" has been marked as returned.";
        };
        return new Notification(recipient, type, message);
    }
}
