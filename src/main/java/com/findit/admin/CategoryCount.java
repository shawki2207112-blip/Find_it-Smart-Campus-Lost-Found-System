package com.findit.admin;

/** A category name with how many items of it were counted. */
public record CategoryCount(String category, Long count) {
}
