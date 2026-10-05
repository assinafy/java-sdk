package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.AuthUser;
import com.assinafy.sdk.models.DocumentStatsRow;
import com.assinafy.sdk.models.NotificationPreferences;
import com.assinafy.sdk.util.ResponseHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Authenticated-user settings documented under {@code /users/self}. */
public class UserResource extends BaseResource {

    private static final Set<String> NOTIFICATION_CODES = Set.of(
            "DocumentCompleted", "SignerDeclined", "DocumentCancelled",
            "DocumentAboutToExpire", "DocumentExpired", "DocumentExpirationReset",
            "DocumentProcessingFailed", "TemplateProcessingFailed", "SignerWhatsappFailed"
    );

    /**
     * Create authenticated-user operations with a logger.
     *
     * @param http HTTP transport
     * @param logger diagnostic logger
     */
    public UserResource(ApiHttpClient http, Logger logger) { super(http, null, logger); }

    /**
     * Create authenticated-user operations with no-op logging.
     *
     * @param http HTTP transport
     */
    public UserResource(ApiHttpClient http) { super(http); }

    /**
     * {@code GET /users/self} — return the authenticated user profile:
     * {@code {id, name, email, telephone, government_id, is_email_verified,
     * has_accepted_terms, created_at, to_be_deleted_at}}.
     *
     * <p>Both {@code data: {user, accounts}} and direct user-data response shapes are normalized to
     * {@link AuthUser}.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/users/self</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "id": "bgjazeo5r9v2lq7l36dx48np",
     *     "name": "John Smith",
     *     "email": "example@example.invalid",
     *     "telephone": null,
     *     "government_id": null,
     *     "is_email_verified": false,
     *     "has_accepted_terms": true,
     *     "created_at": "2023-03-03T11:51:34Z",
     *     "to_be_deleted_at": null
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The current user; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @return the authenticated user profile
     */
    public AuthUser get() {
        Map<String, Object> data = callMap("Failed to fetch authenticated user",
                () -> http.get("/users/self"));
        Object user = data != null && data.containsKey("user") ? data.get("user") : data;
        if (!(user instanceof Map<?, ?> map) || map.isEmpty()) {
            throw new AssinafyException("Authenticated user response is empty");
        }
        return ResponseHandler.convert(user, AuthUser.class);
    }

    /**
     * Return the latest monthly document KPIs across all accessible accounts.
     *
     * <p>Wire contract, payloads and failures: {@link #stats(String, String)}.</p>
     *
     * @return the latest 12 zero-filled monthly rows
     */
    public List<DocumentStatsRow> stats() {
        return stats("monthly", null);
    }

    /**
     * {@code GET /users/self/stats} — return cross-account document KPIs. Each row is
     * {@code {period, documents_uploaded, documents_sent, signature_requests,
     * signature_requests_notification_email, signature_requests_notification_whatsapp,
     * signature_requests_notification_bypass, signature_requests_verification_email,
     * signature_requests_verification_whatsapp, signature_requests_verification_bypass,
     * signature_requests_verification_digital_certificate, signature_requests_viewed,
     * signature_requests_completed, documents_certified}}.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/users/self/stats</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
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
     * @param granularity {@code monthly} or {@code daily}
     * @param month required for daily data, in {@code YYYY-MM} form
     * @return cross-account document KPI rows
     * @throws ValidationException if the granularity or month is invalid
     */
    public List<DocumentStatsRow> stats(String granularity, String month) {
        Map<String, Object> query = statsQuery(granularity, month);
        return callList("Failed to fetch user statistics",
                () -> http.get("/users/self/stats", query),
                DocumentStatsRow.class).getData();
    }

    /**
     * {@code GET /users/self/notification-preferences} — return all nine owner-facing document
     * email switches. Account/security emails are not configurable and are not included.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/users/self/notification-preferences</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "DocumentCompleted": true,
     *     "SignerDeclined": true,
     *     "DocumentCancelled": true,
     *     "DocumentAboutToExpire": true,
     *     "DocumentExpired": true,
     *     "DocumentExpirationReset": true,
     *     "DocumentProcessingFailed": true,
     *     "TemplateProcessingFailed": true,
     *     "SignerWhatsappFailed": true
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The current preferences; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @return all current preferences; {@code true} means the email is enabled
     */
    public NotificationPreferences getNotificationPreferences() {
        return call("Failed to fetch notification preferences",
                () -> http.get("/users/self/notification-preferences"),
                NotificationPreferences.class);
    }

    /**
     * {@code PUT /users/self/notification-preferences} — merge selected email switches into the
     * current user's preferences. Omitted keys keep their existing values.
     *
     * <p><strong>HTTP:</strong> <code>PUT /v1/users/self/notification-preferences</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "DocumentCompleted": true,
     *   "SignerDeclined": true,
     *   "DocumentCancelled": true,
     *   "DocumentAboutToExpire": true,
     *   "DocumentExpired": true,
     *   "DocumentExpirationReset": true,
     *   "DocumentProcessingFailed": true,
     *   "TemplateProcessingFailed": true,
     *   "SignerWhatsappFailed": true
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "DocumentCompleted": true,
     *     "SignerDeclined": true,
     *     "DocumentCancelled": true,
     *     "DocumentAboutToExpire": true,
     *     "DocumentExpired": true,
     *     "DocumentExpirationReset": true,
     *     "DocumentProcessingFailed": true,
     *     "TemplateProcessingFailed": true,
     *     "SignerWhatsappFailed": true
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The updated preferences; 400 One or more fields failed
     * validation.; 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or
     * numeric envelope statuses raise <code>ApiException</code>; 401/403 raise
     * <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O failures
     * raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * @param changes map containing only {@code DocumentCompleted},
     *                {@code SignerDeclined}, {@code DocumentCancelled},
     *                {@code DocumentAboutToExpire}, {@code DocumentExpired},
     *                {@code DocumentExpirationReset}, {@code DocumentProcessingFailed},
     *                {@code TemplateProcessingFailed}, or {@code SignerWhatsappFailed}
     * @return the complete updated preference map
     * @throws ValidationException if the map is null or contains an unknown key or null value
     */
    public NotificationPreferences updateNotificationPreferences(Map<String, Boolean> changes) {
        if (changes == null) throw new ValidationException("Notification preferences are required");
        Map<String, Boolean> body = new LinkedHashMap<>();
        changes.forEach((code, enabled) -> {
            if (!NOTIFICATION_CODES.contains(code) || enabled == null) {
                throw new ValidationException("Unknown notification preference or null value: " + code);
            }
            body.put(code, enabled);
        });
        return call("Failed to update notification preferences",
                () -> http.put("/users/self/notification-preferences", serialise(body)),
                NotificationPreferences.class);
    }
}
