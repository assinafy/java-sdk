package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.CostEstimate;
import com.assinafy.sdk.models.Document;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.ResendNotificationResponse;
import com.assinafy.sdk.models.WhatsappNotification;
import com.assinafy.sdk.models.enums.AssignmentMethod;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.SignerReference;
import com.assinafy.sdk.util.ResponseHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Assignment creation, cost estimation, notification, and signer-facing operations. */
public class AssignmentResource extends BaseResource {

    private static final String VIRTUAL = AssignmentMethod.VIRTUAL.getValue();
    private static final String COLLECT = AssignmentMethod.COLLECT.getValue();

    /**
     * Create assignment operations with a default account and logger.
     *
     * @param http HTTP transport
     * @param defaultAccountId optional default account ID
     * @param logger diagnostic logger
     */
    public AssignmentResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    /**
     * Create assignment operations with a default account and no-op logging.
     *
     * @param http HTTP transport
     * @param defaultAccountId optional default account ID
     */
    public AssignmentResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    /**
     * Create account-independent assignment operations with no-op logging.
     *
     * @param http HTTP transport
     */
    public AssignmentResource(ApiHttpClient http) {
        super(http);
    }

    /**
     * List the assignments belonging to the authenticated user's <em>current account</em>
     * ({@code GET /v1/assignments}), paginated via {@code page}/{@code per-page}.
     *
     * <p>This overload supplies the client's default {@code accountId} query automatically when one
     * is configured; bearer sessions may resolve their account without it.
     *
     * <p>Wire contract, payloads and failures: {@link #list(ListParams, String)}.</p>
     *
     * @param params paging and filtering parameters, or {@code null}
     * @return matching assignments and pagination metadata
     */
    public PaginatedResult<Assignment> list(ListParams params) {
        return list(params, null);
    }

    /**
     * List assignments, adding {@code accountId} query context when an explicit or default account
     * is available.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/assignments</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
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
     *       "resource": "assignment",
     *       "id": "615606ef81d199996981dbce",
     *       "sender_email": "sender@example.invalid",
     *       "method": "virtual",
     *       "expires_at": null,
     *       "message": null,
     *       "signers": [
     *         {
     *           "resource": "signer",
     *           "id": "62d6ee35c7741ca4006b9e11",
     *           "full_name": "John Signer",
     *           "email": null,
     *           "whatsapp_phone_number": null,
     *           "has_accepted_terms": false,
     *           "verification_method": null,
     *           "notification_methods": null,
     *           "step": null,
     *           "notified": null,
     *           "completed": null,
     *           "notification_history": null
     *         }
     *       ],
     *       "copy_receivers": [
     *         {}
     *       ],
     *       "items": [
     *         {
     *           "id": "id_example",
     *           "page": null,
     *           "signer": {},
     *           "field": null,
     *           "display_settings": null,
     *           "value": null,
     *           "completed": false
     *         }
     *       ],
     *       "summary": {
     *         "signer_count": 1,
     *         "completed_count": 0,
     *         "signers": [
     *           {}
     *         ]
     *       },
     *       "signing_urls": [
     *         {
     *           "signer_id": "signer_id_example",
     *           "url": "https://api.assinafy.com.br/v1/sign/doc1?email=joe@example.invalid"
     *         }
     *       ]
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 A page of assignments; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param params paging and filtering parameters, or {@code null}
     * @param accountId explicit account ID, or {@code null} to use the default/session context
     * @return matching assignments and pagination metadata
     */
    public PaginatedResult<Assignment> list(ListParams params, String accountId) {
        Map<String, Object> queryParams = new HashMap<>(params != null ? params.toQueryParams() : Map.of());
        String id = accountId != null ? requireId(accountId, "Account ID") : defaultAccountId;
        if (id != null && !id.isBlank()) queryParams.put("accountId", id);
        return callList("Failed to list assignments", () -> http.get("/assignments", queryParams), Assignment.class);
    }

    /**
     * List assignments with default paging and account context.
     *
     * <p>Wire contract, payloads and failures: {@link #list(ListParams, String)}.</p>
     *
     * @return matching assignments and pagination metadata
     */
    public PaginatedResult<Assignment> list() {
        return list(new ListParams());
    }

    /**
     * Request signatures for a document ({@code POST /documents/{documentId}/assignments}). The
     * request {@code method} defaults to {@code virtual} when unset; at least one signer is
     * required (each {@link SignerReference} needs a signer {@code id}). Verification methods are
     * {@code Email}, {@code Whatsapp}, or {@code DigitalCertificate}; notification methods, when
     * supplied, may be empty, but every element must be {@code Email} or {@code Whatsapp}. If one
     * signer supplies a step, all must do so and the positive steps must be contiguous from 1. A
     * digital-certificate signer must be alone in its step, and {@code collect} requires nonempty
     * entries. Returns the created {@link Assignment} (signers, items, summary and per-signer
     * signing URLs).
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/documents/{documentId}/assignments</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "method": "virtual",
     *   "signers": [
     *     {
     *       "id": "615605f50e968054a5b7c9b8",
     *       "verification_method": "Email",
     *       "notification_methods": [
     *         "Email"
     *       ],
     *       "step": 1
     *     }
     *   ],
     *   "entries": [
     *     {
     *       "page_id": "page_id_example",
     *       "fields": [
     *         {
     *           "signer_id": "signer_id_example",
     *           "field_id": "field_id_example",
     *           "display_settings": {
     *             "left": 69,
     *             "top": 282,
     *             "width": 421,
     *             "height": 45.86,
     *             "fontFamily": "Arial",
     *             "fontSize": 22,
     *             "backgroundColor": "#D5EBFF"
     *           }
     *         }
     *       ]
     *     }
     *   ],
     *   "message": "",
     *   "expires_at": "2030-10-05T12:00:00Z",
     *   "copy_receivers": [
     *     "copy_receivers_example"
     *   ]
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "assignment",
     *     "id": "615606ef81d199996981dbce",
     *     "sender_email": "sender@example.invalid",
     *     "method": "virtual",
     *     "expires_at": null,
     *     "message": null,
     *     "signers": [
     *       {
     *         "resource": "signer",
     *         "id": "62d6ee35c7741ca4006b9e11",
     *         "full_name": "John Signer",
     *         "email": null,
     *         "whatsapp_phone_number": null,
     *         "has_accepted_terms": false,
     *         "verification_method": null,
     *         "notification_methods": null,
     *         "step": null,
     *         "notified": null,
     *         "completed": null,
     *         "notification_history": null
     *       }
     *     ],
     *     "copy_receivers": [
     *       {}
     *     ],
     *     "items": [
     *       {
     *         "id": "id_example",
     *         "page": null,
     *         "signer": {},
     *         "field": null,
     *         "display_settings": null,
     *         "value": null,
     *         "completed": false
     *       }
     *     ],
     *     "summary": {
     *       "signer_count": 1,
     *       "completed_count": 0,
     *       "signers": [
     *         {}
     *       ]
     *     },
     *     "signing_urls": [
     *       {
     *         "signer_id": "signer_id_example",
     *         "url": "https://api.assinafy.com.br/v1/sign/doc1?email=joe@example.invalid"
     *       }
     *     ]
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The created assignment; 400 One or more fields failed
     * validation.; 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or
     * numeric envelope statuses raise <code>ApiException</code>; 401/403 raise
     * <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O failures
     * raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * @param documentId document receiving the assignment
     * @param request assignment method, signers, entries, and notification settings
     * @return the created assignment
     * @throws ValidationException if the request, method, signer references, delivery methods,
     *         signing order, or collect entries are invalid
     */
    public Assignment create(String documentId, CreateAssignmentRequest request) {
        String docId = pathSegment(documentId, "Document ID");
        Map<String, Object> body = buildAssignmentPayload(request, false);
        logInfo("Creating assignment", Map.of("documentId", docId,
                "signers", request.getSigners() != null ? request.getSigners().size() : 0));
        String json = serialise(body);
        Assignment assignment = call("Failed to create assignment",
                () -> http.post("/documents/" + docId + "/assignments", json), Assignment.class);
        if (assignment == null || assignment.getId() == null || assignment.getId().isBlank()) {
            throw new AssinafyException("Assignment creation succeeded but no assignment ID was returned");
        }
        return assignment;
    }

    /**
     * Estimate the credit cost of requesting signatures, without creating the assignment
     * ({@code POST /documents/{documentId}/assignments/estimate-cost}). Unlike creation, the
     * estimate payload contains only explicitly supplied {@code method}, {@code signers}, and
     * {@code entries}; signer IDs and steps are omitted, delivery methods are validated, and unset
     * fields are omitted. Every estimate requires at least one signer, because the API prices per
     * signer in both modes; a collect estimate additionally requires nonempty entries. Returns a
     * cost breakdown map ({@code credits},
     * {@code total_credits},
     * {@code document_balance}, {@code has_sufficient_resources}, …).
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/documents/{documentId}/assignments/estimate-cost</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "method": "virtual",
     *   "signers": [
     *     {
     *       "verification_method": "Whatsapp",
     *       "notification_methods": [
     *         "Email"
     *       ]
     *     }
     *   ],
     *   "entries": [
     *     {}
     *   ]
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "documents": 1,
     *     "credits": 1,
     *     "needs_extra_document": false,
     *     "extra_document_cost": 1,
     *     "total_credits": 1,
     *     "breakdown": [
     *       {
     *         "code": "NotificationWhatsapp",
     *         "name": "Whatsapp Notification",
     *         "cost": 0.9,
     *         "quantity": 2,
     *         "unit_cost": 0.45
     *       }
     *     ],
     *     "document_balance": 1,
     *     "credit_balance": 1,
     *     "has_sufficient_resources": false,
     *     "blocking_reason": null,
     *     "message": null
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Cost estimate and balances; 400 One or more fields failed
     * validation.; 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or
     * numeric envelope statuses raise <code>ApiException</code>; 401/403 raise
     * <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O failures
     * raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * @param documentId document to estimate
     * @param request estimate inputs
     * @return the cost breakdown
     * @throws ValidationException if the request, method, signer delivery methods, or required
     *         estimate inputs are invalid
     */
    public Map<String, Object> estimateCost(String documentId, CreateAssignmentRequest request) {
        String docId = pathSegment(documentId, "Document ID");
        Map<String, Object> body = buildAssignmentPayload(request, true);
        String json = serialise(body);
        return callMap("Failed to estimate assignment cost", () -> http.post("/documents/" + docId + "/assignments/estimate-cost", json));
    }

    /**
     * Return a typed assignment cost estimate.
     *
     * <p>Wire contract, payloads and failures: {@link #estimateCost(String, CreateAssignmentRequest)}.
     * This method converts the same payload to the declared response model.</p>
     *
     * @param documentId document to estimate
     * @param request estimate inputs
     * @return the typed cost breakdown
     */
    public CostEstimate estimateCostTyped(String documentId, CreateAssignmentRequest request) {
        return ResponseHandler.convert(estimateCost(documentId, request), CostEstimate.class);
    }

    /**
     * Update an assignment's expiration. Passing {@code expiresAt = null} sends
     * {@code expires_at: null}; use that deployment extension only when the target supports
     * clearing expiration.
     *
     * <p><strong>HTTP:</strong> <code>PUT
     * /v1/documents/{documentId}/assignments/{assignmentId}/reset-expiration</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * <li><code>assignmentId</code> (path, required): The assignment ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "expires_at": "2030-10-05T12:00:00Z"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "assignment",
     *     "id": "615606ef81d199996981dbce",
     *     "sender_email": "sender@example.invalid",
     *     "method": "virtual",
     *     "expires_at": null,
     *     "message": null,
     *     "signers": [
     *       {
     *         "resource": "signer",
     *         "id": "62d6ee35c7741ca4006b9e11",
     *         "full_name": "John Signer",
     *         "email": null,
     *         "whatsapp_phone_number": null,
     *         "has_accepted_terms": false,
     *         "verification_method": null,
     *         "notification_methods": null,
     *         "step": null,
     *         "notified": null,
     *         "completed": null,
     *         "notification_history": null
     *       }
     *     ],
     *     "copy_receivers": [
     *       {}
     *     ],
     *     "items": [
     *       {
     *         "id": "id_example",
     *         "page": null,
     *         "signer": {},
     *         "field": null,
     *         "display_settings": null,
     *         "value": null,
     *         "completed": false
     *       }
     *     ],
     *     "summary": {
     *       "signer_count": 1,
     *       "completed_count": 0,
     *       "signers": [
     *         {}
     *       ]
     *     },
     *     "signing_urls": [
     *       {
     *         "signer_id": "signer_id_example",
     *         "url": "https://api.assinafy.com.br/v1/sign/doc1?email=joe@example.invalid"
     *       }
     *     ]
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The updated assignment; 400 One or more fields failed
     * validation.; 404 The requested resource does not exist.; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param documentId owning document ID
     * @param assignmentId assignment ID
     * @param expiresAt new ISO-8601 expiration timestamp (whole-second UTC recommended), or
     *                  {@code null} to remove it
     * @return the updated assignment
     */
    public Assignment resetExpiration(String documentId, String assignmentId, String expiresAt) {
        String docId = pathSegment(documentId, "Document ID");
        String asgId = pathSegment(assignmentId, "Assignment ID");
        requireExpiration(expiresAt);
        Map<String, Object> body = new HashMap<>();
        body.put("expires_at", expiresAt);
        String json = serialise(body);
        return call("Failed to update assignment expiration",
                () -> http.put("/documents/" + docId + "/assignments/" + asgId + "/reset-expiration", json),
                Assignment.class);
    }

    /**
     * Resend the signature-request notification to one signer of an assignment
     * ({@code PUT /documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/resend}).
     *
     * <p><strong>HTTP:</strong> <code>PUT
     * /v1/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/resend</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * <li><code>assignmentId</code> (path, required): The assignment ID.</li>
     * <li><code>signerId</code> (path, required): The signer ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "is_sent": false,
     *     "document_id": "document_id_example",
     *     "signer_id": "signer_id_example"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Resend result; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param documentId owning document ID
     * @param assignmentId assignment ID
     * @param signerId recipient signer ID
     * @return resend status and delivery details
     */
    public ResendNotificationResponse resendNotification(String documentId, String assignmentId, String signerId) {
        String docId = pathSegment(documentId, "Document ID");
        String asgId = pathSegment(assignmentId, "Assignment ID");
        String sid = pathSegment(signerId, "Signer ID");
        return call("Failed to resend signer notification",
                () -> http.put("/documents/" + docId + "/assignments/" + asgId + "/signers/" + sid + "/resend", null),
                ResendNotificationResponse.class);
    }

    /**
     * Estimate the credit cost of resending a signer notification, without sending it
     * ({@code POST /documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/estimate-resend-cost}).
     *
     * <p><strong>HTTP:</strong> <code>POST
     * /v1/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/estimate-resend-cost</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * <li><code>assignmentId</code> (path, required): The assignment ID.</li>
     * <li><code>signerId</code> (path, required): The signer ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "documents": 1,
     *     "credits": 1,
     *     "needs_extra_document": false,
     *     "extra_document_cost": 1,
     *     "total_credits": 1,
     *     "breakdown": [
     *       {
     *         "code": "NotificationWhatsapp",
     *         "name": "Whatsapp Notification",
     *         "cost": 0.9,
     *         "quantity": 2,
     *         "unit_cost": 0.45
     *       }
     *     ],
     *     "document_balance": 1,
     *     "credit_balance": 1,
     *     "has_sufficient_resources": false,
     *     "blocking_reason": null,
     *     "message": null
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Cost estimate; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param documentId owning document ID
     * @param assignmentId assignment ID
     * @param signerId recipient signer ID
     * @return the resend cost breakdown
     */
    public Map<String, Object> estimateResendCost(String documentId, String assignmentId, String signerId) {
        String docId = pathSegment(documentId, "Document ID");
        String asgId = pathSegment(assignmentId, "Assignment ID");
        String sid = pathSegment(signerId, "Signer ID");
        return callMap("Failed to estimate resend cost",
                () -> http.post("/documents/" + docId + "/assignments/" + asgId + "/signers/" + sid + "/estimate-resend-cost", null));
    }

    /**
     * Return a typed resend cost estimate.
     *
     * <p>Wire contract, payloads and failures: {@link #estimateResendCost(String, String, String)}.
     * This method converts the same payload to the declared response model.</p>
     *
     * @param documentId owning document ID
     * @param assignmentId assignment ID
     * @param signerId recipient signer ID
     * @return the typed resend cost breakdown
     */
    public CostEstimate estimateResendCostTyped(String documentId, String assignmentId, String signerId) {
        return ResponseHandler.convert(estimateResendCost(documentId, assignmentId, signerId), CostEstimate.class);
    }

    /**
     * Signer-side decline of an assignment. Requires the signer-access-code that was issued
     * to the signer in the invitation flow. A non-blank {@code declineReason} is required.
     *
     * <p>Maps to {@code PUT /documents/{documentId}/assignments/{assignmentId}/reject}.
     *
     * <p><strong>HTTP:</strong> <code>PUT /v1/documents/{documentId}/assignments/{assignmentId}/reject</code>.
     * <strong>Authentication:</strong> <code>signer-access-code</code> query credential.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * <li><code>assignmentId</code> (path, required): The assignment ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "decline_reason": "I do not agree with clause 2."
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     []
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Assignment declined; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param documentId owning document ID
     * @param assignmentId assignment ID
     * @param signerAccessCode signer invitation access code
     * @param declineReason nonblank reason for declining
     * @return the API response payload
     * @throws ValidationException if an identifier, access code, or reason is blank
     */
    public Map<String, Object> decline(String documentId, String assignmentId, String signerAccessCode, String declineReason) {
        String docId = pathSegment(documentId, "Document ID");
        String asgId = pathSegment(assignmentId, "Assignment ID");
        requireId(signerAccessCode, "Signer access code");
        requireText(declineReason, "Decline reason", 2000);
        String json = serialise(Map.of("decline_reason", declineReason));
        return callMap("Failed to decline assignment",
                () -> http.put(withAccessCode(
                        "/documents/" + docId + "/assignments/" + asgId + "/reject", signerAccessCode),
                        json));
    }

    /**
     * Inspect WhatsApp notification delivery status for an assignment. Returns one entry per
     * tracked notification.
     *
     * <p>Maps to {@code GET /documents/{documentId}/assignments/{assignmentId}/whatsapp-notifications}.
     *
     * <p><strong>HTTP:</strong> <code>GET
     * /v1/documents/{documentId}/assignments/{assignmentId}/whatsapp-notifications</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * <li><code>assignmentId</code> (path, required): The assignment ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     {
     *       "sent_at": 1710000000,
     *       "header": "Documento para assinatura: Contrato de Servico",
     *       "body": "body_example",
     *       "buttons": [
     *         {
     *           "text": "Abrir documento"
     *         }
     *       ],
     *       "phone_number": "+5511999990001",
     *       "signer_id": "a51edaee68a7"
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 WhatsApp notifications; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param documentId owning document ID
     * @param assignmentId assignment ID
     * @return raw notification delivery entries
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getWhatsappNotifications(String documentId, String assignmentId) {
        String docId = pathSegment(documentId, "Document ID");
        String asgId = pathSegment(assignmentId, "Assignment ID");
        PaginatedResult<?> result = callList("Failed to fetch WhatsApp notifications",
                () -> http.get("/documents/" + docId + "/assignments/" + asgId + "/whatsapp-notifications"),
                Map.class);
        return (List<Map<String, Object>>) (List<?>) result.getData();
    }

    /**
     * Return typed WhatsApp delivery entries.
     *
     * <p>Wire contract, payloads and failures: {@link #getWhatsappNotifications(String, String)}.
     * This method converts the same payload to the declared response model.</p>
     *
     * @param documentId owning document ID
     * @param assignmentId assignment ID
     * @return typed notification delivery entries
     */
    public List<WhatsappNotification> getWhatsappNotificationsTyped(String documentId, String assignmentId) {
        return getWhatsappNotifications(documentId, assignmentId).stream()
                .map(item -> ResponseHandler.convert(item, WhatsappNotification.class))
                .toList();
    }

    /**
     * Signer-facing fetch of the document + assignment to be signed.
     *
     * <p>Maps to {@code GET /sign?signer-access-code={code}}.
     *
     * <p>Wire contract, payloads and failures: {@link #getForSigner(String, Boolean)}.</p>
     *
     * @param signerAccessCode signer invitation access code
     * @return raw document and assignment data
     */
    public Map<String, Object> getForSigner(String signerAccessCode) {
        return getForSigner(signerAccessCode, null);
    }

    /**
     * Return typed signer-facing document details.
     *
     * <p>Wire contract, payloads and failures: {@link #getForSignerTyped(String, Boolean)}.</p>
     *
     * @param signerAccessCode signer invitation access code
     * @return typed document and assignment data
     */
    public Document getForSignerTyped(String signerAccessCode) {
        return getForSignerTyped(signerAccessCode, null);
    }

    /**
     * Fetch the signer document and optionally send the documented terms-acceptance flag.
     * Digital-certificate signers must normally accept terms through {@code acceptTerms} before
     * this call because the document gate runs first.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/sign</code>.
     * <strong>Authentication:</strong> <code>signer-access-code</code> query credential.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>has_accepted_terms</code> (query, optional): Set true to record terms acceptance.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "document",
     *     "id": "615601fab04c0a3147bb1246",
     *     "account_id": "d199996981dbd199996981db",
     *     "template_id": null,
     *     "name": "document.pdf",
     *     "status": "metadata_ready",
     *     "artifacts": {
     *       "original": "https://api.assinafy.com.br/v1/documents/doc1/download/original"
     *     },
     *     "is_closed": false,
     *     "signing_url": "https://api.assinafy.com.br/v1/sign/doc1",
     *     "decline_reason": null,
     *     "declined_by": null,
     *     "tags": [
     *       {
     *         "id": "id_example",
     *         "name": "name_example"
     *       }
     *     ],
     *     "assignment": null,
     *     "pages": [
     *       {
     *         "id": "615601faf166d6d1d8e7dc30",
     *         "number": 1,
     *         "height": 2100,
     *         "width": 1275,
     *         "download_url": "https://api.assinafy.com.br/v1/documents/doc1/pages/1a/download"
     *       }
     *     ],
     *     "created_at": "2026-06-03T03:54:16Z",
     *     "updated_at": "2026-06-03T03:54:16Z"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The document with the signer&#x27;s assignment; 400 A
     * digital-certificate signer has not yet confirmed their data or accepted the terms.; 409 The
     * document is not ready to be viewed yet.; 401 Missing or invalid credentials.; 500 Unexpected
     * server error. Non-2xx HTTP or numeric envelope statuses raise <code>ApiException</code>; 401/403
     * raise <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O
     * failures raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * @param signerAccessCode signer invitation access code
     * @param hasAcceptedTerms optional terms-acceptance flag
     * @return raw document and assignment data
     */
    public Map<String, Object> getForSigner(String signerAccessCode, Boolean hasAcceptedTerms) {
        requireId(signerAccessCode, "Signer access code");
        String path = withAccessCode("/sign", signerAccessCode);
        if (hasAcceptedTerms != null) path += "&has_accepted_terms=" + hasAcceptedTerms;
        String finalPath = path;
        return callMap("Failed to fetch signer assignment",
                () -> http.get(finalPath));
    }

    /**
     * Return typed signer-facing document details with an optional terms flag.
     *
     * <p>Wire contract, payloads and failures: {@link #getForSigner(String, Boolean)}.
     * This method converts the same payload to the declared response model.</p>
     *
     * @param signerAccessCode signer invitation access code
     * @param hasAcceptedTerms optional terms-acceptance flag
     * @return typed document and assignment data
     */
    public Document getForSignerTyped(String signerAccessCode, Boolean hasAcceptedTerms) {
        return ResponseHandler.convert(getForSigner(signerAccessCode, hasAcceptedTerms), Document.class);
    }

    /**
     * Signer-facing submission of completed assignment items.
     *
     * <p>Maps to {@code POST /documents/{documentId}/assignments/{assignmentId}?signer-access-code={code}}.
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/documents/{documentId}/assignments/{assignmentId}</code>.
     * <strong>Authentication:</strong> <code>signer-access-code</code> query credential.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * <li><code>assignmentId</code> (path, required): The assignment ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>[
     *   {
     *     "itemId": "615606efcde1a39c9d21e30e",
     *     "fieldId": "6152120297080d55bdd13197",
     *     "pageId": "615213ed81b071f4293b2fc2",
     *     "value": "Signed by Sonny Bayer"
     *   }
     * ]</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {}
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Signing result; 400 Signer data must be confirmed before signing
     * (virtual assignments), or the signer must sign with a digital certificate through the digital
     * certificate endpoints.; 409 The document is not ready to be signed yet.; 401 Missing or invalid
     * credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param documentId owning document ID
     * @param assignmentId assignment ID
     * @param signerAccessCode signer invitation access code
     * @param items completed-item list; every item requires string {@code itemId},
     *              {@code fieldId}, {@code pageId}, and {@code value} properties
     * @return the API response payload
     */
    public Map<String, Object> sign(String documentId, String assignmentId, String signerAccessCode,
                                    List<Map<String, Object>> items) {
        String docId = pathSegment(documentId, "Document ID");
        String asgId = pathSegment(assignmentId, "Assignment ID");
        requireId(signerAccessCode, "Signer access code");
        validateSigningItems(items);
        String json = serialise(items);
        return callMap("Failed to submit signature",
                () -> http.post(withAccessCode(
                        "/documents/" + docId + "/assignments/" + asgId, signerAccessCode),
                        json));
    }

    static Map<String, Object> buildAssignmentPayload(CreateAssignmentRequest request, boolean estimate) {
        if (request == null) {
            throw new ValidationException("Assignment request is required");
        }
        String method = request.getMethod();
        SigningRules.validateMethod(method);
        List<SignerReference> signers = request.getSigners();
        if (!estimate && (signers == null || signers.isEmpty())) {
            throw new ValidationException("At least one signer is required");
        }
        // The contract marks signers required only for virtual, but the API prices per signer in
        // both modes and rejects a signer-less estimate with
        // 400 "Pelo menos um signatários precisa ser informado."
        if (estimate && (signers == null || signers.isEmpty())) {
            throw new ValidationException("At least one signer is required for a cost estimate");
        }
        if (COLLECT.equals(method)
                && (request.getEntries() == null || request.getEntries().isEmpty())) {
            throw new ValidationException("At least one entry is required for a collect assignment");
        }

        List<Map<String, Object>> normalisedSigners = (signers != null ? signers : List.<SignerReference>of()).stream()
                .map(ref -> normaliseSignerRef(ref, estimate))
                .toList();
        if (!estimate) validateSignerSteps(signers);

        Map<String, Object> body = new HashMap<>();
        if (method != null || !estimate) body.put("method", method != null ? method : VIRTUAL);
        body.put("signers", normalisedSigners);
        if (request.getEntries() != null) body.put("entries", request.getEntries());
        if (!estimate) {
            requireExpiration(request.getExpiresAt());
            if (request.getMessage() != null) body.put("message", request.getMessage());
            if (request.getExpiresAt() != null) body.put("expires_at", request.getExpiresAt());
            if (request.getCopyReceivers() != null) body.put("copy_receivers", request.getCopyReceivers());
        }
        return body;
    }

    private static Map<String, Object> normaliseSignerRef(SignerReference ref, boolean allowWithoutId) {
        if (ref == null) throw new ValidationException("Signer reference is required");
        Map<String, Object> map = new HashMap<>();
        if (!allowWithoutId) {
            if (ref.getId() == null || ref.getId().isBlank()) {
                throw new ValidationException("Invalid signer reference: id is required");
            }
            map.put("id", ref.getId());
        }
        SigningRules.validateDeliveryMethods(ref.getVerificationMethod(), ref.getNotificationMethods());
        if (ref.getVerificationMethod() != null) map.put("verification_method", ref.getVerificationMethod());
        if (ref.getNotificationMethods() != null) map.put("notification_methods", ref.getNotificationMethods());
        if (!allowWithoutId && ref.getStep() != null) {
            if (ref.getStep() < 1) throw new ValidationException("Signer step must be positive");
            map.put("step", ref.getStep());
        }
        return map;
    }

    private static void validateSignerSteps(List<SignerReference> signers) {
        SigningRules.validateSigningOrder(signers.stream()
                .map(ref -> new SigningRules.Placement(ref.getStep(), ref.getVerificationMethod()))
                .toList(), "signer");
    }

    private static void validateSigningItems(List<Map<String, Object>> items) {
        if (items == null) throw new ValidationException("Signing items are required");
        for (Map<String, Object> item : items) {
            if (item == null) throw new ValidationException("Signing item is required");
            for (String key : List.of("itemId", "fieldId", "pageId")) {
                if (!(item.get(key) instanceof String)) {
                    throw new ValidationException("Signing item " + key + " must be a string");
                }
            }
            if (!(item.get("value") instanceof String)) {
                throw new ValidationException("Signing item value must be a string");
            }
        }
    }
}
