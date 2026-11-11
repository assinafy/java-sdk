package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class EmbedResourceTest {
    @Test
    void exercisesOriginAndSessionWireContracts() {
        var http = new MockApiHttpClient().enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"origin\",\"origin\":\"https://example.invalid\"}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"origin\",\"origin\":\"http://localhost:3000\"}}")
                .enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueue(200, "{\"status\":200,\"data\":{\"url\":\"https://example.invalid/embed/token\",\"expires_at\":\"2026-11-11T12:05:00Z\"}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"signer_access_code\":\"private\",\"signer_id\":\"signer\",\"document_id\":\"doc\",\"verification_method\":\"Email\",\"embed_origins\":[\"https://example.invalid\"]}}");
        var resource = new EmbedResource(http, "a/b");
        assertThat(resource.listOrigins()).isEmpty();
        assertThat(resource.addOrigin("https://example.invalid").getId()).isEqualTo("origin");
        assertThat(http.lastCaptured().getJsonBody()).isEqualTo("{\"origin\":\"https://example.invalid\"}");
        assertThat(resource.updateOrigin("o/r", "http://localhost:3000").getOrigin()).isEqualTo("http://localhost:3000");
        resource.deleteOrigin("o/r");
        assertThat(resource.createSession("d/a", "a/b", "s/a").getExpiresAt()).isEqualTo("2026-11-11T12:05:00Z");
        assertThat(resource.exchangeSession("a".repeat(64), "https://example.invalid").getSignerAccessCode()).isEqualTo("private");
        assertThat(http.lastCaptured().getJsonBody()).isEqualTo("{\"parent_origin\":\"https://example.invalid\"}");
        assertThat(http.getCaptured()).extracting(MockApiHttpClient.CapturedRequest::getMethod)
                .containsExactly("GET", "POST", "PUT", "DELETE", "POST", "POST");
        assertThat(http.getCaptured()).extracting(MockApiHttpClient.CapturedRequest::getPath).containsExactly(
                "/accounts/a%2Fb/embed-origins", "/accounts/a%2Fb/embed-origins", "/accounts/a%2Fb/embed-origins/o%2Fr",
                "/accounts/a%2Fb/embed-origins/o%2Fr", "/documents/d%2Fa/assignments/a%2Fb/signers/s%2Fa/embed-sessions",
                "/embed/sessions/" + "a".repeat(64) + "/exchange");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"http://example.invalid", "https://example.invalid/path", "https://user@example.invalid",
            "https://example.invalid?q=x", "https://example.invalid#x", "https://*.example.invalid",
            "ftp://localhost", "https://example.invalid:65536", "https://example.invalid:0", " "})
    void rejectsInvalidOriginsBeforeSending(String origin) {
        var http = new MockApiHttpClient();
        var resource = new EmbedResource(http, "account");
        assertThatThrownBy(() -> resource.addOrigin(origin)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.updateOrigin("origin", origin)).isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void rejectsTraversalAndInvalidSessionTokensWithoutSending() {
        var http = new MockApiHttpClient();
        var resource = new EmbedResource(http, "account");
        assertThatThrownBy(() -> resource.deleteOrigin("..")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.createSession("d", "a", "")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.exchangeSession("short", null)).isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }
}
