package com.assinafy.sdk.support;

import com.assinafy.sdk.models.WebhookPayload;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/**
 * Verifies and parses incoming Assinafy webhook deliveries.
 *
 * <p>The webhook delivery contract (see the API docs "Payload Reference") specifies the JSON
 * envelope but does not publish a signature scheme. {@link #verify(String, String)} therefore
 * implements the conventional pattern — HMAC-SHA256 of the raw request body, hex-encoded,
 * compared in constant time against the signature your endpoint received — which is what the
 * platform uses when a {@code webhookSecret} is configured. Pass the signature header value
 * verbatim (strip any {@code algo=} prefix yourself if one is present). If no secret is
 * configured, {@code verify} returns {@code false}.
 */
public class WebhookVerifier {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String webhookSecret;

    public WebhookVerifier(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public boolean verify(String payload, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank() || signature == null || signature.isBlank()) {
            return false;
        }
        return verify(payload.getBytes(StandardCharsets.UTF_8), signature);
    }

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

    public WebhookPayload extractEvent(String payload) {
        if (payload == null || payload.isBlank()) return null;
        return extractEvent(payload.getBytes(StandardCharsets.UTF_8));
    }

    public WebhookPayload extractEvent(byte[] payload) {
        if (payload == null || payload.length == 0) return null;
        try {
            String text = new String(payload, StandardCharsets.UTF_8);
            Object parsed = MAPPER.readValue(text, Object.class);
            if (!(parsed instanceof Map)) return null;
            return MAPPER.readValue(text, WebhookPayload.class);
        } catch (Exception e) {
            return null;
        }
    }

    /** The event type (e.g. {@code document_ready}), or {@code null} if absent. */
    public String getEventType(WebhookPayload event) {
        return event != null ? event.getEvent() : null;
    }

    /**
     * The entity the event is about (the {@code object} envelope field, e.g. the document),
     * falling back to {@code payload}. Returns an empty map when neither is present.
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
