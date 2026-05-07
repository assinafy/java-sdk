package com.assinafy.sdk.support;

import com.assinafy.sdk.models.WebhookPayload;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class WebhookVerifierTest {

    private static final String SECRET = "super-secret";
    private static final String PAYLOAD = "{\"event\":\"document_ready\",\"data\":{\"document_id\":\"doc-1\"}}";

    private static String computeHmac(String secret, String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    @Test
    void verifyReturnsTrueForMatchingHmacSha256Signature() throws Exception {
        String signature = computeHmac(SECRET, PAYLOAD);
        WebhookVerifier verifier = new WebhookVerifier(SECRET);
        assertThat(verifier.verify(PAYLOAD, signature)).isTrue();
    }

    @Test
    void verifyReturnsFalseForMismatchedSignature() {
        WebhookVerifier verifier = new WebhookVerifier(SECRET);
        assertThat(verifier.verify(PAYLOAD, "deadbeef")).isFalse();
    }

    @Test
    void verifyReturnsFalseWhenNoSecretConfigured() throws Exception {
        String signature = computeHmac(SECRET, PAYLOAD);
        WebhookVerifier verifier = new WebhookVerifier(null);
        assertThat(verifier.verify(PAYLOAD, signature)).isFalse();
    }

    @Test
    void verifyReturnsFalseForEmptySignature() throws Exception {
        String signature = computeHmac(SECRET, PAYLOAD);
        WebhookVerifier verifier = new WebhookVerifier(SECRET);
        assertThat(verifier.verify(PAYLOAD, "")).isFalse();
    }

    @Test
    void verifyWorksWithByteArrayPayload() throws Exception {
        byte[] payloadBytes = PAYLOAD.getBytes(StandardCharsets.UTF_8);
        String signature = computeHmac(SECRET, PAYLOAD);
        WebhookVerifier verifier = new WebhookVerifier(SECRET);
        assertThat(verifier.verify(payloadBytes, signature)).isTrue();
    }

    @Test
    void extractEventParsesJsonPayload() {
        WebhookVerifier verifier = new WebhookVerifier(SECRET);
        WebhookPayload event = verifier.extractEvent(PAYLOAD);
        assertThat(event).isNotNull();
        assertThat(event.getEvent()).isEqualTo("document_ready");
        assertThat(event.getData()).isNotNull();
        assertThat(event.getData().get("document_id")).isEqualTo("doc-1");
    }

    @Test
    void extractEventReturnsNullOnMalformedPayload() {
        WebhookVerifier verifier = new WebhookVerifier(SECRET);
        assertThat(verifier.extractEvent("{not json")).isNull();
    }

    @Test
    void getEventTypeAndGetEventDataUnwrapEnvelope() {
        WebhookVerifier verifier = new WebhookVerifier(SECRET);
        WebhookPayload event = verifier.extractEvent(PAYLOAD);
        assertThat(verifier.getEventType(event)).isEqualTo("document_ready");

        Map<String, Object> data = verifier.getEventData(event);
        assertThat(data.get("document_id")).isEqualTo("doc-1");
    }

    @Test
    void getEventTypeReturnsNullForNullEvent() {
        WebhookVerifier verifier = new WebhookVerifier(SECRET);
        assertThat(verifier.getEventType(null)).isNull();
    }

    @Test
    void getEventDataReturnsEmptyMapForNullEvent() {
        WebhookVerifier verifier = new WebhookVerifier(SECRET);
        assertThat(verifier.getEventData(null)).isEmpty();
    }
}
