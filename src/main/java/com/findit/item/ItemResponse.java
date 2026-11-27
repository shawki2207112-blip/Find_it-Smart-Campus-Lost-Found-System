package com.findit.item;

import java.time.Instant;
import java.time.LocalDate;

/**
 * API view of an item. The restricted variant hides the description and the owner, because a found item's description
 * holds the distinguishing details that are used to verify a claim.
 */
public record ItemResponse(
        Long id,
        ItemType type,
        String title,
        String description,
        Long categoryId,
        String categoryName,
        String location,
        LocalDate eventDate,
        String photoRef,
        ItemStatus status,
        Long ownerId,
        String ownerName,
        Instant createdAt) {

    public static ItemResponse full(Item item) {
        return new ItemResponse(item.getId(), item.getType(), item.getTitle(), item.getDescription(),
                item.getCategory().getId(), item.getCategory().getName(), item.getLocation(), item.getEventDate(),
                item.getPhotoRef(), item.getStatus(), item.getOwner().getId(), item.getOwner().getFullName(),
                item.getCreatedAt());
    }

    public static ItemResponse restricted(Item item) {
        return new ItemResponse(item.getId(), item.getType(), item.getTitle(), null,
                item.getCategory().getId(), item.getCategory().getName(), item.getLocation(), item.getEventDate(),
                item.getPhotoRef(), item.getStatus(), null, null, item.getCreatedAt());
    }
}
