package com.assinafy.sdk.support;

import com.assinafy.sdk.models.WebhookPayload;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/**
 * Verifies and parses incoming Assinafy webhook deliveries.
 *
 * <p><strong>Important:</strong> the Assinafy webhook delivery contract (see the API docs
 * "Payload Reference") publishes the JSON envelope but does <em>not</em> document a signature
 * header or a signing scheme, and the subscription has no place to register a shared secret.
 * {@link #verify(String, String)} therefore implements the <em>conventional</em> pattern —
 * HMAC-SHA256 of the raw request body, hex-encoded, compared in constant time against a
 * signature your endpoint received — for tenants that have an out-of-band signing arrangement.
 * It is <em>not</em> a documented platform guarantee.
 *
 * <p>Consequently, {@code verify} returning {@code false} does <em>not</em> by itself mean a
 * request is forged: it also returns {@code false} when no {@code webhookSecret} is configured,
 * when no signature is supplied, or when the platform simply sends no signature header. Do not
 * reject deliveries on {@code verify() == false} unless you have confirmed your tenant signs
 * webhooks with this exact scheme; otherwise authenticate deliveries another way (e.g. a secret
 * path/query token) and use {@link #extractEvent(String)} to parse the body. Pass the signature
 * header value verbatim (strip any {@code algo=} prefix yourself if one is present).
 */
public class WebhookVerifier {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String webhookSecret;

    /**
     * Create a verifier for an out-of-band webhook signing secret.
     *
     * @param webhookSecret HMAC secret; {@code null} or blank makes verification return
     *                      {@code false}
     */
    public WebhookVerifier(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    /**
     * Verify a UTF-8 payload against a lowercase hexadecimal HMAC-SHA256 signature.
     *
     * @param payload raw request body
     * @param signature hexadecimal signature without an algorithm prefix
     * @return {@code true} only when the configured secret produces the supplied signature
     */
    public boolean verify(String payload, String signature) {
        if (payload == null || webhookSecret == null || webhookSecret.isBlank()
                || signature == null || signature.isBlank()) {
            return false;
        }
        return verify(payload.getBytes(StandardCharsets.UTF_8), signature);
    }

    /**
     * Verify raw payload bytes against a lowercase hexadecimal HMAC-SHA256 signature.
     *
     * @param payload raw request body bytes
     * @param signature hexadecimal signature without an algorithm prefix
     * @return {@code true} only when the configured secret produces the supplied signature
     */
    public boolean verify(byte[] payload, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank() || signature == null || signature.isBlank()) {
            return false;
        }
        try {
            String expected = computeHmac(payload);
            String provided = signature.trim();
            return timingSafeEquals(expected, provided);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parse a UTF-8 JSON webhook envelope.
     *
     * @param payload raw request body
     * @return the parsed event, or {@code null} for blank, malformed, or non-object JSON
     */
    public WebhookPayload extractEvent(String payload) {
        if (payload == null || payload.isBlank()) return null;
        return extractEvent(payload.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parse UTF-8 JSON webhook bytes.
     *
     * @param payload raw request body bytes
     * @return the parsed event, or {@code null} for empty, malformed, or non-object JSON
     */
    public WebhookPayload extractEvent(byte[] payload) {
        if (payload == null || payload.length == 0) return null;
        try {
            String text = new String(payload, StandardCharsets.UTF_8);
            Object parsed = MAPPER.readValue(text, Object.class);
            if (!(parsed instanceof Map)) return null;
            return MAPPER.readValue(text, WebhookPayload.class);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Return an event's type.
     *
     * @param event parsed webhook event, or {@code null}
     * @return the event type, such as {@code document_ready}, or {@code null} when absent
     */
    public String getEventType(WebhookPayload event) {
        return event != null ? event.getEvent() : null;
    }

    /**
     * The entity the event is about (the {@code object} envelope field, e.g. the document),
     * falling back to {@code payload}. Returns an empty map when neither is present.
     *
     * @param event parsed webhook event, or {@code null}
     * @return the event object, legacy payload, or an empty map
     */
    public Map<String, Object> getEventData(WebhookPayload event) {
        if (event == null) return Map.of();
        if (event.getObject() != null) return event.getObject();
        if (event.getPayload() != null) return event.getPayload();
        return Map.of();
    }

    private String computeHmac(byte[] payload) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec keySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        mac.init(keySpec);
        byte[] digest = mac.doFinal(payload);
        return HexFormat.of().formatHex(digest);
    }

    private static boolean timingSafeEquals(String a, String b) {
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }
}
