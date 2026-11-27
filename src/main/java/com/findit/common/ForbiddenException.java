package com.findit.common;

/** Thrown when an authenticated user may not touch a resource, e.g. not the owner (HTTP 403). */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
