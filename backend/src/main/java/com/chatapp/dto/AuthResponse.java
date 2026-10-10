package com.chatapp.dto;

/**
 * Issued on successful login or registration. The client stores {@code token} and sends it
 * as {@code Authorization: Bearer <token>} on every REST call and STOMP CONNECT frame.
 */
public record AuthResponse(
        String token,
        long expiresInSeconds,
        UserDto user
) {
}
