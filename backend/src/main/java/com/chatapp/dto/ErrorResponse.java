package com.chatapp.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Uniform error body for every REST failure, so the client can render a sensible message.
 * {@code fieldErrors} is populated only for bean-validation failures.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
) {

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now(), status, error, message, Map.of());
    }

    public static ErrorResponse of(int status, String error, String message, Map<String, String> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, error, message, fieldErrors);
    }
}
