package com.chatapp.security;

import java.util.Optional;

/**
 * Minimal parser for the JSON payload of the JWTs this application issues. The payload shape is
 * fully controlled by {@link JwtService}, so only that small subset of JSON is ever read back.
 *
 * <p>Signature verification happens in {@link JwtService} <em>before</em> claims are parsed, so
 * nothing here is trusted until the token has been proven authentic.
 */
record JwtClaims(String subject, String issuer, long issuedAt, long expiresAt) {

    static Optional<JwtClaims> parse(String payload) {
        if (payload == null || payload.isBlank()) {
            return Optional.empty();
        }
        String subject = readString(payload, "sub");
        String issuer = readString(payload, "iss");
        long issuedAt = readNumber(payload, "iat");
        long expiresAt = readNumber(payload, "exp");

        if (subject == null || subject.isBlank() || issuer == null || issuedAt < 0 || expiresAt < 0) {
            return Optional.empty();
        }
        return Optional.of(new JwtClaims(subject, issuer, issuedAt, expiresAt));
    }

    private static String readString(String payload, String claim) {
        String needle = '"' + claim + "\":\"";
        int needleStart = payload.indexOf(needle);
        if (needleStart < 0) {
            return null;
        }
        int valueStart = needleStart + needle.length();
        int valueEnd = payload.indexOf('"', valueStart);
        if (valueEnd < 0) {
            return null;
        }
        return payload.substring(valueStart, valueEnd);
    }

    private static long readNumber(String payload, String claim) {
        String needle = '"' + claim + "\":";
        int needleStart = payload.indexOf(needle);
        if (needleStart < 0) {
            return -1L;
        }
        int valueStart = needleStart + needle.length();
        int valueEnd = valueStart;
        while (valueEnd < payload.length() && Character.isDigit(payload.charAt(valueEnd))) {
            valueEnd++;
        }
        if (valueEnd == valueStart) {
            return -1L;
        }
        try {
            return Long.parseLong(payload.substring(valueStart, valueEnd));
        } catch (NumberFormatException ex) {
            return -1L;
        }
    }
}
