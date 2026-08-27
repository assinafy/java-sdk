package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Template;
import com.assinafy.sdk.models.TemplateListItem;
import com.assinafy.sdk.request.ListParams;

import java.util.Map;

/** Lists account templates and provides an optional single-template deployment lookup. */
public class TemplateResource extends BaseResource {

    /**
     * Create template operations bound to a default account and logger.
     *
     * @param http HTTP transport
     * @param defaultAccountId default account ID
     * @param logger diagnostic logger
     */
    public TemplateResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    /**
     * Create template operations bound to a default account.
     *
     * @param http HTTP transport
     * @param defaultAccountId default account ID
     */
    public TemplateResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    /**
     * List templates for the default account.
     *
     * @return paginated templates
     */
    public PaginatedResult<TemplateListItem> list() {
        return list(new ListParams(), null);
    }

    /**
     * List templates for the default account.
     *
     * @param params paging and filter options; {@code null} sends no query parameters
     * @return paginated templates
     */
    public PaginatedResult<TemplateListItem> list(ListParams params) {
        return list(params, null);
    }

    /**
     * List templates for an explicit or default account ({@code GET /accounts/{id}/templates}).
     *
     * @param params paging and filter options; {@code null} sends no query parameters
     * @param accountId explicit account ID, or {@code null} for the default
     * @return paginated templates
     */
    public PaginatedResult<TemplateListItem> list(ListParams params, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list templates",
                () -> http.get("/accounts/" + id + "/templates", queryParams),
                TemplateListItem.class);
    }

    /**
     * Fetch a single template by ID.
     *
     * <p>The single-template deployment route may be unavailable; confirm support before relying
     * on it.
     *
     * @param templateId template ID
     * @return the template
     */
    public Template get(String templateId) {
        return get(templateId, null);
    }

    /**
     * Fetch one template through an optional deployment route.
     *
     * @param templateId template ID
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the template
     */
    public Template get(String templateId, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String tmplId = pathSegment(templateId, "Template ID");
        return call("Failed to fetch template",
                () -> http.get("/accounts/" + id + "/templates/" + tmplId),
                Template.class);
    }
}
