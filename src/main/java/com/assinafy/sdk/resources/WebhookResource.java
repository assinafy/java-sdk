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

public class WebhookResource extends BaseResource {

    private static final List<String> DEFAULT_EVENTS = List.of(
            "document_ready",
            "document_prepared",
            "signer_signed_document",
            "signer_rejected_document",
            "document_processing_failed"
    );

    public WebhookResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    public WebhookResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    public WebhookResource(ApiHttpClient http) {
        super(http);
    }

    public WebhookSubscription register(RegisterWebhookRequest request) {
        return register(request, null);
    }

    public WebhookSubscription register(RegisterWebhookRequest request, String accountId) {
        if (request.getUrl() == null || request.getUrl().isBlank()) {
            throw new ValidationException("Webhook URL is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ValidationException("Webhook email is required");
        }
        String id = accountId(accountId);
        Map<String, Object> body = new HashMap<>();
        body.put("url", request.getUrl());
        body.put("email", request.getEmail());
        body.put("events", (request.getEvents() != null && !request.getEvents().isEmpty()) ? request.getEvents() : DEFAULT_EVENTS);
        body.put("is_active", request.getIsActive() != null ? request.getIsActive() : true);
        logger.info("Registering webhook", Map.of("url", request.getUrl()));
        String json = serialise(body);
        return call("Failed to register webhook", () -> http.put("/accounts/" + id + "/webhooks/subscriptions", json), WebhookSubscription.class);
    }

    public WebhookSubscription get() {
        return get(null);
    }

    public WebhookSubscription get(String accountId) {
        String id = accountId(accountId);
        return callOptional("Failed to fetch webhook subscription", () -> http.get("/accounts/" + id + "/webhooks/subscriptions"), WebhookSubscription.class);
    }

    /**
     * @deprecated The {@code DELETE /accounts/{id}/webhooks/subscriptions} route is not served
     * by the live API (it returns 404). Use {@link #inactivate()} to stop webhook delivery.
     * Retained because the API reference lists the verb on the subscription object.
     */
    @Deprecated
    public void delete() {
        delete(null);
    }

    /**
     * @deprecated see {@link #delete()} — prefer {@link #inactivate(String)}.
     */
    @Deprecated
    public void delete(String accountId) {
        String id = accountId(accountId);
        logger.info("Deleting webhook subscription");
        callVoid("Failed to delete webhook subscription", () -> http.delete("/accounts/" + id + "/webhooks/subscriptions"));
    }

    public WebhookSubscription inactivate() {
        return inactivate(null);
    }

    public WebhookSubscription inactivate(String accountId) {
        String id = accountId(accountId);
        logger.info("Inactivating webhook subscription");
        return call("Failed to inactivate webhook subscription", () -> http.put("/accounts/" + id + "/webhooks/inactivate", null), WebhookSubscription.class);
    }

    public List<WebhookEventTypeInfo> listEventTypes() {
        return callList("Failed to list webhook event types", () -> http.get("/webhooks/event-types"), WebhookEventTypeInfo.class).getData();
    }

    public PaginatedResult<WebhookDispatch> listDispatches() {
        return listDispatches(new ListParams(), null);
    }

    public PaginatedResult<WebhookDispatch> listDispatches(ListParams params) {
        return listDispatches(params, null);
    }

    public PaginatedResult<WebhookDispatch> listDispatches(ListParams params, String accountId) {
        String id = accountId(accountId);
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list webhook dispatches", () -> http.get("/accounts/" + id + "/webhooks", queryParams), WebhookDispatch.class);
    }

    public WebhookDispatch retryDispatch(String dispatchId) {
        return retryDispatch(dispatchId, null);
    }

    public WebhookDispatch retryDispatch(String dispatchId, String accountId) {
        String id = accountId(accountId);
        String did = requireId(dispatchId, "Dispatch ID");
        return call("Failed to retry webhook dispatch", () -> http.post("/accounts/" + id + "/webhooks/" + did + "/retry", null), WebhookDispatch.class);
    }
}
