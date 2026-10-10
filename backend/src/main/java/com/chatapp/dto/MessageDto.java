package com.chatapp.dto;

import com.chatapp.domain.Message;
import com.chatapp.domain.MessageType;

import java.time.Instant;

/**
 * A single message rendered in a room. {@code sender} is null for SYSTEM notices.
 */
public record MessageDto(
        Long id,
        Long roomId,
        UserDto sender,
        String content,
        MessageType type,
        Instant createdAt
) {

    public static MessageDto from(Message message, Long roomId) {
        MessageDto dto = from(message);
        return new MessageDto(
                dto.id(),
                roomId,
                dto.sender(),
                dto.content(),
                dto.type(),
                dto.createdAt());
    }

    public static MessageDto from(Message message) {
        return new MessageDto(
                message.getId(),
                message.getRoom() == null ? null : message.getRoom().getId(),
                message.getSender() == null ? null : UserDto.from(message.getSender()),
                message.getContent(),
                message.getType(),
                message.getCreatedAt());
    }
}
