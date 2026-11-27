package com.findit.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Payload for creating or updating an item. The type cannot be changed once the item exists. */
public record ItemRequest(
        @NotNull ItemType type,
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 2000) String description,
        @NotNull Long categoryId,
        @NotBlank @Size(max = 150) String location,
        @NotNull @PastOrPresent LocalDate eventDate,
        @Size(max = 500) String photoRef) {
}
