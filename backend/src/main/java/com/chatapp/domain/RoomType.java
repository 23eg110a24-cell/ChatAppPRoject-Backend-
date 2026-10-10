package com.chatapp.domain;

/**
 * A room is either a multi-user channel that anyone may join, or a one-to-one
 * direct conversation between exactly two users.
 */
public enum RoomType {
    CHANNEL,
    DIRECT
}
