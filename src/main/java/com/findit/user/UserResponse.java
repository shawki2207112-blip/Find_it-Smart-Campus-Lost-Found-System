package com.findit.user;

import java.time.Instant;
import java.util.List;

/** API view of a user. The password hash is never part of it. */
public record UserResponse(Long id, String fullName, String email, boolean enabled, List<String> roles,
                           Instant createdAt) {

    public static UserResponse from(User user) {
        List<String> roleNames = user.getRoles().stream().map(role -> role.getName().name()).sorted().toList();
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.isEnabled(), roleNames,
                user.getCreatedAt());
    }
}
