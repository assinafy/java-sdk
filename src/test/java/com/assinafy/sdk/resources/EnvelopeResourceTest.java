package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.request.CreateEnvelopeRequest;
import com.assinafy.sdk.request.EnvelopeDocumentRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.SignerReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

class EnvelopeResourceTest {
    private static final String RESPONSE = """
            {"status":200,"data":{"id":"envelope","name":"Agreements","message":null,
              "documents":[{"id":"document","position":1,"name":"contract.pdf","status":"pending_signature",
                "artifacts":{"original":"https://example.invalid/original"},"signers":[{"id":"signer","completed":false,"signed_at":null}]}],
              "counts":{"pending_signature":1},"signing_urls":[{"signer_id":"signer","url":null,"embedded":true}],
              "created_at":"2026-11-11T12:00:00Z"}}
            """;

    private static CreateEnvelopeRequest request() {
        return CreateEnvelopeRequest.builder().name("Agreements")
                .signers(List.of(SignerReference.builder().id("signer").verificationMethod("Email")
                        .notificationMethods(List.of()).build()))
                .documents(List.of(EnvelopeDocumentRequest.builder().id("document").signerIds(List.of("signer")).build()))
                .maxTotalCredits(new BigDecimal("0.45")).build();
    }

    @Test
    void createsAndEstimatesWithExactBodyAndIdempotencyHeader() throws Exception {
        var http = new MockApiHttpClient().enqueue(200, RESPONSE)
                .enqueue(200, "{\"status\":200,\"data\":{\"documents\":1,\"total_credits\":0.45}}");
        var resource = new EnvelopeResource(http, "account");
        var created = resource.create(request(), "account/override", "key-1");
        assertThat(created.getDocuments().getFirst().getPosition()).isEqualTo(1);
        assertThat(created.getDocuments().getFirst().getSigners().getFirst().getCompleted()).isFalse();
        assertThat(created.getSigningUrls().getFirst().getEmbedded()).isTrue();
        assertThat(created.getCounts()).containsEntry("pending_signature", 1);
        assertThat(http.capturedAt(0).getPath()).isEqualTo("/accounts/account%2Foverride/envelopes");
        assertThat(http.capturedAt(0).getMethod()).isEqualTo("POST");
        assertThat(http.capturedAt(0).getIdempotencyKey()).isEqualTo("key-1");
        var expected = new ObjectMapper().readTree("""
                {"name":"Agreements","max_total_credits":0.45,
                 "signers":[{"id":"signer","verification_method":"Email","notification_methods":[]}],
                 "documents":[{"id":"document","method":"virtual","signer_ids":["signer"]}]}
                """);
        assertThat(new ObjectMapper().readTree(http.capturedAt(0).getJsonBody())).isEqualTo(expected);
        var estimate = request();
        estimate.setMaxTotalCredits(new BigDecimal("-1"));
        assertThat(resource.estimateCost(estimate).getTotalCredits()).isEqualByComparingTo("0.45");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/account/envelopes/estimate-cost");
        assertThat(http.lastCaptured().getJsonBody()).doesNotContain("max_total_credits");
    }

    @Test
    void preservesNullUpdatesAndSupportsTheWholeLifecycle() throws Exception {
        var http = new MockApiHttpClient().enqueue(200, "{\"status\":200,\"data\":[]}",
                        Map.of("X-Pagination-Per-Page", "2"))
                .enqueue(200, RESPONSE).enqueue(200, RESPONSE).enqueue(200, RESPONSE).enqueue(200, RESPONSE)
                .enqueue(200, "{\"status\":200,\"data\":[{\"id\":1,\"document_id\":\"document\",\"document_name\":\"contract.pdf\"}]}")
                .enqueue(200, "zip-bytes").enqueue(200, "{\"status\":200,\"data\":{\"is_sent\":true,\"envelope_id\":\"env\",\"signer_id\":\"signer\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"document\",\"status\":\"cancelled\",\"cancelled\":true}]}")
                .enqueue(200, "{\"status\":200,\"data\":[]}");
        var resource = new EnvelopeResource(http, "account");
        assertThat(resource.list(ListParams.builder().status("open").perPage(2).build()).getMeta().getPerPage()).isEqualTo(2);
        resource.get("env/a");
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("message", null);
        changes.put("document_ids", List.of("document"));
        resource.update("env/a", changes);
        assertThat(new ObjectMapper().readTree(http.lastCaptured().getJsonBody()).get("message").isNull()).isTrue();
        assertThat(changes).containsKey("message");
        resource.addDocument("env/a", EnvelopeDocumentRequest.builder().id("new-document")
                .signerIds(List.of("signer")).maxTotalCredits(BigDecimal.ZERO).build());
        assertThat(http.lastCaptured().getJsonBody()).contains("\"max_total_credits\":0");
        resource.removeDocument("env/a", "new/document");
        assertThat(resource.activities("env/a", ListParams.builder().sort("id").build()).getData().getFirst().getDocumentName())
                .isEqualTo("contract.pdf");
        assertThat(resource.download("env/a", "bundle")).isEqualTo("zip-bytes".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertThat(http.lastCaptured().getAccept()).isEqualTo("*/*");
        assertThat(resource.resendNotification("env/a", "signer/a").getIsSent()).isTrue();
        assertThat(resource.cancel("env/a").getFirst().getCancelled()).isTrue();
        resource.delete("env/a");
        assertThat(http.getCaptured()).extracting(MockApiHttpClient.CapturedRequest::getMethod)
                .containsExactly("GET", "GET", "PATCH", "POST", "DELETE", "GET", "GET_BINARY", "PUT", "PUT", "DELETE");
        assertThat(http.getCaptured()).extracting(MockApiHttpClient.CapturedRequest::getPath).containsExactly(
                "/accounts/account/envelopes", "/accounts/account/envelopes/env%2Fa", "/accounts/account/envelopes/env%2Fa",
                "/accounts/account/envelopes/env%2Fa/documents", "/accounts/account/envelopes/env%2Fa/documents/new%2Fdocument",
                "/accounts/account/envelopes/env%2Fa/activities", "/accounts/account/envelopes/env%2Fa/download/bundle",
                "/accounts/account/envelopes/env%2Fa/signers/signer%2Fa/resend", "/accounts/account/envelopes/env%2Fa/cancel",
                "/accounts/account/envelopes/env%2Fa");
    }

    @Test
    void rejectsInvalidEnvelopeDefinitionsWithoutSending() {
        var http = new MockApiHttpClient();
        var resource = new EnvelopeResource(http, "account");
        List<Consumer<CreateEnvelopeRequest>> invalid = List.of(
                r -> r.setName("<agreement>"), r -> r.setName("x".repeat(256)), r -> r.setName(" "),
                r -> r.setSigners(List.of()), r -> r.setDocuments(List.of()),
                r -> r.setSigners(java.util.Collections.nCopies(21, SignerReference.ofId("s"))),
                r -> r.setDocuments(java.util.Collections.nCopies(21, r.getDocuments().getFirst())),
                r -> r.setSigners(List.of(SignerReference.ofId("signer"), SignerReference.ofId("signer"))),
                r -> r.setDocuments(List.of(r.getDocuments().getFirst(), r.getDocuments().getFirst())),
                r -> r.getSigners().getFirst().setStep(1),
                r -> r.getSigners().getFirst().setVerificationMethod("unsupported"),
                r -> r.getSigners().getFirst().setNotificationMethods(List.of("Email", "Whatsapp")),
                r -> r.getSigners().getFirst().setNotificationMethods(List.of("Whatsapp")),
                r -> r.getDocuments().getFirst().setSignerIds(List.of("missing")),
                r -> r.getDocuments().getFirst().setSignerIds(List.of("signer", "signer")),
                r -> r.getDocuments().getFirst().setExpiresAt("2020-01-01"),
                r -> r.getDocuments().getFirst().setMethod("collect"),
                r -> r.setMaxTotalCredits(new BigDecimal("-0.01")),
                r -> r.setSigners(new ArrayList<>(java.util.Arrays.asList((SignerReference) null)))
        );
        for (var mutate : invalid) {
            var request = request(); mutate.accept(request);
            assertThatThrownBy(() -> resource.create(request)).isInstanceOf(ValidationException.class);
        }
        assertThatThrownBy(() -> resource.create(request(), null, "key\nheader")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.addDocument("env", EnvelopeDocumentRequest.builder()
                .id("document").signerIds(List.of("signer")).method("collect").build()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.get("..")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.list(ListParams.builder().status("other").build())).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.list(ListParams.builder().perPage(51).build())).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.update("env", Map.of())).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.update("env", Map.of("unknown", true))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.update("env", Map.of("message", 1))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.update("env", Map.of("document_ids", List.of("d", "d")))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.download("env", "../secret")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.activities("env", ListParams.builder().sort("name").build())).isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"DigitalCertificate", "IdDocumentFaceMatchBasic"})
    void acceptsVerificationWithEitherChannelAndRejectsEmbedded(String verification) {
        for (String channel : List.of("Email", "Whatsapp")) {
            var http = new MockApiHttpClient().enqueue(200, RESPONSE);
            var resource = new EnvelopeResource(http, "account");
            var request = request();
            request.getSigners().getFirst().setVerificationMethod(verification);
            request.getSigners().getFirst().setNotificationMethods(List.of(channel));
            resource.create(request);
            assertThat(http.lastCaptured().getJsonBody()).contains(verification, channel);
            request.getSigners().getFirst().setEmbedded(true);
            assertThatThrownBy(() -> resource.create(request)).isInstanceOf(ValidationException.class);
            assertThat(http.capturedCount()).isEqualTo(1);
        }
    }

    @Test
    void validatesCollectGeometryAndSignerMembership() {
        var http = new MockApiHttpClient().enqueue(200, RESPONSE);
        var resource = new EnvelopeResource(http, "account");
        var request = request();
        var geometry = new LinkedHashMap<String, Object>(Map.of("left", 0, "top", 0, "width", 100, "height", 20, "fontSize", 12));
        request.getDocuments().getFirst().setMethod("collect");
        request.getDocuments().getFirst().setEntries(List.of(Map.of("page_id", "page", "fields", List.of(
                Map.of("signer_id", "signer", "field_id", "field", "display_settings", geometry)))));
        resource.create(request);
        for (Object invalid : List.of(-1, Double.NaN, Double.POSITIVE_INFINITY, "12")) {
            geometry.put("width", invalid);
            assertThatThrownBy(() -> resource.create(request)).isInstanceOf(ValidationException.class);
        }
        geometry.put("width", 100);
        request.getSigners().getFirst().setVerificationMethod("DigitalCertificate");
        assertThatThrownBy(() -> resource.create(request)).isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isEqualTo(1);
    }

    @Test
    void propagatesRemoteAndNetworkFailuresWithoutReplay() {
        var http = new MockApiHttpClient().enqueue(409, "{\"status\":409,\"code\":\"EnvelopeOpen\"}")
                .enqueueFailure(new IOException("lost connection"));
        var resource = new EnvelopeResource(http, "account");
        assertThatThrownBy(() -> resource.delete("env")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> resource.create(request())).isInstanceOf(NetworkException.class);
        assertThat(http.capturedCount()).isEqualTo(2);
    }
}
