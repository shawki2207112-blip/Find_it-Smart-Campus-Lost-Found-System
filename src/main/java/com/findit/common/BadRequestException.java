package com.findit.common;

/** Thrown when the request is syntactically valid but semantically wrong (HTTP 400). */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
