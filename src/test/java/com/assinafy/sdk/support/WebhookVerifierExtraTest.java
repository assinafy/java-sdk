package com.assinafy.sdk.support;

import com.assinafy.sdk.models.WebhookPayload;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/** Robustness coverage for {@link WebhookVerifier}: array payloads, raw payload, null inputs. */
class WebhookVerifierExtraTest {

    private final WebhookVerifier verifier = new WebhookVerifier("secret");

    @Test
    void extractEventHandlesEmptyArrayPayloadWithoutFailing() {
        // Several event types (document_uploaded/prepared/metadata_ready) deliver payload:[].
        String body = "{\"id\":6,\"event\":\"document_uploaded\",\"message\":\"m\",\"payload\":[]," +
                "\"created_at\":1705312200,\"account_id\":\"1a\"}";
        WebhookPayload event = verifier.extractEvent(body);

        assertThat(event).isNotNull();
        assertThat(event.getEvent()).isEqualTo("document_uploaded");
        assertThat(event.getPayload()).isNull();          // array → not an object map
        assertThat(event.getPayloadRaw()).isInstanceOf(java.util.List.class);
        assertThat(verifier.getEventData(event)).isEmpty();
    }

    @Test
    void extractEventExposesObjectPayloadAsMap() {
        String body = "{\"id\":8,\"event\":\"assignment_created\"," +
                "\"payload\":{\"user_name\":\"John\"},\"object\":null,\"account_id\":\"1a\"}";
        WebhookPayload event = verifier.extractEvent(body);

        assertThat(event.getPayload()).containsEntry("user_name", "John");
        assertThat(verifier.getEventData(event)).containsEntry("user_name", "John");
    }

    @Test
    void getEventDataPrefersObjectOverPayload() {
        String body = "{\"event\":\"document_ready\",\"object\":{\"id\":\"d1\",\"type\":\"document\"}," +
                "\"payload\":{\"ignored\":true}}";
        WebhookPayload event = verifier.extractEvent(body);
        assertThat(verifier.getEventData(event)).containsEntry("id", "d1");
    }

    @Test
    void verifyReturnsFalseOnNullPayloadInsteadOfThrowing() {
        assertThat(verifier.verify((String) null, "any-signature")).isFalse();
    }

    @Test
    void verifyReturnsFalseWhenNoSecretConfigured() {
        WebhookVerifier noSecret = new WebhookVerifier(null);
        assertThat(noSecret.verify("body", "sig")).isFalse();
    }
}
