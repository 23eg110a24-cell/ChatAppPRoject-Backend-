package com.chatapp.domain;

import java.util.List;

/**
 * Picks a stable avatar colour for a user. The same username always maps to the same colour so
 * avatars stay recognisable across sessions and devices.
 */
public final class AvatarColors {

    public static final String FALLBACK = "#6366f1";

    private static final List<String> PALETTE = List.of(
            "#6366f1", // indigo
            "#0ea5e9", // sky
            "#14b8a6", // teal
            "#10b981", // emerald
            "#84cc16", // lime
            "#f59e0b", // amber
            "#f97316", // orange
            "#ef4444", // red
            "#ec4899", // pink
            "#8b5cf6"  // violet
    );

    private AvatarColors() {
    }

    public static String forUsername(String username) {
        if (username == null || username.isBlank()) {
            return FALLBACK;
        }
        int index = Math.floorMod(username.toLowerCase().hashCode(), PALETTE.size());
        return PALETTE.get(index);
    }
}
