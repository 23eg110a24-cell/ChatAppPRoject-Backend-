package com.chatapp.domain;

/**
 * Distinguishes real user messages from system notices rendered inline in a room.
 */
public enum MessageType {
    CHAT,
    SYSTEM
}
