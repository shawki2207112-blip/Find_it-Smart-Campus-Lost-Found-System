package com.findit.common;

/** Thrown on invalid state transitions or duplicates (HTTP 409). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
