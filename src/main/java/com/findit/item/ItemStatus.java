package com.findit.item;

import java.util.EnumSet;
import java.util.Set;

/**
 * Item lifecycle: REPORTED -> MATCHED -> CLAIMED -> VERIFIED -> RETURNED. A rejected or withdrawn claim sends the item
 * back from CLAIMED to MATCHED or REPORTED. RETURNED is final. {@link ItemStatusService} is the only class that
 * applies a transition.
 */
public enum ItemStatus {
    REPORTED,
    MATCHED,
    CLAIMED,
    VERIFIED,
    RETURNED;

    public Set<ItemStatus> allowedNext() {
        return switch (this) {
            case REPORTED -> EnumSet.of(MATCHED, CLAIMED);
            case MATCHED -> EnumSet.of(CLAIMED);
            case CLAIMED -> EnumSet.of(VERIFIED, MATCHED, REPORTED);
            case VERIFIED -> EnumSet.of(RETURNED);
            case RETURNED -> EnumSet.noneOf(ItemStatus.class);
        };
    }

    public boolean canTransitionTo(ItemStatus target) {
        return allowedNext().contains(target);
    }
}
