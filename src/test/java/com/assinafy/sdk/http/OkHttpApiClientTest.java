package com.assinafy.sdk.http;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.resources.WorkspaceResource;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
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
        server.shutdown();
    }

    private OkHttpApiClient withApiKey() {
        return new OkHttpApiClient(baseUrl, "secret-key", null, 5_000);
    }

    @Test
    void getBinaryReturnsBytesOnSuccess() throws Exception {
        byte[] pdf = "%PDF-1.4 hello".getBytes();
        server.enqueue(new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/pdf")
                .setBody(new okio.Buffer().write(pdf)));

        byte[] result = withApiKey().getBinary("/documents/abc/download/original");

        assertThat(result).isEqualTo(pdf);
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/v1/documents/abc/download/original");
        assertThat(req.getHeader("Accept")).isEqualTo("*/*");
    }

    @Test
    void getBinaryThrowsApiExceptionWithMessageOnNon2xx() {
        // The exact body the live API returns for a missing document (see /tmp/probe/bin_missing.bin).
        server.enqueue(new MockResponse().setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":404,\"data\":null,\"message\":\"Documento não encontrado.\"}"));

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
        server.enqueue(new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":404,\"message\":\"missing\",\"data\":null}"));

        assertThatThrownBy(() -> withApiKey().getBinary("/documents/missing/download/original"))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getStatusCode()).isEqualTo(404));
    }

    @Test
    void apiKeySendsXApiKeyHeaderAndNoAuthorization() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        withApiKey().get("/accounts");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getHeader("X-Api-Key")).isEqualTo("secret-key");
        assertThat(req.getHeader("Authorization")).isNull();
        assertThat(req.getHeader("Accept")).isEqualTo("application/json");
        assertThat(req.getHeader("User-Agent")).startsWith("assinafy-java-sdk/");
    }

    @Test
    void tokenSendsBearerAuthorizationAndNoApiKey() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        new OkHttpApiClient(baseUrl, null, "jwt-token", 5_000).get("/accounts");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getHeader("Authorization")).isEqualTo("Bearer jwt-token");
        assertThat(req.getHeader("X-Api-Key")).isNull();
    }

    @Test
    void queryParamsDropNullsAndPercentEncode() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        Map<String, Object> params = new java.util.LinkedHashMap<>();
        params.put("search", "a b&c");
        params.put("page", 2);
        params.put("skipme", null);
        withApiKey().get("/accounts/acc/documents", params);

        RecordedRequest req = server.takeRequest();
        String path = req.getPath();
        assertThat(path).contains("page=2");
        assertThat(path).contains("search=a%20b%26c");
        assertThat(path).doesNotContain("skipme");
    }

    @Test
    void multipartCarriesFileNameMetadataAndOmitsNullMetadata() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        withApiKey().postMultipart("/accounts/acc/documents", "c.pdf", "%PDF".getBytes(), "c.pdf", null);

        RecordedRequest req = server.takeRequest();
        assertThat(req.getHeader("Content-Type")).startsWith("multipart/form-data");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("name=\"file\"");
        assertThat(body).contains("name=\"name\"");
        assertThat(body).doesNotContain("name=\"metadata\"");

        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        withApiKey().postMultipart("/accounts/acc/documents", "c.pdf", "%PDF".getBytes(), "c.pdf", "{\"k\":\"v\"}");
        String body2 = server.takeRequest().getBody().readUtf8();
        assertThat(body2).contains("name=\"metadata\"");
    }

    @Test
    void postSignatureDetectsJpegVsPng() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        byte[] jpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        withApiKey().postSignature("/signature?signer-access-code=x&type=signature", jpeg);
        assertThat(server.takeRequest().getHeader("Content-Type")).isEqualTo("image/jpeg");

        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
        withApiKey().postSignature("/signature?signer-access-code=x&type=signature", png);
        assertThat(server.takeRequest().getHeader("Content-Type")).isEqualTo("image/png");
    }

    @Test
    void patchSendsPatchMethodAndJsonBody() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        withApiKey().patch("/documents/d1", "{\"name\":\"New.pdf\"}");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PATCH");
        assertThat(req.getPath()).isEqualTo("/v1/documents/d1");
        assertThat(req.getHeader("Content-Type")).startsWith("application/json");
        assertThat(req.getBody().readUtf8()).isEqualTo("{\"name\":\"New.pdf\"}");
    }

    @Test
    void deleteWithBodySendsDeleteMethodAndJsonBody() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        withApiKey().delete("/accounts/acc", "{\"force\":true}");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("DELETE");
        assertThat(req.getPath()).isEqualTo("/v1/accounts/acc");
        assertThat(req.getBody().readUtf8()).isEqualTo("{\"force\":true}");
    }

    @Test
    void postFileSendsMultipartWithGivenPartNameAndContentType() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        withApiKey().postFile("/accounts/acc/logo", "file", "logo.png", new byte[]{(byte) 0x89, 'P', 'N', 'G'}, "image/png");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getHeader("Content-Type")).startsWith("multipart/form-data");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("name=\"file\"");
        assertThat(body).contains("filename=\"logo.png\"");
        assertThat(body).contains("Content-Type: image/png");
    }

    @Test
    void trailingSlashInBaseUrlIsNormalised() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        new OkHttpApiClient(server.url("/v1/").toString(), "k", null, 5_000).get("/accounts");
        // No double slash between base and path.
        assertThat(server.takeRequest().getPath()).isEqualTo("/v1/accounts");
    }

    @Test
    void responseHeadersAreLowercasedForPaginationParsing() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200)
                .setHeader("X-Pagination-Total-Count", "42")
                .setBody("{}"));
        HttpRawResponse res = withApiKey().get("/accounts/acc/documents");
        // ResponseHandler.parsePaginationMeta looks these up in lowercase.
        assertThat(res.getHeaders()).containsKey("x-pagination-total-count");
        assertThat(res.getHeaders().get("x-pagination-total-count")).isEqualTo("42");
    }

    @Test
    void doesNotFollowRedirectsWithCredentials() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(302)
                .setHeader("Location", "https://example.com/collect"));

        HttpRawResponse response = withApiKey().get("/accounts");

        assertThat(response.getStatusCode()).isEqualTo(302);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void resourceIdsRemainOneEncodedPathSegmentOnTheWire() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("{\"status\":200,\"data\":{\"id\":\"a/b?c\"}}"));
        WorkspaceResource workspaces = new WorkspaceResource(withApiKey());

        workspaces.get("a/b?c");

        assertThat(server.takeRequest().getPath()).isEqualTo("/v1/accounts/a%2Fb%3Fc");
        assertThatThrownBy(() -> workspaces.get(".."))
                .isInstanceOf(ValidationException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void bodylessJsonVerbsSendZeroLengthBody() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        withApiKey().put("/signers/accept-terms", null);

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBodySize()).isZero();
        assertThat(request.getHeader("Content-Type")).startsWith("application/json");
    }

    @Test
    void binaryDownloadsRequestBinaryMediaAndExposeErrorHeaders() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("PDF"));
        withApiKey().getBinary("/documents/d1/download/original", "application/pdf");
        assertThat(server.takeRequest().getHeader("Accept"))
                .isEqualTo("application/pdf");

        server.enqueue(new MockResponse().setResponseCode(429)
                .setHeader("Retry-After", "12")
                .setBody("{\"message\":\"slow down\"}"));
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
        assertThatThrownBy(() -> new OkHttpApiClient("https://user:pass@example.com/v1", "k", null, 5_000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("credentials");
        assertThatThrownBy(() -> new OkHttpApiClient(baseUrl, "k", null, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
