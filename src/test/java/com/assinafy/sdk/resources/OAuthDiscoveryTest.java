package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.OAuthAuthorizationRequest;
import com.assinafy.sdk.models.OAuthAuthorizationServerMetadata;
import com.assinafy.sdk.models.OAuthProtectedResourceMetadata;
import com.assinafy.sdk.models.enums.OAuthScope;
import com.assinafy.sdk.request.AuthorizationUrlRequest;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Discovery reaches the host root rather than {@code /v1}, and a different host for the
 * authorization server, so it uses its own credential-free transport. These tests drive that
 * transport against a real in-process server; the hand-rolled mock cannot cover it.
 */
class OAuthDiscoveryTest {

    private MockWebServer server;
    private String origin;
    private OAuthResource oauth;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        origin = server.url("/").toString().replaceAll("/$", "");
        oauth = new OAuthResource(new MockApiHttpClient(), new MockApiHttpClient(),
                origin + "/v1", 5_000, null);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.close();
    }

    private void enqueue(String body) {
        server.enqueue(new MockResponse.Builder()
                .code(200)
                .addHeader("Content-Type", "application/json")
                .body(body)
                .build());
    }

    private String protectedResourceJson() {
        return "{\"resource\":\"" + origin + "\",\"authorization_servers\":[\"" + origin + "\"],"
                + "\"scopes_supported\":[\"documents:read\",\"documents:write\"],"
                + "\"bearer_methods_supported\":[\"header\"]}";
    }

    private String authorizationServerJson(String issuer) {
        return "{\"issuer\":\"" + issuer + "\","
                + "\"authorization_endpoint\":\"" + issuer + "/oauth/authorize\","
                + "\"token_endpoint\":\"" + origin + "/v1/oauth/token\","
                + "\"revocation_endpoint\":\"" + origin + "/v1/oauth/revoke\","
                + "\"userinfo_endpoint\":\"" + origin + "/v1/oauth/userinfo\","
                + "\"jwks_uri\":\"" + issuer + "/.well-known/jwks.json\","
                + "\"scopes_supported\":[\"documents:read\",\"offline_access\"],"
                + "\"response_types_supported\":[\"code\"],"
                + "\"grant_types_supported\":[\"authorization_code\",\"refresh_token\"],"
                + "\"code_challenge_methods_supported\":[\"S256\"],"
                + "\"token_endpoint_auth_methods_supported\":[\"client_secret_post\",\"none\"],"
                + "\"authorization_response_iss_parameter_supported\":true,"
                + "\"client_id_metadata_document_supported\":true}";
    }

    @Test
    void readsProtectedResourceMetadataFromTheHostRoot() throws Exception {
        enqueue(protectedResourceJson());

        OAuthProtectedResourceMetadata metadata = oauth.protectedResourceMetadata();

        assertThat(metadata.getResource()).isEqualTo(origin);
        assertThat(metadata.getAuthorizationServers()).containsExactly(origin);
        assertThat(metadata.getScopesSupported()).containsExactly("documents:read", "documents:write");
        assertThat(metadata.getBearerMethodsSupported()).containsExactly("header");

        RecordedRequest request = server.takeRequest();
        // Served at the host root, not under /v1, and bare rather than enveloped.
        assertThat(request.getUrl().encodedPath()).isEqualTo("/.well-known/oauth-protected-resource");
        // The application authenticates itself; no workspace credential may leak to discovery.
        assertThat(request.getHeaders().get("X-Api-Key")).isNull();
        assertThat(request.getHeaders().get("Authorization")).isNull();
    }

    @Test
    void discoversIssuerThenReadsAuthorizationServerMetadata() throws Exception {
        enqueue(protectedResourceJson());
        enqueue(authorizationServerJson(origin));

        OAuthAuthorizationServerMetadata metadata = oauth.authorizationServerMetadata();

        assertThat(metadata.getIssuer()).isEqualTo(origin);
        assertThat(metadata.getAuthorizationEndpoint()).isEqualTo(origin + "/oauth/authorize");
        assertThat(metadata.getTokenEndpoint()).isEqualTo(origin + "/v1/oauth/token");
        assertThat(metadata.getRevocationEndpoint()).isEqualTo(origin + "/v1/oauth/revoke");
        assertThat(metadata.getUserinfoEndpoint()).isEqualTo(origin + "/v1/oauth/userinfo");
        assertThat(metadata.getJwksUri()).isEqualTo(origin + "/.well-known/jwks.json");
        assertThat(metadata.getCodeChallengeMethodsSupported()).containsExactly("S256");
        assertThat(metadata.getTokenEndpointAuthMethodsSupported())
                .containsExactly("client_secret_post", "none");
        assertThat(metadata.getAuthorizationResponseIssParameterSupported()).isTrue();
        assertThat(metadata.getClientIdMetadataDocumentSupported()).isTrue();

        assertThat(server.takeRequest().getUrl().encodedPath())
                .isEqualTo("/.well-known/oauth-protected-resource");
        assertThat(server.takeRequest().getUrl().encodedPath())
                .isEqualTo("/.well-known/oauth-authorization-server");
    }

    @Test
    void rejectsMetadataWhoseIssuerDisagreesWithItsLocation() {
        enqueue(authorizationServerJson("https://evil.example"));

        assertThatThrownBy(() -> oauth.authorizationServerMetadata(origin))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("issuer does not match");
    }

    @Test
    void rejectsProtectedResourceMetadataWithoutAnAuthorizationServer() {
        enqueue("{\"resource\":\"" + origin + "\",\"authorization_servers\":[]}");

        assertThatThrownBy(() -> oauth.authorizationServerMetadata())
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("no authorization server");
    }

    @Test
    void surfacesADiscoveryFailure() {
        server.enqueue(new MockResponse.Builder().code(404).body("{\"status\":404}").build());

        assertThatThrownBy(() -> oauth.protectedResourceMetadata())
                .isInstanceOfSatisfying(ApiException.class,
                        failure -> assertThat(failure.getStatusCode()).isEqualTo(404));
    }

    @Test
    void buildsAnAuthorizationUrlFromDiscoveredEndpoints() {
        enqueue(protectedResourceJson());
        enqueue(authorizationServerJson(origin));

        OAuthAuthorizationRequest request = oauth.createAuthorizationUrl(
                AuthorizationUrlRequest.builder()
                        .clientId("cli_1a2b3c")
                        .redirectUri("https://myapp.com/oauth/callback")
                        .scopes(OAuthScope.DOCUMENTS_READ)
                        .build());

        assertThat(request.url()).startsWith(origin + "/oauth/authorize?");
        assertThat(request.issuer()).isEqualTo(origin);
        // A loopback http origin is not a valid RFC 8707 resource identifier, so it is omitted.
        assertThat(request.url()).doesNotContain("resource=");
    }

    @Test
    void reusesOneDiscoveryTransportPerOrigin() throws Exception {
        enqueue(protectedResourceJson());
        enqueue(protectedResourceJson());

        oauth.protectedResourceMetadata();
        oauth.protectedResourceMetadata();

        assertThat(server.takeRequest()).isNotNull();
        assertThat(server.takeRequest()).isNotNull();
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    void rejectsANonHttpsIssuer() {
        assertThatThrownBy(() -> oauth.authorizationServerMetadata("ftp://auth.example"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("absolute https URL");
        assertThatThrownBy(() -> oauth.authorizationServerMetadata("not a url"))
                .isInstanceOf(ValidationException.class);
    }
}
