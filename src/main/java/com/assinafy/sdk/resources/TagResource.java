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
     * @return paginated tags
     */
    public PaginatedResult<Tag> list() {
        return list(new ListParams(), null);
    }

    /**
     * List tags for the default account.
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
     * @param request tag name and optional color
     * @return the created tag
     */
    public Tag create(CreateTagRequest request) {
        return create(request, null);
    }

    /**
     * Create a tag ({@code POST /accounts/{id}/tags}).
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
     * @param tagId tag ID
     * @param request fields to update; {@code null} sends an empty object
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the updated tag
     */
    public Tag rename(String tagId, RenameTagRequest request, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String tid = pathSegment(tagId, "Tag ID");
        // Build the body explicitly so the documented tri-state for `color` is honoured:
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
     * @param tagId tag ID
     * @param force whether to detach the tag from resources before deletion
     */
    public void delete(String tagId, boolean force) {
        delete(tagId, force, null);
    }

    /**
     * Delete a tag from an explicit or default account.
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
