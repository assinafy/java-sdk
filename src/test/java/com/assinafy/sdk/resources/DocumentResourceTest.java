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
        resource.replaceTags("doc-1", List.of("Contracts", "2026-Q1"));

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/documents/doc-1/tags");
        assertThat(mock.lastCaptured().getJsonBody()).contains("tags").contains("Contracts").contains("2026-Q1");
    }

    @Test
    void appendTagsPostsTagNames() {
        mock.enqueue(200, "{\"status\":200,\"data\":[]}");
        resource.appendTags("doc-1", List.of("Urgent"));

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/documents/doc-1/tags");
        assertThat(mock.lastCaptured().getJsonBody()).contains("Urgent");
    }

    @Test
    void detachTagDeletesSpecificTag() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"detached\":true}}");
        resource.detachTag("doc-1", "t1");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("DELETE");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/documents/doc-1/tags/t1");
    }
}
