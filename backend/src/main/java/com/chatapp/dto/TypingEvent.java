package com.chatapp.dto;

/**
 * Broadcast over STOMP when a participant starts or stops typing in a room.
 */
public record TypingEvent(
        Long roomId,
        Long userId,
        String displayName,
        boolean typing
) {
}
