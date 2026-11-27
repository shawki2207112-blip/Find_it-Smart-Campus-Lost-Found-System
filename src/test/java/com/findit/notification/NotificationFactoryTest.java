package com.findit.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.findit.TestData;
import com.findit.user.RoleName;
import com.findit.user.User;
import org.junit.jupiter.api.Test;

class NotificationFactoryTest {

    private final NotificationFactory factory = new NotificationFactory();
    private final User recipient = TestData.user(1L, "Rita", RoleName.ROLE_STUDENT);

    @Test
    void create_matchFound_mentionsItemAndScore() {
        Notification n = factory.create(NotificationType.MATCH_FOUND, recipient,
                NotificationContext.of("Blue Wallet", "score 82"));

        assertEquals(NotificationType.MATCH_FOUND, n.getType());
        assertSame(recipient, n.getRecipient());
        assertTrue(n.getMessage().contains("Blue Wallet"));
        assertTrue(n.getMessage().contains("score 82"));
    }

    @Test
    void create_claimSubmitted_tellsPosterAboutNewClaim() {
        Notification n = factory.create(NotificationType.CLAIM_SUBMITTED, recipient, NotificationContext.of("Keys"));

        assertEquals(NotificationType.CLAIM_SUBMITTED, n.getType());
        assertTrue(n.getMessage().contains("claim was submitted"));
        assertTrue(n.getMessage().contains("Keys"));
    }

    @Test
    void create_claimApproved_mentionsApproval() {
        Notification n = factory.create(NotificationType.CLAIM_APPROVED, recipient, NotificationContext.of("Keys"));

        assertEquals(NotificationType.CLAIM_APPROVED, n.getType());
        assertTrue(n.getMessage().contains("approved"));
    }

    @Test
    void create_claimRejected_includesReviewNote() {
        Notification n = factory.create(NotificationType.CLAIM_REJECTED, recipient,
                NotificationContext.of("Keys", "Colour does not match"));

        assertEquals(NotificationType.CLAIM_REJECTED, n.getType());
        assertTrue(n.getMessage().contains("rejected"));
        assertTrue(n.getMessage().contains("Colour does not match"));
    }

    @Test
    void create_itemReturned_mentionsReturn() {
        Notification n = factory.create(NotificationType.ITEM_RETURNED, recipient, NotificationContext.of("Keys"));

        assertEquals(NotificationType.ITEM_RETURNED, n.getType());
        assertTrue(n.getMessage().contains("returned"));
        assertEquals(false, n.isRead());
    }
}
