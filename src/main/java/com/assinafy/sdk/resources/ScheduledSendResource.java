package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.ScheduledSend;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.CreateEnvelopeRequest;
import com.assinafy.sdk.request.ListParams;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** Deferred assignments and envelopes. All routes require documents:write for OAuth, including listing.
 * Available on staging. Scheduling does not charge; the API revalidates and charges at send time.
 * Mutations have no idempotency key and must not be replayed after an uncertain response.
 */
public class ScheduledSendResource extends BaseResource {
    /** Creates scheduled-send operations.
     * @param http transport
     * @param defaultAccountId default workspace ID
     * @param logger optional logger
     */
    public ScheduledSendResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    /** Creates scheduled-send operations without a logger.
     * @param http transport
     * @param defaultAccountId default workspace ID
     */
    public ScheduledSendResource(ApiHttpClient http, String defaultAccountId) { super(http, defaultAccountId); }

    /** Schedule an assignment. Available on staging.
     * <p><strong>HTTP:</strong> {@code POST /v1/documents/{documentId}/scheduled-sends}. Keeps a <strong>Create assignment</strong> request and makes it at <code>send_at</code>, as the member who scheduled it, through the same checks and charges as <strong>Create assignment</strong> then. Nothing is sent or charged now, and nothing changes for the document: it can still be sent at once, and the scheduled send then fails when it goes. A document takes one pending scheduled send at a time, counting one being sent. The request is checked now as <strong>Estimate assignment cost</strong> checks it (the method, each signer&#x27;s methods, the plan&#x27;s features), and each signer named must be a contact of the workspace; the balance is checked when it is sent. A job checks every five minutes and makes the sends that are due. A send that can&#x27;t be made then becomes <code>failed</code> with the reason, nothing is charged, and the member who scheduled it is e-mailed; if that member has left the workspace, it is not made as anyone else: it fails and the workspace&#x27;s active owners are e-mailed. <strong>List scheduled sends</strong>, <strong>Cancel a scheduled send</strong> and <strong>Send a scheduled send now</strong> follow it.</p>
     * <p>Request:</p><pre>{&quot;send_at&quot;:&quot;2026-12-01T12:00:00Z&quot;,&quot;method&quot;:&quot;virtual&quot;,&quot;signers&quot;:[{&quot;id&quot;:&quot;signer-id&quot;,&quot;verification_method&quot;:&quot;Email&quot;,&quot;notification_methods&quot;:[],&quot;sign_as&quot;:&quot;Witness&quot;}],&quot;message&quot;:&quot;Please sign&quot;,&quot;expires_at&quot;:&quot;2026-12-08T12:00:00Z&quot;,&quot;copy_receivers&quot;:[],&quot;max_total_credits&quot;:1}</pre>
     * <p>Success:</p><pre>{&quot;status&quot;:200,&quot;data&quot;:{&quot;resource&quot;:&quot;scheduled_send&quot;,&quot;id&quot;:&quot;send-id&quot;,&quot;type&quot;:&quot;assignment&quot;,&quot;document_ids&quot;:[&quot;document-id&quot;],&quot;status&quot;:&quot;pending&quot;,&quot;send_at&quot;:&quot;2026-12-01T12:00:00Z&quot;,&quot;assignment_id&quot;:null,&quot;envelope_id&quot;:null,&quot;failure_reason&quot;:null,&quot;created_at&quot;:&quot;2026-11-30T12:00:00Z&quot;,&quot;processed_at&quot;:null}}</pre>
     * <p>200: The scheduled send; 400: <code>send_at</code> missing, unreadable, less than an hour or more than 90 days ahead; an <code>expires_at</code> less than two hours after it; the document already has an assignment, or a scheduled send pending or being sent; or the request refused as <strong>Estimate assignment cost</strong> refuses it.; 403: The workspace&#x27;s plan lacks a verification or notification method sent (<code>PlanRestricted</code>), or an OAuth app&#x27;s token lacks <code>documents:write</code> (<code>InsufficientScope</code>); also a deactivated membership or a role.; 404: The document is not in the workspace, or a signer named is not a contact of it.; 401: Missing or invalid credentials.; 500: Unexpected server error. HTTP/envelope errors raise ApiException; I/O errors raise NetworkException.</p>
     * @param documentId unassigned document ID
     * @param request ordinary assignment request; collect entries use the same shape as AssignmentResource.create
     * @param sendAt send instant, one hour to 90 days ahead; use Instant.ofEpochSecond for Unix seconds
     * @return unwrapped response data
     * @throws ValidationException if local arguments are invalid
     */
    public ScheduledSend createAssignment(String documentId, CreateAssignmentRequest request, Instant sendAt) {
        String path = "/documents/" + pathSegment(documentId, "Document ID") + "/scheduled-sends";
        validateSendAt(sendAt);
        Map<String, Object> body = AssignmentResource.buildAssignmentPayload(request, false);
        validateDeadline(request.getExpiresAt(), sendAt);
        body.put("send_at", sendAt.toString());
        String json = serialise(body);
        return call("Failed to schedule assignment", () -> http.post(path, json), ScheduledSend.class);
    }

    /** Schedule an envelope. Available on staging.
     * <p><strong>HTTP:</strong> {@code POST /v1/accounts/{accountId}/scheduled-sends}. Keeps a <strong>Create envelope</strong> request and makes it at <code>send_at</code>, as the member who scheduled it, through the same checks and charges as <strong>Create envelope</strong> then. Nothing is created, sent or charged now, and nothing changes for its documents: each can still be sent at once, and the scheduled send then fails when it goes. A document takes one pending scheduled send at a time, counting one being sent. The request is checked now as <strong>Estimate envelope cost</strong> checks it (<code>403</code> with code <code>PlanRestricted</code> without the Envelopes feature, <code>400</code> with <code>errors</code> on the same fields); the balance is checked when it is sent. A job checks every five minutes and makes the sends that are due; failures go as for <strong>Schedule an assignment</strong>: <code>failed</code> with the reason, nothing charged, the member who scheduled it e-mailed, or the workspace&#x27;s active owners if that member has left. An <code>Idempotency-Key</code> is not read here: each call keeps one send. An assignment is scheduled on its document (<strong>Schedule an assignment</strong>).</p>
     * <p>Request:</p><pre>{&quot;send_at&quot;:&quot;2026-12-01T12:00:00Z&quot;,&quot;name&quot;:&quot;Agreement&quot;,&quot;message&quot;:&quot;Please sign&quot;,&quot;signers&quot;:[{&quot;id&quot;:&quot;signer-id&quot;,&quot;verification_method&quot;:&quot;Email&quot;,&quot;notification_methods&quot;:[]}],&quot;documents&quot;:[{&quot;id&quot;:&quot;document-id&quot;,&quot;method&quot;:&quot;virtual&quot;,&quot;signer_ids&quot;:[&quot;signer-id&quot;],&quot;expires_at&quot;:&quot;2026-12-08T12:00:00Z&quot;,&quot;copy_receiver_ids&quot;:[]}],&quot;max_total_credits&quot;:1}</pre>
     * <p>Success:</p><pre>{&quot;status&quot;:200,&quot;data&quot;:{&quot;resource&quot;:&quot;scheduled_send&quot;,&quot;id&quot;:&quot;send-id&quot;,&quot;type&quot;:&quot;envelope&quot;,&quot;document_ids&quot;:[&quot;document-id&quot;],&quot;status&quot;:&quot;pending&quot;,&quot;send_at&quot;:&quot;2026-12-01T12:00:00Z&quot;,&quot;assignment_id&quot;:null,&quot;envelope_id&quot;:null,&quot;failure_reason&quot;:null,&quot;created_at&quot;:&quot;2026-11-30T12:00:00Z&quot;,&quot;processed_at&quot;:null}}</pre>
     * <p>200: The scheduled send; 400: <code>send_at</code> missing, unreadable, less than an hour or more than 90 days ahead; a document&#x27;s <code>expires_at</code> less than two hours after it; a document already assigned or in a scheduled send pending or being sent; or the request refused as <strong>Estimate envelope cost</strong> refuses it, in <code>errors</code>.; 403: The workspace&#x27;s plan lacks the Envelopes feature, or a signer&#x27;s method or embedded signing the plan lacks (<code>PlanRestricted</code>); or an OAuth app&#x27;s token lacks <code>documents:write</code> (<code>InsufficientScope</code>). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404: The requested resource does not exist.; 401: Missing or invalid credentials.; 500: Unexpected server error. HTTP/envelope errors raise ApiException; I/O errors raise NetworkException.</p>
     * @param request ordinary envelope request, including any credit ceiling
     * @param sendAt send instant, one hour to 90 days ahead; use Instant.ofEpochSecond for Unix seconds
     * @param accountId explicit workspace ID, or null for default
     * @return unwrapped response data
     * @throws ValidationException if local arguments are invalid
     */
    public ScheduledSend createEnvelope(CreateEnvelopeRequest request, Instant sendAt, String accountId) {
        String path = sendsPath(accountId);
        validateSendAt(sendAt);
        Map<String, Object> body = new EnvelopeResource(http, defaultAccountId, logger).envelopePayload(request, false);
        for (var document : request.getDocuments()) validateDeadline(document.getExpiresAt(), sendAt);
        body.put("send_at", sendAt.toString());
        String json = serialise(body);
        return call("Failed to schedule envelope", () -> http.post(path, json), ScheduledSend.class);
    }

    /** Uses the default account. See {@link #createEnvelope(CreateEnvelopeRequest, Instant, String)}.
     * @param request envelope request
     * @param sendAt instant one hour to 90 days ahead
     * @return scheduled envelope state
     */
    public ScheduledSend createEnvelope(CreateEnvelopeRequest request, Instant sendAt) {
        return createEnvelope(request, sendAt, null);
    }

    /** List scheduled sends. Available on staging.
     * <p><strong>HTTP:</strong> {@code GET /v1/accounts/{accountId}/scheduled-sends}. The workspace&#x27;s scheduled sends, assignments and envelopes, newest first, whatever their status. Paged: 20 per page by default; the <code>X-Pagination-Total-Count</code>, <code>X-Pagination-Page-Count</code>, <code>X-Pagination-Current-Page</code> and <code>X-Pagination-Per-Page</code> headers describe the full list. An OAuth app needs <code>documents:write</code> here, not only <code>documents:read</code>: a failed send&#x27;s <code>failure_reason</code> can say the workspace&#x27;s balance ran out or a member left, which an app learns only from a request it can make.</p>
     * <p>Request:</p><pre>query: page=1&amp;per-page=20&amp;document_id=document-id</pre>
     * <p>Success:</p><pre>{&quot;status&quot;:200,&quot;data&quot;:[{&quot;resource&quot;:&quot;scheduled_send&quot;,&quot;id&quot;:&quot;send-id&quot;,&quot;type&quot;:&quot;assignment&quot;,&quot;document_ids&quot;:[&quot;document-id&quot;],&quot;status&quot;:&quot;pending&quot;,&quot;send_at&quot;:&quot;2026-12-01T12:00:00Z&quot;,&quot;assignment_id&quot;:null,&quot;envelope_id&quot;:null,&quot;failure_reason&quot;:null,&quot;created_at&quot;:&quot;2026-11-30T12:00:00Z&quot;,&quot;processed_at&quot;:null}]}</pre>
     * <p>200: The workspace&#x27;s scheduled sends; 400: <code>document_id</code> not text.; 403: An OAuth app&#x27;s token lacks <code>documents:write</code> (<code>InsufficientScope</code>). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404: The requested resource does not exist.; 401: Missing or invalid credentials.; 500: Unexpected server error. HTTP/envelope errors raise ApiException; I/O errors raise NetworkException.</p>
     * @param params optional pagination; per-page is at most 50
     * @param documentId optional filter for sends containing this document
     * @param accountId explicit workspace ID, or null for default
     * @return unwrapped response data
     * @throws ValidationException if local arguments are invalid
     */
    public PaginatedResult<ScheduledSend> list(ListParams params, String documentId, String accountId) {
        String path = sendsPath(accountId);
        Map<String, Object> query = new HashMap<>(params == null ? Map.of() : params.toQueryParams());
        if (query.get("per-page") instanceof Integer size && size > 50) {
            throw new ValidationException("Scheduled-send page size must not exceed 50");
        }
        if (documentId != null) query.put("document_id", requireId(documentId, "Document ID"));
        return callList("Failed to list scheduled sends", () -> http.get(path, query), ScheduledSend.class);
    }

    /** Uses the default account. See {@link #list(ListParams, String, String)}.
     * @param params optional pagination
     * @param documentId optional document filter
     * @return scheduled sends and pagination
     */
    public PaginatedResult<ScheduledSend> list(ListParams params, String documentId) { return list(params, documentId, null); }

    /** Lists the first page in the default account. See {@link #list(ListParams, String, String)}.
     * @return scheduled sends and pagination
     */
    public PaginatedResult<ScheduledSend> list() { return list(null, null, null); }

    /** Cancel a scheduled send. Available on staging.
     * <p><strong>HTTP:</strong> {@code PUT /v1/accounts/{accountId}/scheduled-sends/{scheduledSendId}/cancel}. Cancels a <code>pending</code> scheduled send; nothing is sent or charged. A send already being sent, sent, failed or cancelled answers <code>409</code>.</p>
     * <p>Request:</p><pre>none</pre>
     * <p>Success:</p><pre>{&quot;status&quot;:200,&quot;data&quot;:{&quot;resource&quot;:&quot;scheduled_send&quot;,&quot;id&quot;:&quot;send-id&quot;,&quot;type&quot;:&quot;assignment&quot;,&quot;document_ids&quot;:[&quot;document-id&quot;],&quot;status&quot;:&quot;cancelled&quot;,&quot;send_at&quot;:&quot;2026-12-01T12:00:00Z&quot;,&quot;assignment_id&quot;:null,&quot;envelope_id&quot;:null,&quot;failure_reason&quot;:null,&quot;created_at&quot;:&quot;2026-11-30T12:00:00Z&quot;,&quot;processed_at&quot;:null}}</pre>
     * <p>200: The cancelled scheduled send; 403: An OAuth app&#x27;s token lacks <code>documents:write</code> (<code>InsufficientScope</code>). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404: The workspace does not exist, or the scheduled send is not the workspace&#x27;s.; 409: Only a <code>pending</code> scheduled send can be cancelled.; 401: Missing or invalid credentials.; 500: Unexpected server error. HTTP/envelope errors raise ApiException; I/O errors raise NetworkException.</p>
     * @param scheduledSendId pending scheduled-send ID
     * @param accountId explicit workspace ID, or null for default
     * @return unwrapped response data
     * @throws ValidationException if local arguments are invalid
     */
    public ScheduledSend cancel(String scheduledSendId, String accountId) {
        String path = sendsPath(accountId) + "/" + pathSegment(scheduledSendId, "Scheduled send ID") + "/cancel";
        return call("Failed to cancel scheduled send", () -> http.put(path, null), ScheduledSend.class);
    }

    /** Uses the default account. See {@link #cancel(String, String)}.
     * @param scheduledSendId pending scheduled-send ID
     * @return scheduled-send state
     */
    public ScheduledSend cancel(String scheduledSendId) { return cancel(scheduledSendId, null); }

    /** Send a scheduled send now. Available on staging.
     * <p><strong>HTTP:</strong> {@code PUT /v1/accounts/{accountId}/scheduled-sends/{scheduledSendId}/send}. Makes a <code>pending</code> scheduled send&#x27;s request now rather than at <code>send_at</code>: as <strong>Create assignment</strong> or <strong>Create envelope</strong> would make it then, as the member who scheduled it, with the same checks and charges. A refusal answers as that route would answer the member who scheduled it: no documents or credits left, a signer removed, the document already sent, the plan without Envelopes, a <code>max_total_credits</code> below the cost now (<code>409</code> <code>CreditCeilingExceeded</code>), that member gone from the workspace (<code>400</code>) or the workspace&#x27;s deletion pending (<code>403</code>); the send then stays <code>pending</code>, to go at <code>send_at</code> unless cancelled, and no one is e-mailed. A send already being sent, sent, failed or cancelled answers <code>409</code>.</p>
     * <p>Request:</p><pre>none</pre>
     * <p>Success:</p><pre>{&quot;status&quot;:200,&quot;data&quot;:{&quot;resource&quot;:&quot;scheduled_send&quot;,&quot;id&quot;:&quot;send-id&quot;,&quot;type&quot;:&quot;assignment&quot;,&quot;document_ids&quot;:[&quot;document-id&quot;],&quot;status&quot;:&quot;sent&quot;,&quot;send_at&quot;:&quot;2026-12-01T12:00:00Z&quot;,&quot;assignment_id&quot;:null,&quot;envelope_id&quot;:null,&quot;failure_reason&quot;:null,&quot;created_at&quot;:&quot;2026-11-30T12:00:00Z&quot;,&quot;processed_at&quot;:null}}</pre>
     * <p>200: The scheduled send, <code>sent</code>, with the assignment or envelope it created; 400: The request refused as <strong>Create assignment</strong> or <strong>Create envelope</strong> refuses it, or the member who scheduled it has left the workspace. The send stays <code>pending</code>.; 403: The workspace&#x27;s plan lacks a feature the request uses (<code>PlanRestricted</code>), or an OAuth app&#x27;s token lacks <code>documents:write</code> (<code>InsufficientScope</code>); also a deactivated membership, a role, or the workspace&#x27;s deletion pending. The send stays <code>pending</code>.; 404: The workspace does not exist, or the scheduled send is not the workspace&#x27;s; or the document an assignment sends no longer exists (<code>Document not found.</code>), and the send stays <code>pending</code>.; 409: Only a <code>pending</code> scheduled send can be sent now; or the request&#x27;s <code>max_total_credits</code> is below what it costs now (<code>CreditCeilingExceeded</code>, nothing charged), and the send stays <code>pending</code>.; 401: Missing or invalid credentials.; 500: Unexpected server error. HTTP/envelope errors raise ApiException; I/O errors raise NetworkException.</p>
     * @param scheduledSendId pending scheduled-send ID
     * @param accountId explicit workspace ID, or null for default
     * @return unwrapped response data
     * @throws ValidationException if local arguments are invalid
     */
    public ScheduledSend sendNow(String scheduledSendId, String accountId) {
        String path = sendsPath(accountId) + "/" + pathSegment(scheduledSendId, "Scheduled send ID") + "/send";
        return call("Failed to send scheduled send now", () -> http.put(path, null), ScheduledSend.class);
    }

    /** Uses the default account. See {@link #sendNow(String, String)}.
     * @param scheduledSendId pending scheduled-send ID
     * @return scheduled-send state
     */
    public ScheduledSend sendNow(String scheduledSendId) { return sendNow(scheduledSendId, null); }

    private String sendsPath(String accountId) {
        return "/accounts/" + pathSegment(accountId(accountId), "Account ID") + "/scheduled-sends";
    }

    private static void validateSendAt(Instant sendAt) {
        Instant now = Instant.now();
        if (sendAt == null || sendAt.isBefore(now.plusSeconds(3600)) || sendAt.isAfter(now.plusSeconds(90 * 86400L))) {
            throw new ValidationException("Send time must be one hour to 90 days ahead");
        }
    }

    private static void validateDeadline(String expiresAt, Instant sendAt) {
        if (expiresAt != null && parseExpiration(expiresAt).isBefore(sendAt.plusSeconds(7200))) {
            throw new ValidationException("Expiration must be at least two hours after send time");
        }
    }
}
