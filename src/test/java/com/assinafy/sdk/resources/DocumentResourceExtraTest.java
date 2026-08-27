package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.CostEstimate;
import com.assinafy.sdk.models.Document;
import com.assinafy.sdk.models.DocumentVerification;
import com.assinafy.sdk.models.SigningProgress;
import com.assinafy.sdk.request.CreateDocumentFromTemplateRequest;
import com.assinafy.sdk.request.TemplateSigner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/** Coverage for the DocumentResource binary, verify, polling and derived-logic methods. */
class DocumentResourceExtraTest {

    private MockApiHttpClient http;
    private DocumentResource documents;

    @BeforeEach
    void setUp() {
        http = new MockApiHttpClient();
        documents = new DocumentResource(http, "acc");
    }

    @Test
    void renameSendsPatchWithNameBody() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"name\":\"New.pdf\"}}");
        Document d = documents.rename("d1", "New.pdf");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("PATCH");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/documents/d1");
        assertThat(http.lastCaptured().getJsonBody()).isEqualTo("{\"name\":\"New.pdf\"}");
        assertThat(d.getName()).isEqualTo("New.pdf");
    }

    @Test
    void renameRequiresName() {
        assertThatThrownBy(() -> documents.rename("d1", ""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void searchHitsLightweightSearchPath() {
        http.enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"d1\",\"name\":\"x.pdf\"}]}");
        var result = documents.search(com.assinafy.sdk.request.ListParams.builder().search("contract").build());
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/acc/documents/search");
        assertThat(http.lastCaptured().getQueryParams()).containsEntry("search", "contract");
        assertThat(result.getData()).hasSize(1);
    }

    @Test
    void downloadUrlEncodesArtifactName() {
        http.enqueue(200, "PDFBYTES");
        documents.download("d1", "certificate-page");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/documents/d1/download/certificate-page");
        assertThat(http.lastCaptured().getAccept()).isEqualTo("*/*");
    }

    @Test
    void thumbnailHitsThumbnailPath() {
        http.enqueue(200, "JPEGBYTES");
        byte[] bytes = documents.thumbnail("d1");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET_BINARY");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/documents/d1/thumbnail");
        assertThat(new String(bytes)).isEqualTo("JPEGBYTES");
    }

    @Test
    void downloadPageHitsPagePathAndValidatesIds() {
        http.enqueue(200, "PAGE");
        documents.downloadPage("d1", "p1");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET_BINARY");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/documents/d1/pages/p1/download");
        assertThatThrownBy(() -> documents.downloadPage("d1", " "))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void downloadThrowsApiExceptionOnNon2xxInsteadOfReturningErrorBytes() {
        http.enqueue(404, "{\"status\":404,\"message\":\"Artefato não está disponível.\",\"data\":null}");
        assertThatThrownBy(() -> documents.download("d1"))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatusCode()).isEqualTo(404));
        assertThat(http.lastCaptured().getPath()).isEqualTo("/documents/d1/download/certificated");
    }

    @Test
    void downloadRejectsHttp200ErrorEnvelope() {
        http.enqueue(200, "{\"status\":404,\"message\":\"missing\"}",
                Map.of("Content-Type", "application/json"));

        assertThatThrownBy(() -> documents.download("d1", "original"))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getStatusCode()).isEqualTo(404));
    }

    @Test
    void verifyValidDocumentReturnsValidFlag() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"hash\":\"H\",\"is_valid\":true,\"status\":\"certificated\"}}");
        Map<String, Object> result = documents.verify("H");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/documents/H/verify");
        assertThat(result).containsEntry("is_valid", true);
    }

    @Test
    void verifyInvalidDocumentReturnsFalseAndNulls() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"hash\":\"X\",\"id\":null,\"is_valid\":false," +
                "\"message\":\"Document not signed or not found.\"}}");
        Map<String, Object> result = documents.verify("X");
        assertThat(result).containsEntry("is_valid", false);
        assertThat(result.get("id")).isNull();
    }

    @Test
    void verifyTypedReturnsCompleteVerificationModel() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"hash\":\"H\",\"id\":\"d1\"," +
                "\"status\":\"certificated\",\"page_count\":\"2\",\"signer_count\":\"3\"," +
                "\"completed_count\":3,\"completed_at\":\"2026-08-20T12:00:00Z\"," +
                "\"verified_at\":\"2026-08-20T12:01:00Z\",\"is_valid\":true,\"message\":\"\"}}");

        DocumentVerification result = documents.verifyTyped("H");

        assertThat(result.getHash()).isEqualTo("H");
        assertThat(result.getId()).isEqualTo("d1");
        assertThat(result.getStatus()).isEqualTo("certificated");
        assertThat(result.getPageCount()).isEqualTo("2");
        assertThat(result.getSignerCount()).isEqualTo("3");
        assertThat(result.getCompletedCount()).isEqualTo(3);
        assertThat(result.getCompletedAt()).isEqualTo("2026-08-20T12:00:00Z");
        assertThat(result.getVerifiedAt()).isEqualTo("2026-08-20T12:01:00Z");
        assertThat(result.getIsValid()).isTrue();
        assertThat(result.getMessage()).isEmpty();
        assertThat(http.lastCaptured().getPath()).isEqualTo("/documents/H/verify");
    }

    @Test
    void waitUntilReadyReturnsWhenStatusReady() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"status\":\"certificated\"}}");
        Document details = documents.waitUntilReady("d1", 5_000, 10);
        assertThat(details.getStatus()).isEqualTo("certificated");
    }

    @Test
    void waitUntilReadyPollsImmediatelyForTinyPositiveBudget() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"status\":\"certificated\"}}");

        Document details = documents.waitUntilReady("d1", 1, 1_000);

        assertThat(details.getStatus()).isEqualTo("certificated");
        assertThat(http.capturedCount()).isEqualTo(1);
    }

    @Test
    void waitUntilReadyRetriesNotFoundAndServerErrors() {
        http.enqueue(404, "{\"status\":404,\"message\":\"not ready\"}");
        http.enqueue(503, "{\"status\":503,\"message\":\"try again\"}");
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"status\":\"metadata_ready\"}}");

        Document details = documents.waitUntilReady("d1", 1_000, 1);

        assertThat(details.getStatus()).isEqualTo("metadata_ready");
        assertThat(http.capturedCount()).isEqualTo(3);
    }

    @Test
    void waitUntilReadyDoesNotRetryFatalClientErrors() {
        http.enqueue(400, "{\"status\":400,\"message\":\"invalid document\"}");

        assertThatThrownBy(() -> documents.waitUntilReady("d1", 1_000, 1))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getStatusCode()).isEqualTo(400));
        assertThat(http.capturedCount()).isEqualTo(1);
    }

    @Test
    void waitUntilReadyRestoresInterruptStatus() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"status\":\"uploading\"}}");
        Thread.currentThread().interrupt();
        try {
            assertThatThrownBy(() -> documents.waitUntilReady("d1", 1_000, 1_000))
                    .isInstanceOf(NetworkException.class)
                    .hasMessageContaining("Interrupted");
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void waitUntilReadyCapsSleepAtRemainingBudget() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"status\":\"uploading\"}}");
        long start = System.nanoTime();

        assertThatThrownBy(() -> documents.waitUntilReady("d1", 20, 500))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Timeout");

        long elapsedMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(elapsedMs).isLessThan(400);
    }

    @Test
    void waitUntilReadyThrowsOnFailedStatus() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"status\":\"failed\"}}");
        assertThatThrownBy(() -> documents.waitUntilReady("d1", 5_000, 10))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(e.getMessage()).contains("failed"));
    }

    @Test
    void waitUntilReadyTimesOut() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"status\":\"uploading\"}}");
        assertThatThrownBy(() -> documents.waitUntilReady("d1", 40, 15))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(e.getMessage()).contains("Timeout"));
    }

    @Test
    void isFullySignedTrueWhenCertificated() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"status\":\"certificated\"}}");
        assertThat(documents.isFullySigned("d1")).isTrue();
    }

    @Test
    void isFullySignedReflectsSummaryCounts() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"status\":\"pending_signature\"," +
                "\"assignment\":{\"summary\":{\"signer_count\":2,\"completed_count\":2}}}}");
        assertThat(documents.isFullySigned("d1")).isTrue();
    }

    @Test
    void getSigningProgressComputesPercentage() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"status\":\"pending_signature\"," +
                "\"assignment\":{\"summary\":{\"signer_count\":2,\"completed_count\":1}}}}");
        SigningProgress p = documents.getSigningProgress("d1");
        assertThat(p.getSigned()).isEqualTo(1);
        assertThat(p.getTotal()).isEqualTo(2);
        assertThat(p.getPending()).isEqualTo(1);
        assertThat(p.getPercentage()).isEqualTo(50.0);
    }

    @Test
    void getSigningProgressHandlesZeroSignersWithoutDivideByZero() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"status\":\"metadata_ready\"}}");
        SigningProgress p = documents.getSigningProgress("d1");
        assertThat(p.getTotal()).isZero();
        assertThat(p.getPercentage()).isEqualTo(0.0);
    }

    @Test
    void createFromTemplatePostsToTemplateDocumentsPath() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"template_id\":\"tmpl\"}}");
        Document doc = documents.createFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().name("c.pdf")
                        .signers(List.of(TemplateSigner.builder().roleId("r1").id("s1").build()))
                        .build());
        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/acc/templates/tmpl/documents");
        assertThat(doc.getId()).isEqualTo("doc1");
    }

    @Test
    void estimateCostFromTemplatePostsToEstimateCostPath() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"total_credits\":0}}");
        Map<String, Object> cost = documents.estimateCostFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder()
                        .signers(List.of(TemplateSigner.builder().roleId("r1").id("ignored").step(2).build()))
                        .name("ignored.pdf")
                        .build());
        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/accounts/acc/templates/tmpl/documents/estimate-cost");
        assertThat(cost).containsKey("total_credits");
        assertThat(http.lastCaptured().getJsonBody()).contains("\"role_id\":\"r1\"")
                .doesNotContain("\"id\"").doesNotContain("step").doesNotContain("name");
    }

    @Test
    void estimateCostFromTemplateTypedReturnsCostEstimate() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"documents\":1," +
                "\"total_credits\":0.45,\"has_sufficient_resources\":true}}");

        CostEstimate cost = documents.estimateCostFromTemplateTyped("tmpl",
                CreateDocumentFromTemplateRequest.builder()
                        .signers(List.of(TemplateSigner.builder().roleId("r1").build()))
                        .build(), "other-account");

        assertThat(cost.getDocuments()).isEqualTo(1);
        assertThat(cost.getTotalCredits()).isEqualByComparingTo("0.45");
        assertThat(cost.getHasSufficientResources()).isTrue();
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/accounts/other-account/templates/tmpl/documents/estimate-cost");
    }

    @Test
    void templateOperationsRejectMissingRequiredSignersBeforeSending() {
        assertThatThrownBy(() -> documents.createFromTemplate("tmpl", null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> documents.estimateCostFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().build()))
                .isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void templateOperationsRejectUnsupportedDeliveryMethods() {
        assertThatThrownBy(() -> documents.createFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().signers(List.of(
                        TemplateSigner.builder().roleId("r1").id("s1")
                                .verificationMethod("Sms").build())).build()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> documents.createFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().signers(List.of(
                        TemplateSigner.builder().roleId("r1").id("s1")
                                .notificationMethods(List.of("Sms")).build())).build()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> documents.createFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().signers(List.of(
                        TemplateSigner.builder().roleId("r1").id("s1")
                                .notificationMethods(java.util.Arrays.asList((String) null)).build())).build()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> documents.createFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().signers(List.of(
                        TemplateSigner.builder().roleId("r1").id("s1")
                                .notificationMethods(List.of("Email", "Whatsapp")).build())).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("only one");
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void templateEstimatePassesEmptyNotificationMethods() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"total_credits\":0}}");

        documents.estimateCostFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().signers(List.of(
                        TemplateSigner.builder().roleId("r1")
                                .notificationMethods(List.of()).build())).build());

        assertThat(http.lastCaptured().getJsonBody()).contains("\"notification_methods\":[]");
    }

    @Test
    void templateEstimateAllowsMultipleDocumentedNotificationMethods() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"total_credits\":0.45}}");

        documents.estimateCostFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().signers(List.of(
                        TemplateSigner.builder().roleId("r1")
                                .notificationMethods(List.of("Email", "Whatsapp")).build())).build());

        assertThat(http.lastCaptured().getJsonBody())
                .contains("\"notification_methods\":[\"Email\",\"Whatsapp\"]");
    }

    @Test
    void templateCreationValidatesSequentialSignerSteps() {
        assertThatThrownBy(() -> documents.createFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().signers(List.of(
                        TemplateSigner.builder().roleId("r1").id("s1").step(1).build(),
                        TemplateSigner.builder().roleId("r2").id("s2").build())).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Every template signer");
        assertThatThrownBy(() -> documents.createFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().signers(List.of(
                        TemplateSigner.builder().roleId("r1").id("s1").step(1).build(),
                        TemplateSigner.builder().roleId("r2").id("s2").step(3).build())).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("contiguous");
        assertThatThrownBy(() -> documents.createFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().signers(List.of(
                        TemplateSigner.builder().roleId("r1").id("s1").step(1)
                                .verificationMethod("DigitalCertificate").build(),
                        TemplateSigner.builder().roleId("r2").id("s2").step(1).build())).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("alone");
        assertThatThrownBy(() -> documents.createFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().signers(List.of(
                        TemplateSigner.builder().roleId("r1").id("s1").step(0).build())).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("positive");
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void templateCreationAllowsOnlyOneNotificationMethodPerSigner() {
        assertThatThrownBy(() -> documents.createFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().signers(List.of(
                        TemplateSigner.builder().roleId("r1").id("s1")
                                .notificationMethods(List.of("Email", "Whatsapp")).build())).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("only one notification method");
    }

    @Test
    void templateCreationAllowsDigitalCertificateInAnIsolatedStep() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\"}}");

        Document document = documents.createFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().signers(List.of(
                        TemplateSigner.builder().roleId("r1").id("s1").step(1).build(),
                        TemplateSigner.builder().roleId("r2").id("s2").step(2)
                                .verificationMethod("DigitalCertificate").build())).build());

        assertThat(document.getId()).isEqualTo("doc1");
    }
}
