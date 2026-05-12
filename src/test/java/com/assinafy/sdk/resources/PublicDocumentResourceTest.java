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
    void getBasicInfoGetsPublicDocumentsPath() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc1\",\"name\":\"x.pdf\"}}");
        resource.getBasicInfo("doc1");
        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/public/documents/doc1");
    }

    @Test
    void getBasicInfoRequiresId() {
        assertThatThrownBy(() -> resource.getBasicInfo(""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void sendTokenPutsToSendTokenPath() {
        mock.enqueue(200, "{\"status\":200,\"data\":{}}");
        resource.sendToken("doc1", "user@example.com", "email");
        assertThat(mock.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/public/documents/doc1/send-token");
        String body = mock.lastCaptured().getJsonBody();
        assertThat(body).contains("\"recipient\":\"user@example.com\"");
        assertThat(body).contains("\"channel\":\"email\"");
    }

    @Test
    void sendTokenRequiresRecipientAndChannel() {
        assertThatThrownBy(() -> resource.sendToken("doc1", "", "email"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.sendToken("doc1", "user@example.com", ""))
                .isInstanceOf(ValidationException.class);
    }
}
