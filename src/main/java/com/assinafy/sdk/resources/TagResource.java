package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Tag;
import com.assinafy.sdk.request.CreateTagRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.RenameTagRequest;

import java.util.Map;

/**
 * Tag resource — workspace-level CRUD for the reusable tags that can be attached to
 * documents and templates.
 *
 * <p>Maps to the {@code /accounts/{accountId}/tags} endpoints.
 */
public class TagResource extends BaseResource {

    public TagResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    public TagResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    public PaginatedResult<Tag> list() {
        return list(new ListParams(), null);
    }

    public PaginatedResult<Tag> list(ListParams params) {
        return list(params, null);
    }

    public PaginatedResult<Tag> list(ListParams params, String accountId) {
        String id = accountId(accountId);
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list tags",
                () -> http.get("/accounts/" + id + "/tags", queryParams),
                Tag.class);
    }

    public Tag create(CreateTagRequest request) {
        return create(request, null);
    }

    public Tag create(CreateTagRequest request, String accountId) {
        String id = accountId(accountId);
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new com.assinafy.sdk.exceptions.ValidationException("Tag name is required");
        }
        String body = serialise(request);
        logger.info("Creating tag", Map.of("name", request.getName()));
        return call("Failed to create tag",
                () -> http.post("/accounts/" + id + "/tags", body),
                Tag.class);
    }

    public Tag rename(String tagId, RenameTagRequest request) {
        return rename(tagId, request, null);
    }

    public Tag rename(String tagId, RenameTagRequest request, String accountId) {
        String id = accountId(accountId);
        String tid = requireId(tagId, "Tag ID");
        String body = serialise(request);
        return call("Failed to rename tag",
                () -> http.put("/accounts/" + id + "/tags/" + tid, body),
                Tag.class);
    }

    /** Delete a tag. Equivalent to {@link #delete(String, boolean)} with {@code force = false}. */
    public void delete(String tagId) {
        delete(tagId, false, null);
    }

    /**
     * Delete a tag. When the tag is still attached to documents the API responds with
     * 409 Conflict unless {@code force} is {@code true}, in which case it is detached
     * from every document and then deleted.
     */
    public void delete(String tagId, boolean force) {
        delete(tagId, force, null);
    }

    public void delete(String tagId, boolean force, String accountId) {
        String id = accountId(accountId);
        String tid = requireId(tagId, "Tag ID");
        String path = "/accounts/" + id + "/tags/" + tid + (force ? "?force=true" : "");
        callVoid("Failed to delete tag", () -> http.delete(path));
    }
}
