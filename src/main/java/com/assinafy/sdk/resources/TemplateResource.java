package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Template;
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
     * <p>Wire contract, payloads and failures: {@link #list(ListParams, String)}.</p>
     *
     * @return paginated templates
     */
    public PaginatedResult<Template> list() {
        return list(new ListParams(), null);
    }

    /**
     * List templates for the default account.
     *
     * <p>Wire contract, payloads and failures: {@link #list(ListParams, String)}.</p>
     *
     * @param params paging and filter options; {@code null} sends no query parameters
     * @return paginated templates
     */
    public PaginatedResult<Template> list(ListParams params) {
        return list(params, null);
    }

    /**
     * List templates for an explicit or default account ({@code GET /accounts/{id}/templates}).
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts/{accountId}/templates</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>search</code> (query, optional): Search term.</li>
     * <li><code>page</code> (query, optional): Page number.</li>
     * <li><code>per-page</code> (query, optional): Records per page (max 100).</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     {
     *       "resource": "template",
     *       "id": "fa88b732db84d01427d4cdd1092",
     *       "name": "template.pdf",
     *       "document_name": null,
     *       "message": null,
     *       "status": "ready",
     *       "pages": [
     *         {
     *           "id": "id_example",
     *           "number": 1,
     *           "height": 2100,
     *           "width": 1275,
     *           "download_url": "download_url_example",
     *           "fields": [
     *             {
     *               "id": "id_example",
     *               "field_id": "field_id_example",
     *               "role_id": "role_id_example",
     *               "label": "label_example",
     *               "display_settings": null,
     *               "created_at": "2030-10-05T12:00:00Z",
     *               "updated_at": "2030-10-05T12:00:00Z"
     *             }
     *           ]
     *         }
     *       ],
     *       "roles": [
     *         {
     *           "id": "id_example",
     *           "name": "Editor",
     *           "assignment_type": "Editor",
     *           "created_at": "2030-10-05T12:00:00Z",
     *           "updated_at": "2030-10-05T12:00:00Z"
     *         }
     *       ],
     *       "tags": [
     *         {
     *           "id": "id_example",
     *           "name": "name_example"
     *         }
     *       ],
     *       "default_document_tags": [
     *         {
     *           "id": "id_example",
     *           "name": "name_example"
     *         }
     *       ],
     *       "created_at": "2030-10-05T12:00:00Z",
     *       "updated_at": "2030-10-05T12:00:00Z"
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 A page of templates (default_document_tags omitted in the list);
     * 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param params paging and filter options; {@code null} sends no query parameters
     * @param accountId explicit account ID, or {@code null} for the default
     * @return paginated templates
     */
    public PaginatedResult<Template> list(ListParams params, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list templates",
                () -> http.get("/accounts/" + id + "/templates", queryParams),
                Template.class);
    }

    /**
     * Fetch a single template by ID from the default account.
     *
     * <p>Wire payloads and HTTP failures follow {@link #list(ListParams, String)}. The GET response has
     * one template in <code>data</code> instead of the list array; request body: none.</p>
     *
     * @param templateId template ID
     * @return the template
     */
    public Template get(String templateId) {
        return get(templateId, null);
    }

    /**
     * Fetch one template ({@code GET /accounts/{accountId}/templates/{templateId}}).
     *
     * <p><b>Deployment note.</b> This route is live on production and sandbox but is absent from
     * the published OpenAPI document, so it carries no compatibility promise. Use
     * {@link #list(com.assinafy.sdk.request.ListParams, String)} when you need a contract the
     * reference guarantees.
     *
     * <p>Wire payloads and HTTP failures follow {@link #list(ListParams, String)}. The GET response has
     * one template in <code>data</code> instead of the list array; request body: none.</p>
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
