package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.AuthenticationException;
import com.assinafy.sdk.exceptions.OAuthException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.OAuthAuthorizationRequest;
import com.assinafy.sdk.models.OAuthTokens;
import com.assinafy.sdk.models.OAuthUserInfo;
import com.assinafy.sdk.models.enums.OAuthScope;
import com.assinafy.sdk.request.AuthorizationUrlRequest;
import com.assinafy.sdk.request.OAuthClient;
import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuthResourceTest {

    private static final String BASE_URL = "https://api.assinafy.com.br/v1";
    private static final String ISSUER = "https://auth.assinafy.com.br";
    private static final String AUTHORIZE = ISSUER + "/oauth/authorize";
    private static final String REDIRECT = "https://myapp.com/oauth/callback";

    private static final String TOKENS = "{\"access_token\":\"at\",\"token_type\":\"Bearer\","
            + "\"expires_in\":3600,\"scope\":\"documents:read documents:write\","
            + "\"refresh_token\":\"rt\",\"id_token\":\"idt\"}";

    private static OAuthResource resource(MockApiHttpClient http) {
        return new OAuthResource(http, http, BASE_URL, 5_000, null);
    }

    private static AuthorizationUrlRequest.Builder offlineRequest() {
        return AuthorizationUrlRequest.builder()
                .clientId("cli_1a2b3c")
                .redirectUri(REDIRECT)
                .authorizationEndpoint(AUTHORIZE)
                .issuer(ISSUER);
    }

    private static Map<String, String> query(String url) {
        Map<String, String> params = new HashMap<>();
        String rest = url.substring(url.indexOf('?') + 1);
        for (String pair : rest.split("&")) {
            int eq = pair.indexOf('=');
            params.put(URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8),
                    URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
        }
        return params;
    }

    // ----------------------------------------------------------- authorization URL

    @Test
    void buildsAuthorizationUrlWithPkceStateAndResource() {
        OAuthAuthorizationRequest request = resource(new MockApiHttpClient()).createAuthorizationUrl(
                offlineRequest()
                        .scopes(OAuthScope.DOCUMENTS_READ, OAuthScope.DOCUMENTS_WRITE,
                                OAuthScope.OFFLINE_ACCESS)
                        .build());

        assertThat(request.url()).startsWith(AUTHORIZE + "?");
        Map<String, String> params = query(request.url());
        assertThat(params).containsEntry("response_type", "code")
                .containsEntry("client_id", "cli_1a2b3c")
                .containsEntry("redirect_uri", REDIRECT)
                .containsEntry("scope", "documents:read documents:write offline_access")
                .containsEntry("code_challenge_method", "S256")
                .containsEntry("resource", "https://api.assinafy.com.br")
                .doesNotContainKey("nonce");
        assertThat(params.get("state")).isEqualTo(request.state()).isNotBlank();
        assertThat(request.codeVerifier()).matches("[A-Za-z0-9\\-._~]{43}");
        assertThat(params.get("code_challenge"))
                .isEqualTo(OAuthResource.codeChallengeFor(request.codeVerifier()));
        assertThat(request.issuer()).isEqualTo(ISSUER);
        assertThat(request.nonce()).isNull();
    }

    @Test
    void addsNonceOnlyWhenOpenIdIsRequested() {
        OAuthResource oauth = resource(new MockApiHttpClient());

        OAuthAuthorizationRequest openid = oauth.createAuthorizationUrl(offlineRequest()
                .scopes(OAuthScope.OPENID, OAuthScope.EMAIL).build());
        OAuthAuthorizationRequest suppressed = oauth.createAuthorizationUrl(offlineRequest()
                .scopes(OAuthScope.OPENID).withoutNonce().build());

        assertThat(openid.nonce()).isNotBlank();
        assertThat(query(openid.url())).containsEntry("nonce", openid.nonce());
        assertThat(suppressed.nonce()).isNull();
        assertThat(query(suppressed.url())).doesNotContainKey("nonce");
    }

    @Test
    void deduplicatesScopesAndForwardsPrompt() {
        OAuthAuthorizationRequest request = resource(new MockApiHttpClient()).createAuthorizationUrl(
                offlineRequest()
                        .scopes(OAuthScope.DOCUMENTS_READ, OAuthScope.DOCUMENTS_READ)
                        .prompt("consent")
                        .build());

        assertThat(query(request.url()))
                .containsEntry("scope", "documents:read")
                .containsEntry("prompt", "consent");
    }

    @Test
    void rejectsInvalidAuthorizationInputs() {
        OAuthResource oauth = resource(new MockApiHttpClient());

        assertThatThrownBy(() -> oauth.createAuthorizationUrl(null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> oauth.createAuthorizationUrl(offlineRequest()
                .clientId(" ").scopes(OAuthScope.DOCUMENTS_READ).build()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> oauth.createAuthorizationUrl(offlineRequest()
                .redirectUri("http://myapp.com/cb").scopes(OAuthScope.DOCUMENTS_READ).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Redirect URI");
        assertThatThrownBy(() -> oauth.createAuthorizationUrl(offlineRequest()
                .redirectUri(REDIRECT + "#frag").scopes(OAuthScope.DOCUMENTS_READ).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("fragment");
        assertThatThrownBy(() -> oauth.createAuthorizationUrl(offlineRequest().build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("scope");
        assertThatThrownBy(() -> oauth.createAuthorizationUrl(offlineRequest()
                .scopes(OAuthScope.DOCUMENTS_READ).codeVerifier("too-short").build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Code verifier");
    }

    // ---------------------------------------------------------------- callback

    @Test
    void readsCallbackFromUrlAndFromParameterMap() {
        OAuthResource oauth = resource(new MockApiHttpClient());
        OAuthAuthorizationRequest stored = oauth.createAuthorizationUrl(
                offlineRequest().scopes(OAuthScope.DOCUMENTS_READ).build());

        String url = REDIRECT + "?code=abc123&state=" + stored.state() + "&iss=" + ISSUER;
        assertThat(oauth.readAuthorizationCallback(url, stored)).isEqualTo("abc123");
        assertThat(oauth.readAuthorizationCallback(
                Map.of("code", "abc123", "state", stored.state(), "iss", ISSUER), stored))
                .isEqualTo("abc123");
    }

    @Test
    void rejectsCallbackWithWrongStateOrIssuer() {
        OAuthResource oauth = resource(new MockApiHttpClient());
        OAuthAuthorizationRequest stored = oauth.createAuthorizationUrl(
                offlineRequest().scopes(OAuthScope.DOCUMENTS_READ).build());

        assertThatThrownBy(() -> oauth.readAuthorizationCallback(
                "?code=abc&state=not-mine&iss=" + ISSUER, stored))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("state");
        // RFC 9207: the server always sends iss, so a missing one is as suspect as a wrong one.
        assertThatThrownBy(() -> oauth.readAuthorizationCallback(
                "?code=abc&state=" + stored.state(), stored))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("issuer");
        assertThatThrownBy(() -> oauth.readAuthorizationCallback(
                "?code=abc&state=" + stored.state() + "&iss=https://evil.example", stored))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("issuer");
    }

    @Test
    void reportsDeclinedConsentAsOAuthException() {
        OAuthResource oauth = resource(new MockApiHttpClient());
        OAuthAuthorizationRequest stored = oauth.createAuthorizationUrl(
                offlineRequest().scopes(OAuthScope.DOCUMENTS_READ).build());

        assertThatThrownBy(() -> oauth.readAuthorizationCallback(
                REDIRECT + "?error=access_denied&error_description=User+declined&state="
                        + stored.state() + "&iss=" + ISSUER, stored))
                .isInstanceOfSatisfying(OAuthException.class, failure -> {
                    assertThat(failure.getError()).isEqualTo("access_denied");
                    assertThat(failure.getErrorDescription()).isEqualTo("User declined");
                    assertThat(failure.getStatusCode()).isEqualTo(400);
                });
    }

    @Test
    void rejectsCallbackWithNeitherCodeNorError() {
        OAuthResource oauth = resource(new MockApiHttpClient());
        OAuthAuthorizationRequest stored = oauth.createAuthorizationUrl(
                offlineRequest().scopes(OAuthScope.DOCUMENTS_READ).build());

        assertThatThrownBy(() -> oauth.readAuthorizationCallback(
                "?state=" + stored.state() + "&iss=" + ISSUER, stored))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("neither a code nor an error");
    }

    // ------------------------------------------------------------------ tokens

    @Test
    void exchangesCodeForTokens() {
        MockApiHttpClient http = new MockApiHttpClient().enqueue(200, TOKENS);
        String verifier = OAuthResource.createCodeVerifier();

        OAuthTokens tokens = resource(http).exchangeCode(
                OAuthClient.confidential("cli_1a2b3c", "shh"), "code-123", verifier, REDIRECT);

        assertThat(tokens.getAccessToken()).isEqualTo("at");
        assertThat(tokens.getTokenType()).isEqualTo("Bearer");
        assertThat(tokens.getExpiresIn()).isEqualTo(3600);
        assertThat(tokens.getRefreshToken()).isEqualTo("rt");
        assertThat(tokens.getIdToken()).isEqualTo("idt");
        assertThat(tokens.getScope()).isEqualTo("documents:read documents:write");

        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/oauth/token");
        assertThat(http.lastCaptured().getJsonBody())
                .contains("\"grant_type\":\"authorization_code\"")
                .contains("\"code\":\"code-123\"")
                .contains("\"redirect_uri\":\"" + REDIRECT + "\"")
                .contains("\"code_verifier\":\"" + verifier + "\"")
                .contains("\"client_id\":\"cli_1a2b3c\"")
                .contains("\"client_secret\":\"shh\"")
                .contains("\"resource\":\"https://api.assinafy.com.br\"");
    }

    @Test
    void omitsClientSecretForPublicClients() {
        MockApiHttpClient http = new MockApiHttpClient().enqueue(200, TOKENS);

        resource(http).exchangeCode(OAuthClient.publicClient("cli_public"), "code-123",
                OAuthResource.createCodeVerifier(), REDIRECT);

        assertThat(http.lastCaptured().getJsonBody()).doesNotContain("client_secret");
    }

    @Test
    void refreshesAccessToken() {
        MockApiHttpClient http = new MockApiHttpClient().enqueue(200, TOKENS);

        OAuthTokens tokens = resource(http)
                .refreshToken(OAuthClient.confidential("cli_1a2b3c", "shh"), "old-refresh");

        assertThat(tokens.getRefreshToken()).isEqualTo("rt");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/oauth/token");
        assertThat(http.lastCaptured().getJsonBody())
                .contains("\"grant_type\":\"refresh_token\"")
                .contains("\"refresh_token\":\"old-refresh\"");
    }

    @Test
    void translatesRfc6749ErrorBodyIntoOAuthException() {
        MockApiHttpClient http = new MockApiHttpClient().enqueue(400,
                "{\"error\":\"invalid_grant\",\"error_description\":\"Authorization code expired.\"}");

        assertThatThrownBy(() -> resource(http).exchangeCode(
                OAuthClient.publicClient("cli"), "spent", OAuthResource.createCodeVerifier(), REDIRECT))
                .isInstanceOfSatisfying(OAuthException.class, failure -> {
                    assertThat(failure.getError()).isEqualTo("invalid_grant");
                    assertThat(failure.getErrorDescription()).isEqualTo("Authorization code expired.");
                    assertThat(failure.getStatusCode()).isEqualTo(400);
                    assertThat(failure.getMessage())
                            .isEqualTo("invalid_grant: Authorization code expired.");
                });
    }

    @Test
    void translatesInvalidClientIntoOAuthException() {
        MockApiHttpClient http = new MockApiHttpClient().enqueue(401,
                "{\"error\":\"invalid_client\",\"error_description\":\"Client authentication failed.\"}");

        assertThatThrownBy(() -> resource(http)
                .refreshToken(OAuthClient.confidential("cli", "wrong"), "rt"))
                .isInstanceOfSatisfying(OAuthException.class,
                        failure -> assertThat(failure.getError()).isEqualTo("invalid_client"));
    }

    @Test
    void rejectsTokenResponseWithoutAccessToken() {
        MockApiHttpClient http = new MockApiHttpClient().enqueue(200, "{\"token_type\":\"Bearer\"}");

        assertThatThrownBy(() -> resource(http)
                .refreshToken(OAuthClient.publicClient("cli"), "rt"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("no access_token");
    }

    @Test
    void validatesTokenArgumentsBeforeSending() {
        MockApiHttpClient http = new MockApiHttpClient();
        OAuthResource oauth = resource(http);
        OAuthClient client = OAuthClient.publicClient("cli");
        String verifier = OAuthResource.createCodeVerifier();

        assertThatThrownBy(() -> oauth.exchangeCode(client, " ", verifier, REDIRECT))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> oauth.exchangeCode(client, "code", "short", REDIRECT))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> oauth.exchangeCode(null, "code", verifier, REDIRECT))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> oauth.refreshToken(client, ""))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> OAuthClient.confidential("cli", null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> OAuthClient.publicClient(" "))
                .isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    // ------------------------------------------------------------------ revoke

    @Test
    void revokesTokenWithOptionalHint() {
        MockApiHttpClient http = new MockApiHttpClient().enqueue(200, "").enqueue(200, "");
        OAuthResource oauth = resource(http);

        oauth.revokeToken(OAuthClient.confidential("cli", "shh"), "rt", "refresh_token");
        oauth.revokeToken(OAuthClient.publicClient("cli"), "at", null);

        assertThat(http.capturedAt(0).getPath()).isEqualTo("/oauth/revoke");
        assertThat(http.capturedAt(0).getJsonBody())
                .contains("\"token\":\"rt\"")
                .contains("\"token_type_hint\":\"refresh_token\"")
                .contains("\"client_secret\":\"shh\"");
        assertThat(http.capturedAt(1).getJsonBody()).doesNotContain("token_type_hint");
    }

    @Test
    void rejectsUnknownTokenTypeHint() {
        MockApiHttpClient http = new MockApiHttpClient();

        assertThatThrownBy(() -> resource(http)
                .revokeToken(OAuthClient.publicClient("cli"), "tok", "id_token"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("access_token or refresh_token");
        assertThat(http.capturedCount()).isZero();
    }

    // ---------------------------------------------------------------- userinfo

    @Test
    void readsFlatUserinfoClaims() {
        MockApiHttpClient http = new MockApiHttpClient().enqueue(200,
                "{\"sub\":\"d6zqpbyog2v3xvxerwn8la94\",\"name\":\"Maria Silva\","
                        + "\"email\":\"maria@example.invalid\",\"email_verified\":true}");

        OAuthUserInfo info = resource(http).userInfo();

        assertThat(info.getSub()).isEqualTo("d6zqpbyog2v3xvxerwn8la94");
        assertThat(info.getName()).isEqualTo("Maria Silva");
        assertThat(info.getEmail()).isEqualTo("maria@example.invalid");
        assertThat(info.getEmailVerified()).isTrue();
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/oauth/userinfo");
    }

    @Test
    void surfacesInsufficientScopeChallenge() {
        MockApiHttpClient http = new MockApiHttpClient().enqueue(403,
                "{\"status\":403,\"data\":null,\"message\":\"Forbidden\"}",
                Map.of("WWW-Authenticate",
                        "Bearer error=\"insufficient_scope\", scope=\"documents:write\""));

        assertThatThrownBy(() -> resource(http).userInfo())
                .isInstanceOfSatisfying(AuthenticationException.class, failure -> {
                    assertThat(failure.getStatusCode()).isEqualTo(403);
                    assertThat(failure.getResponseHeader("www-authenticate"))
                            .contains("insufficient_scope")
                            .contains("scope=\"documents:write\"");
                });
    }

    // -------------------------------------------------------------------- PKCE

    @Test
    void generatesRfc7636VerifiersAndChallenges() {
        String verifier = OAuthResource.createCodeVerifier();

        assertThat(verifier).matches("[A-Za-z0-9\\-._~]{43,128}");
        assertThat(OAuthResource.createCodeVerifier()).isNotEqualTo(verifier);
        // RFC 7636 appendix B reference vector.
        assertThat(OAuthResource.codeChallengeFor("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"))
                .isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
        assertThatThrownBy(() -> OAuthResource.codeChallengeFor("nope"))
                .isInstanceOf(ValidationException.class);
    }
}
