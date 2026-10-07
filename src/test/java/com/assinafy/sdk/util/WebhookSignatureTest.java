package com.assinafy.sdk.util;

import com.assinafy.sdk.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/** Reference vector from the Standard Webhooks specification. */
class WebhookSignatureTest {

    private static final String SECRET = "whsec_MfKQ9r8GKYqrTwjUPD8ILPZIo2LaLaSw";
    private static final String ID = "msg_p5jXN8AQM9LWM0D4loKWxJek";
    private static final String TIMESTAMP = "1614265330";
    private static final String SIGNATURE = "v1,g0hM9SsE+OTPJTGt/tmIKtSyZlE3uFJELVlNIOLJ1OE=";
    private static final byte[] BODY = "{\"test\": 2432232314}".getBytes(StandardCharsets.UTF_8);
    private static final Instant SENT = Instant.ofEpochSecond(1614265330);
    private static final Duration TOLERANCE = WebhookSignature.DEFAULT_TOLERANCE;

    @Test
    void acceptsTheReferenceSignature() {
        assertThat(WebhookSignature.verify(SECRET, ID, TIMESTAMP, SIGNATURE, BODY, TOLERANCE, SENT)).isTrue();
    }

    @Test
    void acceptsAnyMatchingEntryAmongSeveral() {
        String header = "v1,bm90LXRoZS1zaWduYXR1cmU= " + SIGNATURE;
        assertThat(WebhookSignature.verify(SECRET, ID, TIMESTAMP, header, BODY, TOLERANCE, SENT)).isTrue();
    }

    @Test
    void rejectsTamperedBodyIdOrVersion() {
        byte[] tampered = "{\"test\": 2432232315}".getBytes(StandardCharsets.UTF_8);
        assertThat(WebhookSignature.verify(SECRET, ID, TIMESTAMP, SIGNATURE, tampered, TOLERANCE, SENT)).isFalse();
        assertThat(WebhookSignature.verify(SECRET, "msg_other", TIMESTAMP, SIGNATURE, BODY, TOLERANCE, SENT)).isFalse();
        assertThat(WebhookSignature.verify(SECRET, ID, TIMESTAMP, SIGNATURE.replace("v1,", "v2,"), BODY, TOLERANCE, SENT)).isFalse();
    }

    @Test
    void rejectsTimestampsOutsideTheTolerance() {
        assertThat(WebhookSignature.verify(SECRET, ID, TIMESTAMP, SIGNATURE, BODY, TOLERANCE, SENT.plusSeconds(301))).isFalse();
        assertThat(WebhookSignature.verify(SECRET, ID, TIMESTAMP, SIGNATURE, BODY, TOLERANCE, SENT.minusSeconds(301))).isFalse();
        assertThat(WebhookSignature.verify(SECRET, ID, TIMESTAMP, SIGNATURE, BODY, TOLERANCE, SENT.plusSeconds(300))).isTrue();
        assertThat(WebhookSignature.verify(SECRET, ID, "not-a-number", SIGNATURE, BODY, TOLERANCE, SENT)).isFalse();
    }

    @Test
    void rejectsMissingHeaders() {
        assertThat(WebhookSignature.verify(SECRET, Map.of(), BODY)).isFalse();
        assertThat(WebhookSignature.verify(SECRET, (Map<String, String>) null, BODY)).isFalse();
        assertThat(WebhookSignature.verify(SECRET, null, TIMESTAMP, SIGNATURE, BODY, TOLERANCE, SENT)).isFalse();
    }

    @Test
    void headerMapIsCaseInsensitiveAndUsesTheCurrentClock() {
        String now = Long.toString(Instant.now().getEpochSecond());
        Map<String, String> stale = Map.of("Webhook-Id", ID, "WEBHOOK-TIMESTAMP", TIMESTAMP, "webhook-signature", SIGNATURE);
        // The reference vector is from 2021, so the live clock correctly treats it as a replay.
        assertThat(WebhookSignature.verify(SECRET, stale, new String(BODY, StandardCharsets.UTF_8))).isFalse();
        Map<String, String> fresh = Map.of("Webhook-Id", ID, "WEBHOOK-TIMESTAMP", now, "webhook-signature", "v1,x");
        assertThat(WebhookSignature.verify(SECRET, fresh, BODY)).isFalse();
    }

    @Test
    void rejectsSecretsThatAreNotWhsecBase64() {
        assertThatThrownBy(() -> WebhookSignature.verify("MfKQ9r8GKYqrTwjUPD8ILPZIo2LaLaSw", ID, TIMESTAMP, SIGNATURE, BODY, TOLERANCE, SENT))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> WebhookSignature.verify("whsec_%%%", ID, TIMESTAMP, SIGNATURE, BODY, TOLERANCE, SENT))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> WebhookSignature.verify("whsec_", ID, TIMESTAMP, SIGNATURE, BODY, TOLERANCE, SENT))
                .isInstanceOf(ValidationException.class);
    }
}
