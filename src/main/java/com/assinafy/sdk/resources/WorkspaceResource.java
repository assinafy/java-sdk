package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Workspace;
import com.assinafy.sdk.models.WorkspaceListItem;
import com.assinafy.sdk.request.CreateWorkspaceRequest;
import com.assinafy.sdk.request.UpdateWorkspaceRequest;

public class WorkspaceResource extends BaseResource {

    public WorkspaceResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    public WorkspaceResource(ApiHttpClient http) {
        super(http);
    }

    public Workspace create(CreateWorkspaceRequest request) {
        String json = serialise(request);
        return call("Failed to create workspace", () -> http.post("/accounts", json), Workspace.class);
    }

    public PaginatedResult<WorkspaceListItem> list() {
        return callList("Failed to list workspaces", () -> http.get("/accounts"), WorkspaceListItem.class);
    }

    public Workspace get(String accountId) {
        String id = requireId(accountId, "Account ID");
        return call("Failed to fetch workspace", () -> http.get("/accounts/" + id), Workspace.class);
    }

    public Workspace update(String accountId, UpdateWorkspaceRequest request) {
        String id = requireId(accountId, "Account ID");
        String json = serialise(request);
        return call("Failed to update workspace", () -> http.put("/accounts/" + id, json), Workspace.class);
    }

    public void delete(String accountId) {
        String id = requireId(accountId, "Account ID");
        callVoid("Failed to delete workspace", () -> http.delete("/accounts/" + id));
    }
}
