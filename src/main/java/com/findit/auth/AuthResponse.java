package com.findit.auth;

import com.findit.user.UserResponse;

public record AuthResponse(String token, String tokenType, long expiresInMs, UserResponse user) {
}
