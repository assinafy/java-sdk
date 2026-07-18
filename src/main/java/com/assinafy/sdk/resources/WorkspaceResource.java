package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.AccountTheme;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Workspace;
import com.assinafy.sdk.models.WorkspaceListItem;
import com.assinafy.sdk.request.CreateWorkspaceRequest;
import com.assinafy.sdk.request.UpdateWorkspaceRequest;

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

    public WorkspaceResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    public WorkspaceResource(ApiHttpClient http) {
        super(http);
    }

    /** Create a workspace account ({@code POST /accounts}). */
    public Workspace create(CreateWorkspaceRequest request) {
        String json = serialise(request);
        return call("Failed to create workspace", () -> http.post("/accounts", json), Workspace.class);
    }

    /** List the workspaces the authenticated user can access ({@code GET /accounts}). */
    public PaginatedResult<WorkspaceListItem> list() {
        return callList("Failed to list workspaces", () -> http.get("/accounts"), WorkspaceListItem.class);
    }

    /** Fetch a workspace's profile ({@code GET /accounts/{accountId}}). */
    public Workspace get(String accountId) {
        String id = requireId(accountId, "Account ID");
        return call("Failed to fetch workspace", () -> http.get("/accounts/" + id), Workspace.class);
    }

    /** Update a workspace's profile ({@code PUT /accounts/{accountId}}). */
    public Workspace update(String accountId, UpdateWorkspaceRequest request) {
        String id = requireId(accountId, "Account ID");
        String json = serialise(request);
        return call("Failed to update workspace", () -> http.put("/accounts/" + id, json), Workspace.class);
    }

    /**
     * Delete a workspace ({@code DELETE /accounts/{accountId}}). Equivalent to
     * {@link #delete(String, boolean)} with {@code force = false}: the server responds with 400
     * (listing the blockers under {@code restrictions}) if the workspace has an active paid
     * subscription.
     */
    public void delete(String accountId) {
        delete(accountId, false);
    }

    /**
     * Delete a workspace. When {@code force} is {@code true} the API cancels any active paid
     * subscription on the workspace and proceeds with immediate deletion; this sends the documented
     * {@code {"force": true}} request body. The default ({@code force = false}) path issues a plain
     * bodyless DELETE (the historical, verified behaviour), which the server already treats as
     * {@code force = false}.
     */
    public void delete(String accountId, boolean force) {
        String id = requireId(accountId, "Account ID");
        if (force) {
            callVoid("Failed to delete workspace", () -> http.delete("/accounts/" + id, "{\"force\":true}"));
        } else {
            callVoid("Failed to delete workspace", () -> http.delete("/accounts/" + id));
        }
    }

    /**
     * Get a workspace's branding theme ({@code GET /accounts/{accountId}/theme}): display name,
     * primary/secondary colours and the logo URL.
     */
    public AccountTheme getTheme(String accountId) {
        String id = requireId(accountId, "Account ID");
        return call("Failed to fetch account theme", () -> http.get("/accounts/" + id + "/theme"), AccountTheme.class);
    }

    /**
     * Download the workspace logo image bytes ({@code GET /accounts/{accountId}/logo}). Throws
     * {@link com.assinafy.sdk.exceptions.ApiException} (404) when no logo has been uploaded.
     */
    public byte[] downloadLogo(String accountId) {
        String id = requireId(accountId, "Account ID");
        return callBinary("Failed to download account logo", () -> http.getBinary("/accounts/" + id + "/logo"));
    }

    /**
     * Upload (replace) the workspace logo ({@code POST /accounts/{accountId}/logo}, multipart
     * {@code file}). The image content type is auto-detected (PNG/JPEG/GIF) from the bytes.
     */
    public void uploadLogo(String accountId, byte[] imageData, String fileName) {
        String id = requireId(accountId, "Account ID");
        if (imageData == null || imageData.length == 0) {
            throw new com.assinafy.sdk.exceptions.ValidationException("Logo image data is empty");
        }
        String name = fileName != null ? fileName : "logo";
        String contentType = detectImageContentType(imageData);
        callVoid("Failed to upload account logo",
                () -> http.postFile("/accounts/" + id + "/logo", "file", name, imageData, contentType));
    }

    /** Remove the workspace logo ({@code DELETE /accounts/{accountId}/logo}). */
    public void deleteLogo(String accountId) {
        String id = requireId(accountId, "Account ID");
        callVoid("Failed to delete account logo", () -> http.delete("/accounts/" + id + "/logo"));
    }

    private static String detectImageContentType(byte[] data) {
        if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (data.length >= 4 && (data[0] & 0xFF) == 0x47 && data[1] == 'I' && data[2] == 'F') {
            return "image/gif";
        }
        return "image/png";
    }
}
