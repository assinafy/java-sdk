package com.assinafy.sdk.http;

import com.assinafy.sdk.exceptions.ApiException;
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
}
