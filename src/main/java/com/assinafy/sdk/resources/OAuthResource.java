package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.OAuthException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.http.OkHttpApiClient;
import com.assinafy.sdk.models.OAuthAuthorizationRequest;
import com.assinafy.sdk.models.OAuthAuthorizationServerMetadata;
import com.assinafy.sdk.models.OAuthProtectedResourceMetadata;
import com.assinafy.sdk.models.OAuthTokens;
import com.assinafy.sdk.models.OAuthUserInfo;
import com.assinafy.sdk.models.enums.OAuthScope;
import com.assinafy.sdk.request.AuthorizationUrlRequest;
import com.assinafy.sdk.request.OAuthClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * OAuth 2.1 and OpenID Connect operations for applications acting inside <em>other people's</em>
 * workspaces.
 *
 * <p>Use this resource only when your product is connected by its users. To automate your own
 * workspace, keep using an API key and ignore everything here.
 *
 * <table class="striped">
 * <caption>Choosing between an API key and OAuth</caption>
 * <thead><tr><th scope="col"></th><th scope="col">API key</th><th scope="col">OAuth</th></tr></thead>
 * <tbody>
 * <tr><th scope="row">Acts on</th><td>Your own workspace</td>
 *     <td>Someone else's workspace, with their permission</td></tr>
 * <tr><th scope="row">Can do</th><td>Everything your account can do</td>
 *     <td>Only what the user approved</td></tr>
 * <tr><th scope="row">User can switch it off</th><td>No</td><td>Yes, at any time</td></tr>
 * </tbody>
 * </table>
 *
 * <p>Two hosts are involved on purpose: the consent page lives on the authorization server
 * ({@code https://auth.assinafy.com.br}) while the token, revocation and userinfo endpoints live on
 * this API. Endpoint URLs are read from the published metadata documents rather than hardcoded.
 *
 * <h2>The flow</h2>
 * <ol>
 *   <li>{@link #createAuthorizationUrl(AuthorizationUrlRequest)} — mint PKCE and {@code state},
 *       store the result in the user's session, redirect the browser to its
 *       {@link OAuthAuthorizationRequest#url() url}.</li>
 *   <li>The user signs in, picks <b>one</b> workspace and approves.</li>
 *   <li>{@link #readAuthorizationCallback(String, OAuthAuthorizationRequest)} — validate the
 *       response that lands on your redirect URI and get the code.</li>
 *   <li>{@link #exchangeCode(OAuthClient, String, String, String)} — swap the 60-second code for
 *       tokens.</li>
 *   <li>Build a client with that token and read the single workspace it covers.</li>
 *   <li>{@link #refreshToken(OAuthClient, String)} before the hour is up (needs
 *       {@link OAuthScope#OFFLINE_ACCESS}), and
 *       {@link #revokeToken(OAuthClient, String, String)} when the user disconnects.</li>
 * </ol>
 *
 * {@snippet lang="java" :
 * AssinafyClient client = new AssinafyClient(new AssinafyClientOptions()); // no credentials needed
 *
 * // Step 1 — before redirecting the user
 * OAuthAuthorizationRequest request = client.oauth().createAuthorizationUrl(
 *         AuthorizationUrlRequest.builder()
 *                 .clientId(System.getenv("ASSINAFY_CLIENT_ID"))
 *                 .redirectUri("https://myapp.com/oauth/callback")
 *                 .scopes(OAuthScope.DOCUMENTS_READ, OAuthScope.DOCUMENTS_WRITE,
 *                         OAuthScope.OFFLINE_ACCESS)
 *                 .build());
 * session.setAttribute("assinafy.oauth", request);
 * response.sendRedirect(request.url());
 *
 * // Steps 3 and 4 — on https://myapp.com/oauth/callback
 * OAuthAuthorizationRequest stored =
 *         (OAuthAuthorizationRequest) session.getAttribute("assinafy.oauth");
 * String code = client.oauth().readAuthorizationCallback(httpRequest.getQueryString(), stored);
 * OAuthTokens tokens = client.oauth().exchangeCode(
 *         OAuthClient.confidential(System.getenv("ASSINAFY_CLIENT_ID"),
 *                                  System.getenv("ASSINAFY_CLIENT_SECRET")),
 *         code, stored.codeVerifier(), "https://myapp.com/oauth/callback");
 *
 * // Step 5 — the token covers exactly one workspace
 * AssinafyClient connected = new AssinafyClient(AssinafyClientOptions.builder()
 *         .token(tokens.getAccessToken())
 *         .build());
 * String accountId = connected.workspaces().list().getData().get(0).getId();
 * }
 *
 * <h2>Two facts behind most integration bugs</h2>
 * <ul>
 *   <li>A token works for exactly <b>one</b> workspace. Calling any other answers {@code 403},
 *       even one the same user belongs to. Connect each workspace separately.</li>
 *   <li>A connection lasts <b>30 days from the user's approval</b>. Refreshing does not extend it,
 *       so plan for users to reconnect monthly.</li>
 * </ul>
 *
 * <p>Requests to the token and revocation endpoints deliberately carry no {@code X-Api-Key} or
 * {@code Authorization} header: they authenticate the <em>application</em> through
 * {@link OAuthClient}, and sending a workspace credential to a route that has no use for it would
 * leak it.
 */
public class OAuthResource extends BaseResource {

    /** RFC 8615 path of this API's protected-resource metadata, served at the host root. */
    private static final String PROTECTED_RESOURCE_PATH = "/.well-known/oauth-protected-resource";

    /** RFC 8414 path of the authorization server's own metadata. */
    private static final String AUTHORIZATION_SERVER_PATH = "/.well-known/oauth-authorization-server";

    /** RFC 7636 code-verifier grammar: 43-128 unreserved characters. */
    private static final Pattern CODE_VERIFIER = Pattern.compile("^[A-Za-z0-9\\-._~]{43,128}$");

    private static final Set<String> TOKEN_TYPE_HINTS = Set.of("access_token", "refresh_token");

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64URL = Base64.getUrlEncoder().withoutPadding();

    /** Credential-free transport for the token and revocation endpoints. */
    private final ApiHttpClient publicHttp;
    private final String baseUrl;
    private final long timeoutMs;
    /** One credential-free transport per metadata origin, created on first use. */
    private final Map<String, ApiHttpClient> discovery = new ConcurrentHashMap<>();

    /**
     * Create OAuth operations.
     *
     * @param http transport carrying the client's configured credential, used by {@link #userInfo()}
     * @param publicHttp credential-free transport for the token and revocation endpoints
     * @param baseUrl the configured API base URL, used to derive the metadata origin and the
     *                RFC 8707 {@code resource} indicator
     * @param timeoutMs transport timeout applied to the metadata requests
     * @param logger diagnostic logger; {@code null} selects the no-op logger
     */
    public OAuthResource(ApiHttpClient http, ApiHttpClient publicHttp, String baseUrl,
                         long timeoutMs, Logger logger) {
        super(http, null, logger);
        this.publicHttp = publicHttp != null ? publicHttp : http;
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs > 0 ? timeoutMs : 30_000L;
    }

    // ---------------------------------------------------------------- discovery

    /**
     * Read this API's protected-resource metadata
     * ({@code GET {apiOrigin}/.well-known/oauth-protected-resource}, RFC 9728).
     *
     * <p>Served at the API host root — not under {@code /v1} — and bare, without the
     * {@code {status, message, data}} envelope. Use it to discover which authorization server may
     * issue tokens for this API and which scopes it accepts.
     *
     * <p>Request body: none. Authentication: none.
     *
     * <pre>{@code
     * {
     *   "resource": "https://api.assinafy.com.br",
     *   "authorization_servers": ["https://auth.assinafy.com.br"],
     *   "scopes_supported": ["documents:read", "documents:write", "templates:read",
     *                        "templates:write", "account:read", "openid", "profile", "email"],
     *   "bearer_methods_supported": ["header"]
     * }
     * }</pre>
     *
     * @return the metadata document
     * @throws AssinafyException if the host does not publish the document
     */
    public OAuthProtectedResourceMetadata protectedResourceMetadata() {
        String origin = apiOrigin();
        return call("Failed to fetch OAuth protected-resource metadata",
                () -> transportFor(origin).get(PROTECTED_RESOURCE_PATH),
                OAuthProtectedResourceMetadata.class);
    }

    /**
     * Read the authorization server's metadata from the issuer discovered through
     * {@link #protectedResourceMetadata()}.
     *
     * @return the metadata document
     * @throws AssinafyException if discovery fails or the document is not authoritative
     */
    public OAuthAuthorizationServerMetadata authorizationServerMetadata() {
        return authorizationServerMetadata(null);
    }

    /**
     * Read the authorization server's metadata
     * ({@code GET {issuer}/.well-known/oauth-authorization-server}, RFC 8414).
     *
     * <p>Every endpoint URL an OAuth client needs comes from here, so nothing has to be hardcoded.
     * The document is published by the authorization server, a different host from this API.
     *
     * <p>Request body: none. Authentication: none.
     *
     * <pre>{@code
     * {
     *   "issuer": "https://auth.assinafy.com.br",
     *   "authorization_endpoint": "https://auth.assinafy.com.br/oauth/authorize",
     *   "token_endpoint": "https://api.assinafy.com.br/v1/oauth/token",
     *   "revocation_endpoint": "https://api.assinafy.com.br/v1/oauth/revoke",
     *   "userinfo_endpoint": "https://api.assinafy.com.br/v1/oauth/userinfo",
     *   "jwks_uri": "https://auth.assinafy.com.br/.well-known/jwks.json",
     *   "response_types_supported": ["code"],
     *   "grant_types_supported": ["authorization_code", "refresh_token"],
     *   "code_challenge_methods_supported": ["S256"],
     *   "token_endpoint_auth_methods_supported": ["client_secret_post", "none"]
     * }
     * }</pre>
     *
     * @param issuer issuer to read; {@code null} discovers it through
     *               {@link #protectedResourceMetadata()}, which costs one extra request
     * @return the metadata document
     * @throws ValidationException if {@code issuer} is not an absolute HTTPS URL, or the document's
     *         own {@code issuer} disagrees with where it was fetched from (RFC 8414 §3.3 — a
     *         mismatch means the document is not authoritative)
     * @throws AssinafyException if the authorization server rejects the request
     */
    public OAuthAuthorizationServerMetadata authorizationServerMetadata(String issuer) {
        String origin = stripTrailingSlashes(issuer != null ? requireHttpsUrl(issuer, "Issuer") : defaultIssuer());
        OAuthAuthorizationServerMetadata metadata = call(
                "Failed to fetch OAuth authorization-server metadata",
                () -> transportFor(origin).get(AUTHORIZATION_SERVER_PATH),
                OAuthAuthorizationServerMetadata.class);
        if (metadata == null || !stripTrailingSlashes(metadata.getIssuer()).equals(origin)) {
            throw new ValidationException(
                    "Authorization-server metadata issuer does not match the requested issuer",
                    Map.of("expected", origin,
                            "received", metadata != null && metadata.getIssuer() != null
                                    ? metadata.getIssuer() : "none"));
        }
        return metadata;
    }

    // ------------------------------------------------------------ authorization

    /**
     * Mint a PKCE pair and a {@code state}, then build the consent URL to send the user's browser
     * to ({@code GET {authorizationEndpoint}}).
     *
     * <p>Call this once per connection attempt and keep the whole returned object in the user's
     * session: reusing a verifier or a {@code state} across attempts defeats PKCE and CSRF
     * protection respectively. Navigate to the URL with a full page load — an AJAX request cannot
     * show a consent screen.
     *
     * <p>PKCE is mandatory for confidential applications too, and Assinafy accepts only the
     * {@code S256} challenge method. Unless
     * {@link AuthorizationUrlRequest#setAuthorizationEndpoint(String)} and
     * {@link AuthorizationUrlRequest#setIssuer(String)} are both supplied, this performs discovery
     * first.
     *
     * <p>The resulting query carries {@code response_type=code}, {@code client_id},
     * {@code redirect_uri}, {@code scope}, {@code state}, {@code code_challenge},
     * {@code code_challenge_method=S256}, the RFC 8707 {@code resource} indicator, and — when the
     * {@code openid} scope is requested — a {@code nonce}.
     *
     * @param request client ID, redirect URI, scopes, and optional overrides
     * @return the consent URL plus the {@code state}, PKCE verifier, issuer and nonce to store
     * @throws ValidationException if the request is absent, the client ID is blank, the redirect
     *         URI is not an absolute HTTPS URL without a fragment, the scope list is empty or
     *         contains {@code null}, or a supplied {@code codeVerifier}/{@code state} is invalid
     * @throws AssinafyException if discovery is needed and fails
     */
    public OAuthAuthorizationRequest createAuthorizationUrl(AuthorizationUrlRequest request) {
        if (request == null) throw new ValidationException("Authorization request is required");
        requireId(request.getClientId(), "Client ID");
        String redirectUri = requireRedirectUri(request.getRedirectUri());
        String scope = joinScopes(request.getScopes());

        String endpoint = request.getAuthorizationEndpoint();
        String issuer = request.getIssuer();
        if (endpoint == null || issuer == null) {
            OAuthAuthorizationServerMetadata metadata = authorizationServerMetadata(issuer);
            if (endpoint == null) endpoint = metadata.getAuthorizationEndpoint();
            if (issuer == null) issuer = metadata.getIssuer();
        }
        requireHttpsUrl(endpoint, "Authorization endpoint");
        requireHttpsUrl(issuer, "Issuer");

        String codeVerifier = request.getCodeVerifier() != null
                ? requireCodeVerifier(request.getCodeVerifier()) : createCodeVerifier();
        String state = request.getState() != null
                ? requireId(request.getState(), "State") : randomValue(16);

        Map<String, String> query = new LinkedHashMap<>();
        query.put("response_type", "code");
        query.put("client_id", request.getClientId());
        query.put("redirect_uri", redirectUri);
        query.put("scope", scope);
        query.put("state", state);
        query.put("code_challenge", codeChallengeFor(codeVerifier));
        query.put("code_challenge_method", "S256");
        String resource = resourceIndicator();
        if (resource != null) query.put("resource", resource);

        // A nonce only means something for OpenID Connect, so default it to the presence of the
        // openid scope rather than always emitting one.
        String nonce = null;
        if (!request.isNonceSuppressed()) {
            if (request.getNonce() != null) {
                nonce = requireId(request.getNonce(), "Nonce");
            } else if (request.getScopes().contains(OAuthScope.OPENID)) {
                nonce = randomValue(16);
            }
        }
        if (nonce != null) query.put("nonce", nonce);
        if (request.getPrompt() != null) query.put("prompt", requireId(request.getPrompt(), "Prompt"));

        logInfo("Built OAuth authorization URL", Map.of("scope", scope));
        return new OAuthAuthorizationRequest(
                endpoint + (endpoint.indexOf('?') >= 0 ? "&" : "?") + queryString(query),
                state, codeVerifier, issuer, nonce);
    }

    /**
     * Validate the authorization response that lands on your redirect URI and return the code to
     * exchange.
     *
     * <p>Checks, in order and before anything else is trusted: {@code state} equals the stored
     * value (compared in constant time), {@code iss} equals the stored issuer, and only then
     * whether the server reported an error. A declined consent arrives as
     * {@code ?error=access_denied}, not as a failed HTTP request.
     *
     * <p>The {@code iss} check is strict because the authorization server advertises RFC 9207
     * support and always sends the parameter: a missing {@code iss} is treated exactly like a wrong
     * one. This performs no network I/O.
     *
     * @param callbackUrlOrQuery the full callback URL, or just its query string, with or without a
     *                           leading {@code ?}
     * @param stored the request returned by {@link #createAuthorizationUrl(AuthorizationUrlRequest)}
     * @return the validated single-use authorization code
     * @throws ValidationException if {@code state} is missing or does not match, {@code iss} is
     *         absent or disagrees with the stored issuer, or a successful response carries no
     *         {@code code}. In every case the response is not yours — stop, do not exchange.
     * @throws OAuthException if the server reported an {@code error}, such as
     *         {@code access_denied}, {@code invalid_scope}, {@code invalid_request},
     *         {@code unsupported_response_type} or {@code invalid_target}
     */
    public String readAuthorizationCallback(String callbackUrlOrQuery, OAuthAuthorizationRequest stored) {
        return readAuthorizationCallback(parseQuery(callbackUrlOrQuery), stored);
    }

    /**
     * Validate an authorization response already parsed into query parameters.
     *
     * @param params the callback's query parameters, such as a servlet's single-valued parameter map
     * @param stored the request returned by {@link #createAuthorizationUrl(AuthorizationUrlRequest)}
     * @return the validated single-use authorization code
     * @throws ValidationException if the response does not belong to the stored request
     * @throws OAuthException if the server reported an {@code error}
     */
    public String readAuthorizationCallback(Map<String, String> params, OAuthAuthorizationRequest stored) {
        if (params == null) throw new ValidationException("Callback parameters are required");
        if (stored == null || stored.state() == null || stored.state().isBlank()) {
            throw new ValidationException("Stored authorization request with a state is required");
        }

        String state = params.get("state");
        if (state == null || !constantTimeEquals(state, stored.state())) {
            throw new ValidationException(
                    "OAuth callback state does not match the stored authorization request");
        }

        String issuer = params.get("iss");
        if (stored.issuer() != null
                && (issuer == null || !stripTrailingSlashes(issuer).equals(stripTrailingSlashes(stored.issuer())))) {
            throw new ValidationException(
                    "OAuth callback issuer is missing or does not match the expected issuer",
                    Map.of("expected", stored.issuer(), "received", issuer != null ? issuer : "none"));
        }

        String error = params.get("error");
        if (error != null && !error.isBlank()) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("error", error);
            body.put("error_description", params.get("error_description"));
            throw new OAuthException(error, params.get("error_description"), 400, body, Map.of());
        }

        String code = params.get("code");
        if (code == null || code.isBlank()) {
            throw new ValidationException("OAuth callback carries neither a code nor an error");
        }
        return code;
    }

    // ------------------------------------------------------------------ tokens

    /**
     * Exchange an authorization code for tokens ({@code POST /oauth/token},
     * {@code grant_type=authorization_code}).
     *
     * <p>Run this on your server: the code is single-use and expires <b>60 seconds</b> after
     * approval, and a confidential application's secret must never reach a browser. Every value
     * must match the authorization request exactly, or the API answers {@code invalid_grant}.
     *
     * <p>Request body:
     * <pre>{@code
     * {
     *   "grant_type": "authorization_code",
     *   "code": "def50200a1b2c3...",
     *   "redirect_uri": "https://myapp.com/oauth/callback",
     *   "code_verifier": "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk",
     *   "client_id": "cli_1a2b3c",
     *   "client_secret": "...",
     *   "resource": "https://api.assinafy.com.br"
     * }
     * }</pre>
     *
     * <p>Response — a flat object, <b>not</b> the API's usual envelope:
     * <pre>{@code
     * {
     *   "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "token_type": "Bearer",
     *   "expires_in": 3600,
     *   "scope": "documents:read documents:write",
     *   "refresh_token": "def5020088c2...",
     *   "id_token": "eyJraWQiOiJEQlR0S0..."
     * }
     * }</pre>
     *
     * <p>{@code refresh_token} is present only with {@link OAuthScope#OFFLINE_ACCESS} and
     * {@code id_token} only with {@link OAuthScope#OPENID}. Read
     * {@link OAuthTokens#getScope()} rather than assuming every requested permission was granted.
     *
     * @param client the application's credentials
     * @param code the code returned by
     *             {@link #readAuthorizationCallback(String, OAuthAuthorizationRequest)}
     * @param codeVerifier the verifier stored alongside the authorization request
     * @param redirectUri the same redirect URI that was authorized
     * @return the token set
     * @throws ValidationException if an argument is missing or malformed, or a 2xx response carries
     *         no {@code access_token}
     * @throws OAuthException {@code invalid_grant} for a spent, expired, replayed or mismatched
     *         code; {@code invalid_client} for bad application credentials; {@code invalid_target}
     *         for a {@code resource} mismatch
     */
    public OAuthTokens exchangeCode(OAuthClient client, String code, String codeVerifier, String redirectUri) {
        requireId(code, "Authorization code");
        requireCodeVerifier(codeVerifier);
        requireRedirectUri(redirectUri);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("grant_type", "authorization_code");
        body.put("code", code);
        body.put("redirect_uri", redirectUri);
        body.put("code_verifier", codeVerifier);
        applyClientAuth(body, client);
        applyResource(body);
        return requestToken("Failed to exchange the OAuth authorization code", body);
    }

    /**
     * Renew an access token ({@code POST /oauth/token}, {@code grant_type=refresh_token}).
     *
     * <p>Access tokens last one hour; refresh tokens exist only when
     * {@link OAuthScope#OFFLINE_ACCESS} was requested and granted.
     *
     * <p><b>Refresh tokens rotate.</b> Every call returns a new one and retires the one you sent,
     * and a replayed refresh token cannot be told apart from a stolen one — so the server ends the
     * entire connection and the user must reconnect. Therefore:
     * <ol>
     *   <li>persist {@link OAuthTokens#getRefreshToken()} before doing anything else with the
     *       response;</li>
     *   <li>treat a timeout as "it may have succeeded" and re-read your stored token instead of
     *       retrying blindly;</li>
     *   <li>never run two refreshes concurrently for one connection.</li>
     * </ol>
     *
     * <p>Refreshing does not extend the connection's 30-day life.
     *
     * <p>Request body:
     * <pre>{@code
     * {
     *   "grant_type": "refresh_token",
     *   "refresh_token": "def50200f1e2...",
     *   "client_id": "cli_1a2b3c",
     *   "client_secret": "...",
     *   "resource": "https://api.assinafy.com.br"
     * }
     * }</pre>
     *
     * <p>The response has the same shape as
     * {@link #exchangeCode(OAuthClient, String, String, String)}, with a <b>new</b>
     * {@code refresh_token}.
     *
     * @param client the application's credentials
     * @param refreshToken the current refresh token
     * @return a fresh token set
     * @throws ValidationException if an argument is missing, or a 2xx response carries no
     *         {@code access_token}
     * @throws OAuthException {@code invalid_grant} when the refresh token was already used, expired,
     *         or the user reconnected with different permissions — ask the user to reconnect;
     *         {@code invalid_client} for bad application credentials
     */
    public OAuthTokens refreshToken(OAuthClient client, String refreshToken) {
        requireId(refreshToken, "Refresh token");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("grant_type", "refresh_token");
        body.put("refresh_token", refreshToken);
        applyClientAuth(body, client);
        applyResource(body);
        return requestToken("Failed to refresh the OAuth access token", body);
    }

    /**
     * Revoke an access or refresh token ({@code POST /oauth/revoke}, RFC 7009).
     *
     * <p>Call this when a user disconnects your app, instead of only deleting your copy of the
     * token. Revoking a refresh token ends the whole connection.
     *
     * <p>Every token outcome answers {@code 200} — unknown, malformed and already-revoked included
     * — so the endpoint cannot be used to probe whether a token exists. Only failed client
     * authentication returns {@code 401}.
     *
     * <p>Request body:
     * <pre>{@code
     * {
     *   "token": "def50200f1e2...",
     *   "token_type_hint": "refresh_token",
     *   "client_id": "cli_1a2b3c",
     *   "client_secret": "..."
     * }
     * }</pre>
     *
     * <p>Response: {@code 200} with no payload.
     *
     * @param client the application's credentials
     * @param token the access or refresh token to revoke
     * @param tokenTypeHint optional {@code access_token} or {@code refresh_token} hint that lets the
     *                      server skip a lookup; {@code null} omits it
     * @throws ValidationException if {@code token} is blank, the client is absent, or
     *         {@code tokenTypeHint} is not one of the two documented values
     * @throws OAuthException {@code invalid_client} when client authentication fails
     */
    public void revokeToken(OAuthClient client, String token, String tokenTypeHint) {
        requireId(token, "Token");
        if (tokenTypeHint != null && !TOKEN_TYPE_HINTS.contains(tokenTypeHint)) {
            throw new ValidationException("Token type hint must be access_token or refresh_token");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("token", token);
        if (tokenTypeHint != null) body.put("token_type_hint", tokenTypeHint);
        applyClientAuth(body, client);

        String json = serialise(body);
        try {
            callVoid("Failed to revoke the OAuth token", () -> publicHttp.post("/oauth/revoke", json));
        } catch (RuntimeException failure) {
            throw OAuthException.upgrade(failure);
        }
    }

    /**
     * Read the OpenID Connect claims of the user who authorized this client's token
     * ({@code GET /oauth/userinfo}).
     *
     * <p>Build the client with the OAuth access token
     * ({@code AssinafyClientOptions.builder().token(tokens.getAccessToken())}) — this call uses the
     * client's configured credential.
     *
     * <p>Requires {@link OAuthScope#OPENID}; {@code name} additionally requires
     * {@link OAuthScope#PROFILE}, and {@code email}/{@code email_verified} require
     * {@link OAuthScope#EMAIL}. Per OIDC Core §5.3.2 the response is a flat claims object, not this
     * API's usual envelope.
     *
     * <p>Request body: none.
     *
     * <pre>{@code
     * {
     *   "sub": "d6zqpbyog2v3xvxerwn8la94",
     *   "name": "Maria Silva",
     *   "email": "maria@example.com",
     *   "email_verified": true
     * }
     * }</pre>
     *
     * @return the claims the granted scopes allow
     * @throws com.assinafy.sdk.exceptions.AuthenticationException {@code 401} when the token is
     *         missing, expired or revoked; {@code 403} when the {@code openid} scope was not
     *         granted, whose {@code WWW-Authenticate} header names the scope to reconnect with
     */
    public OAuthUserInfo userInfo() {
        return call("Failed to fetch OAuth userinfo", () -> http.get("/oauth/userinfo"), OAuthUserInfo.class);
    }

    // --------------------------------------------------------------- PKCE tools

    /**
     * Generate an RFC 7636 code verifier: 32 random bytes rendered as 43 base64url characters.
     *
     * <p>{@link #createAuthorizationUrl(AuthorizationUrlRequest)} calls this for you; use it
     * directly only when you mint the verifier yourself.
     *
     * @return a fresh code verifier
     */
    public static String createCodeVerifier() {
        return randomValue(32);
    }

    /**
     * Derive the RFC 7636 {@code S256} code challenge from a verifier.
     *
     * @param codeVerifier an RFC 7636 code verifier
     * @return the base64url-encoded SHA-256 challenge
     * @throws ValidationException if the verifier is outside the 43-128 unreserved-character grammar
     */
    public static String codeChallengeFor(String codeVerifier) {
        requireCodeVerifier(codeVerifier);
        try {
            return BASE64URL.encodeToString(MessageDigest.getInstance("SHA-256")
                    .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException e) {
            // Every Java platform is required to provide SHA-256.
            throw new AssinafyException("SHA-256 is unavailable on this JVM", Map.of(), e);
        }
    }

    // ------------------------------------------------------------------ internals

    private OAuthTokens requestToken(String label, Map<String, Object> body) {
        String json = serialise(body);
        OAuthTokens tokens;
        try {
            tokens = call(label, () -> publicHttp.post("/oauth/token", json), OAuthTokens.class);
        } catch (RuntimeException failure) {
            throw OAuthException.upgrade(failure);
        }
        if (tokens == null || tokens.getAccessToken() == null || tokens.getAccessToken().isBlank()) {
            throw new ValidationException(label + ": the token endpoint returned no access_token");
        }
        return tokens;
    }

    private void applyClientAuth(Map<String, Object> body, OAuthClient client) {
        if (client == null) throw new ValidationException("OAuth client credentials are required");
        body.put("client_id", client.clientId());
        if (client.clientSecret() != null) body.put("client_secret", client.clientSecret());
    }

    private void applyResource(Map<String, Object> body) {
        String resource = resourceIndicator();
        if (resource != null) body.put("resource", resource);
    }

    /**
     * The RFC 8707 resource indicator, which must be identical on the authorization and token
     * requests or the exchange fails with {@code invalid_target}. A loopback {@code http://} base
     * URL — the shape used by mock servers — has no valid resource identifier, so the parameter is
     * omitted rather than rejected.
     */
    private String resourceIndicator() {
        String origin = apiOrigin();
        return origin.startsWith("https:") ? origin : null;
    }

    private String defaultIssuer() {
        List<String> servers = protectedResourceMetadata().getAuthorizationServers();
        if (servers.isEmpty() || servers.get(0) == null || servers.get(0).isBlank()) {
            throw new ValidationException("Protected-resource metadata lists no authorization server");
        }
        return servers.get(0);
    }

    /**
     * Origin of the configured API host: the metadata documents and the resource indicator both sit
     * at the host root, while the base URL points at {@code /v1}.
     */
    private String apiOrigin() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new ValidationException("The client has no base URL to derive the API origin from");
        }
        try {
            URI uri = new URI(baseUrl);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new ValidationException("The client base URL is not an absolute URL");
            }
            return uri.getPort() >= 0
                    ? uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort()
                    : uri.getScheme() + "://" + uri.getHost();
        } catch (URISyntaxException e) {
            throw new ValidationException("The client base URL is not a valid URL");
        }
    }

    /** One credential-free transport per metadata origin, so repeated discovery reuses connections. */
    private ApiHttpClient transportFor(String origin) {
        return discovery.computeIfAbsent(origin, url -> new OkHttpApiClient(url, null, null, timeoutMs));
    }

    private static String joinScopes(List<OAuthScope> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            throw new ValidationException("At least one OAuth scope is required");
        }
        Set<String> unique = new LinkedHashSet<>();
        for (OAuthScope scope : scopes) {
            if (scope == null) throw new ValidationException("OAuth scopes must not contain null");
            unique.add(scope.getValue());
        }
        return String.join(" ", unique);
    }

    private static String requireRedirectUri(String value) {
        String uri = requireHttpsUrl(value, "Redirect URI");
        if (uri.indexOf('#') >= 0) {
            throw new ValidationException("Redirect URI must not contain a fragment");
        }
        return uri;
    }

    /**
     * Require an absolute HTTPS URL, allowing loopback HTTP so the same rule as the transport's
     * base URL applies to locally hosted test servers.
     */
    private static String requireHttpsUrl(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(name + " must be an absolute https URL");
        }
        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException e) {
            throw new ValidationException(name + " must be an absolute https URL");
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || host == null) {
            throw new ValidationException(name + " must be an absolute https URL");
        }
        boolean loopback = host.equals("localhost") || host.equals("127.0.0.1") || host.equals("[::1]");
        if (!scheme.equalsIgnoreCase("https") && !(scheme.equalsIgnoreCase("http") && loopback)) {
            throw new ValidationException(name + " must be an absolute https URL");
        }
        return value;
    }

    private static String requireCodeVerifier(String value) {
        if (value == null || !CODE_VERIFIER.matcher(value).matches()) {
            throw new ValidationException(
                    "Code verifier must be 43-128 characters from A-Z a-z 0-9 - . _ ~");
        }
        return value;
    }

    private static String randomValue(int bytes) {
        byte[] buffer = new byte[bytes];
        RANDOM.nextBytes(buffer);
        return BASE64URL.encodeToString(buffer);
    }

    private static String stripTrailingSlashes(String value) {
        if (value == null) return "";
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') end--;
        return value.substring(0, end);
    }

    /** Compare two {@code state} values without leaking their contents through timing. */
    private static boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8));
    }

    private static String queryString(Map<String, String> params) {
        List<String> pairs = new ArrayList<>(params.size());
        params.forEach((name, value) -> pairs.add(encode(name) + "=" + encode(value)));
        return String.join("&", pairs);
    }

    /** Accept a full callback URL or a bare {@code a=b&c=d} query, with or without a leading {@code ?}. */
    private static Map<String, String> parseQuery(String callbackUrlOrQuery) {
        if (callbackUrlOrQuery == null || callbackUrlOrQuery.isBlank()) {
            throw new ValidationException("OAuth callback query is required");
        }
        String query = callbackUrlOrQuery;
        int mark = query.indexOf('?');
        if (mark >= 0) query = query.substring(mark + 1);
        int fragment = query.indexOf('#');
        if (fragment >= 0) query = query.substring(0, fragment);

        Map<String, String> params = new HashMap<>();
        for (String pair : query.split("&")) {
            if (pair.isEmpty()) continue;
            int eq = pair.indexOf('=');
            String name = decode(eq >= 0 ? pair.substring(0, eq) : pair);
            String value = eq >= 0 ? decode(pair.substring(eq + 1)) : "";
            // OAuth defines no repeated parameters; the first value is the one the browser sent.
            params.putIfAbsent(name, value);
        }
        return params;
    }

    private static String decode(String value) {
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
