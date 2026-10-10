package com.chatapp.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which users currently hold at least one open WebSocket session. Presence is inherently
 * per instance and short lived, so it is held in memory rather than the database.
 */
@Service
public class PresenceService {

    private final Map<Long, Set<String>> sessionsByUser = new ConcurrentHashMap<>();

    public void sessionConnected(Long userId, String sessionId) {
        sessionsByUser.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    /**
     * Removes a session.
     *
     * @return {@code true} when that was the user's last open session, meaning they just went offline
     */
    public boolean sessionDisconnected(Long userId, String sessionId) {
        Set<String> sessions = sessionsByUser.get(userId);
        if (sessions == null) {
            return false;
        }
        sessions.remove(sessionId);
        if (sessions.isEmpty()) {
            sessionsByUser.remove(userId);
            return true;
        }
        return false;
    }

    public boolean isOnline(Long userId) {
        return sessionsByUser.containsKey(userId);
    }

    public Set<Long> onlineUserIds() {
        return Collections.unmodifiableSet(sessionsByUser.keySet());
    }
}
