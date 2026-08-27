package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.Document;
import com.assinafy.sdk.models.PaginatedResult;
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
    void searchDocumentsHitsSearchPathWithAccessCode() {
        http.enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"d1\"}]}");
        PaginatedResult<Document> res = signers.searchDocuments("s1", "code1", "invoice");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/signers/s1/documents/search");
        assertThat(http.lastCaptured().getQueryParams())
                .containsEntry("signer-access-code", "code1")
                .containsEntry("search", "invoice");
        assertThat(res.getData()).hasSize(1);
    }

    @Test
    void uploadSignatureOmitsTypeWhenBlankAndAppendsReuse() {
        http.enqueue(200, "{\"status\":200,\"data\":[]}");
        signers.uploadSignature("code1", null, new byte[]{(byte) 0x89, 'P', 'N', 'G'}, true);
        // type omitted (null); reuse appended.
        assertThat(http.lastCaptured().getPath()).isEqualTo("/signature?signer-access-code=code1&reuse=true");
    }

    @Test
    void confirmSignerDataReturnsSigner() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"s1\",\"full_name\":\"Normalised\"}}");
        Signer s = signers.confirmSignerData("d1", "code1", Map.of("full_name", "Normalised"));
        assertThat(http.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/documents/d1/signers/confirm-data?signer-access-code=code1");
        assertThat(s.getFullName()).isEqualTo("Normalised");
    }

    @Test
    void findOrCreateReturnsExistingSignerWhenApiRejectsDuplicateEmailWith400() {
        // Live API returns 400 (not 409) for a duplicate email. Pre-check (findByEmail via search)
        // misses here (empty), then the POST 400s; the SDK re-queries and returns the existing signer.
        http.enqueue(200, "{\"status\":200,\"data\":[]}");                       // pre-check findByEmail -> none
        http.enqueue(400, "{\"status\":400,\"message\":\"Um signatário com este e-mail já existe.\"}"); // POST create -> duplicate
        http.enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"existing\",\"full_name\":\"Dup\",\"email\":\"dup@example.invalid\"}]}"); // re-query finds it
        Signer s = signers.findOrCreate(
                CreateSignerRequest.builder().fullName("Dup").email("dup@example.invalid")
                        .cpf("400.676.228-36").build());
        assertThat(s.getId()).isEqualTo("existing");
        assertThat(http.capturedCount()).isEqualTo(3);
    }

    @Test
    void findOrCreateDoesNotHideUnrelatedValidationErrors() {
        http.enqueue(200, "{\"status\":200,\"data\":[]}");
        http.enqueue(400, "{\"status\":400,\"message\":\"Invalid signer data\"}");

        assertThatThrownBy(() -> signers.findOrCreate(
                CreateSignerRequest.builder().fullName("Dup").email("dup@example.invalid").build()))
                .isInstanceOf(ApiException.class);
        assertThat(http.capturedCount()).isEqualTo(2);
    }

    @Test
    void uploadSignatureEncodesAccessCodeAndType() {
        http.enqueue(200, "{\"status\":200,\"data\":[]}");
        signers.uploadSignature("a b", "signature", new byte[]{(byte) 0x89, 'P', 'N', 'G'});
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
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET_BINARY");
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
    void getCurrentDocumentTypedReturnsTypedDocument() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\"," +
                "\"status\":\"pending_signature\",\"assignment\":{\"id\":\"a1\"}}}");

        Document document = signers.getCurrentDocumentTyped("s1", "code 1");

        assertThat(document.getId()).isEqualTo("d1");
        assertThat(document.getStatus()).isEqualTo("pending_signature");
        assertThat(document.getAssignment().getId()).isEqualTo("a1");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/signers/s1/document?signer-access-code=code+1");
    }

    @Test
    void listDocumentsMergesAccessCodeIntoQuery() {
        http.enqueue(200, "{\"status\":200,\"data\":[]}",
                Map.of("x-pagination-total-count", "0"));
        PaginatedResult<Document> result = signers.listDocuments("s1", "code1");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/signers/s1/documents");
        assertThat(http.lastCaptured().getQueryParams()).containsEntry("signer-access-code", "code1");
        assertThat(result.getData()).isEmpty();
    }

    @Test
    void verifyEmailSendsAccessCodeAsQueryParamAndCodeInBody() {
        // Auth via signer-access-code query param; the body carries only verification-code.
        http.enqueue(200, "{\"message\":\"Code verified successfully\"}");
        Map<String, Object> result = signers.verifyEmail("code1", "123456");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/verify?signer-access-code=code1");
        String body = http.lastCaptured().getJsonBody();
        assertThat(body).contains("\"verification-code\"").contains("123456");
        assertThat(body).doesNotContain("signer-access-code");
        assertThat(result).containsEntry("message", "Code verified successfully");
    }

    @Test
    void createDigitStripsCpfInWireBody() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"s9\",\"full_name\":\"Maria\"}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"s9\",\"full_name\":\"Maria\"}}");
        Signer s = signers.create(CreateSignerRequest.builder()
                .fullName("Maria")
                .whatsappPhoneNumber("+5548999990000")
                .cpf("400.676.228-36")
                .build());
        assertThat(http.capturedAt(0).getJsonBody())
                .contains("\"full_name\":\"Maria\"")
                .doesNotContain("cpf", "government_id");
        assertThat(http.capturedAt(1).getJsonBody())
                .contains("\"government_id\":\"40067622836\"");
        assertThat(s.getId()).isEqualTo("s9");
    }

    @Test
    void createDeletesNewSignerWhenCpfUpdateFailsAndSuppressesCleanupFailure() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"s9\"}}")
                .enqueue(500, "{\"status\":500,\"message\":\"update failed\"}")
                .enqueue(500, "{\"status\":500,\"message\":\"delete failed\"}");

        assertThatThrownBy(() -> signers.create(CreateSignerRequest.builder()
                .fullName("Maria").cpf("400.676.228-36").build()))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(error.getSuppressed()).hasSize(1));
        assertThat(http.capturedAt(1).getMethod()).isEqualTo("PUT");
        assertThat(http.capturedAt(2).getMethod()).isEqualTo("DELETE");
        assertThat(http.capturedAt(2).getPath()).isEqualTo("/accounts/acc/signers/s9");
    }

    @Test
    void findOrCreateDoesNotReconcileDuplicateLookingCpfUpdateFailure() {
        http.enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"s9\"}}")
                .enqueue(400, "{\"status\":400,\"message\":\"government_id already exists\"}")
                .enqueue(200, "{\"status\":200,\"data\":[]}");

        assertThatThrownBy(() -> signers.findOrCreate(CreateSignerRequest.builder()
                .fullName("Maria").email("maria@example.invalid")
                .cpf("400.676.228-36").build()))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already exists");

        assertThat(http.capturedCount()).isEqualTo(4);
        assertThat(http.capturedAt(0).getMethod()).isEqualTo("GET");
        assertThat(http.capturedAt(1).getMethod()).isEqualTo("POST");
        assertThat(http.capturedAt(2).getMethod()).isEqualTo("PUT");
        assertThat(http.capturedAt(3).getMethod()).isEqualTo("DELETE");
    }

    @Test
    void uploadSignatureRejectsUnknownImageBytesBeforeSending() {
        assertThatThrownBy(() -> signers.uploadSignature("code1", "signature", new byte[]{1, 2, 3}))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }
}
