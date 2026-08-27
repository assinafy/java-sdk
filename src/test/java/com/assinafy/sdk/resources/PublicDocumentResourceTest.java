package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PublicDocumentResourceTest {

    private MockApiHttpClient mock;
    private PublicDocumentResource resource;

    @BeforeEach
    void setUp() {
        mock = new MockApiHttpClient();
        resource = new PublicDocumentResource(mock);
    }

    @Test
    void getBasicInfoGetsPublicDocumentsPathAndReturnsTypedDetails() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"name\":\"x.pdf\",\"status\":\"metadata_ready\"}}");
        com.assinafy.sdk.models.Document d = resource.getBasicInfo("doc1");
        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/public/documents/doc1");
        assertThat(d.getId()).isEqualTo("doc1");
        assertThat(d.getStatus()).isEqualTo("metadata_ready");
    }

    @Test
    void getBasicInfoRequiresId() {
        assertThatThrownBy(() -> resource.getBasicInfo(""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void sendTokenPutsEmailBodyToSendTokenPath() {
        mock.enqueue(200, "{\"status\":200,\"message\":\"\"}");
        resource.sendToken("doc1", "user@example.com");
        assertThat(mock.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/public/documents/doc1/send-token");
        String body = mock.lastCaptured().getJsonBody();
        assertThat(body).isEqualTo("{\"email\":\"user@example.com\"}");
    }

    @Test
    void explicitChannelOverloadPreservesDeployedPayload() {
        mock.enqueue(200, "{\"status\":200,\"message\":\"\"}");
        resource.sendToken("doc1", "user@example.com", "email");
        assertThat(mock.lastCaptured().getJsonBody()).contains(
                "\"email\":\"user@example.com\"",
                "\"recipient\":\"user@example.com\"",
                "\"channel\":\"email\"");
    }

    @Test
    void sendTokenRequiresEmail() {
        assertThatThrownBy(() -> resource.sendToken("doc1", ""))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.sendToken("", "user@example.com"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void sendTokenSupportsDocumentConfiguredRecipientWithoutBody() {
        mock.enqueue(200, "{\"status\":200,\"message\":\"\"}");

        resource.sendToken("doc1");

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/public/documents/doc1/send-token");
        assertThat(mock.lastCaptured().getJsonBody()).isNull();
    }
}
