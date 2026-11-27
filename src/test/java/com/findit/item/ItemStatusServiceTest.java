package com.findit.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.findit.TestData;
import com.findit.common.ConflictException;
import com.findit.user.RoleName;
import com.findit.user.User;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ItemStatusServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemStatusHistoryRepository historyRepository;

    @InjectMocks
    private ItemStatusService statusService;

    private final User actor = TestData.user(1L, "Staff", RoleName.ROLE_SECURITY_STAFF);

    private Item itemWithStatus(ItemStatus status) {
        return TestData.item(10L, ItemType.FOUND, "Keys", "Silver keys", TestData.category(1L, "Keys"),
                "Library", LocalDate.now(), actor, status);
    }

    @Test
    void transition_validStep_updatesStatusAndRecordsHistory() {
        Item item = itemWithStatus(ItemStatus.REPORTED);
        when(itemRepository.save(item)).thenReturn(item);

        statusService.transition(item, ItemStatus.MATCHED, actor);

        assertEquals(ItemStatus.MATCHED, item.getStatus());
        ArgumentCaptor<ItemStatusHistory> captor = ArgumentCaptor.forClass(ItemStatusHistory.class);
        verify(historyRepository).save(captor.capture());
        assertEquals(ItemStatus.REPORTED, captor.getValue().getFromStatus());
        assertEquals(ItemStatus.MATCHED, captor.getValue().getToStatus());
        assertEquals(actor, captor.getValue().getChangedBy());
    }

    @Test
    void transition_invalidStep_throwsConflictAndWritesNothing() {
        Item item = itemWithStatus(ItemStatus.REPORTED);

        assertThrows(ConflictException.class, () -> statusService.transition(item, ItemStatus.VERIFIED, actor));

        assertEquals(ItemStatus.REPORTED, item.getStatus());
        verifyNoInteractions(historyRepository);
    }

    @Test
    void transition_fromReturned_isAlwaysRejected() {
        Item item = itemWithStatus(ItemStatus.RETURNED);

        for (ItemStatus target : ItemStatus.values()) {
            assertThrows(ConflictException.class, () -> statusService.transition(item, target, actor));
        }
        assertTrue(ItemStatus.RETURNED.allowedNext().isEmpty());
    }

    @Test
    void lifecycle_forwardPathIsAllowedAndSkippingIsNot() {
        assertTrue(ItemStatus.REPORTED.canTransitionTo(ItemStatus.MATCHED));
        assertTrue(ItemStatus.MATCHED.canTransitionTo(ItemStatus.CLAIMED));
        assertTrue(ItemStatus.CLAIMED.canTransitionTo(ItemStatus.VERIFIED));
        assertTrue(ItemStatus.VERIFIED.canTransitionTo(ItemStatus.RETURNED));
        assertTrue(ItemStatus.CLAIMED.canTransitionTo(ItemStatus.MATCHED));
        assertTrue(ItemStatus.CLAIMED.canTransitionTo(ItemStatus.REPORTED));
        assertFalse(ItemStatus.MATCHED.canTransitionTo(ItemStatus.VERIFIED));
        assertFalse(ItemStatus.VERIFIED.canTransitionTo(ItemStatus.MATCHED));
    }

    @Test
    void recordInitial_savesHistoryWithoutPreviousStatus() {
        Item item = itemWithStatus(ItemStatus.REPORTED);

        statusService.recordInitial(item, actor);

        ArgumentCaptor<ItemStatusHistory> captor = ArgumentCaptor.forClass(ItemStatusHistory.class);
        verify(historyRepository).save(captor.capture());
        assertNull(captor.getValue().getFromStatus());
        assertEquals(ItemStatus.REPORTED, captor.getValue().getToStatus());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void transition_savesTheItem() {
        Item item = itemWithStatus(ItemStatus.MATCHED);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item result = statusService.transition(item, ItemStatus.CLAIMED, actor);

        assertEquals(ItemStatus.CLAIMED, result.getStatus());
        verify(itemRepository).save(item);
    }
}
