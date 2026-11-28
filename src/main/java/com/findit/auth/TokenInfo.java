package com.findit.auth;

import java.time.Instant;

/** The parts of a validated JWT the application cares about. */
public record TokenInfo(String jti, String subject, Instant expiresAt) {
}
