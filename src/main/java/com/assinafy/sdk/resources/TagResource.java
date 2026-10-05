package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Tag;
import com.assinafy.sdk.request.CreateTagRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.RenameTagRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tag resource — workspace-level CRUD for the reusable tags that can be attached to
 * documents and templates.
 *
 * <p>Maps to the {@code /accounts/{accountId}/tags} endpoints.
 */
public class TagResource extends BaseResource {

    /**
     * Create tag operations bound to a default account and logger.
     *
     * @param http HTTP transport
     * @param defaultAccountId default account ID
     * @param logger diagnostic logger
     */
    public TagResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    /**
     * Create tag operations bound to a default account.
     *
     * @param http HTTP transport
     * @param defaultAccountId default account ID
     */
    public TagResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    /**
     * List tags for the default account.
     *
     * <p>Wire contract, payloads and failures: {@link #list(ListParams, String)}.</p>
     *
     * @return paginated tags
     */
    public PaginatedResult<Tag> list() {
        return list(new ListParams(), null);
    }

    /**
     * List tags for the default account.
     *
     * <p>Wire contract, payloads and failures: {@link #list(ListParams, String)}.</p>
     *
     * @param params paging and filter options; {@code null} sends no query parameters
     * @return paginated tags
     */
    public PaginatedResult<Tag> list(ListParams params) {
        return list(params, null);
    }

    /**
     * List tags for an explicit or default account ({@code GET /accounts/{id}/tags}).
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts/{accountId}/tags</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>search</code> (query, optional): Search term.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     {
     *       "resource": "tag",
     *       "id": "fa8c09f3e709a8a1c82d69b1454",
     *       "name": "Contracts",
     *       "color": null,
     *       "created_at": "2026-05-14T12:00:00Z",
     *       "updated_at": "2026-05-14T12:00:00Z"
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The workspace tags; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param params paging and filter options; {@code null} sends no query parameters
     * @param accountId explicit account ID, or {@code null} for the default
     * @return paginated tags
     */
    public PaginatedResult<Tag> list(ListParams params, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list tags",
                () -> http.get("/accounts/" + id + "/tags", queryParams),
                Tag.class);
    }

    /**
     * Create a tag in the default account.
     *
     * <p>Wire contract, payloads and failures: {@link #create(CreateTagRequest, String)}.</p>
     *
     * @param request tag name and optional color
     * @return the created tag
     */
    public Tag create(CreateTagRequest request) {
        return create(request, null);
    }

    /**
     * Create a tag ({@code POST /accounts/{id}/tags}).
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/accounts/{accountId}/tags</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "name": "Contracts",
     *   "color": null
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "tag",
     *     "id": "fa8c09f3e709a8a1c82d69b1454",
     *     "name": "Contracts",
     *     "color": null,
     *     "created_at": "2026-05-14T12:00:00Z",
     *     "updated_at": "2026-05-14T12:00:00Z"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The created tag; 400 One or more fields failed validation.; 409 A
     * tag with the same name already exists.; 401 Missing or invalid credentials.; 500 Unexpected server
     * error. Non-2xx HTTP or numeric envelope statuses raise <code>ApiException</code>; 401/403 raise
     * <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O failures
     * raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * @param request tag name and optional color
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the created tag
     */
    public Tag create(CreateTagRequest request, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("Tag name is required");
        }
        String body = serialise(request);
        logInfo("Creating tag", Map.of());
        return call("Failed to create tag",
                () -> http.post("/accounts/" + id + "/tags", body),
                Tag.class);
    }

    /**
     * Update a tag in the default account.
     *
     * <p>Wire contract, payloads and failures: {@link #rename(String, RenameTagRequest, String)}.</p>
     *
     * @param tagId tag ID
     * @param request fields to update; {@code null} sends an empty object
     * @return the updated tag
     */
    public Tag rename(String tagId, RenameTagRequest request) {
        return rename(tagId, request, null);
    }

    /**
     * Update a tag ({@code PUT /accounts/{id}/tags/{tagId}}).
     *
     * <p><strong>HTTP:</strong> <code>PUT /v1/accounts/{accountId}/tags/{tagId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>tagId</code> (path, required): The tag ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "name": "Signed Contracts",
     *   "color": null
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "tag",
     *     "id": "fa8c09f3e709a8a1c82d69b1454",
     *     "name": "Contracts",
     *     "color": null,
     *     "created_at": "2026-05-14T12:00:00Z",
     *     "updated_at": "2026-05-14T12:00:00Z"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The updated tag; 400 One or more fields failed validation.; 404
     * The requested resource does not exist.; 401 Missing or invalid credentials.; 500 Unexpected server
     * error. Non-2xx HTTP or numeric envelope statuses raise <code>ApiException</code>; 401/403 raise
     * <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O failures
     * raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * @param tagId tag ID
     * @param request fields to update; {@code null} sends an empty object
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the updated tag
     */
    public Tag rename(String tagId, RenameTagRequest request, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String tid = pathSegment(tagId, "Tag ID");
        // Build the body explicitly so the documented tri-state for </code>color</code> is honoured:
        // omit = leave unchanged, value = set, explicit null (clearColor) = clear.
        Map<String, Object> payload = new LinkedHashMap<>();
        if (request != null) {
            if (request.getName() != null) payload.put("name", request.getName());
            if (request.getColor() != null) {
                payload.put("color", request.getColor());
            } else if (request.isClearColor()) {
                payload.put("color", null);
            }
        }
        String body = serialise(payload);
        return call("Failed to rename tag",
                () -> http.put("/accounts/" + id + "/tags/" + tid, body),
                Tag.class);
    }

    /**
     * Delete a tag. Equivalent to {@link #delete(String, boolean)} with {@code force = false}.
     *
     * <p>Wire contract, payloads and failures: {@link #delete(String, boolean, String)}.</p>
     *
     * @param tagId tag ID
     */
    public void delete(String tagId) {
        delete(tagId, false, null);
    }

    /**
     * Delete a tag. When the tag is still attached to documents the API responds with
     * 409 Conflict unless {@code force} is {@code true}, in which case it is detached
     * from every document and then deleted.
     *
     * <p>Wire contract, payloads and failures: {@link #delete(String, boolean, String)}.</p>
     *
     * @param tagId tag ID
     * @param force whether to detach the tag from resources before deletion
     */
    public void delete(String tagId, boolean force) {
        delete(tagId, force, null);
    }

    /**
     * Delete a tag from an explicit or default account.
     *
     * <p><strong>HTTP:</strong> <code>DELETE /v1/accounts/{accountId}/tags/{tagId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>tagId</code> (path, required): The tag ID.</li>
     * <li><code>force</code> (query, optional): Detach from resources before deleting.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "deleted": true
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Tag deleted; 404 The requested resource does not exist.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param tagId tag ID
     * @param force whether to detach the tag from resources before deletion
     * @param accountId explicit account ID, or {@code null} for the default
     */
    public void delete(String tagId, boolean force, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String tid = pathSegment(tagId, "Tag ID");
        String path = "/accounts/" + id + "/tags/" + tid + (force ? "?force=true" : "");
        callVoid("Failed to delete tag", () -> http.delete(path));
    }
}
