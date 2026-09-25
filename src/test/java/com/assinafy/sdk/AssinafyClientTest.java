package com.assinafy.sdk;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.OAuthTokens;
import com.assinafy.sdk.request.OAuthClient;
import com.assinafy.sdk.resources.OAuthResource;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import mockwebserver3.SocketEffect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class AssinafyClientTest {

    @Test
    void acceptsNoCredentialsForPublicAndAuthenticationEndpoints() {
        AssinafyClient client = new AssinafyClient(AssinafyClientOptions.builder().build());

        assertThat(client.publicDocuments()).isNotNull();
        assertThat(client.authentication()).isNotNull();
    }

    @Test
    void acceptsApiKeyCredentials() {
        AssinafyClient client = new AssinafyClient(AssinafyClientOptions.builder()
                .apiKey("k")
                .accountId("acc")
                .build());
        assertThat(client.documents()).isNotNull();
        assertThat(client.signers()).isNotNull();
        assertThat(client.workspaces()).isNotNull();
        assertThat(client.assignments()).isNotNull();
        assertThat(client.webhooks()).isNotNull();
        assertThat(client.users()).isNotNull();
        assertThat(client.fields()).isNotNull();
        assertThat(client.tags()).isNotNull();
        assertThat(client.apiKeys()).isNotNull();
        assertThat(client.oauth()).isNotNull();
    }

    @Test
    void acceptsLegacyTokenCredentials() {
        AssinafyClient client = new AssinafyClient(AssinafyClientOptions.builder()
                .token("t")
                .accountId("acc")
                .build());
        assertThat(client.documents()).isNotNull();
    }

    @Test
    void staticCreateBuildsConfiguredClient() {
        AssinafyClient client = AssinafyClient.create("k", "acc");
        assertThat(client.documents()).isNotNull();
    }

    @Test
    void staticCreateWithOptionsBuildsConfiguredClient() {
        AssinafyClientOptions extras = AssinafyClientOptions.builder()
                .timeoutMs(45_000)
                .build();
        AssinafyClient client = AssinafyClient.create("k", "acc", extras);
        assertThat(client.documents()).isNotNull();
    }

    @Test
    void acceptsNoCredentialsWithInjectableHttpClient() {
        MockApiHttpClient mock = new MockApiHttpClient();
        AssinafyClient client = new AssinafyClient(mock, AssinafyClientOptions.builder().build());

        assertThat(client.publicDocuments()).isNotNull();
        assertThat(client.authentication()).isNotNull();
    }

    @Test
    void buildsClientWithInjectableHttpClient() {
        MockApiHttpClient mock = new MockApiHttpClient();
        AssinafyClient client = new AssinafyClient(mock, AssinafyClientOptions.builder()
                .apiKey("k")
                .accountId("acc")
                .build());
        assertThat(client.documents()).isNotNull();
        assertThat(client.signers()).isNotNull();
        assertThat(client.templates()).isNotNull();
    }

    @Test
    void oauthTokenEndpointNeverReceivesTheWorkspaceCredential() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.start();
            server.enqueue(new MockResponse.Builder()
                    .code(200)
                    .addHeader("Content-Type", "application/json")
                    .body("{\"access_token\":\"at\",\"token_type\":\"Bearer\",\"expires_in\":3600,"
                            + "\"refresh_token\":\"rt2\"}")
                    .build());
            AssinafyClient client = new AssinafyClient(AssinafyClientOptions.builder()
                    .apiKey("workspace-secret")
                    .token("workspace-token")
                    .accountId("acc")
                    .baseUrl(server.url("/v1").toString())
                    .build());

            OAuthTokens tokens = client.oauth().refreshToken(
                    OAuthClient.confidential("cli_1a2b3c", "app-secret"), "rt");

            assertThat(tokens.getAccessToken()).isEqualTo("at");
            RecordedRequest request = server.takeRequest();
            assertThat(request.getUrl().encodedPath()).isEqualTo("/v1/oauth/token");
            // The token endpoint authenticates the application, not the workspace. Sending the
            // integrator's own credential there would leak it to a route with no use for it.
            assertThat(request.getHeaders().get("X-Api-Key")).isNull();
            assertThat(request.getHeaders().get("Authorization")).isNull();
            assertThat(request.getBody().utf8()).contains("app-secret")
                    .doesNotContain("workspace-secret")
                    .doesNotContain("workspace-token");
        }
    }

    @Test
    void oauthTokenRequestIsNeverResentAfterAConnectionFailure() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.start();
            // The revoke leaves a pooled connection behind; the server then drops that connection
            // after reading the refresh. OkHttp's default recovery would re-send the refresh on a
            // new connection and replay a refresh token the first attempt may already have retired.
            server.enqueue(new MockResponse.Builder().code(200).build());
            server.enqueue(new MockResponse.Builder().onResponseStart(new SocketEffect.CloseSocket()).build());
            server.enqueue(new MockResponse.Builder()
                    .code(200)
                    .addHeader("Content-Type", "application/json")
                    .body("{\"access_token\":\"at\",\"refresh_token\":\"rt2\"}")
                    .build());
            AssinafyClient client = new AssinafyClient(AssinafyClientOptions.builder()
                    .baseUrl(server.url("/v1").toString())
                    .build());
            OAuthClient app = OAuthClient.publicClient("cli_1a2b3c");

            client.oauth().revokeToken(app, "old-access-token", null);

            assertThatThrownBy(() -> client.oauth().refreshToken(app, "rt1"))
                    .isInstanceOf(NetworkException.class);
            assertThat(server.getRequestCount()).isEqualTo(2);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {307, 308, 408, 503})
    void oauthTokenRequestIsSentOnceWhateverTheResponse(int status) throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.start();
            // OkHttp repeats a request answered 503 with Retry-After: 0 even when connection retries
            // are off. The second response is what any re-sent refresh would receive.
            server.enqueue(new MockResponse.Builder()
                    .code(status)
                    .addHeader("Retry-After", "0")
                    .addHeader("Location", "/v1/oauth/token")
                    .build());
            server.enqueue(new MockResponse.Builder()
                    .code(200)
                    .addHeader("Content-Type", "application/json")
                    .body("{\"access_token\":\"at\",\"refresh_token\":\"rt2\"}")
                    .build());
            AssinafyClient client = new AssinafyClient(AssinafyClientOptions.builder()
                    .baseUrl(server.url("/v1").toString())
                    .build());

            assertThatThrownBy(() -> client.oauth().refreshToken(OAuthClient.publicClient("cli_1a2b3c"), "rt1"))
                    .isInstanceOfSatisfying(ApiException.class,
                            failure -> assertThat(failure.getStatusCode()).isEqualTo(status));
            assertThat(server.getRequestCount()).isEqualTo(1);
        }
    }

    @Test
    void codeVerifiersAreUniquePerConnectionAttempt() {
        assertThat(OAuthResource.createCodeVerifier())
                .isNotEqualTo(OAuthResource.createCodeVerifier());
    }
}
