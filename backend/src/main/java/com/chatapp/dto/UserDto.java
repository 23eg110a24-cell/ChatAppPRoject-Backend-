package com.chatapp.dto;

import com.chatapp.domain.User;

import java.time.Instant;

/**
 * Public projection of a user. Never carries the password hash.
 */
public record UserDto(
        Long id,
        String username,
        String displayName,
        String avatarColor,
        String initials,
        String bio,
        boolean online,
        Instant lastSeenAt
) {

    public static UserDto from(User user) {
        return from(user, false);
    }

    public static UserDto from(User user, boolean online) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatarColor(),
                user.getInitials(),
                user.getBio(),
                online,
                user.getLastSeenAt());
    }
}
