package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.DocumentListItem;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.request.CreateSignerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/** Coverage for the signer self-service, binary and access-code-encoding methods. */
class SignerResourceExtraTest {

    private MockApiHttpClient http;
    private SignerResource signers;

    @BeforeEach
    void setUp() {
        http = new MockApiHttpClient();
        signers = new SignerResource(http, "acc");
    }

    @Test
    void uploadSignatureEncodesAccessCodeAndType() {
        http.enqueue(200, "{\"status\":200,\"data\":[]}");
        signers.uploadSignature("a b", "signature", new byte[]{1, 2, 3});
        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST_SIGNATURE");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/signature?signer-access-code=a+b&type=signature");
    }

    @Test
    void downloadSignatureBuildsTypedPathWithAccessCode() {
        http.enqueue(200, "PNGBYTES");
        byte[] img = signers.downloadSignature("code1", "initial");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET_BINARY");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/signature/initial?signer-access-code=code1");
        assertThat(new String(img)).isEqualTo("PNGBYTES");
    }

    @Test
    void downloadSignatureThrowsOnNon2xx() {
        http.enqueue(404, "{\"status\":404,\"message\":\"not found\",\"data\":null}");
        assertThatThrownBy(() -> signers.downloadSignature("code1", "signature"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void downloadDocumentDefaultsToCertificatedAndAppendsAccessCode() {
        http.enqueue(200, "PDF");
        signers.downloadDocument("s1", "d1", null, "code1");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/signers/s1/documents/d1/download/certificated?signer-access-code=code1");
    }

    @Test
    void getCurrentDocumentBuildsPathAndReturnsMap() {
        http.enqueue(200, "{\"id\":\"d1\",\"status\":\"pending\"}");
        Map<String, Object> doc = signers.getCurrentDocument("s1", "code1");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/signers/s1/document?signer-access-code=code1");
        assertThat(doc).containsEntry("id", "d1");
    }

    @Test
    void listDocumentsMergesAccessCodeIntoQuery() {
        http.enqueue(200, "{\"status\":200,\"data\":[]}",
                Map.of("x-pagination-total-count", "0"));
        PaginatedResult<DocumentListItem> result = signers.listDocuments("s1", "code1");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/signers/s1/documents");
        assertThat(http.lastCaptured().getQueryParams()).containsEntry("signer-access-code", "code1");
        assertThat(result.getData()).isEmpty();
    }

    @Test
    void verifyEmailPostsHyphenatedBodyKeys() {
        http.enqueue(200, "{\"message\":\"Code verified successfully\"}");
        Map<String, Object> result = signers.verifyEmail("code1", "123456");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/verify");
        String body = http.lastCaptured().getJsonBody();
        assertThat(body).contains("\"signer-access-code\"").contains("\"verification-code\"");
        assertThat(result).containsEntry("message", "Code verified successfully");
    }

    @Test
    void createDigitStripsCpfInWireBody() {
        // WhatsApp-only signer (no email) skips the findByEmail dedupe round-trip.
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"s9\",\"full_name\":\"Maria\"}}");
        Signer s = signers.create(CreateSignerRequest.builder()
                .fullName("Maria")
                .whatsappPhoneNumber("+5548999990000")
                .cpf("400.676.228-36")
                .build());
        String body = http.lastCaptured().getJsonBody();
        assertThat(body).contains("\"cpf\":\"40067622836\"");
        assertThat(body).contains("\"full_name\":\"Maria\"");
        assertThat(s.getId()).isEqualTo("s9");
    }
}
