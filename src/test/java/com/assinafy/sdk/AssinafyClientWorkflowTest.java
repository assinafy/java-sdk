package com.assinafy.sdk;

import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.models.UploadAndRequestSignaturesResult;
import com.assinafy.sdk.request.UploadAndRequestSignaturesRequest;
import com.assinafy.sdk.request.UploadAndRequestSignaturesRequest.SignerEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** Exercises the flagship high-level {@link AssinafyClient#uploadAndRequestSignatures} workflow. */
class AssinafyClientWorkflowTest {

    private AssinafyClient clientWith(MockApiHttpClient http) {
        return new AssinafyClient(http, AssinafyClientOptions.builder()
                .apiKey("k").accountId("acc").build());
    }

    @Test
    void uploadAndRequestSignaturesOrchestratesUploadWaitCreateAssign() {
        MockApiHttpClient http = new MockApiHttpClient();
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}");       // upload
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"metadata_ready\"}}"); // waitUntilReady -> details
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"sig1\",\"full_name\":\"Maria\"}}");        // create signer
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"asg1\",\"method\":\"virtual\"}}");         // create assignment

        UploadAndRequestSignaturesResult result = clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder()
                                .name("Maria").whatsappPhoneNumber("+5548999990000").build()))
                        .message("Please sign")
                        .waitForReady(true)
                        .build());

        assertThat(result.getDocument().getId()).isEqualTo("doc1");
        assertThat(result.getSignerIds()).containsExactly("sig1");
        assertThat(result.getAssignment().getId()).isEqualTo("asg1");

        assertThat(http.capturedCount()).isEqualTo(4);
        assertThat(http.capturedAt(0).getMethod()).isEqualTo("POST_MULTIPART");
        assertThat(http.capturedAt(0).getPath()).isEqualTo("/accounts/acc/documents");
        assertThat(http.capturedAt(1).getMethod()).isEqualTo("GET");
        assertThat(http.capturedAt(1).getPath()).isEqualTo("/documents/doc1");
        assertThat(http.capturedAt(2).getMethod()).isEqualTo("POST");
        assertThat(http.capturedAt(2).getPath()).isEqualTo("/accounts/acc/signers");
        assertThat(http.capturedAt(3).getMethod()).isEqualTo("POST");
        assertThat(http.capturedAt(3).getPath()).isEqualTo("/documents/doc1/assignments");
        assertThat(http.capturedAt(3).getJsonBody())
                .contains("\"method\":\"virtual\"")
                .contains("\"verification_method\":\"Whatsapp\"")
                .contains("\"notification_methods\":[\"Whatsapp\"]")
                .contains("sig1");
    }

    @Test
    void uploadAndRequestSignaturesSkipsReadyPollWhenWaitForReadyFalse() {
        MockApiHttpClient http = new MockApiHttpClient();
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}"); // upload
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"sig1\"}}");                          // create signer
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"asg1\"}}");                          // create assignment

        clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder().name("Maria")
                                .whatsappPhoneNumber("+5548999990000").build()))
                        .waitForReady(false)
                        .build());

        assertThat(http.capturedCount()).isEqualTo(3); // no GET /documents/doc1 readiness poll
        assertThat(http.getCaptured()).noneMatch(r -> "GET".equals(r.getMethod()));
    }

    @Test
    void uploadAndRequestSignaturesRejectsEmptySignerList() {
        MockApiHttpClient http = new MockApiHttpClient();
        assertThatThrownBy(() -> clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes()).fileName("c.pdf").signers(List.of()).build()))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
    }

    @Test
    void uploadAndRequestSignaturesValidatesEveryEmailBeforeUpload() {
        MockApiHttpClient http = new MockApiHttpClient();

        assertThatThrownBy(() -> clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(
                                SignerEntry.builder().name("Valid")
                                        .email("valid@example.invalid").build(),
                                SignerEntry.builder().name("Invalid")
                                        .email("not-an-email").build()))
                        .build()))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void uploadAndRequestSignaturesRequiresADeliveryChannelBeforeUpload() {
        MockApiHttpClient http = new MockApiHttpClient();

        assertThatThrownBy(() -> clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder().name("No Channel").build()))
                        .build()))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void uploadAndRequestSignaturesRollsBackItsDocumentAndSignersOnFailure() {
        MockApiHttpClient http = new MockApiHttpClient();
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}");
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"sig1\"}}");
        http.enqueue(400, "{\"status\":400,\"message\":\"assignment rejected\"}");
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"metadata_ready\"}}");
        http.enqueue(200, "{\"status\":200,\"data\":[]}");
        http.enqueue(200, "{\"status\":200,\"data\":[]}");

        assertThatThrownBy(() -> clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder()
                                .name("Maria").whatsappPhoneNumber("+5548999990000").build()))
                        .waitForReady(false)
                        .build()))
                .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class);

        assertThat(http.capturedAt(3).getMethod()).isEqualTo("GET");
        assertThat(http.capturedAt(3).getPath()).isEqualTo("/documents/doc1");
        assertThat(http.capturedAt(4).getMethod()).isEqualTo("DELETE");
        assertThat(http.capturedAt(4).getPath()).isEqualTo("/documents/doc1");
        assertThat(http.capturedAt(5).getMethod()).isEqualTo("DELETE");
        assertThat(http.capturedAt(5).getPath()).isEqualTo("/accounts/acc/signers/sig1");
    }

    @Test
    void uploadAndRequestSignaturesRecoversCommittedAssignmentAfterServerError() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"sig1\"}}")
                .enqueue(500, "{\"status\":500,\"message\":\"response lost\"}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\","
                        + "\"assignment\":{\"id\":\"asg1\",\"method\":\"virtual\"}}}");

        UploadAndRequestSignaturesResult result = clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder().name("Maria")
                                .whatsappPhoneNumber("+5548999990000").build()))
                        .waitForReady(false)
                        .build());

        assertThat(result.getAssignment().getId()).isEqualTo("asg1");
        assertThat(http.getCaptured()).noneMatch(r -> "DELETE".equals(r.getMethod()));
    }

    @Test
    void uploadAndRequestSignaturesRetainsResourcesWhenAssignmentOutcomeStaysIndeterminate() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"sig1\"}}")
                .enqueue(500, "{\"status\":500,\"message\":\"response lost\"}");
        for (int attempt = 0; attempt < 5; attempt++) {
            http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\","
                    + "\"status\":\"metadata_ready\"}}}"
            );
        }

        assertThatThrownBy(() -> clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder().name("Maria")
                                .whatsappPhoneNumber("+5548999990000").build()))
                        .waitForReady(false)
                        .build()))
                .isInstanceOf(ApiException.class)
                .satisfies(failure -> assertThat(failure.getSuppressed())
                        .anySatisfy(suppressed -> {
                            assertThat(suppressed).isInstanceOf(AssinafyException.class);
                            assertThat(((AssinafyException) suppressed).getContext())
                                    .containsEntry("documentId", "doc1");
                        }));

        assertThat(http.getCaptured()).noneMatch(r -> "DELETE".equals(r.getMethod()));
    }

    @Test
    void uploadAndRequestSignaturesRejectsDuplicateRecipientsBeforeUpload() {
        MockApiHttpClient http = new MockApiHttpClient();

        assertThatThrownBy(() -> clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(
                                SignerEntry.builder().name("One")
                                        .email("same@example.invalid").build(),
                                SignerEntry.builder().name("Two")
                                        .email("SAME@example.invalid").build()))
                        .build()))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void uploadAndRequestSignaturesLeavesReusedEmailSignerUnchangedOnFailure() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"existing\","
                        + "\"full_name\":\"Maria\",\"email\":\"maria@example.invalid\"}]}")
                .enqueue(400, "{\"status\":400,\"message\":\"assignment rejected\"}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\","
                        + "\"status\":\"metadata_ready\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[]}");

        assertThatThrownBy(() -> clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder().name("Maria")
                                .email("maria@example.invalid")
                                .cpf("400.676.228-36").build()))
                        .waitForReady(false)
                        .build()))
                .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class);

        assertThat(http.getCaptured()).noneMatch(r -> "PUT".equals(r.getMethod()));
        assertThat(http.getCaptured().stream()
                .filter(r -> "DELETE".equals(r.getMethod())))
                .extracting(MockApiHttpClient.CapturedRequest::getPath)
                .containsExactly("/documents/doc1");
    }

    @Test
    void uploadAndRequestSignaturesRecoversConcurrentEmailSignerCreation() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueue(409, "{\"status\":409,\"message\":\"already exists\"}")
                .enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"existing\","
                        + "\"full_name\":\"Maria\",\"email\":\"maria@example.invalid\"}]}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"asg1\"}}");

        UploadAndRequestSignaturesResult result = clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder().name("Maria")
                                .email("maria@example.invalid")
                                .cpf("400.676.228-36").build()))
                        .waitForReady(false)
                        .build());

        assertThat(result.getSignerIds()).containsExactly("existing");
        assertThat(http.capturedAt(1).getMethod()).isEqualTo("GET");
        assertThat(http.capturedAt(2).getMethod()).isEqualTo("POST");
        assertThat(http.capturedAt(3).getMethod()).isEqualTo("GET");
        assertThat(http.getCaptured()).noneMatch(r -> "PUT".equals(r.getMethod()));
    }

    @Test
    void uploadAndRequestSignaturesDoesNotOwnMalformedCreateRecovery() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueue(200, "{\"status\":200,\"data\":null}")
                .enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"sig1\","
                        + "\"email\":\"maria@example.invalid\"}]}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\","
                        + "\"status\":\"metadata_ready\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[]}");

        assertThatThrownBy(() -> clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder().name("Maria")
                                .email("maria@example.invalid")
                                .cpf("400.676.228-36").build()))
                        .waitForReady(false)
                        .build()))
                .isInstanceOf(AssinafyException.class)
                .hasMessageContaining("indeterminate");

        assertThat(http.getCaptured()).noneMatch(request -> "PUT".equals(request.getMethod()));
        assertThat(http.getCaptured().stream()
                .filter(request -> "DELETE".equals(request.getMethod())))
                .extracting(MockApiHttpClient.CapturedRequest::getPath)
                .containsExactly("/documents/doc1");
    }

    @Test
    void uploadAndRequestSignaturesAppliesGovernmentIdOnlyAfterDefiniteCreate() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"sig1\","
                        + "\"email\":\"maria@example.invalid\"}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"sig1\"}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"asg1\"}}");

        UploadAndRequestSignaturesResult result = clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder().name("Maria")
                                .email("maria@example.invalid")
                                .cpf("400.676.228-36").build()))
                        .waitForReady(false)
                        .build());

        assertThat(result.getSignerIds()).containsExactly("sig1");
        assertThat(http.capturedAt(3).getMethod()).isEqualTo("PUT");
        assertThat(http.capturedAt(3).getJsonBody())
                .contains("\"government_id\":\"40067622836\"");
    }

    @Test
    void uploadAndRequestSignaturesRecoversCommittedCreateAfterNetworkFailure() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueueFailure(new java.io.IOException("response lost"))
                .enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"sig1\","
                        + "\"email\":\"maria@example.invalid\"}]}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"asg1\"}}");

        UploadAndRequestSignaturesResult result = clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder().name("Maria")
                                .email("maria@example.invalid").build()))
                        .waitForReady(false)
                        .build());

        assertThat(result.getSignerIds()).containsExactly("sig1");
        assertThat(result.getAssignment().getId()).isEqualTo("asg1");
    }

    @Test
    void uploadAndRequestSignaturesRetainsAmbiguousSignerOnLaterFailure() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueueFailure(new java.io.IOException("response lost"))
                .enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"sig1\","
                        + "\"email\":\"maria@example.invalid\"}]}")
                .enqueue(400, "{\"status\":400,\"message\":\"assignment rejected\"}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\","
                        + "\"status\":\"metadata_ready\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[]}");

        assertThatThrownBy(() -> clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder().name("Maria")
                                .email("maria@example.invalid").build()))
                        .waitForReady(false)
                        .build()))
                .isInstanceOf(ApiException.class)
                .satisfies(failure -> assertThat(failure.getSuppressed())
                        .anySatisfy(suppressed -> assertThat(suppressed.getMessage())
                                .contains("signer records were retained")));

        assertThat(http.getCaptured().stream()
                .filter(request -> "DELETE".equals(request.getMethod())))
                .extracting(MockApiHttpClient.CapturedRequest::getPath)
                .containsExactly("/documents/doc1");
    }

    @Test
    void uploadAndRequestSignaturesRetriesRecoveryAfterServerFailure() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueue(500, "{\"status\":500,\"message\":\"response lost\"}")
                .enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"sig1\","
                        + "\"email\":\"maria@example.invalid\"}]}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"asg1\"}}");

        UploadAndRequestSignaturesResult result = clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder().name("Maria")
                                .email("maria@example.invalid").build()))
                        .waitForReady(false)
                        .build());

        assertThat(result.getSignerIds()).containsExactly("sig1");
        assertThat(http.getCaptured().stream()
                .filter(r -> "GET".equals(r.getMethod())))
                .hasSize(3);
    }

    @Test
    void uploadAndRequestSignaturesCleansEarlierSignerWhenLaterCreationFails() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"sig1\"}}")
                .enqueue(500, "{\"status\":500,\"message\":\"signer failed\"}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\","
                        + "\"status\":\"metadata_ready\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueue(200, "{\"status\":200,\"data\":[]}");

        assertThatThrownBy(() -> clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(
                                SignerEntry.builder().name("One")
                                        .whatsappPhoneNumber("+5548999990001").build(),
                                SignerEntry.builder().name("Two")
                                        .whatsappPhoneNumber("+5548999990002").build()))
                        .waitForReady(false)
                        .build()))
                .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class);

        assertThat(http.getCaptured().stream()
                .filter(r -> "DELETE".equals(r.getMethod())))
                .extracting(MockApiHttpClient.CapturedRequest::getPath)
                .containsExactly("/documents/doc1", "/accounts/acc/signers/sig1");
    }

    @Test
    void uploadAndRequestSignaturesRetriesTransientSignerCleanup() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"status\":\"uploaded\"}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"sig1\"}}")
                .enqueue(400, "{\"status\":400,\"message\":\"assignment rejected\"}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\","
                        + "\"status\":\"metadata_ready\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueue(404, "{\"status\":404,\"message\":\"not visible yet\"}")
                .enqueue(200, "{\"status\":200,\"data\":[]}");

        assertThatThrownBy(() -> clientWith(http).uploadAndRequestSignatures(
                UploadAndRequestSignaturesRequest.builder()
                        .fileData("%PDF-1.4".getBytes())
                        .fileName("c.pdf")
                        .signers(List.of(SignerEntry.builder().name("Maria")
                                .whatsappPhoneNumber("+5548999990000").build()))
                        .waitForReady(false)
                        .build()))
                .isInstanceOf(ApiException.class);

        assertThat(http.getCaptured().stream()
                .filter(r -> "DELETE".equals(r.getMethod())))
                .extracting(MockApiHttpClient.CapturedRequest::getPath)
                .containsExactly("/documents/doc1", "/accounts/acc/signers/sig1",
                        "/accounts/acc/signers/sig1");
    }
}
