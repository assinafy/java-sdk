package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Template;
import com.assinafy.sdk.models.TemplateListItem;
import com.assinafy.sdk.request.ListParams;

public class TemplateResource extends BaseResource {

    public TemplateResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    public TemplateResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    public PaginatedResult<TemplateListItem> list() {
        return list(new ListParams(), null);
    }

    public PaginatedResult<TemplateListItem> list(ListParams params) {
        return list(params, null);
    }

    public PaginatedResult<TemplateListItem> list(ListParams params, String accountId) {
        String id = accountId(accountId);
        return callList("Failed to list templates",
                () -> http.get("/accounts/" + id + "/templates", params != null ? params.toQueryParams() : null),
                TemplateListItem.class);
    }

    public Template get(String templateId) {
        return get(templateId, null);
    }

    public Template get(String templateId, String accountId) {
        String id = accountId(accountId);
        String tmplId = requireId(templateId, "Template ID");
        return call("Failed to fetch template",
                () -> http.get("/accounts/" + id + "/templates/" + tmplId),
                Template.class);
    }
}
