package com.findit.item;

import java.time.LocalDate;

/** Optional filters of GET /api/items. The category is matched by name, ignoring case. */
public record ItemSearchCriteria(String category, String location, ItemType type, ItemStatus status,
                                 LocalDate fromDate, LocalDate toDate) {
}
