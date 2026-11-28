package com.findit.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.findit.TestData;
import com.findit.common.ForbiddenException;
import com.findit.user.RoleName;
import com.findit.user.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Tests the Adapter implementations of NotificationSender and the service that uses Factory + Adapter. */
class NotificationAdapterTest {

    private final User recipient = TestData.user(1L, "Rita", RoleName.ROLE_STUDENT);
    private final Notification notification =
            new Notification(recipient, NotificationType.CLAIM_APPROVED, "Your claim was approved");

    @Test
    void inAppAdapter_persistsNotification() {
        NotificationRepository repository = mock(NotificationRepository.class);

        new InAppNotificationAdapter(repository).send(notification);

        verify(repository).save(notification);
    }

    @Test
    void emailAdapter_translatesNotificationToMailClientCall() {
        EmailClient client = mock(EmailClient.class);

        new EmailNotificationAdapter(client).send(notification);

        verify(client).sendMail(eq(recipient.getEmail()), contains("CLAIM_APPROVED"), eq("Your claim was approved"));
    }

    @Test
    void notificationService_buildsThroughFactoryAndDeliversThroughSender() {
        NotificationSender sender = mock(NotificationSender.class);
        NotificationService service = new NotificationService(new NotificationFactory(), sender,
                mock(NotificationRepository.class));

        service.notify(NotificationType.ITEM_RETURNED, recipient, NotificationContext.of("Umbrella"));

        org.mockito.ArgumentCaptor<Notification> captor = org.mockito.ArgumentCaptor.forClass(Notification.class);
        verify(sender).send(captor.capture());
        assertEquals(NotificationType.ITEM_RETURNED, captor.getValue().getType());
        assertTrue(captor.getValue().getMessage().contains("Umbrella"));
    }

    @Test
    void notificationService_markRead_onForeignNotification_throwsForbidden() {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationService service =
                new NotificationService(new NotificationFactory(), mock(NotificationSender.class), repository);
        notification.setId(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(notification));
        User stranger = TestData.user(99L, "Stranger", RoleName.ROLE_STUDENT);

        assertThrows(ForbiddenException.class, () -> service.markRead(5L, stranger));

        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void notificationService_markRead_onOwnNotification_setsReadFlag() {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationService service =
                new NotificationService(new NotificationFactory(), mock(NotificationSender.class), repository);
        notification.setId(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(notification));
        when(repository.save(notification)).thenReturn(notification);

        NotificationResponse response = service.markRead(5L, recipient);

        assertTrue(response.read());
    }
}
