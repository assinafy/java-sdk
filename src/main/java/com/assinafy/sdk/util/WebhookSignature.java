package com.assinafy.sdk.util;

import com.assinafy.sdk.exceptions.ValidationException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * Verifies signed webhook deliveries. Local computation only — makes no HTTP request.
 *
 * <p>An endpoint with {@code signing_enabled: true} signs every delivery following the
 * <a href="https://www.standardwebhooks.com">Standard Webhooks</a> specification: the
 * {@code webhook-signature} header holds one or more space-separated {@code v1,<base64>} entries,
 * each an HMAC-SHA256 of {@code {webhook-id}.{webhook-timestamp}.{raw body}} keyed by the
 * endpoint's {@code whsec_} secret ({@code WebhookResource.getEndpointSecret(...)}).</p>
 *
 * <p>Delivery your endpoint receives:</p>
 * <pre>
 * POST /webhooks/assinafy
 * webhook-id: msg_p5jXN8AQM9LWM0D4loKWxJek
 * webhook-timestamp: 1614265330
 * webhook-signature: v1,g0hM9SsE+OTPJTGt/tmIKtSyZlE3uFJELVlNIOLJ1OE=
 *
 * {"test": 2432232314}</pre>
 *
 * <p>Verify against the raw bytes, exactly as received — never a re-serialized copy of parsed JSON:</p>
 * <pre>{@code
 * if (!WebhookSignature.verify(secret, headers, rawBody)) {
 *     return ResponseEntity.status(401).build();   // do not process the body
 * }
 * WebhookPayload event = mapper.readValue(rawBody, WebhookPayload.class);
 * }</pre>
 *
 * <p>Deduplicate by {@code webhook-id}: it is identical on every attempt of the same event to the
 * same endpoint, and distinct per endpoint. A rotated secret takes effect immediately, so read it
 * from configuration on each request rather than caching it for the process lifetime.</p>
 */
public final class WebhookSignature {

    /** Accepted distance between {@code webhook-timestamp} and the local clock. */
    public static final Duration DEFAULT_TOLERANCE = Duration.ofMinutes(5);

    private static final String SECRET_PREFIX = "whsec_";

    private WebhookSignature() {}

    /**
     * Verify a delivery using its request headers and the current clock.
     *
     * @param secret the endpoint's {@code whsec_} signing secret
     * @param headers request headers; names are matched case-insensitively
     * @param rawBody the request body bytes, exactly as received
     * @return {@code true} when a {@code v1} signature matches and the timestamp is within
     *         {@link #DEFAULT_TOLERANCE}; {@code false} otherwise, including when a header is missing
     * @throws ValidationException when {@code secret} is not a {@code whsec_} base64 secret
     */
    public static boolean verify(String secret, Map<String, String> headers, byte[] rawBody) {
        if (headers == null) return false;
        return verify(secret, header(headers, "webhook-id"), header(headers, "webhook-timestamp"),
                header(headers, "webhook-signature"), rawBody, DEFAULT_TOLERANCE, Instant.now());
    }

    /**
     * Verify a delivery whose body is held as a string (decoded as UTF-8 when signed).
     *
     * @param secret the endpoint's {@code whsec_} signing secret
     * @param headers request headers; names are matched case-insensitively
     * @param rawBody the request body, exactly as received
     * @return whether the delivery is authentic and fresh
     * @throws ValidationException when {@code secret} is not a {@code whsec_} base64 secret
     */
    public static boolean verify(String secret, Map<String, String> headers, String rawBody) {
        return verify(secret, headers, rawBody == null ? null : rawBody.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Verify a delivery from its individual header values.
     *
     * @param secret the endpoint's {@code whsec_} signing secret
     * @param webhookId the {@code webhook-id} header
     * @param webhookTimestamp the {@code webhook-timestamp} header (Unix seconds)
     * @param webhookSignature the {@code webhook-signature} header
     * @param rawBody the request body bytes, exactly as received
     * @param tolerance accepted distance between the timestamp and {@code now}
     * @param now the instant to compare the timestamp against
     * @return whether the delivery is authentic and fresh
     * @throws ValidationException when {@code secret} is not a {@code whsec_} base64 secret
     */
    public static boolean verify(String secret, String webhookId, String webhookTimestamp,
                                 String webhookSignature, byte[] rawBody, Duration tolerance, Instant now) {
        byte[] key = decodeSecret(secret);
        if (webhookId == null || webhookTimestamp == null || webhookSignature == null || rawBody == null
                || !webhookTimestamp.matches("\\d{1,18}")) {
            return false;
        }
        long sentAt = Long.parseLong(webhookTimestamp);
        if (Math.abs(now.getEpochSecond() - sentAt) > tolerance.toSeconds()) return false;

        byte[] expected = Base64.getEncoder().encode(hmac(key, webhookId, webhookTimestamp, rawBody));
        for (String entry : webhookSignature.split(" ")) {
            if (entry.startsWith("v1,") && MessageDigest.isEqual(expected,
                    entry.substring(3).getBytes(StandardCharsets.US_ASCII))) {
                return true;
            }
        }
        return false;
    }

    private static byte[] decodeSecret(String secret) {
        try {
            if (secret != null && secret.startsWith(SECRET_PREFIX)) {
                byte[] key = Base64.getDecoder().decode(secret.substring(SECRET_PREFIX.length()));
                if (key.length > 0) return key;
            }
        } catch (IllegalArgumentException ignored) {
            // falls through to the validation error
        }
        throw new ValidationException("Webhook secret must be the whsec_ value issued for the endpoint");
    }

    private static byte[] hmac(byte[] key, String id, String timestamp, byte[] body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            mac.update((id + "." + timestamp + ".").getBytes(StandardCharsets.UTF_8));
            return mac.doFinal(body);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is unavailable", e);
        }
    }

    private static String header(Map<String, String> headers, String name) {
        for (Map.Entry<String, String> h : headers.entrySet()) {
            if (name.equalsIgnoreCase(h.getKey())) return h.getValue();
        }
        return null;
    }
}
