package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.CostEstimate;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.ResendNotificationResponse;
import com.assinafy.sdk.models.WhatsappNotification;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.SignerReference;
import com.assinafy.sdk.util.ResponseHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Assignment creation, cost estimation, notification, and signer-facing operations. */
public class AssignmentResource extends BaseResource {

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
     * <p>The deployed sandbox requires an {@code accountId} query when API-key authentication is
     * used. This overload supplies the client's default account automatically; bearer sessions may
     * resolve their account without it.
     *
     * @param params paging and filtering parameters, or {@code null}
     * @return matching assignments and pagination metadata
     */
    public PaginatedResult<Assignment> list(ListParams params) {
        return list(params, null);
    }

    /**
     * List assignments, adding the live API's required {@code accountId} query context when an
     * explicit or default account is available. The parameter is not yet described by OpenAPI.
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
     * @return matching assignments and pagination metadata
     */
    public PaginatedResult<Assignment> list() {
        return list(new ListParams());
    }

    /**
     * Request signatures for a document ({@code POST /documents/{documentId}/assignments}). The
     * request {@code method} defaults to {@code virtual} when unset; at least one signer is
     * required (each {@link SignerReference} needs a signer {@code id}). Returns the created
     * {@link Assignment} (signers, items, summary and per-signer signing URLs).
     *
     * @param documentId document receiving the assignment
     * @param request assignment method, signers, entries, and notification settings
     * @return the created assignment
     * @throws ValidationException if the request, method, or signer references are invalid
     */
    public Assignment create(String documentId, CreateAssignmentRequest request) {
        String docId = pathSegment(documentId, "Document ID");
        Map<String, Object> body = buildAssignmentPayload(request, false);
        logger.info("Creating assignment", Map.of("documentId", docId, "signers", request.getSigners() != null ? request.getSigners().size() : 0));
        String json = serialise(body);
        return call("Failed to create assignment", () -> http.post("/documents/" + docId + "/assignments", json), Assignment.class);
    }

    /**
     * Estimate the credit cost of requesting signatures, without creating the assignment
     * ({@code POST /documents/{documentId}/assignments/estimate-cost}). Unlike creation, the
     * estimate payload contains only explicitly supplied {@code method}, {@code signers}, and
     * {@code entries}; unset fields are omitted. Returns a cost breakdown map ({@code credits},
     * {@code total_credits},
     * {@code document_balance}, {@code has_sufficient_resources}, …).
     *
     * @param documentId document to estimate
     * @param request estimate inputs
     * @return the cost breakdown
     * @throws ValidationException if the request or method is invalid
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
     * @param documentId document to estimate
     * @param request estimate inputs
     * @return the typed cost breakdown
     */
    public CostEstimate estimateCostTyped(String documentId, CreateAssignmentRequest request) {
        return ResponseHandler.convert(estimateCost(documentId, request), CostEstimate.class);
    }

    /**
     * Update an assignment's expiration. Pass {@code expiresAt = null} to remove the
     * expiration entirely (the assignment will no longer expire).
     *
     * @param documentId owning document ID
     * @param assignmentId assignment ID
     * @param expiresAt new expiration timestamp, or {@code null} to remove it
     * @return the updated assignment
     */
    public Assignment resetExpiration(String documentId, String assignmentId, String expiresAt) {
        String docId = pathSegment(documentId, "Document ID");
        String asgId = pathSegment(assignmentId, "Assignment ID");
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
        requireId(declineReason, "Decline reason");
        String json = serialise(Map.of("decline_reason", declineReason));
        return callMap("Failed to decline assignment",
                () -> http.put(
                        "/documents/" + docId + "/assignments/" + asgId + "/reject?signer-access-code=" + encode(signerAccessCode),
                        json));
    }

    /**
     * Inspect WhatsApp notification delivery status for an assignment. Returns one entry per
     * tracked notification.
     *
     * <p>Maps to {@code GET /documents/{documentId}/assignments/{assignmentId}/whatsapp-notifications}.
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
     * @param signerAccessCode signer invitation access code
     * @return raw document and assignment data
     */
    public Map<String, Object> getForSigner(String signerAccessCode) {
        return getForSigner(signerAccessCode, null);
    }

    /**
     * Return typed signer-facing document details.
     *
     * @param signerAccessCode signer invitation access code
     * @return typed document and assignment data
     */
    public DocumentDetails getForSignerTyped(String signerAccessCode) {
        return getForSignerTyped(signerAccessCode, null);
    }

    /**
     * Fetch the signer document and optionally send the documented terms-acceptance flag.
     * Digital-certificate signers must normally accept terms through {@code acceptTerms} before
     * this call because the document gate runs first.
     *
     * @param signerAccessCode signer invitation access code
     * @param hasAcceptedTerms optional terms-acceptance flag
     * @return raw document and assignment data
     */
    public Map<String, Object> getForSigner(String signerAccessCode, Boolean hasAcceptedTerms) {
        requireId(signerAccessCode, "Signer access code");
        String path = "/sign?signer-access-code=" + encode(signerAccessCode);
        if (hasAcceptedTerms != null) path += "&has_accepted_terms=" + hasAcceptedTerms;
        String finalPath = path;
        return callMap("Failed to fetch signer assignment",
                () -> http.get(finalPath));
    }

    /**
     * Return typed signer-facing document details with an optional terms flag.
     *
     * @param signerAccessCode signer invitation access code
     * @param hasAcceptedTerms optional terms-acceptance flag
     * @return typed document and assignment data
     */
    public DocumentDetails getForSignerTyped(String signerAccessCode, Boolean hasAcceptedTerms) {
        return ResponseHandler.convert(getForSigner(signerAccessCode, hasAcceptedTerms), DocumentDetails.class);
    }

    /**
     * Signer-facing submission of completed assignment items.
     *
     * <p>Maps to {@code POST /documents/{documentId}/assignments/{assignmentId}?signer-access-code={code}}.
     *
     * @param documentId owning document ID
     * @param assignmentId assignment ID
     * @param signerAccessCode signer invitation access code
     * @param items the completed items, each typically {@code {itemId, fieldId, pageId, value}}
     * @return the API response payload
     */
    public Map<String, Object> sign(String documentId, String assignmentId, String signerAccessCode,
                                    List<Map<String, Object>> items) {
        String docId = pathSegment(documentId, "Document ID");
        String asgId = pathSegment(assignmentId, "Assignment ID");
        requireId(signerAccessCode, "Signer access code");
        String json = serialise(items != null ? items : List.of());
        return callMap("Failed to submit signature",
                () -> http.post(
                        "/documents/" + docId + "/assignments/" + asgId + "?signer-access-code=" + encode(signerAccessCode),
                        json));
    }

    static Map<String, Object> buildAssignmentPayload(CreateAssignmentRequest request, boolean estimate) {
        if (request == null) {
            throw new ValidationException("Assignment request is required");
        }
        String method = request.getMethod();
        if (method != null && !method.equals("virtual") && !method.equals("collect")) {
            throw new ValidationException("Assignment method must be virtual or collect");
        }
        List<SignerReference> signers = request.getSigners();
        if (!estimate && (signers == null || signers.isEmpty())) {
            throw new ValidationException("At least one signer is required");
        }

        List<Map<String, Object>> normalisedSigners = (signers != null ? signers : List.<SignerReference>of()).stream()
                .map(ref -> normaliseSignerRef(ref, estimate))
                .toList();

        Map<String, Object> body = new HashMap<>();
        if (method != null || !estimate) body.put("method", method != null ? method : "virtual");
        if (!normalisedSigners.isEmpty() || !estimate) body.put("signers", normalisedSigners);
        if (request.getEntries() != null) body.put("entries", request.getEntries());
        if (!estimate) {
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
        if (ref.getVerificationMethod() != null) map.put("verification_method", ref.getVerificationMethod());
        if (ref.getNotificationMethods() != null) map.put("notification_methods", ref.getNotificationMethods());
        if (!allowWithoutId && ref.getStep() != null) {
            if (ref.getStep() < 1) throw new ValidationException("Signer step must be positive");
            map.put("step", ref.getStep());
        }
        return map;
    }
}
