package com.chatapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Typed view of the {@code chatapp.*} block in {@code application.yml}.
 */
@ConfigurationProperties(prefix = "chatapp")
public record ChatAppProperties(
        Jwt jwt,
        Cors cors,
        boolean seedDemoData
) {

    public ChatAppProperties {
        if (jwt == null) {
            jwt = new Jwt(null, 1440, "chatapp");
        }
        if (cors == null) {
            cors = new Cors(List.of("http://localhost:5173"));
        }
    }

    public record Jwt(String secret, long expirationMinutes, String issuer) {
    }

    public record Cors(List<String> allowedOrigins) {
    }
}
