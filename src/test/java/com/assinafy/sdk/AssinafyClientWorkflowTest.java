package com.assinafy.sdk;

import com.assinafy.sdk.helper.MockApiHttpClient;
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
        assertThat(http.capturedAt(3).getJsonBody()).contains("\"method\":\"virtual\"").contains("sig1");
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
}
