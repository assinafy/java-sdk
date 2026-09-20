package com.assinafy.sdk;

import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.OAuthTokens;
import com.assinafy.sdk.request.OAuthClient;
import com.assinafy.sdk.resources.OAuthResource;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.junit.jupiter.api.Test;

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
                    .body("{\"access_token\":\"at\",\"token_type\":\"Bearer\",\"expires_in\":3600}")
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
    void codeVerifiersAreUniquePerConnectionAttempt() {
        assertThat(OAuthResource.createCodeVerifier())
                .isNotEqualTo(OAuthResource.createCodeVerifier());
    }
}
