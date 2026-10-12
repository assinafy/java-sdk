package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.TokenDelivery;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final String SEND_TOKEN_RESPONSE = """
            {"status":200,"message":"","data":{
              "document":{"resource":"document","id":"doc1","name":"contract.pdf",
                          "page_count":"2","created_by":"Owner"},
              "channel":"email","recipient":"signer@example.com"}}
            """;

    @Test
    void sendTokenPutsChannelAndRecipientBody() throws Exception {
        mock.enqueue(200, SEND_TOKEN_RESPONSE);

        var data = resource.sendToken("doc1", "signer@example.com");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/public/documents/doc1/send-token");
        assertThat(new ObjectMapper().readTree(mock.lastCaptured().getJsonBody()))
                .isEqualTo(new ObjectMapper().readTree(
                        "{\"channel\":\"email\",\"recipient\":\"signer@example.com\"}"));
        assertThat(data).containsEntry("channel", "email").containsEntry("recipient", "signer@example.com");
    }

    @Test
    void explicitChannelOverloadSendsOnlyTheDocumentedFields() throws Exception {
        mock.enqueue(200, SEND_TOKEN_RESPONSE);

        resource.sendToken("doc1", "signer@example.com", "email");

        assertThat(new ObjectMapper().readTree(mock.lastCaptured().getJsonBody()))
                .isEqualTo(new ObjectMapper().readTree(
                        "{\"channel\":\"email\",\"recipient\":\"signer@example.com\"}"));
    }

    @Test
    void sendTokenTypedDecodesDocumentSummary() {
        mock.enqueue(200, SEND_TOKEN_RESPONSE);

        TokenDelivery delivery = resource.sendTokenTyped("doc1", "signer@example.com");

        assertThat(delivery.channel()).isEqualTo("email");
        assertThat(delivery.recipient()).isEqualTo("signer@example.com");
        assertThat(delivery.document().id()).isEqualTo("doc1");
        assertThat(delivery.document().name()).isEqualTo("contract.pdf");
        assertThat(delivery.document().pageCount()).isEqualTo(2);
        assertThat(delivery.document().createdBy()).isEqualTo("Owner");
    }

    @Test
    void sendTokenRejectsInvalidInputBeforeSending() {
        assertThatThrownBy(() -> resource.sendToken("doc1", ""))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.sendToken("", "signer@example.com"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.sendToken("doc1", "+5548999990000", "whatsapp"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Channel must be email");
        assertThatThrownBy(() -> resource.sendToken("doc1", "signer@example.com", null))
                .isInstanceOf(ValidationException.class);
        assertThat(mock.capturedCount()).isZero();
    }
}
