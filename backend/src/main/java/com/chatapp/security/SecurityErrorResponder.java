package com.chatapp.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Writes the small, fixed JSON error bodies that Spring Security needs. These run inside the
 * filter chain, before the dispatcher servlet and its message converters are involved, so the
 * bodies are plain constants rather than anything rendered from user input.
 */
public final class SecurityErrorResponder {

    private static final String UNAUTHORIZED_BODY =
            "{\"status\":401,\"error\":\"Unauthorized\","
                    + "\"message\":\"Authentication is required to access this resource\"}";

    private static final String FORBIDDEN_BODY =
            "{\"status\":403,\"error\":\"Forbidden\","
                    + "\"message\":\"You do not have permission to perform this action\"}";

    private SecurityErrorResponder() {
    }

    public static void unauthorized(HttpServletResponse response) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, UNAUTHORIZED_BODY);
    }

    public static void forbidden(HttpServletResponse response) throws IOException {
        write(response, HttpStatus.FORBIDDEN, FORBIDDEN_BODY);
    }

    private static void write(HttpServletResponse response, HttpStatus status, String body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(body);
    }
}
