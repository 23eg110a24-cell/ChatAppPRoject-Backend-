package com.chatapp.security;

import com.chatapp.config.ChatAppProperties;
import com.chatapp.domain.User;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Issues and verifies compact HS256 JSON Web Tokens.
 *
 * <p>A token is the standard three part JWT:
 * {@code base64url(header).base64url(payload).base64url(hmacSha256)}. Only the numeric user id
 * travels inside it; everything else is resolved from the database per request, so a stale token
 * can never impersonate a renamed or deleted account.
 */
@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final String DEFAULT_ISSUER = "chatapp";
    private static final Pattern SAFE_ISSUER = Pattern.compile("^[A-Za-z0-9._-]{1,32}$");
    private static final int MIN_SECRET_BYTES = 32;
    private static final long MIN_EXPIRATION_SECONDS = 60L;
    private static final int EXPECTED_PART_COUNT = 3;

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final byte[] secret;
    private final long expirationSeconds;
    private final String issuer;

    public JwtService(ChatAppProperties properties) {
        ChatAppProperties.Jwt jwt = properties.jwt();
        String rawSecret = jwt.secret();
        if (rawSecret == null || rawSecret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "chatapp.jwt.secret must be at least " + MIN_SECRET_BYTES + " bytes long");
        }
        this.secret = rawSecret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = Math.max(MIN_EXPIRATION_SECONDS, jwt.expirationMinutes() * 60L);
        this.issuer = (jwt.issuer() != null && SAFE_ISSUER.matcher(jwt.issuer()).matches())
                ? jwt.issuer()
                : DEFAULT_ISSUER;
    }

    /**
     * Builds a signed token identifying {@code user}, which must already be persisted so that its
     * generated id is populated.
     */
    public String issueToken(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("Cannot issue a token for an unsaved user");
        }
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + expirationSeconds;

        String payload = "{\"sub\":\"" + user.getId()
                + "\",\"iss\":\"" + issuer
                + "\",\"iat\":" + issuedAt
                + ",\"exp\":" + expiresAt + "}";

        String encodedHeader = encode(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = encode(payload.getBytes(StandardCharsets.UTF_8));
        String signature = encode(sign(encodedHeader + "." + encodedPayload));

        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    /**
     * Verifies the signature, issuer and expiry, then returns the id of the user the token belongs
     * to. Returns an empty optional for any malformed, tampered with or expired token.
     */
    public Optional<Long> extractUserId(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.");
        if (parts.length != EXPECTED_PART_COUNT) {
            return Optional.empty();
        }

        byte[] providedSignature;
        String payloadJson;
        try {
            providedSignature = BASE64_URL_DECODER.decode(parts[2]);
            payloadJson = new String(BASE64_URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        byte[] expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!MessageDigest.isEqual(expectedSignature, providedSignature)) {
            return Optional.empty();
        }

        Optional<JwtClaims> parsedClaims = JwtClaims.parse(payloadJson);
        if (parsedClaims.isEmpty()) {
            return Optional.empty();
        }
        JwtClaims claims = parsedClaims.get();
        if (!issuer.equals(claims.issuer()) || claims.expiresAt() <= Instant.now().getEpochSecond()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.valueOf(claims.subject()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private byte[] sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to sign JWT", ex);
        }
    }

    private static String encode(byte[] bytes) {
        return BASE64_URL_ENCODER.encodeToString(bytes);
    }
}
