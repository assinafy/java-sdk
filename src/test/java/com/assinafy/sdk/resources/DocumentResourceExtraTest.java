package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.SigningProgress;
import com.assinafy.sdk.request.CreateDocumentFromTemplateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void verifyValidDocumentReturnsValidFlag() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"hash\":\"H\",\"is_valid\":true,\"status\":\"certificated\"}}");
        Map<String, Object> result = documents.verify("H");
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
    void waitUntilReadyReturnsWhenStatusReady() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"status\":\"certificated\"}}");
        DocumentDetails details = documents.waitUntilReady("d1", 5_000, 10);
        assertThat(details.getStatus()).isEqualTo("certificated");
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
        DocumentDetails doc = documents.createFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().name("c.pdf").build());
        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/acc/templates/tmpl/documents");
        assertThat(doc.getId()).isEqualTo("doc1");
    }

    @Test
    void estimateCostFromTemplatePostsToEstimateCostPath() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"total_credits\":0}}");
        Map<String, Object> cost = documents.estimateCostFromTemplate("tmpl",
                CreateDocumentFromTemplateRequest.builder().build());
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/accounts/acc/templates/tmpl/documents/estimate-cost");
        assertThat(cost).containsKey("total_credits");
    }
}
