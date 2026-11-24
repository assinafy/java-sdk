package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.CreateEnvelopeRequest;
import com.assinafy.sdk.request.EnvelopeDocumentRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.SignerReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

class ScheduledSendResourceTest {
    private static final String RESPONSE = """
            {"status":200,"data":{"resource":"scheduled_send","id":"send","type":"assignment",
            "document_ids":["doc"],"status":"pending","send_at":"2026-12-01T12:00:00Z",
            "assignment_id":null,"envelope_id":null,"failure_reason":null,
            "created_at":"2026-11-30T12:00:00Z","processed_at":null}}
            """;

    private CreateAssignmentRequest assignment() {
        return CreateAssignmentRequest.builder().signers(List.of(SignerReference.builder().id("signer")
                .verificationMethod("Email").notificationMethods(List.of()).signAs("Witness").build()))
                .maxTotalCredits(new BigDecimal("1.25")).build();
    }

    private CreateEnvelopeRequest envelope() {
        return CreateEnvelopeRequest.builder().name("Agreement").signers(List.of(SignerReference.ofId("signer")))
                .documents(List.of(EnvelopeDocumentRequest.builder().id("doc").signerIds(List.of("signer")).build()))
                .maxTotalCredits(new BigDecimal("1.25")).build();
    }

    @Test
    void schedulesAssignmentWithSharedValidationAndPreservesRequest() throws Exception {
        var http = new MockApiHttpClient().enqueue(200, RESPONSE);
        var request = assignment();
        Instant sendAt = Instant.now().plusSeconds(86400);
        request.setExpiresAt(sendAt.plusSeconds(10800).toString());
        var result = new ScheduledSendResource(http, "account").createAssignment("d/a", request, sendAt);
        assertThat(result.getDocumentIds()).containsExactly("doc");
        assertThat(result.getFailureReason()).isNull();
        assertThat(result.getSendAt()).isEqualTo("2026-12-01T12:00:00Z");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/documents/d%2Fa/scheduled-sends");
        var body = new ObjectMapper().readTree(http.lastCaptured().getJsonBody());
        assertThat(body.get("send_at").asText()).isEqualTo(sendAt.toString());
        assertThat(body.get("expires_at").asText()).isEqualTo(request.getExpiresAt());
        assertThat(body.get("max_total_credits").decimalValue()).isEqualByComparingTo("1.25");
        assertThat(body.get("signers").get(0).get("sign_as").asText()).isEqualTo("Witness");
        assertThat(body.get("signers").get(0).get("notification_methods").isEmpty()).isTrue();
        assertThat(request.getMethod()).isNull();
    }

    @Test
    void schedulesEnvelopeAndUsesExplicitWorkspace() throws Exception {
        var http = new MockApiHttpClient().enqueue(200, RESPONSE.replace("assignment", "envelope"));
        Instant sendAt = Instant.now().plusSeconds(86400);
        var request = envelope();
        assertThat(new ScheduledSendResource(http, "default").createEnvelope(request, sendAt, "a/b").getType())
                .isEqualTo("envelope");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/a%2Fb/scheduled-sends");
        var body = new ObjectMapper().readTree(http.lastCaptured().getJsonBody());
        assertThat(body.get("documents").get(0).get("signer_ids").get(0).asText()).isEqualTo("signer");
        assertThat(body.get("max_total_credits").decimalValue()).isEqualByComparingTo("1.25");
        assertThat(body.get("send_at").asText()).isEqualTo(sendAt.toString());
    }

    @Test
    void listsWithPaginationAndPerformsStateTransitions() {
        var http = new MockApiHttpClient().enqueue(200, "{\"status\":200,\"data\":[]}", Map.of("X-Pagination-Total-Count", "0"))
                .enqueue(200, RESPONSE.replace("pending", "cancelled"))
                .enqueue(200, RESPONSE.replace("pending", "sent"));
        var resource = new ScheduledSendResource(http, "a/b");
        assertThat(resource.list(ListParams.builder().page(2).perPage(20).build(), "doc").getMeta().getTotal()).isZero();
        assertThat(http.lastCaptured().getQueryParams()).containsExactlyInAnyOrderEntriesOf(Map.of("page", 2, "per-page", 20, "document_id", "doc"));
        assertThat(resource.cancel("s/a").getStatus()).isEqualTo("cancelled");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/a%2Fb/scheduled-sends/s%2Fa/cancel");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(http.lastCaptured().getJsonBody()).isNull();
        assertThat(resource.sendNow("s/a", "other").getStatus()).isEqualTo("sent");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/other/scheduled-sends/s%2Fa/send");
    }

    @Test
    void rejectsInvalidTimesDeadlinesSigningRequestsAndIdsWithoutSending() {
        var http = new MockApiHttpClient();
        var resource = new ScheduledSendResource(http, "account");
        Instant later = Instant.now().plusSeconds(86400);
        for (Instant invalid : new Instant[]{null, Instant.now(), Instant.now().plusSeconds(91 * 86400L)}) {
            assertThatThrownBy(() -> resource.createAssignment("doc", assignment(), invalid)).isInstanceOf(ValidationException.class);
        }
        var request = assignment();
        request.setExpiresAt(later.plusSeconds(7199).toString());
        assertThatThrownBy(() -> resource.createAssignment("doc", request, later)).isInstanceOf(ValidationException.class);
        var envelope = envelope();
        envelope.getDocuments().getFirst().setExpiresAt(later.plusSeconds(7199).toString());
        assertThatThrownBy(() -> resource.createEnvelope(envelope, later)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.createAssignment("doc", null, later)).isInstanceOf(ValidationException.class);
        var capacityEnvelope = envelope();
        capacityEnvelope.getSigners().getFirst().setSignAs("Witness");
        assertThatThrownBy(() -> resource.createEnvelope(capacityEnvelope, later)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new EnvelopeResource(http, "account").estimateCost(capacityEnvelope)).isInstanceOf(ValidationException.class);
        request.setExpiresAt(null);
        request.getSigners().getFirst().setSignAs("witness");
        assertThatThrownBy(() -> resource.createAssignment("doc", request, later)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.cancel("..")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.list(ListParams.builder().perPage(51).build(), null)).isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void preservesRemoteConflictsAndNetworkFailures() {
        var http = new MockApiHttpClient().enqueue(409, "{\"status\":409,\"code\":\"CreditCeilingExceeded\",\"message\":\"Ceiling exceeded\"}");
        assertThatThrownBy(() -> new ScheduledSendResource(http, "account").sendNow("send"))
                .isInstanceOfSatisfying(ApiException.class, error -> assertThat(error.getStatusCode()).isEqualTo(409));
        var offline = new MockApiHttpClient() {
            @Override public com.assinafy.sdk.http.HttpRawResponse put(String path, String body) throws java.io.IOException {
                throw new java.io.IOException("offline");
            }
        };
        assertThatThrownBy(() -> new ScheduledSendResource(offline, "account").cancel("send")).isInstanceOf(NetworkException.class);
    }
}
