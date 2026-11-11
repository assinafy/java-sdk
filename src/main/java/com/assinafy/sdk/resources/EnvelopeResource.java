package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.CostEstimate;
import com.assinafy.sdk.models.DocumentEnvelope;
import com.assinafy.sdk.models.DocumentActivity;
import com.assinafy.sdk.models.EnvelopeCancellation;
import com.assinafy.sdk.models.EnvelopeResendResult;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.enums.DocumentArtifactName;
import com.assinafy.sdk.request.CreateEnvelopeRequest;
import com.assinafy.sdk.request.EnvelopeDocumentRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.SignerReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Operations for envelope. */
public class EnvelopeResource extends BaseResource {
    /** Creates a resource.
     * @param http transport
     * @param defaultAccountId default workspace ID
     * @param logger optional logger
     */
    public EnvelopeResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    /** Creates a resource with no logger.
     * @param http transport
     * @param defaultAccountId default workspace ID
     */
    public EnvelopeResource(ApiHttpClient http, String defaultAccountId) { super(http, defaultAccountId); }

    /**
     * List envelopes.
     * <p><strong>HTTP:</strong> {@code GET /v1/accounts/{accountId}/envelopes}.
     * The workspace&#x27;s envelopes, the most recent first: each with its documents in order, their statuses and signers, and the counts by status.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: [{&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;name&quot;: &quot;value&quot;, &quot;message&quot;: &quot;value&quot;, &quot;documents&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;position&quot;: 1, &quot;name&quot;: &quot;value&quot;, &quot;status&quot;: &quot;value&quot;, &quot;artifacts&quot;: {}, &quot;signers&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;full_name&quot;: &quot;value&quot;, &quot;email&quot;: &quot;value&quot;, &quot;whatsapp_phone_number&quot;: &quot;value&quot;, &quot;completed&quot;: true, &quot;signed_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}]}], &quot;counts&quot;: {}, &quot;signing_urls&quot;: [{&quot;signer_id&quot;: &quot;value&quot;, &quot;url&quot;: &quot;value&quot;, &quot;embedded&quot;: true}], &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}]}</pre>
     * <p>Statuses: 200 The workspace&#x27;s envelopes; 400 A filter refused as sent, an entry of `errors` each: `search` (not text), `status` (other than `open` or `closed`).; 403 An OAuth app&#x27;s token lacks `documents:read` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404 The requested resource does not exist.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param params optional search, open/closed status and pagination
     * @param accountId explicit workspace ID, or null for the default
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public PaginatedResult<DocumentEnvelope> list(ListParams params, String accountId) {
        String path = envelopesPath(accountId);
        Map<String, Object> query = params != null ? params.toQueryParams() : Map.of();
        Object status = query.get("status");
        if (status != null && !"open".equals(status) && !"closed".equals(status)) {
            throw new ValidationException("Envelope status must be open or closed");
        }
        if (query.get("per-page") instanceof Integer size && size > 50) {
            throw new ValidationException("Envelope page size must not exceed 50");
        }
        return callList("Failed to list envelopes", () -> http.get(path, query), DocumentEnvelope.class);
    }

    /** Uses the default account. See {@link list(ListParams, String)}.
     * @param params optional search, open/closed status and pagination
     * @return the unwrapped result
     */
    public PaginatedResult<DocumentEnvelope> list(ListParams params) { return list(params, null); }

    /**
     * Get envelope.
     * <p><strong>HTTP:</strong> {@code GET /v1/accounts/{accountId}/envelopes/{envelopeId}}.
     * One envelope of the workspace: its documents in order, each with its status and signers, and the counts by status.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;name&quot;: &quot;value&quot;, &quot;message&quot;: &quot;value&quot;, &quot;documents&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;position&quot;: 1, &quot;name&quot;: &quot;value&quot;, &quot;status&quot;: &quot;value&quot;, &quot;artifacts&quot;: {}, &quot;signers&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;full_name&quot;: &quot;value&quot;, &quot;email&quot;: &quot;value&quot;, &quot;whatsapp_phone_number&quot;: &quot;value&quot;, &quot;completed&quot;: true, &quot;signed_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}]}], &quot;counts&quot;: {}, &quot;signing_urls&quot;: [{&quot;signer_id&quot;: &quot;value&quot;, &quot;url&quot;: &quot;value&quot;, &quot;embedded&quot;: true}], &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}}</pre>
     * <p>Statuses: 200 The envelope; 403 An OAuth app&#x27;s token lacks `documents:read` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404 The requested resource does not exist.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param envelopeId envelope ID
     * @param accountId explicit workspace ID, or null for the default
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public DocumentEnvelope get(String envelopeId, String accountId) {
        String path = envelopePath(envelopeId, accountId);
        return call("Failed to fetch envelope", () -> http.get(path), DocumentEnvelope.class);
    }

    /** Uses the default account. See {@link get(String, String)}.
     * @param envelopeId envelope ID
     * @return the unwrapped result
     */
    public DocumentEnvelope get(String envelopeId) { return get(envelopeId, null); }

    /**
     * Create envelope.
     * <p><strong>HTTP:</strong> {@code POST /v1/accounts/{accountId}/envelopes}.
     * Send several documents to the same signers at once. Each document gets its own assignment and keeps its own status, certificate and downloads; one invitation goes to each signer, through the methods sent for them, for all the documents they are on, from the first of them. A send that fails is recorded on that signer&#x27;s request (`notification_history`) and doesn&#x27;t undo the envelope: `200` means the envelope was created, not that every invitation was delivered. The envelope is charged as one document, whatever its documents, each signer&#x27;s notifications and identity check once for the whole envelope, and each digital-certificate signature on every document it is made on. Every document must be uploaded and processed (`metadata_ready`) with no assignment, and every listed signer must be on at least one document. Each document is signed on its virtual page or, with `collect`, on the fields placed on its pages, as on an assignment; a digital-certificate signer signs only virtual documents, alone on each. Needs the Envelopes feature on the workspace&#x27;s plan (`403` with code `PlanRestricted` otherwise). A refused request creates and charges nothing; `errors` names every field at fault.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: {&quot;name&quot;: &quot;value&quot;, &quot;message&quot;: &quot;value&quot;, &quot;max_total_credits&quot;: 1, &quot;signers&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;verification_method&quot;: &quot;Email&quot;, &quot;notification_methods&quot;: [&quot;Email&quot;], &quot;embedded&quot;: true}], &quot;documents&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;method&quot;: &quot;virtual&quot;, &quot;entries&quot;: [{&quot;page_id&quot;: &quot;value&quot;, &quot;fields&quot;: [{&quot;signer_id&quot;: &quot;value&quot;, &quot;field_id&quot;: &quot;value&quot;, &quot;display_settings&quot;: {&quot;left&quot;: 1, &quot;top&quot;: 1, &quot;width&quot;: 1, &quot;height&quot;: 1, &quot;fontFamily&quot;: &quot;value&quot;, &quot;fontSize&quot;: 1, &quot;backgroundColor&quot;: &quot;value&quot;}}]}], &quot;signer_ids&quot;: [&quot;value&quot;], &quot;expires_at&quot;: &quot;value&quot;, &quot;copy_receiver_ids&quot;: [&quot;value&quot;]}]}.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;name&quot;: &quot;value&quot;, &quot;message&quot;: &quot;value&quot;, &quot;documents&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;position&quot;: 1, &quot;name&quot;: &quot;value&quot;, &quot;status&quot;: &quot;value&quot;, &quot;artifacts&quot;: {}, &quot;signers&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;full_name&quot;: &quot;value&quot;, &quot;email&quot;: &quot;value&quot;, &quot;whatsapp_phone_number&quot;: &quot;value&quot;, &quot;completed&quot;: true, &quot;signed_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}]}], &quot;counts&quot;: {}, &quot;signing_urls&quot;: [{&quot;signer_id&quot;: &quot;value&quot;, &quot;url&quot;: &quot;value&quot;, &quot;embedded&quot;: true}], &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}}</pre>
     * <p>Statuses: 200 The created envelope; 400 Refused as sent; nothing was created or charged. Each field at fault is an entry of `errors` (a list refused as a whole is not read item by item), an item of a list named by its index (`documents[1].signer_ids`): `name`; `message` (not text); `signers` and `documents` (missing, empty, not a list, or more than 20 signers or 20 documents; `documents` also when its documents hold more than 2,000 fields between them, a virtual document&#x27;s being one per signer, or more than 100 copy receivers); `signers[i].id` (missing, listed twice, not a signer of the workspace, or on no document); `signers[i].verification_method` (not a code); `signers[i].notification_methods` (not a list of codes); `signers[i].step` (sent at all); `documents[i].id` (missing, listed twice, or a document that can&#x27;t take the assignment: not found, not `metadata_ready`, or already assigned); `documents[i].method` (other than `virtual` or `collect`); `documents[i].entries` (missing for `collect`, or not a list of pages with fields naming their `signer_id`); `documents[i].expires_at` (not text, a number or null); `documents[i].signer_ids` (missing, not a list of ids, a signer not listed once in `signers`, for `collect` not exactly the signers on its fields, or a digital-certificate signer on a `collect` document or with another signer). A page or field the assignment doesn&#x27;t find on a `collect` document, a method code the platform doesn&#x27;t know, a verification and notification that don&#x27;t pair, a signer without the e-mail or WhatsApp number their method needs, an `embedded` other than `true`, `false`, `1` or `0` (`null` reads as left out, as on an assignment) or set for a signer verified other than by Bypass, Email or WhatsApp, a balance too low for the envelope, an `expires_at` or `copy_receiver_ids` the assignment refuses, and a document another request assigned or put back into processing meanwhile answer `400` with `message` only, as on an assignment.; 403 The workspace&#x27;s plan lacks the Envelopes feature, or a signer&#x27;s method or embedded signing the plan lacks (`PlanRestricted`); or an OAuth app&#x27;s token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404 The requested resource does not exist.; 409 The envelope costs more than `max_total_credits` (`CreditCeilingExceeded`), or another request with this `Idempotency-Key` is still running (`IdempotencyKeyInUse`).; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param request envelope definition
     * @param accountId explicit workspace ID, or null for the default
     * @param idempotencyKey new key per intended envelope, or null to omit
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public DocumentEnvelope create(CreateEnvelopeRequest request, String accountId, String idempotencyKey) {
        String path = envelopesPath(accountId);
        SigningRules.validateIdempotencyKey(idempotencyKey);
        String json = serialise(envelopePayload(request, false));
        return call("Failed to create envelope", () -> http.post(path, json, idempotencyKey), DocumentEnvelope.class);
    }

    /** Uses the default account. See {@link create(CreateEnvelopeRequest, String, String)}.
     * @param request envelope definition
     * @return the unwrapped result
     */
    public DocumentEnvelope create(CreateEnvelopeRequest request) { return create(request, null, null); }

    /** Uses the default account. See {@link create(CreateEnvelopeRequest, String, String)}.
     * @param request envelope definition
     * @param accountId explicit workspace ID, or null for the default
     * @return the unwrapped result
     */
    public DocumentEnvelope create(CreateEnvelopeRequest request, String accountId) { return create(request, accountId, null); }

    /**
     * Estimate envelope cost.
     * <p><strong>HTTP:</strong> {@code POST /v1/accounts/{accountId}/envelopes/estimate-cost}.
     * What creating the envelope would cost, creating and charging nothing. Takes the creation body and checks it as creation does (`max_total_credits` aside); answers as an assignment&#x27;s estimate: the envelope counts as one document (`documents` is 1, an extra one when the plan&#x27;s allowance doesn&#x27;t cover it), each signer&#x27;s notifications and identity check count once for the whole envelope, and each digital-certificate signature on every document it is made on, as creation charges them.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: {&quot;name&quot;: &quot;value&quot;, &quot;message&quot;: &quot;value&quot;, &quot;max_total_credits&quot;: 1, &quot;signers&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;verification_method&quot;: &quot;Email&quot;, &quot;notification_methods&quot;: [&quot;Email&quot;], &quot;embedded&quot;: true}], &quot;documents&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;method&quot;: &quot;virtual&quot;, &quot;entries&quot;: [{&quot;page_id&quot;: &quot;value&quot;, &quot;fields&quot;: [{&quot;signer_id&quot;: &quot;value&quot;, &quot;field_id&quot;: &quot;value&quot;, &quot;display_settings&quot;: {&quot;left&quot;: 1, &quot;top&quot;: 1, &quot;width&quot;: 1, &quot;height&quot;: 1, &quot;fontFamily&quot;: &quot;value&quot;, &quot;fontSize&quot;: 1, &quot;backgroundColor&quot;: &quot;value&quot;}}]}], &quot;signer_ids&quot;: [&quot;value&quot;], &quot;expires_at&quot;: &quot;value&quot;, &quot;copy_receiver_ids&quot;: [&quot;value&quot;]}]}.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;documents&quot;: 1, &quot;credits&quot;: 1, &quot;needs_extra_document&quot;: true, &quot;extra_document_cost&quot;: 1, &quot;total_credits&quot;: 1, &quot;breakdown&quot;: [{&quot;code&quot;: &quot;value&quot;, &quot;name&quot;: &quot;value&quot;, &quot;cost&quot;: 1, &quot;quantity&quot;: 1, &quot;unit_cost&quot;: 1}], &quot;document_balance&quot;: 1, &quot;credit_balance&quot;: 1, &quot;has_sufficient_resources&quot;: true, &quot;blocking_reason&quot;: &quot;PendingPayment&quot;, &quot;message&quot;: &quot;value&quot;}}</pre>
     * <p>Statuses: 200 Cost estimate and balances; 400 Refused on the same fields as creation, in `errors`, and with `message` alone for a method the platform does not know or a pairing it refuses. What creation checks while it creates each document (a signer&#x27;s missing contact, a page or field not found, the balance) is not checked here: `has_sufficient_resources` says what the balance allows.; 403 The workspace&#x27;s plan lacks the Envelopes feature, or a signer&#x27;s method or embedded signing the plan lacks (`PlanRestricted`); or an OAuth app&#x27;s token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404 The requested resource does not exist.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param request same definition as creation; credit ceiling ignored
     * @param accountId explicit workspace ID, or null for the default
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public CostEstimate estimateCost(CreateEnvelopeRequest request, String accountId) {
        String path = envelopesPath(accountId) + "/estimate-cost";
        String json = serialise(envelopePayload(request, true));
        return call("Failed to estimate envelope cost", () -> http.post(path, json), CostEstimate.class);
    }

    /** Uses the default account. See {@link estimateCost(CreateEnvelopeRequest, String)}.
     * @param request same definition as creation; credit ceiling ignored
     * @return the unwrapped result
     */
    public CostEstimate estimateCost(CreateEnvelopeRequest request) { return estimateCost(request, null); }

    /**
     * Update envelope.
     * <p><strong>HTTP:</strong> {@code PATCH /v1/accounts/{accountId}/envelopes/{envelopeId}}.
     * Rename the envelope, change its message or put its documents in a new order; send only what changes. Nothing is sent to anyone: the reads and every notice sent from then on (a reminder, a cancel, a document added) use the new name, message and order. The message is also each document&#x27;s assignment `message`. Works on a closed envelope too. A refused request changes nothing; `errors` names every field at fault.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: {&quot;name&quot;: &quot;value&quot;, &quot;message&quot;: &quot;value&quot;, &quot;document_ids&quot;: [&quot;value&quot;]}.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;name&quot;: &quot;value&quot;, &quot;message&quot;: &quot;value&quot;, &quot;documents&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;position&quot;: 1, &quot;name&quot;: &quot;value&quot;, &quot;status&quot;: &quot;value&quot;, &quot;artifacts&quot;: {}, &quot;signers&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;full_name&quot;: &quot;value&quot;, &quot;email&quot;: &quot;value&quot;, &quot;whatsapp_phone_number&quot;: &quot;value&quot;, &quot;completed&quot;: true, &quot;signed_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}]}], &quot;counts&quot;: {}, &quot;signing_urls&quot;: [{&quot;signer_id&quot;: &quot;value&quot;, &quot;url&quot;: &quot;value&quot;, &quot;embedded&quot;: true}], &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}}</pre>
     * <p>Statuses: 200 The envelope as changed; 400 Refused as sent; nothing changed. Each field at fault is an entry of `errors`: `name` (empty, not text, over 255 characters, or holding `&lt;`), `message` (not text), `document_ids` (empty, or not a list of every document of the envelope, each once).; 403 An OAuth app&#x27;s token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404 The requested resource does not exist.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param envelopeId envelope ID
     * @param changes name, message (including explicit null), and/or document_ids in desired order
     * @param accountId explicit workspace ID, or null for the default
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public DocumentEnvelope update(String envelopeId, Map<String, Object> changes, String accountId) {
        String path = envelopePath(envelopeId, accountId);
        if (changes == null || changes.isEmpty()) throw new ValidationException("Envelope changes are required");
        Map<String, Object> body = new LinkedHashMap<>(changes);
        for (String key : body.keySet()) {
            if (!Set.of("name", "message", "document_ids").contains(key == null ? "" : key)) {
                throw new ValidationException("Unknown envelope field");
            }
        }
        if (body.containsKey("name")) validateName(body.get("name"));
        if (body.containsKey("message") && body.get("message") != null && !(body.get("message") instanceof String)) {
            throw new ValidationException("Envelope message must be text or null");
        }
        if (body.containsKey("document_ids")) uniqueIds(body.get("document_ids"), "Document IDs", false);
        String json = serialise(body);
        return call("Failed to update envelope", () -> http.patch(path, json), DocumentEnvelope.class);
    }

    /** Uses the default account. See {@link update(String, Map<String, Object>, String)}.
     * @param envelopeId envelope ID
     * @param changes name, message (including explicit null), and/or document_ids in desired order
     * @return the unwrapped result
     */
    public DocumentEnvelope update(String envelopeId, Map<String, Object> changes) { return update(envelopeId, changes, null); }

    /**
     * Delete envelope.
     * <p><strong>HTTP:</strong> {@code DELETE /v1/accounts/{accountId}/envelopes/{envelopeId}}.
     * Delete an envelope once every document of it is closed: certificated, declined, cancelled, expired or failed. Before that it answers `409` with code `EnvelopeOpen`: cancel it first. The envelope is gone: it is no longer listed and reads `404`, and a create retried with its `Idempotency-Key` no longer answers it. Its documents stay as ordinary documents, with their certificates, downloads and webhooks; `GET /v1/documents/{documentId}?expand=envelope_id` reads null for them.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: []}</pre>
     * <p>Statuses: 200 Envelope deleted; 403 An OAuth app&#x27;s token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404 The requested resource does not exist.; 409 A document of the envelope is still open (`EnvelopeOpen`).; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param envelopeId envelope ID
     * @param accountId explicit workspace ID, or null for the default
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public void delete(String envelopeId, String accountId) {
        String path = envelopePath(envelopeId, accountId);
        callVoid("Failed to delete envelope", () -> http.delete(path));
    }

    /** Uses the default account. See {@link delete(String, String)}.
     * @param envelopeId envelope ID
     */
    public void delete(String envelopeId) { delete(envelopeId, null); }

    /**
     * List envelope activities.
     * <p><strong>HTTP:</strong> {@code GET /v1/accounts/{accountId}/envelopes/{envelopeId}/activities}.
     * The envelope&#x27;s trail: the activities of every document it has had, one taken out included (not one deleted, whose own list answers 404), newest first (`sort=id` for oldest first), each naming its document (`document_id`, `document_name`). Each entry is otherwise a document activity, as `GET /v1/documents/{documentId}/activities` answers it. Paged: 20 per page by default; the `X-Pagination-Total-Count`, `X-Pagination-Page-Count`, `X-Pagination-Current-Page` and `X-Pagination-Per-Page` headers describe the full list, and a page past the end returns an empty `data`.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: [{&quot;id&quot;: 1, &quot;event&quot;: &quot;value&quot;, &quot;message&quot;: &quot;value&quot;, &quot;payload&quot;: {}, &quot;origin&quot;: {&quot;ip&quot;: &quot;value&quot;, &quot;user-agent&quot;: &quot;value&quot;}, &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;, &quot;document_id&quot;: &quot;value&quot;, &quot;document_name&quot;: &quot;value&quot;}]}</pre>
     * <p>Statuses: 200 The envelope&#x27;s activities; 403 An OAuth app&#x27;s token lacks `documents:read` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404 The requested resource does not exist.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param envelopeId envelope ID
     * @param params pagination and sort id or -id
     * @param accountId explicit workspace ID, or null for the default
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public PaginatedResult<DocumentActivity> activities(String envelopeId, ListParams params, String accountId) {
        String path = envelopePath(envelopeId, accountId) + "/activities";
        Map<String, Object> query = params != null ? params.toQueryParams() : Map.of();
        Object sort = query.get("sort");
        if (sort != null && !"id".equals(sort) && !"-id".equals(sort)) {
            throw new ValidationException("Activity sort must be id or -id");
        }
        return callList("Failed to fetch envelope activities", () -> http.get(path, query), DocumentActivity.class);
    }

    /** Uses the default account. See {@link activities(String, ListParams, String)}.
     * @param envelopeId envelope ID
     * @param params pagination and sort id or -id
     * @return the unwrapped result
     */
    public PaginatedResult<DocumentActivity> activities(String envelopeId, ListParams params) { return activities(envelopeId, params, null); }

    /**
     * Download envelope artifacts.
     * <p><strong>HTTP:</strong> {@code GET /v1/accounts/{accountId}/envelopes/{envelopeId}/download/{artifactName}}.
     * One zip, named after the envelope, of the chosen artifact of every document of it, in order. Each file is named after its document&#x27;s position (`1 - contract.pdf`); `bundle` gives each document a folder of its bundle&#x27;s files (`1 - contract/`). `certificated`, `certificate-page`, `pades` and `bundle` include only the documents certified so far, and `pades` only those that have one; `404` when no document has the artifact. The artifacts are those of `GET /v1/documents/{documentId}/download/{artifactName}`.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: none.</p>
     * <p>Success (application/zip):</p><pre>&quot;(binary bytes)&quot;</pre>
     * <p>Statuses: 200 The zip; 403 An OAuth app&#x27;s token lacks `documents:read` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404 The requested resource does not exist.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param envelopeId envelope ID
     * @param artifactName original, certificated, certificate-page, pades or bundle
     * @param accountId explicit workspace ID, or null for the default
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public byte[] download(String envelopeId, String artifactName, String accountId) {
        String path = envelopePath(envelopeId, accountId);
        if (artifactName == null || Arrays.stream(DocumentArtifactName.values()).noneMatch(v -> v.getValue().equals(artifactName))) {
            throw new ValidationException("Invalid document artifact");
        }
        return callBinary("Failed to download envelope", () -> http.getBinary(path + "/download/" + artifactName, "*/*"));
    }

    /** Uses the default account. See {@link download(String, String, String)}.
     * @param envelopeId envelope ID
     * @param artifactName original, certificated, certificate-page, pades or bundle
     * @return the unwrapped result
     */
    public byte[] download(String envelopeId, String artifactName) { return download(envelopeId, artifactName, null); }

    /**
     * Cancel envelope.
     * <p><strong>HTTP:</strong> {@code PUT /v1/accounts/{accountId}/envelopes/{envelopeId}/cancel}.
     * Cancels every document of the envelope that can still be cancelled, the way deleting a pending document cancels it: the owner, its signers and its copy receivers are told. A document already closed, one every signer has signed (its certification queued), or one being certificated, is left as it is. A notice that fails doesn&#x27;t stop the other documents. Safe to repeat: a second call cancels nothing and answers each document&#x27;s status.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;status&quot;: &quot;value&quot;, &quot;cancelled&quot;: true}]}</pre>
     * <p>Statuses: 200 What happened to each document, in the envelope&#x27;s order; 403 An OAuth app&#x27;s token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404 The requested resource does not exist.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param envelopeId envelope ID
     * @param accountId explicit workspace ID, or null for the default
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public List<EnvelopeCancellation> cancel(String envelopeId, String accountId) {
        String path = envelopePath(envelopeId, accountId) + "/cancel";
        return callList("Failed to cancel envelope", () -> http.put(path, null), EnvelopeCancellation.class).getData();
    }

    /** Uses the default account. See {@link cancel(String, String)}.
     * @param envelopeId envelope ID
     * @return the unwrapped result
     */
    public List<EnvelopeCancellation> cancel(String envelopeId) { return cancel(envelopeId, null); }

    /**
     * Remind envelope signer.
     * <p><strong>HTTP:</strong> {@code PUT /v1/accounts/{accountId}/envelopes/{envelopeId}/signers/{signerId}/resend}.
     * Reminds a signer of the whole envelope at once: one message, the envelope&#x27;s invitation sent again from their first link, listing the documents they still have to sign and, by e-mail, those they already signed. Paid notification methods are charged once for it, for each message that goes out. Each document they still have to sign records a `signature_requested` event, as the invitation does.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;is_sent&quot;: true, &quot;envelope_id&quot;: &quot;value&quot;, &quot;signer_id&quot;: &quot;value&quot;}}</pre>
     * <p>Statuses: 200 Reminder result; 400 The signer has nothing left to sign in the envelope: every document of theirs is signed or closed; or the account lacks credits for the reminder.; 403 An OAuth app&#x27;s token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404 The envelope was not found, or the signer is not one of its signers.; 409 The envelope is closed: every document of it is signed, declined, cancelled, expired or failed (`EnvelopeClosed`).; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param envelopeId envelope ID
     * @param signerId signer to remind; sends one invitation for all pending documents
     * @param accountId explicit workspace ID, or null for the default
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public EnvelopeResendResult resendNotification(String envelopeId, String signerId, String accountId) {
        String path = envelopePath(envelopeId, accountId) + "/signers/" + pathSegment(signerId, "Signer ID") + "/resend";
        return call("Failed to remind envelope signer", () -> http.put(path, null), EnvelopeResendResult.class);
    }

    /** Uses the default account. See {@link resendNotification(String, String, String)}.
     * @param envelopeId envelope ID
     * @param signerId signer to remind; sends one invitation for all pending documents
     * @return the unwrapped result
     */
    public EnvelopeResendResult resendNotification(String envelopeId, String signerId) { return resendNotification(envelopeId, signerId, null); }

    /**
     * Add document to envelope.
     * <p><strong>HTTP:</strong> {@code POST /v1/accounts/{accountId}/envelopes/{envelopeId}/documents}.
     * Add a document to an open envelope for some of its signers. The document is checked as on creation: a `metadata_ready` document of the workspace with no assignment, signed on its virtual page or, with `collect`, on the fields placed in `entries`, with its own `expires_at` and `copy_receiver_ids`. An envelope holds up to 20 documents. It charges only its digital-certificate signatures, the envelope having paid for its document and its signers&#x27; notifications and identity checks when it was sent, and goes last in the envelope&#x27;s order; a signer whose identity check the envelope approved is verified on it too. Each chosen signer is sent the envelope&#x27;s invitation again, through the methods and with the link of their first invitation, now listing the new document, even once the document that invitation was sent from is taken out; the other signers hear nothing. The signers are the envelope&#x27;s own: no one new joins once it was sent. Needs the Envelopes feature on the workspace&#x27;s plan, as creating does. A refused request creates and charges nothing; `errors` names every field at fault.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: {&quot;id&quot;: &quot;value&quot;, &quot;method&quot;: &quot;virtual&quot;, &quot;entries&quot;: [{&quot;page_id&quot;: &quot;value&quot;, &quot;fields&quot;: [{&quot;signer_id&quot;: &quot;value&quot;, &quot;field_id&quot;: &quot;value&quot;, &quot;display_settings&quot;: {&quot;left&quot;: 1, &quot;top&quot;: 1, &quot;width&quot;: 1, &quot;height&quot;: 1, &quot;fontFamily&quot;: &quot;value&quot;, &quot;fontSize&quot;: 1, &quot;backgroundColor&quot;: &quot;value&quot;}}]}], &quot;signer_ids&quot;: [&quot;value&quot;], &quot;expires_at&quot;: &quot;value&quot;, &quot;copy_receiver_ids&quot;: [&quot;value&quot;], &quot;max_total_credits&quot;: 1}.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;name&quot;: &quot;value&quot;, &quot;message&quot;: &quot;value&quot;, &quot;documents&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;position&quot;: 1, &quot;name&quot;: &quot;value&quot;, &quot;status&quot;: &quot;value&quot;, &quot;artifacts&quot;: {}, &quot;signers&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;full_name&quot;: &quot;value&quot;, &quot;email&quot;: &quot;value&quot;, &quot;whatsapp_phone_number&quot;: &quot;value&quot;, &quot;completed&quot;: true, &quot;signed_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}]}], &quot;counts&quot;: {}, &quot;signing_urls&quot;: [{&quot;signer_id&quot;: &quot;value&quot;, &quot;url&quot;: &quot;value&quot;, &quot;embedded&quot;: true}], &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}}</pre>
     * <p>Statuses: 200 The envelope with the document added; 400 Refused as sent; nothing was created or charged. Each field at fault is an entry of `errors`: `id` (missing, or a document that can&#x27;t take the assignment: not found, not `metadata_ready`, or already assigned); `method` (other than `virtual` or `collect`); `entries` (missing for `collect`, or not a list of pages with fields naming their `signer_id`); `signer_ids` (missing, not a list of ids, listed twice, not a signer of the envelope&#x27;s documents, for `collect` not exactly the signers on its fields, or a digital-certificate signer on a `collect` document or with another signer); `expires_at` (not text, a number or null); a null `field` when the envelope already has 20 documents, or would hold more than 2,000 fields (a virtual document&#x27;s being one per signer) or more than 100 copy receivers with this one. A page or field the assignment doesn&#x27;t find on a `collect` document, a balance too low, an `expires_at` or `copy_receiver_ids` the assignment refuses, and a document another request assigned or put back into processing meanwhile answer `400` with `message` only, as on an assignment.; 403 The workspace&#x27;s plan lacks the Envelopes feature, or a signer&#x27;s method or embedded signing the plan lacks (`PlanRestricted`); or an OAuth app&#x27;s token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404 The requested resource does not exist.; 409 Every document of the envelope is closed (`EnvelopeClosed`), or the document costs more than `max_total_credits` on its own (`CreditCeilingExceeded`).; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param envelopeId envelope ID
     * @param request document and existing envelope signer IDs
     * @param accountId explicit workspace ID, or null for the default
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public DocumentEnvelope addDocument(String envelopeId, EnvelopeDocumentRequest request, String accountId) {
        String path = envelopePath(envelopeId, accountId) + "/documents";
        Map<String, Object> body = documentPayload(request);
        if ("collect".equals(request.getMethod())) {
            SigningRules.validateEntries(request.getEntries(), new HashSet<>(request.getSignerIds()));
        }
        SigningRules.validateCreditCeiling(request.getMaxTotalCredits());
        if (request.getMaxTotalCredits() != null) body.put("max_total_credits", request.getMaxTotalCredits());
        String json = serialise(body);
        return call("Failed to add envelope document", () -> http.post(path, json), DocumentEnvelope.class);
    }

    /** Uses the default account. See {@link addDocument(String, EnvelopeDocumentRequest, String)}.
     * @param envelopeId envelope ID
     * @param request document and existing envelope signer IDs
     * @return the unwrapped result
     */
    public DocumentEnvelope addDocument(String envelopeId, EnvelopeDocumentRequest request) { return addDocument(envelopeId, request, null); }

    /**
     * Take document out of envelope.
     * <p><strong>HTTP:</strong> {@code DELETE /v1/accounts/{accountId}/envelopes/{envelopeId}/documents/{documentId}}.
     * Take an open document out of the envelope: it is cancelled as cancelling the envelope cancels it, its owner, signers and copy receivers told in a notice naming the envelope, and it is no longer listed; the other documents carry on. Out of the envelope, `GET /v1/documents/{documentId}?expand=envelope_id` reads null for it; a signer whose invitation link was sent from it keeps reaching the envelope&#x27;s other documents with that link, so it can&#x27;t be deleted on its own (`409` with code `EnvelopeOpen`) until every document of the envelope is closed. A document that is not in the envelope, or was taken out already, answers `404`.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;name&quot;: &quot;value&quot;, &quot;message&quot;: &quot;value&quot;, &quot;documents&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;position&quot;: 1, &quot;name&quot;: &quot;value&quot;, &quot;status&quot;: &quot;value&quot;, &quot;artifacts&quot;: {}, &quot;signers&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;full_name&quot;: &quot;value&quot;, &quot;email&quot;: &quot;value&quot;, &quot;whatsapp_phone_number&quot;: &quot;value&quot;, &quot;completed&quot;: true, &quot;signed_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}]}], &quot;counts&quot;: {}, &quot;signing_urls&quot;: [{&quot;signer_id&quot;: &quot;value&quot;, &quot;url&quot;: &quot;value&quot;, &quot;embedded&quot;: true}], &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}}</pre>
     * <p>Statuses: 200 The envelope without the document; 403 An OAuth app&#x27;s token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; 404 The requested resource does not exist.; 409 The document is closed, signed by every signer (its certification queued) or being certificated, and can no longer be cancelled (`DocumentClosed`).; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param envelopeId envelope ID
     * @param documentId open document to cancel and take out
     * @param accountId explicit workspace ID, or null for the default
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public DocumentEnvelope removeDocument(String envelopeId, String documentId, String accountId) {
        String path = envelopePath(envelopeId, accountId) + "/documents/" + pathSegment(documentId, "Document ID");
        return call("Failed to remove envelope document", () -> http.delete(path), DocumentEnvelope.class);
    }

    /** Uses the default account. See {@link removeDocument(String, String, String)}.
     * @param envelopeId envelope ID
     * @param documentId open document to cancel and take out
     * @return the unwrapped result
     */
    public DocumentEnvelope removeDocument(String envelopeId, String documentId) { return removeDocument(envelopeId, documentId, null); }

    private String envelopesPath(String account) {
        return "/accounts/" + pathSegment(accountId(account), "Account ID") + "/envelopes";
    }

    private String envelopePath(String envelopeId, String account) {
        return envelopesPath(account) + "/" + pathSegment(envelopeId, "Envelope ID");
    }

    private Map<String, Object> envelopePayload(CreateEnvelopeRequest request, boolean estimate) {
        if (request == null) throw new ValidationException("Envelope request is required");
        validateName(request.getName());
        if (request.getSigners() == null || request.getSigners().isEmpty() || request.getSigners().size() > 20
                || request.getDocuments() == null || request.getDocuments().isEmpty() || request.getDocuments().size() > 20) {
            throw new ValidationException("Envelopes require 1 to 20 signers and 1 to 20 documents");
        }
        Map<String, SignerReference> signers = new LinkedHashMap<>();
        for (SignerReference signer : request.getSigners()) {
            if (signer == null) throw new ValidationException("Signer is required");
            requireId(signer.getId(), "Signer ID");
            if (signers.putIfAbsent(signer.getId(), signer) != null) throw new ValidationException("Duplicate signer ID");
            if (signer.getStep() != null) throw new ValidationException("Envelope signers cannot have steps");
            SigningRules.validateDeliveryMethods(signer.getVerificationMethod(), signer.getNotificationMethods());
            SigningRules.validateEmbedded(signer.getEmbedded(), signer.getVerificationMethod());
        }
        Set<String> documentIds = new HashSet<>();
        Set<String> usedSigners = new HashSet<>();
        List<Map<String, Object>> documents = new ArrayList<>();
        int totalFields = 0;
        int totalCopyReceivers = 0;
        for (EnvelopeDocumentRequest document : request.getDocuments()) {
            Map<String, Object> payload = documentPayload(document);
            if (!documentIds.add(document.getId())) throw new ValidationException("Duplicate document ID");
            for (String signerId : document.getSignerIds()) {
                SignerReference signer = signers.get(signerId);
                if (signer == null) throw new ValidationException("Document signer must belong to the envelope");
                if ("DigitalCertificate".equals(signer.getVerificationMethod())
                        && ("collect".equals(document.getMethod()) || document.getSignerIds().size() != 1)) {
                    throw new ValidationException("Certificate signers require virtual documents with no other signers");
                }
                usedSigners.add(signerId);
            }
            totalFields += "collect".equals(document.getMethod())
                    ? SigningRules.validateEntries(document.getEntries(), new HashSet<>(document.getSignerIds()))
                    : document.getSignerIds().size();
            totalCopyReceivers += document.getCopyReceiverIds() == null ? 0 : document.getCopyReceiverIds().size();
            documents.add(payload);
        }
        if (!usedSigners.equals(signers.keySet())) throw new ValidationException("Every envelope signer must be on a document");
        if (totalFields > 2000 || totalCopyReceivers > 100) {
            throw new ValidationException("Envelope exceeds 2000 fields or 100 copy receivers");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", request.getName());
        body.put("signers", request.getSigners());
        body.put("documents", documents);
        if (request.getMessage() != null) body.put("message", request.getMessage());
        if (!estimate) {
            SigningRules.validateCreditCeiling(request.getMaxTotalCredits());
            if (request.getMaxTotalCredits() != null) body.put("max_total_credits", request.getMaxTotalCredits());
        }
        return body;
    }

    private Map<String, Object> documentPayload(EnvelopeDocumentRequest request) {
        if (request == null) throw new ValidationException("Envelope document is required");
        requireId(request.getId(), "Document ID");
        uniqueIds(request.getSignerIds(), "Signer IDs", false);
        SigningRules.validateMethod(request.getMethod());
        requireExpiration(request.getExpiresAt());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", request.getId());
        body.put("signer_ids", request.getSignerIds());
        body.put("method", request.getMethod() == null ? "virtual" : request.getMethod());
        if (request.getEntries() != null) body.put("entries", request.getEntries());
        if (request.getExpiresAt() != null) body.put("expires_at", request.getExpiresAt());
        if (request.getCopyReceiverIds() != null) {
            uniqueIds(request.getCopyReceiverIds(), "Copy receiver IDs", true);
            if (request.getCopyReceiverIds().size() > 100) throw new ValidationException("Too many copy receivers");
            body.put("copy_receiver_ids", request.getCopyReceiverIds());
        }
        return body;
    }

    private void validateName(Object value) {
        if (!(value instanceof String name)) throw new ValidationException("Envelope name must be text");
        requireText(name, "Envelope name", 255);
        if (name.contains("<")) throw new ValidationException("Envelope name must not contain a less-than sign");
    }

    private Set<String> uniqueIds(Object value, String label, boolean allowEmpty) {
        if (!(value instanceof List<?> ids) || (!allowEmpty && ids.isEmpty())) {
            throw new ValidationException(label + " must be a list of IDs");
        }
        Set<String> unique = new HashSet<>();
        for (Object id : ids) {
            if (!(id instanceof String text) || text.isBlank() || !unique.add(text)) {
                throw new ValidationException(label + " must contain unique nonblank IDs");
            }
        }
        return unique;
    }
}
