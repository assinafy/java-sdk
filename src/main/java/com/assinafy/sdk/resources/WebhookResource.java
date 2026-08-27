package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.WebhookDispatch;
import com.assinafy.sdk.models.WebhookEventTypeInfo;
import com.assinafy.sdk.models.WebhookSubscription;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.RegisterWebhookRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;

/** Manages account webhook subscriptions, event types, delivery history, and retries. */
public class WebhookResource extends BaseResource {

    private static final List<String> DEFAULT_EVENTS = List.of(
            "document_ready",
            "document_prepared",
            "signer_signed_document",
            "signer_rejected_document",
            "document_processing_failed"
    );

    /**
     * Create webhook operations bound to a default account and logger.
     *
     * @param http HTTP transport
     * @param defaultAccountId default account ID
     * @param logger diagnostic logger
     */
    public WebhookResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    /**
     * Create webhook operations bound to a default account.
     *
     * @param http HTTP transport
     * @param defaultAccountId default account ID
     */
    public WebhookResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    /**
     * Create webhook operations without a default account.
     *
     * @param http HTTP transport
     */
    public WebhookResource(ApiHttpClient http) {
        super(http);
    }

    /**
     * Create or update the account's webhook subscription
     * ({@code PUT /accounts/{accountId}/webhooks/subscriptions}). {@code url} and {@code email} are
     * required. Convenience defaults are applied: when {@code events} is null/empty the SDK
     * subscribes to {@code document_ready}, {@code document_prepared},
     * {@code signer_signed_document}, {@code signer_rejected_document} and
     * {@code document_processing_failed}; when {@code isActive} is null it defaults to
     * {@code true}. Pass explicit values to override either default.
     *
     * @param request subscription URL, email, events, and active state
     * @return the created or updated subscription
     */
    public WebhookSubscription register(RegisterWebhookRequest request) {
        return register(request, null);
    }

    /**
     * Create or update a subscription for an explicit or default account.
     *
     * @param request subscription URL, email, events, and active state
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the created or updated subscription
     */
    public WebhookSubscription register(RegisterWebhookRequest request, String accountId) {
        if (request == null) throw new ValidationException("Webhook request is required");
        if (request.getUrl() == null || request.getUrl().isBlank()) {
            throw new ValidationException("Webhook URL is required");
        }
        try {
            URI url = URI.create(request.getUrl());
            if (!(url.getScheme() != null && (url.getScheme().equalsIgnoreCase("http")
                    || url.getScheme().equalsIgnoreCase("https"))
                    && url.getHost() != null)) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Webhook URL must be an absolute HTTP(S) URL");
        }
        requireEmail(request.getEmail());
        String id = pathSegment(accountId(accountId), "Account ID");
        Map<String, Object> body = new HashMap<>();
        body.put("url", request.getUrl());
        body.put("email", request.getEmail());
        body.put("events", (request.getEvents() != null && !request.getEvents().isEmpty()) ? request.getEvents() : DEFAULT_EVENTS);
        body.put("is_active", request.getIsActive() != null ? request.getIsActive() : true);
        logInfo("Registering webhook", Map.of());
        String json = serialise(body);
        return call("Failed to register webhook", () -> http.put("/accounts/" + id + "/webhooks/subscriptions", json), WebhookSubscription.class);
    }

    /**
     * Fetch the default account's subscription.
     *
     * @return the subscription, or {@code null} when no subscription exists
     */
    public WebhookSubscription get() {
        return get(null);
    }

    /**
     * Fetch an account subscription ({@code GET /accounts/{id}/webhooks/subscriptions}).
     *
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the subscription, or {@code null} when no subscription exists
     */
    public WebhookSubscription get(String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        return callOptional("Failed to fetch webhook subscription", () -> http.get("/accounts/" + id + "/webhooks/subscriptions"), WebhookSubscription.class);
    }

    /**
     * @deprecated This deployment-specific route may be unavailable. Use {@link #inactivate()}
     * to stop webhook delivery.
     */
    @Deprecated
    public void delete() {
        delete(null);
    }

    /**
     * @deprecated see {@link #delete()} — prefer {@link #inactivate(String)}.
     *
     * @param accountId explicit account ID, or {@code null} for the default
     */
    @Deprecated
    public void delete(String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        logInfo("Deleting webhook subscription", Map.of());
        callVoid("Failed to delete webhook subscription", () -> http.delete("/accounts/" + id + "/webhooks/subscriptions"));
    }

    /**
     * Inactivate the default account's subscription without deleting it.
     *
     * @return the inactive subscription
     */
    public WebhookSubscription inactivate() {
        return inactivate(null);
    }

    /**
     * Inactivate an account subscription ({@code PUT /accounts/{id}/webhooks/inactivate}).
     *
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the inactive subscription
     */
    public WebhookSubscription inactivate(String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        logInfo("Inactivating webhook subscription", Map.of());
        return call("Failed to inactivate webhook subscription", () -> http.put("/accounts/" + id + "/webhooks/inactivate", null), WebhookSubscription.class);
    }

    /**
     * List webhook event types ({@code GET /webhooks/event-types}).
     *
     * @return supported event types
     */
    public List<WebhookEventTypeInfo> listEventTypes() {
        return callList("Failed to list webhook event types", () -> http.get("/webhooks/event-types"), WebhookEventTypeInfo.class).getData();
    }

    /**
     * List webhook delivery history ({@code GET /accounts/{id}/webhooks}). Besides paging, the
     * endpoint supports the {@code event}, {@code delivered}, {@code from} and {@code to} filters;
     * pass them via {@link ListParams.Builder#extra(String, Object)} using those exact keys, e.g.
     * {@code ListParams.builder().extra("delivered", false).extra("event", "document_ready").build()}.
     *
     * @return paginated delivery history for the default account
     */
    public PaginatedResult<WebhookDispatch> listDispatches() {
        return listDispatches(new ListParams(), null);
    }

    /**
     * List delivery history for the default account.
     *
     * @param params paging and webhook filters; {@code null} sends no query parameters
     * @return paginated delivery history
     */
    public PaginatedResult<WebhookDispatch> listDispatches(ListParams params) {
        return listDispatches(params, null);
    }

    /**
     * List delivery history for an explicit or default account.
     *
     * @param params paging and webhook filters; {@code null} sends no query parameters
     * @param accountId explicit account ID, or {@code null} for the default
     * @return paginated delivery history
     */
    public PaginatedResult<WebhookDispatch> listDispatches(ListParams params, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list webhook dispatches", () -> http.get("/accounts/" + id + "/webhooks", queryParams), WebhookDispatch.class);
    }

    /**
     * Retry one delivery in the default account.
     *
     * @param dispatchId delivery-history ID
     * @return the new dispatch entry
     */
    public WebhookDispatch retryDispatch(String dispatchId) {
        return retryDispatch(dispatchId, null);
    }

    /**
     * Retry one delivery ({@code POST /accounts/{id}/webhooks/{dispatchId}/retry}).
     *
     * @param dispatchId delivery-history ID
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the new dispatch entry
     */
    public WebhookDispatch retryDispatch(String dispatchId, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String did = pathSegment(dispatchId, "Dispatch ID");
        return call("Failed to retry webhook dispatch", () -> http.post("/accounts/" + id + "/webhooks/" + did + "/retry", null), WebhookDispatch.class);
    }
}
