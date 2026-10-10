package com.chatapp.security;

import java.util.Optional;

/**
 * Extracts the raw token from an {@code Authorization: Bearer <token>} header value.
 * Shared by the REST filter and the STOMP handshake interceptor so both accept the same format.
 */
public final class BearerTokens {

    private static final String BEARER_PREFIX = "Bearer ";

    private BearerTokens() {
    }

    public static Optional<String> extract(String authorizationHeaderValue) {
        if (authorizationHeaderValue == null) {
            return Optional.empty();
        }
        String header = authorizationHeaderValue.strip();
        if (header.length() <= BEARER_PREFIX.length()
                || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return Optional.empty();
        }
        String token = header.substring(BEARER_PREFIX.length()).strip();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }
}
