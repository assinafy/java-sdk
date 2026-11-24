package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.SignerReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class DocumentDeliveryTest {
    @Test
    void capacitiesAreValidatedAndIncludedInCreationAndEstimates() throws Exception {
        for (String capacity : SigningRules.SIGNER_CAPACITIES) {
            var http = new MockApiHttpClient().enqueue(200, "{\"status\":200,\"data\":{\"id\":\"assignment\"}}")
                    .enqueue(200, "{\"status\":200,\"data\":{}}");
            var resource = new AssignmentResource(http);
            var request = CreateAssignmentRequest.builder().signers(List.of(SignerReference.builder()
                    .id("signer").signAs(capacity).build())).build();
            resource.create("doc", request);
            resource.estimateCost("doc", request);
            var mapper = new ObjectMapper();
            for (var call : http.getCaptured()) {
                assertThat(mapper.readTree(call.getJsonBody()).get("signers").get(0).get("sign_as").asText()).isEqualTo(capacity);
            }
            assertThat(mapper.readTree(http.lastCaptured().getJsonBody()).get("signers").get(0).has("id")).isFalse();
        }
        var http = new MockApiHttpClient();
        var request = CreateAssignmentRequest.builder().signers(List.of(SignerReference.builder().id("s").signAs("witness").build())).build();
        assertThatThrownBy(() -> new AssignmentResource(http).create("doc", request)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new AssignmentResource(http).estimateCost("doc", request)).isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void readsCapacitiesAndNullableNotificationDeliveryTimes() {
        var http = new MockApiHttpClient().enqueue(200, "{\"status\":200,\"data\":[{\"signer_id\":\"s\",\"sign_as\":\"Witness\"}]}")
                .enqueue(200, """
                    {"status":200,"data":[{"signer_id":"s","notification_history":[
                    {"event":"signature_request","delivered_at":"2026-12-01T12:00:00Z","opened_at":null}]}]}
                    """);
        var resource = new AssignmentResource(http);
        assertThat(resource.signerCapacities("d/a").getFirst().getSignAs()).isEqualTo("Witness");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/documents/d%2Fa/signer-capacities");
        var history = resource.notificationHistory("d/a").getFirst();
        assertThat(http.lastCaptured().getPath()).isEqualTo("/documents/d%2Fa/notification-history");
        assertThat(history.getSignerId()).isEqualTo("s");
        assertThat(history.getNotificationHistory().getFirst().getDeliveredAt()).isEqualTo("2026-12-01T12:00:00Z");
        assertThat(history.getNotificationHistory().getFirst().getOpenedAt()).isNull();
    }

    @Test
    void watchersCanBeReadDeduplicatedAndCleared() {
        var http = new MockApiHttpClient().enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"s\"}]}")
                .enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"s\"}]}")
                .enqueue(200, "{\"status\":200,\"data\":[]}");
        var resource = new DocumentResource(http, "account");
        assertThat(resource.listWatchers("d/a").getFirst().getId()).isEqualTo("s");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/documents/d%2Fa/watchers");
        resource.setWatchers("d/a", List.of("s", "s"));
        assertThat(http.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(http.lastCaptured().getJsonBody()).isEqualTo("{\"watcher_ids\":[\"s\"]}");
        assertThat(resource.setWatchers("doc", List.of())).isEmpty();
        assertThat(http.lastCaptured().getJsonBody()).isEqualTo("{\"watcher_ids\":[]}");
        assertThatThrownBy(() -> resource.setWatchers("doc", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.setWatchers("doc", List.of(" "))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.setWatchers("doc", java.util.stream.IntStream.range(0, 11).mapToObj(String::valueOf).toList()))
                .isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isEqualTo(3);
    }

    @Test
    void estimatesEnvelopeReminderWithoutSendingOne() {
        var http = new MockApiHttpClient().enqueue(200, "{\"status\":200,\"data\":{\"total\":0,\"has_sufficient_credits\":true}}");
        var result = new EnvelopeResource(http, "a/b").estimateResendCost("e/a", "s/a");
        assertThat(result.getTotal()).isEqualByComparingTo("0");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/a%2Fb/envelopes/e%2Fa/signers/s%2Fa/estimate-resend-cost");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(http.lastCaptured().getJsonBody()).isNull();
    }
}
