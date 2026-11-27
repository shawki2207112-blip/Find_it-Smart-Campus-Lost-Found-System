package com.findit.notification;

/** Event details a {@link NotificationFactory} needs to write a message: the item title and an optional detail. */
public record NotificationContext(String itemTitle, String detail) {

    public static NotificationContext of(String itemTitle) {
        return new NotificationContext(itemTitle, "");
    }

    public static NotificationContext of(String itemTitle, String detail) {
        return new NotificationContext(itemTitle, detail == null ? "" : detail);
    }
}
