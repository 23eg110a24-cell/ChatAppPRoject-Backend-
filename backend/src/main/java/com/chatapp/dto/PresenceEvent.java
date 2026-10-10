package com.chatapp.dto;

import java.time.Instant;

public record PresenceEvent(Long userId, boolean online, Instant lastSeenAt) {
}