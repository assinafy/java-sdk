package com.assinafy.sdk.http;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.resources.WorkspaceResource;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Wire-level tests for the real {@link OkHttpApiClient}, driven by an in-process
 * {@link MockWebServer}. These cover behaviour the hand-rolled mock cannot: auth header
 * selection, multipart shape, image content-type detection, header lowercasing and — most
 * importantly — that a non-2xx binary download throws instead of returning the error body.
 */
class OkHttpApiClientTest {

    private MockWebServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        baseUrl = server.url("/v1").toString(); // e.g. http://localhost:PORT/v1
    }

    @AfterEach
    void tearDown() throws Exception {
        server.close();
    }

    private static MockResponse.Builder response(int status) {
        return new MockResponse.Builder().code(status);
    }

    private OkHttpApiClient withApiKey() {
        return new OkHttpApiClient(baseUrl, "secret-key", null, 5_000);
    }

    @Test
    void getBinaryReturnsBytesOnSuccess() throws Exception {
        String body = "%PDF-1.4 hello";
        byte[] pdf = body.getBytes();
        server.enqueue(response(200)
                .addHeader("Content-Type", "application/pdf")
                .body(body)
                .build());

        byte[] result = withApiKey().getBinary("/documents/abc/download/original");

        assertThat(result).isEqualTo(pdf);
        RecordedRequest req = server.takeRequest();
        assertThat(req.getTarget()).isEqualTo("/v1/documents/abc/download/original");
        assertThat(req.getHeaders().get("Accept")).isEqualTo("*/*");
    }

    @Test
    void getBinaryThrowsApiExceptionWithMessageOnNon2xx() {
        server.enqueue(response(404)
                .addHeader("Content-Type", "application/json")
                .body("{\"status\":404,\"data\":null,\"message\":\"Documento não encontrado.\"}")
                .build());

        assertThatThrownBy(() -> withApiKey().getBinary("/documents/missing/download/original"))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> {
                    ApiException api = (ApiException) e;
                    assertThat(api.getStatusCode()).isEqualTo(404);
                    assertThat(api.getMessage()).isEqualTo("Documento não encontrado.");
                });
    }

    @Test
    void getBinaryRejectsErrorEnvelopeReturnedWithHttp200() {
        server.enqueue(response(200)
                .addHeader("Content-Type", "application/json")
                .body("{\"status\":404,\"message\":\"missing\",\"data\":null}")
                .build());

        assertThatThrownBy(() -> withApiKey().getBinary("/documents/missing/download/original"))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getStatusCode()).isEqualTo(404));
    }

    @Test
    void getBinaryRejectsErrorEnvelopeWithMissingOrIncorrectContentType() {
        server.enqueue(response(200)
                .addHeader("Content-Type", "application/octet-stream")
                .body("{\"status\":404,\"message\":\"missing\",\"data\":null}")
                .build());

        assertThatThrownBy(() -> withApiKey().getBinary("/documents/missing/download/original"))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getStatusCode()).isEqualTo(404));
    }

    @Test
    void apiKeySendsXApiKeyHeaderAndNoAuthorization() throws Exception {
        server.enqueue(response(200).body("{}").build());
        withApiKey().get("/accounts");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getHeaders().get("X-Api-Key")).isEqualTo("secret-key");
        assertThat(req.getHeaders().get("Authorization")).isNull();
        assertThat(req.getHeaders().get("Accept")).isEqualTo("application/json");
        assertThat(req.getHeaders().get("User-Agent")).startsWith("assinafy-java-sdk/");
    }

    @Test
    void tokenSendsBearerAuthorizationAndNoApiKey() throws Exception {
        server.enqueue(response(200).body("{}").build());
        new OkHttpApiClient(baseUrl, null, "jwt-token", 5_000).get("/accounts");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getHeaders().get("Authorization")).isEqualTo("Bearer jwt-token");
        assertThat(req.getHeaders().get("X-Api-Key")).isNull();
    }

    @Test
    void queryParamsDropNullsAndPercentEncode() throws Exception {
        server.enqueue(response(200).body("{}").build());
        Map<String, Object> params = new java.util.LinkedHashMap<>();
        params.put("search", "a b&c");
        params.put("page", 2);
        params.put("skipme", null);
        withApiKey().get("/accounts/acc/documents", params);

        RecordedRequest req = server.takeRequest();
        String path = req.getTarget();
        assertThat(path).contains("page=2");
        assertThat(path).contains("search=a%20b%26c");
        assertThat(path).doesNotContain("skipme");
    }

    @Test
    void multipartCarriesFileNameMetadataAndOmitsNullMetadata() throws Exception {
        server.enqueue(response(200).body("{}").build());
        withApiKey().postMultipart("/accounts/acc/documents", "c.pdf", "%PDF".getBytes(), "c.pdf", null);

        RecordedRequest req = server.takeRequest();
        assertThat(req.getHeaders().get("Content-Type")).startsWith("multipart/form-data");
        String body = req.getBody().utf8();
        assertThat(body).contains("name=\"file\"");
        assertThat(body).contains("name=\"name\"");
        assertThat(body).doesNotContain("name=\"metadata\"");

        server.enqueue(response(200).body("{}").build());
        withApiKey().postMultipart("/accounts/acc/documents", "c.pdf", "%PDF".getBytes(), "c.pdf", "{\"k\":\"v\"}");
        String body2 = server.takeRequest().getBody().utf8();
        assertThat(body2).contains("name=\"metadata\"");
    }

    @Test
    void postSignatureDetectsJpegVsPng() throws Exception {
        server.enqueue(response(200).body("{}").build());
        byte[] jpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        withApiKey().postSignature("/signature?signer-access-code=x&type=signature", jpeg);
        assertThat(server.takeRequest().getHeaders().get("Content-Type")).isEqualTo("image/jpeg");

        server.enqueue(response(200).body("{}").build());
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
        withApiKey().postSignature("/signature?signer-access-code=x&type=signature", png);
        assertThat(server.takeRequest().getHeaders().get("Content-Type")).isEqualTo("image/png");
    }

    @Test
    void patchSendsPatchMethodAndJsonBody() throws Exception {
        server.enqueue(response(200).body("{}").build());
        withApiKey().patch("/documents/d1", "{\"name\":\"New.pdf\"}");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PATCH");
        assertThat(req.getTarget()).isEqualTo("/v1/documents/d1");
        assertThat(req.getHeaders().get("Content-Type")).startsWith("application/json");
        assertThat(req.getBody().utf8()).isEqualTo("{\"name\":\"New.pdf\"}");
    }

    @Test
    void deleteWithBodySendsDeleteMethodAndJsonBody() throws Exception {
        server.enqueue(response(200).body("{}").build());
        withApiKey().delete("/accounts/acc", "{\"force\":true}");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("DELETE");
        assertThat(req.getTarget()).isEqualTo("/v1/accounts/acc");
        assertThat(req.getBody().utf8()).isEqualTo("{\"force\":true}");
    }

    @Test
    void postFileSendsMultipartWithGivenPartNameAndContentType() throws Exception {
        server.enqueue(response(200).body("{}").build());
        withApiKey().postFile("/accounts/acc/logo", "file", "logo.png", new byte[]{(byte) 0x89, 'P', 'N', 'G'}, "image/png");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getHeaders().get("Content-Type")).startsWith("multipart/form-data");
        String body = req.getBody().utf8();
        assertThat(body).contains("name=\"file\"");
        assertThat(body).contains("filename=\"logo.png\"");
        assertThat(body).contains("Content-Type: image/png");
    }

    @Test
    void trailingSlashInBaseUrlIsNormalised() throws Exception {
        server.enqueue(response(200).body("{}").build());
        new OkHttpApiClient(server.url("/v1/").toString(), "k", null, 5_000).get("/accounts");
        // No double slash between base and path.
        assertThat(server.takeRequest().getTarget()).isEqualTo("/v1/accounts");
    }

    @Test
    void responseHeadersAreLowercasedForPaginationParsing() throws Exception {
        server.enqueue(response(200)
                .addHeader("X-Pagination-Total-Count", "42")
                .body("{}")
                .build());
        HttpRawResponse res = withApiKey().get("/accounts/acc/documents");
        // ResponseHandler.parsePaginationMeta looks these up in lowercase.
        assertThat(res.getHeaders()).containsKey("x-pagination-total-count");
        assertThat(res.getHeaders().get("x-pagination-total-count")).isEqualTo("42");
    }

    @Test
    void doesNotFollowRedirectsWithCredentials() throws Exception {
        server.enqueue(response(302)
                .addHeader("Location", "https://example.com/collect")
                .build());

        HttpRawResponse response = withApiKey().get("/accounts");

        assertThat(response.getStatusCode()).isEqualTo(302);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void resourceIdsRemainOneEncodedPathSegmentOnTheWire() throws Exception {
        server.enqueue(response(200)
                .body("{\"status\":200,\"data\":{\"id\":\"a/b?c\"}}")
                .build());
        WorkspaceResource workspaces = new WorkspaceResource(withApiKey());

        workspaces.get("a/b?c");

        assertThat(server.takeRequest().getTarget()).isEqualTo("/v1/accounts/a%2Fb%3Fc");
        assertThatThrownBy(() -> workspaces.get(".."))
                .isInstanceOf(ValidationException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void bodylessJsonVerbsSendZeroLengthBody() throws Exception {
        server.enqueue(response(200).body("{}").build());

        withApiKey().put("/signers/accept-terms", null);

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBodySize()).isZero();
        assertThat(request.getHeaders().get("Content-Type")).startsWith("application/json");
    }

    @Test
    void binaryDownloadsRequestBinaryMediaAndExposeErrorHeaders() throws Exception {
        server.enqueue(response(200).body("PDF").build());
        withApiKey().getBinary("/documents/d1/download/original", "application/pdf");
        assertThat(server.takeRequest().getHeaders().get("Accept"))
                .isEqualTo("application/pdf");

        server.enqueue(response(429)
                .addHeader("Retry-After", "12")
                .body("{\"message\":\"slow down\"}")
                .build());
        assertThatThrownBy(() -> withApiKey().getBinary("/documents/d1/download/original"))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getResponseHeader("Retry-After"))
                        .isEqualTo("12"));
    }

    @Test
    void rejectsInvalidTransportConfiguration() {
        assertThatThrownBy(() -> new OkHttpApiClient("not-a-url", "k", null, 5_000))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OkHttpApiClient("http://example.com/v1", "k", null, 5_000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTPS");
        assertThatThrownBy(() -> new OkHttpApiClient("http://127.example.com/v1", "k", null, 5_000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTPS");
        assertThatThrownBy(() -> new OkHttpApiClient("https://user:pass@example.com/v1", "k", null, 5_000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("credentials");
        assertThatThrownBy(() -> new OkHttpApiClient(baseUrl, "k", null, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
