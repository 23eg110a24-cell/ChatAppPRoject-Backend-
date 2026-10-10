package com.chatapp.dto;

import com.chatapp.domain.ChatRoom;
import com.chatapp.domain.RoomType;
import com.chatapp.domain.User;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Everything the sidebar needs to render a conversation, plus optional preview metadata
 * for the room's most recent message.
 */
public record RoomDto(
        Long id,
        String name,
        String description,
        RoomType type,
        Long ownerId,
        List<UserDto> members,
        Long partnerId,
        Instant createdAt,
        Instant lastMessageAt,
        String lastMessagePreview
) {

    private static final int PREVIEW_MAX_LENGTH = 90;

    public static RoomDto from(ChatRoom room) {
        return from(room, null, null);
    }

    public static RoomDto from(ChatRoom room, Long viewerId, String lastMessagePreview) {
        List<UserDto> members = room.getMembers().stream()
                .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
                .map(UserDto::from)
                .toList();

        Long partnerId = null;
        if (room.isDirect() && viewerId != null) {
            partnerId = room.getMembers().stream()
                    .map(User::getId)
                    .filter(id -> !id.equals(viewerId))
                    .findFirst()
                    .orElse(null);
        }

        return new RoomDto(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getType(),
                room.getOwner() == null ? null : room.getOwner().getId(),
                members,
                partnerId,
                room.getCreatedAt(),
                room.getLastMessageAt(),
                truncate(lastMessagePreview));
    }

    private static String truncate(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.strip();
        if (trimmed.length() <= PREVIEW_MAX_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, PREVIEW_MAX_LENGTH - 1).stripTrailing() + "\u2026";
    }
}
