package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.DocumentActivity;
import com.assinafy.sdk.models.DocumentUploadResponse;
import com.assinafy.sdk.models.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DocumentResourceTest {

    private MockApiHttpClient mock;
    private DocumentResource resource;

    @BeforeEach
    void setUp() {
        mock = new MockApiHttpClient();
        resource = new DocumentResource(mock, "acc");
    }

    @Test
    void uploadPostsMultipartToAccountDocuments() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc-1\",\"status\":\"uploaded\"}}");
        DocumentUploadResponse doc = resource.upload("%PDF-1.4 data".getBytes(), "contract.pdf");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("POST_MULTIPART");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/documents");
        assertThat(doc.getId()).isEqualTo("doc-1");
    }

    @Test
    void uploadRejectsNonPdf() {
        assertThatThrownBy(() -> resource.upload("data".getBytes(), "image.png"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void uploadRejectsEmptyData() {
        assertThatThrownBy(() -> resource.upload(new byte[0], "contract.pdf"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void uploadIgnoresDiagnosticLoggerFailures() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc-1\",\"status\":\"uploaded\"}}");

        DocumentUploadResponse document = new DocumentResource(mock, "acc", throwingLogger())
                .upload("%PDF-1.4 data".getBytes(), "contract.pdf");

        assertThat(document.getId()).isEqualTo("doc-1");
    }

    @Test
    void readinessPollingIgnoresDiagnosticLoggerFailures() {
        mock.enqueue(404, "{\"status\":404,\"message\":\"not ready\"}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc-1\","
                        + "\"status\":\"metadata_ready\"}}");

        assertThat(new DocumentResource(mock, "acc", throwingLogger())
                .waitUntilReady("doc-1", 1_000, 1).getId()).isEqualTo("doc-1");
    }

    @Test
    void listDetailsDeleteAndStatusesUseDocumentedWires() {
        mock.enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"doc-1\"}]}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc-1\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueue(200, "{\"status\":200,\"data\":[{\"code\":\"uploaded\",\"deletable\":true}]}");

        resource.list();
        resource.details("doc-1");
        resource.delete("doc-1");
        resource.getStatuses();

        assertThat(mock.capturedAt(0).getMethod()).isEqualTo("GET");
        assertThat(mock.capturedAt(0).getPath()).isEqualTo("/accounts/acc/documents");
        assertThat(mock.capturedAt(1).getMethod()).isEqualTo("GET");
        assertThat(mock.capturedAt(1).getPath()).isEqualTo("/documents/doc-1");
        assertThat(mock.capturedAt(2).getMethod()).isEqualTo("DELETE");
        assertThat(mock.capturedAt(2).getPath()).isEqualTo("/documents/doc-1");
        assertThat(mock.capturedAt(3).getMethod()).isEqualTo("GET");
        assertThat(mock.capturedAt(3).getPath()).isEqualTo("/documents/statuses");
    }

    @Test
    void downloadDefaultsToCertificatedArtifact() {
        mock.enqueue(200, "PDFBYTES");
        resource.download("doc-1");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET_BINARY");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/documents/doc-1/download/certificated");
    }

    @Test
    void activitiesParseObjectOriginWithoutFailing() {
        // Regression: origin is an object {ip, user-agent}, not a String.
        mock.enqueue(200, "{\"status\":200,\"data\":[{\"id\":1,\"event\":\"document_ready\","
                + "\"message\":\"ok\",\"payload\":[],"
                + "\"origin\":{\"ip\":\"1.2.3.4\",\"user-agent\":\"sdk/1.0\"},"
                + "\"created_at\":\"2026-01-01T00:00:00Z\"}]}");

        List<DocumentActivity> activities = resource.activities("doc-1");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/documents/doc-1/activities");
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getEvent()).isEqualTo("document_ready");
        assertThat(activities.get(0).getOrigin()).isNotNull();
        assertThat(activities.get(0).getOrigin().getIp()).isEqualTo("1.2.3.4");
        assertThat(activities.get(0).getOrigin().getUserAgent()).isEqualTo("sdk/1.0");
    }

    @Test
    void listTagsGetsDocumentTags() {
        mock.enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"t1\",\"name\":\"Contracts\",\"color\":\"FF0000\"}]}");
        List<Tag> tags = resource.listTags("doc-1");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/documents/doc-1/tags");
        assertThat(tags).hasSize(1);
        assertThat(tags.get(0).getName()).isEqualTo("Contracts");
    }

    @Test
    void replaceTagsPutsTagNames() {
        mock.enqueue(200, "{\"status\":200,\"data\":[]}");
        resource.replaceTags("doc-1", List.of("Contracts", "Urgent"));

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/documents/doc-1/tags");
        assertThat(mock.lastCaptured().getJsonBody()).contains("tags").contains("Contracts").contains("Urgent");
    }

    @Test
    void appendTagsPreservesExistingNamedTag() {
        mock.enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"tag-1\","
                + "\"name\":\"Contracts\"}]}");
        List<Tag> tags = resource.appendTags("doc-1", List.of("Contracts"));

        assertThat(tags).extracting(Tag::getId).containsExactly("tag-1");
        assertThat(mock.capturedCount()).isOne();
        assertThat(mock.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/documents/doc-1/tags");
        assertThat(mock.lastCaptured().getJsonBody()).contains("Contracts");
    }

    @Test
    void appendTagIdsResolvesBeforeChangingDocument() {
        mock.enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"tag-1\","
                        + "\"name\":\"Contracts\"}],\"meta\":{\"current_page\":1,"
                        + "\"last_page\":1,\"per_page\":100,\"total\":1}}")
                .enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"attached\","
                        + "\"name\":\"Contracts\"}]}");

        List<Tag> tags = resource.appendTagIds("doc-1", List.of("tag-1"));

        assertThat(tags).extracting(Tag::getName).containsExactly("Contracts");
        assertThat(mock.capturedAt(0).getMethod()).isEqualTo("GET");
        assertThat(mock.capturedAt(0).getPath()).isEqualTo("/accounts/acc/tags");
        assertThat(mock.capturedAt(1).getMethod()).isEqualTo("POST");
        assertThat(mock.capturedAt(1).getJsonBody()).contains("Contracts").doesNotContain("tag-1");
        assertThat(mock.getCaptured()).noneMatch(request -> "DELETE".equals(request.getMethod()));
    }

    @Test
    void replaceTagIdsRejectsUnknownIdBeforeChangingDocument() {
        mock.enqueue(200, "{\"status\":200,\"data\":[]}");

        assertThatThrownBy(() -> resource.replaceTagIds("doc-1", List.of("missing")))
                .isInstanceOf(com.assinafy.sdk.exceptions.AssinafyException.class);

        assertThat(mock.capturedCount()).isOne();
        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
    }

    @Test
    void detachTagDeletesSpecificTag() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"detached\":true}}");
        resource.detachTag("doc-1", "t1");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("DELETE");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/documents/doc-1/tags/t1");
    }

    private static com.assinafy.sdk.Logger throwingLogger() {
        return new com.assinafy.sdk.Logger() {
            @Override public void debug(String message, java.util.Map<String, Object> context) {
                throw new IllegalStateException("logger failed");
            }
            @Override public void info(String message, java.util.Map<String, Object> context) {
                throw new IllegalStateException("logger failed");
            }
            @Override public void warn(String message, java.util.Map<String, Object> context) {
                throw new IllegalStateException("logger failed");
            }
            @Override public void error(String message, java.util.Map<String, Object> context) {
                throw new IllegalStateException("logger failed");
            }
        };
    }
}
