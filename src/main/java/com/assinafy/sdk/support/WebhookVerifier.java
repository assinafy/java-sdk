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

    public String getEventType(WebhookPayload event) {
        if (event == null) return null;
        if (event.getEvent() != null) return event.getEvent();
        return event.getType();
    }

    public Map<String, Object> getEventData(WebhookPayload event) {
        if (event == null) return Map.of();
        if (event.getData() != null) return event.getData();
        if (event.getObject() != null) return event.getObject();
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
