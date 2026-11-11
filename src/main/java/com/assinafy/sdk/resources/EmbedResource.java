package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.EmbedOrigin;
import com.assinafy.sdk.models.EmbedSession;
import com.assinafy.sdk.models.EmbedSessionExchange;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/** Operations for embed. */
public class EmbedResource extends BaseResource {
    /** Creates a resource.
     * @param http transport
     * @param defaultAccountId default workspace ID
     * @param logger optional logger
     */
    public EmbedResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    /** Creates a resource with no logger.
     * @param http transport
     * @param defaultAccountId default workspace ID
     */
    public EmbedResource(ApiHttpClient http, String defaultAccountId) { super(http, defaultAccountId); }

    /**
     * List embed origins.
     * <p><strong>HTTP:</strong> {@code GET /v1/accounts/{accountId}/embed-origins}.
     * List the origins allowed to embed the workspace&#x27;s signing pages, oldest first. Returns `403` with code `PlanRestricted` when the workspace&#x27;s plan does not include embedded signing. Not available to OAuth applications.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;origin&quot;: &quot;value&quot;, &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}]}</pre>
     * <p>Statuses: 200 The allowed origins; 403 Authenticated but not allowed to perform this action.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param accountId explicit workspace ID, or null for default
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public List<EmbedOrigin> listOrigins(String accountId) {
        String path = originsPath(accountId);
        return callList("Failed to list embed origins", () -> http.get(path), EmbedOrigin.class).getData();
    }

    /** Uses the default account. See {@link listOrigins(String)}.
     * @return the unwrapped result
     */
    public List<EmbedOrigin> listOrigins() { return listOrigins(null); }

    /**
     * Add embed origin.
     * <p><strong>HTTP:</strong> {@code POST /v1/accounts/{accountId}/embed-origins}.
     * Allow one more origin to embed the workspace&#x27;s signing pages. An origin is an exact `scheme://host[:port]`: `https` (or `http` for `localhost` and `127.0.0.1` only), with no path, query, fragment, credentials or wildcard. The host is lowercased and a trailing `/` and the default port are dropped. Returns `400` when the origin is invalid, already registered, or the workspace already has 10 origins. Returns `403` with code `PlanRestricted` when the workspace&#x27;s plan does not include embedded signing. Not available to OAuth applications.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: {&quot;origin&quot;: &quot;value&quot;}.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;id&quot;: &quot;value&quot;, &quot;origin&quot;: &quot;value&quot;, &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}}</pre>
     * <p>Statuses: 200 The origin added; 400 One or more fields failed validation.; 403 Authenticated but not allowed to perform this action.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param origin exact HTTPS origin; HTTP only for localhost or 127.0.0.1
     * @param accountId explicit workspace ID, or null for default
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public EmbedOrigin addOrigin(String origin, String accountId) {
        String path = originsPath(accountId);
        validateOrigin(origin);
        String json = serialise(Map.of("origin", origin));
        return call("Failed to add embed origin", () -> http.post(path, json), EmbedOrigin.class);
    }

    /** Uses the default account. See {@link addOrigin(String, String)}.
     * @param origin exact HTTPS origin; HTTP only for localhost or 127.0.0.1
     * @return the unwrapped result
     */
    public EmbedOrigin addOrigin(String origin) { return addOrigin(origin, null); }

    /**
     * Update embed origin.
     * <p><strong>HTTP:</strong> {@code PUT /v1/accounts/{accountId}/embed-origins/{originId}}.
     * Replace one allowed origin with another, keeping its `id`. The same rules as **Add embed origin** apply. Returns `400` when the new origin is invalid or another entry already has it. Returns `403` with code `PlanRestricted` when the workspace&#x27;s plan does not include embedded signing. Not available to OAuth applications.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: {&quot;origin&quot;: &quot;value&quot;}.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;id&quot;: &quot;value&quot;, &quot;origin&quot;: &quot;value&quot;, &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}}</pre>
     * <p>Statuses: 200 The updated origin; 400 One or more fields failed validation.; 403 Authenticated but not allowed to perform this action.; 404 The requested resource does not exist.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param originId registered origin ID
     * @param origin replacement exact origin
     * @param accountId explicit workspace ID, or null for default
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public EmbedOrigin updateOrigin(String originId, String origin, String accountId) {
        String path = originsPath(accountId) + "/" + pathSegment(originId, "Origin ID");
        validateOrigin(origin);
        String json = serialise(Map.of("origin", origin));
        return call("Failed to update embed origin", () -> http.put(path, json), EmbedOrigin.class);
    }

    /** Uses the default account. See {@link updateOrigin(String, String, String)}.
     * @param originId registered origin ID
     * @param origin replacement exact origin
     * @return the unwrapped result
     */
    public EmbedOrigin updateOrigin(String originId, String origin) { return updateOrigin(originId, origin, null); }

    /**
     * Remove embed origin.
     * <p><strong>HTTP:</strong> {@code DELETE /v1/accounts/{accountId}/embed-origins/{originId}}.
     * Stop allowing an origin to embed the workspace&#x27;s signing pages. Removing the last origin disables embedding: new sessions return `409` with code `embed_origins_missing`. Returns `403` with code `PlanRestricted` when the workspace&#x27;s plan does not include embedded signing. Not available to OAuth applications.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: []}</pre>
     * <p>Statuses: 200 Origin removed; 403 Authenticated but not allowed to perform this action.; 404 The requested resource does not exist.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param originId registered origin ID
     * @param accountId explicit workspace ID, or null for default
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public void deleteOrigin(String originId, String accountId) {
        String path = originsPath(accountId) + "/" + pathSegment(originId, "Origin ID");
        callVoid("Failed to remove embed origin", () -> http.delete(path));
    }

    /** Uses the default account. See {@link deleteOrigin(String, String)}.
     * @param originId registered origin ID
     */
    public void deleteOrigin(String originId) { deleteOrigin(originId, null); }

    /**
     * Create an embed session.
     * <p><strong>HTTP:</strong> {@code POST /v1/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/embed-sessions}.
     * Mint a single-use embed URL for a signer created with `embedded: true`. Load the URL in an iframe on one of the workspace&#x27;s embed origins within 5 minutes; opening it spends the token, so mint a fresh one each time the signer opens the page. The signer&#x27;s access code is never returned. Refused with `400` (`embed_not_enabled_for_signer`) for a signer that is not embedded, `409` when the signer already finished (`already_signed`), the document is still being uploaded or processed (`document_not_ready`: retry in a moment), the document can no longer be signed or the signer&#x27;s step has not started (`document_unavailable`), or the workspace has no embed origin (`embed_origins_missing`), `403` with code `PlanRestricted` when the plan does not include embedded signing, and `429` after 30 sessions for one signer in an hour.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;url&quot;: &quot;value&quot;, &quot;expires_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}}</pre>
     * <p>Statuses: 200 The embed session; 400 The request was rejected because of its current state or invalid input.; 403 Authenticated but not allowed to perform this action.; 404 The document, assignment or signer was not found.; 409 The signer can not sign right now (`already_signed`, `document_not_ready`, `document_unavailable`), or the workspace has no embed origin (`embed_origins_missing`).; 429 More than 30 sessions for this signer in the last hour.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param documentId document ID
     * @param assignmentId assignment ID
     * @param signerId embedded signer ID
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public EmbedSession createSession(String documentId, String assignmentId, String signerId) {
        String path = "/documents/" + pathSegment(documentId, "Document ID")
                + "/assignments/" + pathSegment(assignmentId, "Assignment ID")
                + "/signers/" + pathSegment(signerId, "Signer ID") + "/embed-sessions";
        return call("Failed to create embed session", () -> http.post(path, null), EmbedSession.class);
    }

    /**
     * Exchange an embed session.
     * <p><strong>HTTP:</strong> {@code POST /v1/embed/sessions/{token}/exchange}.
     * Called by Assinafy&#x27;s embed page, not by integrations. Public: the token is the credential. Trades a session token for the signer&#x27;s access code, once; the token is spent even when the call is refused with `409`. An unknown, expired or already used token returns `410` with code `embed_session_invalid`. Returns `409` when the signer or document can no longer sign.</p>
     * <p>Authentication: public token only.</p>
     * <p>Request: {&quot;parent_origin&quot;: &quot;value&quot;}.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;signer_access_code&quot;: &quot;value&quot;, &quot;signer_id&quot;: &quot;value&quot;, &quot;document_id&quot;: &quot;value&quot;, &quot;verification_method&quot;: &quot;value&quot;, &quot;embed_origins&quot;: [&quot;value&quot;]}}</pre>
     * <p>Statuses: 200 The signing session; 409 The signer or document can not sign right now (`already_signed`, `document_not_ready`, `document_unavailable`).; 410 Unknown, expired or already used token (`embed_session_invalid`).; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param token single-use session token; only the embed page should exchange it
     * @param parentOrigin optional origin hosting the iframe
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public EmbedSessionExchange exchangeSession(String token, String parentOrigin) {
        requireId(token, "Embed token");
        if (token.length() != 64) throw new ValidationException("Embed token must have 64 characters");
        String path = "/embed/sessions/" + pathSegment(token, "Embed token") + "/exchange";
        if (parentOrigin != null) validateOrigin(parentOrigin);
        String json = serialise(parentOrigin == null ? Map.of() : Map.of("parent_origin", parentOrigin));
        return call("Failed to exchange embed session", () -> http.post(path, json), EmbedSessionExchange.class);
    }

    private String originsPath(String account) {
        return "/accounts/" + pathSegment(accountId(account), "Account ID") + "/embed-origins";
    }

    private static void validateOrigin(String origin) {
        try {
            URI uri = new URI(origin == null ? "" : origin);
            String host = uri.getHost();
            boolean https = "https".equalsIgnoreCase(uri.getScheme());
            boolean localHttp = "http".equalsIgnoreCase(uri.getScheme())
                    && ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host));
            if ((!https && !localHttp) || host == null || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || (!uri.getRawPath().isEmpty() && !"/".equals(uri.getRawPath()))
                    || uri.getPort() > 65535 || uri.getPort() == 0) {
                throw new ValidationException("Origin must be an exact HTTPS origin without credentials, path, query or fragment");
            }
        } catch (URISyntaxException error) {
            throw new ValidationException("Invalid embed origin");
        }
    }
}
