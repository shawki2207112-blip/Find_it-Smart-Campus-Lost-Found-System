package com.findit.item;

import java.time.Instant;

public record ItemStatusHistoryResponse(Long id, ItemStatus fromStatus, ItemStatus toStatus, String changedBy,
                                        Instant changedAt) {

    public static ItemStatusHistoryResponse from(ItemStatusHistory history) {
        return new ItemStatusHistoryResponse(history.getId(), history.getFromStatus(), history.getToStatus(),
                history.getChangedBy().getFullName(), history.getChangedAt());
    }
}
