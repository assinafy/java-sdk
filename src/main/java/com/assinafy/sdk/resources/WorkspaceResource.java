package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.AccountTheme;
import com.assinafy.sdk.models.DocumentStatsRow;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Workspace;
import com.assinafy.sdk.request.CreateWorkspaceRequest;
import com.assinafy.sdk.request.UpdateWorkspaceRequest;

import java.util.List;
import java.util.Map;

/**
 * Workspace (account) resource — maps to the {@code /accounts} endpoints.
 *
 * <p>All of {@link #list()}, {@link #get(String)}, {@link #create(CreateWorkspaceRequest)},
 * {@link #update(String, UpdateWorkspaceRequest)}, {@link #delete(String)} and the theme/logo
 * operations are part of the documented Accounts API reference. Treat {@link #delete(String)}
 * with care — it removes a real workspace; pass {@code force = true} to also cancel any active
 * paid subscription that would otherwise block deletion.
 */
public class WorkspaceResource extends BaseResource {

    /**
     * Create account operations with diagnostic logging.
     *
     * @param http HTTP transport
     * @param defaultAccountId retained constructor context; operations use explicit account IDs
     * @param logger diagnostic logger
     */
    public WorkspaceResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    /**
     * Create account operations with no-op logging.
     *
     * @param http HTTP transport
     */
    public WorkspaceResource(ApiHttpClient http) {
        super(http);
    }

    /**
     * Create a workspace account ({@code POST /accounts}).
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/accounts</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "name": "Acme Inc.",
     *   "notification_sender_type": "Account"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "account",
     *     "id": "6401df46d6a6b0c692d9ec49",
     *     "name": "Acme Inc.",
     *     "primary_color": null,
     *     "secondary_color": null,
     *     "notification_sender_type": "User",
     *     "roles": [
     *       "roles_example"
     *     ],
     *     "is_delete_allowed": true,
     *     "created_at": "2026-06-03T03:54:16Z"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The created account; 400 One or more fields failed validation.;
     * 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param request required account name and optional notification-sender type
     * @return the created account
     */
    public Workspace create(CreateWorkspaceRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("Workspace name is required");
        }
        validateSenderType(request.getNotificationSenderType());
        String json = serialise(request);
        return call("Failed to create workspace", () -> http.post("/accounts", json), Workspace.class);
    }

    /**
     * List the workspaces the authenticated user can access ({@code GET /accounts}).
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     {
     *       "resource": "account",
     *       "id": "6401df46d6a6b0c692d9ec49",
     *       "name": "Acme Inc.",
     *       "primary_color": null,
     *       "secondary_color": null,
     *       "notification_sender_type": "User",
     *       "roles": [
     *         "roles_example"
     *       ],
     *       "is_delete_allowed": true,
     *       "created_at": "2026-06-03T03:54:16Z"
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The user&#x27;s accounts; 401 Missing or invalid credentials.;
     * 500 Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @return paginated accessible accounts
     */
    public PaginatedResult<Workspace> list() {
        return callList("Failed to list workspaces", () -> http.get("/accounts"), Workspace.class);
    }

    /**
     * Fetch a workspace's profile ({@code GET /accounts/{accountId}}).
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts/{accountId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "account",
     *     "id": "6401df46d6a6b0c692d9ec49",
     *     "name": "Acme Inc.",
     *     "primary_color": null,
     *     "secondary_color": null,
     *     "notification_sender_type": "User",
     *     "roles": [
     *       "roles_example"
     *     ],
     *     "is_delete_allowed": true,
     *     "created_at": "2026-06-03T03:54:16Z"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The account; 404 The requested resource does not exist.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param accountId account ID
     * @return the account
     */
    public Workspace get(String accountId) {
        String id = pathSegment(accountId, "Account ID");
        return call("Failed to fetch workspace", () -> http.get("/accounts/" + id), Workspace.class);
    }

    /**
     * Update a workspace's profile ({@code PUT /accounts/{accountId}}).
     *
     * <p><strong>HTTP:</strong> <code>PUT /v1/accounts/{accountId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "name": "Acme Inc.",
     *   "notification_sender_type": "Account"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "account",
     *     "id": "6401df46d6a6b0c692d9ec49",
     *     "name": "Acme Inc.",
     *     "primary_color": null,
     *     "secondary_color": null,
     *     "notification_sender_type": "User",
     *     "roles": [
     *       "roles_example"
     *     ],
     *     "is_delete_allowed": true,
     *     "created_at": "2026-06-03T03:54:16Z"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The updated account; 400 One or more fields failed validation.;
     * 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param accountId account ID
     * @param request fields to update
     * @return the updated account
     */
    public Workspace update(String accountId, UpdateWorkspaceRequest request) {
        String id = pathSegment(accountId, "Account ID");
        if (request == null) throw new ValidationException("Workspace update is required");
        validateSenderType(request.getNotificationSenderType());
        String json = serialise(request);
        return call("Failed to update workspace", () -> http.put("/accounts/" + id, json), Workspace.class);
    }

    /**
     * Delete a workspace ({@code DELETE /accounts/{accountId}}). Equivalent to
     * {@link #delete(String, boolean)} with {@code force = false}: the server responds with 400
     * (listing the blockers under {@code restrictions}) if the workspace has an active paid
     * subscription.
     *
     * <p>Wire contract, payloads and failures: {@link #delete(String, boolean)}.</p>
     *
     * @param accountId account ID
     */
    public void delete(String accountId) {
        delete(accountId, false);
    }

    /**
     * Delete a workspace. When {@code force} is {@code true} the API cancels any active paid
     * subscription on the workspace and proceeds with immediate deletion; this sends the documented
     * {@code {"force": true}} request body. The default ({@code force = false}) path issues a plain
     * bodyless DELETE, which the server treats as {@code force = false}.
     *
     * <p><strong>HTTP:</strong> <code>DELETE /v1/accounts/{accountId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "force": false
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     []
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Account deleted; 400 Deletion blocked by active restrictions.
     * Each `restrictions` entry describes one blocker; resolve them individually, or retry with `force:
     * true` to cancel blocking subscriptions/documents automatically.; 404 The requested resource does
     * not exist.; 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or
     * numeric envelope statuses raise <code>ApiException</code>; 401/403 raise
     * <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O failures
     * raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * @param accountId account ID
     * @param force whether to cancel a blocking paid subscription and force deletion
     */
    public void delete(String accountId, boolean force) {
        String id = pathSegment(accountId, "Account ID");
        if (force) {
            callVoid("Failed to delete workspace", () -> http.delete("/accounts/" + id, "{\"force\":true}"));
        } else {
            callVoid("Failed to delete workspace", () -> http.delete("/accounts/" + id));
        }
    }

    /**
     * Get a workspace's branding theme ({@code GET /accounts/{accountId}/theme}): display name,
     * primary/secondary colours and the logo URL.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts/{accountId}/theme</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "account_name": "Account Name",
     *     "primary_color": "aabbcc",
     *     "secondary_color": null,
     *     "logo": "https://api.assinafy.com.br/v1/accounts/1a/logo"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The theme; 401 Missing or invalid credentials.; 500 Unexpected
     * server error. Non-2xx HTTP or numeric envelope statuses raise <code>ApiException</code>; 401/403
     * raise <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O
     * failures raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * @param accountId account ID
     * @return the account theme
     */
    public AccountTheme getTheme(String accountId) {
        String id = pathSegment(accountId, "Account ID");
        return call("Failed to fetch account theme", () -> http.get("/accounts/" + id + "/theme"), AccountTheme.class);
    }

    /**
     * Return the latest 12 zero-filled monthly document KPI rows for an account.
     *
     * <p>Wire contract, payloads and failures: {@link #stats(String, String, String)}.</p>
     *
     * @param accountId account ID
     * @return monthly document statistics
     */
    public List<DocumentStatsRow> stats(String accountId) {
        return stats(accountId, "monthly", null);
    }

    /**
     * {@code GET /accounts/{accountId}/stats} — return document funnel KPIs. Each row is
     * {@code {period, documents_uploaded, documents_sent, signature_requests,
     * signature_requests_notification_email, signature_requests_notification_whatsapp,
     * signature_requests_notification_bypass, signature_requests_verification_email,
     * signature_requests_verification_whatsapp, signature_requests_verification_bypass,
     * signature_requests_verification_digital_certificate, signature_requests_viewed,
     * signature_requests_completed, documents_certified}}.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts/{accountId}/stats</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>granularity</code> (query, optional): <code>monthly</code> (default) or <code>daily</code>.</li>
     * <li><code>month</code> (query, optional): Target month <code>YYYY-MM</code> (required when <code>granularity=daily</code>).</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     {
     *       "period": "2026-06",
     *       "documents_uploaded": 42,
     *       "documents_sent": 37,
     *       "signature_requests": 61,
     *       "signature_requests_notification_email": 55,
     *       "signature_requests_notification_whatsapp": 18,
     *       "signature_requests_notification_bypass": 3,
     *       "signature_requests_verification_email": 48,
     *       "signature_requests_verification_whatsapp": 6,
     *       "signature_requests_verification_bypass": 3,
     *       "signature_requests_verification_digital_certificate": 4,
     *       "signature_requests_viewed": 44,
     *       "signature_requests_completed": 52,
     *       "documents_certified": 30
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 KPI series; 400 One or more fields failed validation.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param accountId account whose statistics to return
     * @param granularity {@code monthly} or {@code daily}
     * @param month required for daily data, in {@code YYYY-MM} form
     * @return document statistics rows
     */
    public List<DocumentStatsRow> stats(String accountId, String granularity, String month) {
        String id = pathSegment(accountId, "Account ID");
        Map<String, Object> query = statsQuery(granularity, month);
        return callList("Failed to fetch account statistics",
                () -> http.get("/accounts/" + id + "/stats", query),
                DocumentStatsRow.class).getData();
    }

    /**
     * Download the workspace logo image bytes ({@code GET /accounts/{accountId}/logo}). Throws
     * {@link com.assinafy.sdk.exceptions.ApiException} (404) when no logo has been uploaded.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts/{accountId}/logo</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>image/*</code>, raw artifact bytes.</p>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The logo image; 404 The requested resource does not exist.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param accountId account ID
     * @return logo image bytes
     */
    public byte[] downloadLogo(String accountId) {
        String id = pathSegment(accountId, "Account ID");
        return callBinary("Failed to download account logo", () -> http.getBinary("/accounts/" + id + "/logo"));
    }

    /**
     * Upload (replace) the workspace logo ({@code POST /accounts/{accountId}/logo}, multipart
     * {@code file}). The image content type is auto-detected (PNG/JPEG/GIF) from the bytes.
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/accounts/{accountId}/logo</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * </ul>
     * <p>Request body: <code>multipart/form-data</code>. Illustrative payload with the documented
     * fields; optional fields may be absent or null.</p>
     * <pre>{
     *   "file": "file.pdf (binary bytes)"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": ""
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Logo updated; 400 One or more fields failed validation.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param accountId account ID
     * @param imageData non-empty PNG, JPEG, or GIF bytes
     * @param fileName uploaded filename, or {@code null} to use {@code logo}
     */
    public void uploadLogo(String accountId, byte[] imageData, String fileName) {
        String id = pathSegment(accountId, "Account ID");
        if (imageData == null || imageData.length == 0) {
            throw new ValidationException("Logo image data is empty");
        }
        String name = fileName != null ? fileName : "logo";
        String contentType = detectImageContentType(imageData);
        callVoid("Failed to upload account logo",
                () -> http.postFile("/accounts/" + id + "/logo", "file", name, imageData, contentType));
    }

    /**
     * Remove the workspace logo ({@code DELETE /accounts/{accountId}/logo}).
     *
     * <p><strong>HTTP:</strong> <code>DELETE /v1/accounts/{accountId}/logo</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": ""
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Logo deleted; 401 Missing or invalid credentials.; 500 Unexpected
     * server error. Non-2xx HTTP or numeric envelope statuses raise <code>ApiException</code>; 401/403
     * raise <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O
     * failures raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * @param accountId account ID
     */
    public void deleteLogo(String accountId) {
        String id = pathSegment(accountId, "Account ID");
        callVoid("Failed to delete account logo", () -> http.delete("/accounts/" + id + "/logo"));
    }

    private static String detectImageContentType(byte[] data) {
        if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (data.length >= 4 && (data[0] & 0xFF) == 0x47 && data[1] == 'I' && data[2] == 'F') {
            return "image/gif";
        }
        if (data.length >= 4 && (data[0] & 0xff) == 0x89
                && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') return "image/png";
        throw new ValidationException("Logo image must be PNG, JPEG, or GIF");
    }

    private static void validateSenderType(String value) {
        if (value != null && !value.equals("User") && !value.equals("Account")) {
            throw new ValidationException("Notification sender type must be User or Account");
        }
    }
}
