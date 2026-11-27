package com.findit.item;

public enum ItemType {
    LOST,
    FOUND;

    public ItemType opposite() {
        return this == LOST ? FOUND : LOST;
    }
}
