package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.ResendNotificationResponse;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.SignerReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignmentResource extends BaseResource {

    public AssignmentResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    public AssignmentResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    public AssignmentResource(ApiHttpClient http) {
        super(http);
    }

    /**
     * List the assignments belonging to the authenticated user's <em>current account</em>
     * ({@code GET /v1/assignments}), paginated via {@code page}/{@code per-page}.
     *
     * <p><strong>Note:</strong> this endpoint resolves the account from an interactive session and
     * is <em>not</em> available to API-key clients — with {@code X-Api-Key} auth the API returns
     * {@code 400 "account context required"} (verified against the sandbox). It works only when the
     * client is configured with a session/Bearer token. To inspect a single document's assignment
     * use {@link DocumentResource#details(String)} ({@code getAssignment()}).
     */
    public PaginatedResult<Assignment> list(ListParams params) {
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list assignments", () -> http.get("/assignments", queryParams), Assignment.class);
    }

    public PaginatedResult<Assignment> list() {
        return list(new ListParams());
    }

    /**
     * Request signatures for a document ({@code POST /documents/{documentId}/assignments}). The
     * request {@code method} defaults to {@code virtual} when unset; at least one signer is
     * required (each {@link SignerReference} needs a signer {@code id}). Returns the created
     * {@link Assignment} (signers, items, summary and per-signer signing URLs).
     */
    public Assignment create(String documentId, CreateAssignmentRequest request) {
        String docId = requireId(documentId, "Document ID");
        Map<String, Object> body = buildAssignmentPayload(request, false);
        logger.info("Creating assignment", Map.of("documentId", docId, "signers", request.getSigners() != null ? request.getSigners().size() : 0));
        String json = serialise(body);
        return call("Failed to create assignment", () -> http.post("/documents/" + docId + "/assignments", json), Assignment.class);
    }

    /**
     * Estimate the credit cost of requesting signatures, without creating the assignment
     * ({@code POST /documents/{documentId}/assignments/estimate-cost}). Same payload as
     * {@link #create}; returns a cost breakdown map ({@code credits}, {@code total_credits},
     * {@code document_balance}, {@code has_sufficient_resources}, …).
     */
    public Map<String, Object> estimateCost(String documentId, CreateAssignmentRequest request) {
        String docId = requireId(documentId, "Document ID");
        Map<String, Object> body = buildAssignmentPayload(request, true);
        String json = serialise(body);
        return callMap("Failed to estimate assignment cost", () -> http.post("/documents/" + docId + "/assignments/estimate-cost", json));
    }

    /**
     * Update an assignment's expiration. Pass {@code expiresAt = null} to remove the
     * expiration entirely (the assignment will no longer expire).
     */
    public Assignment resetExpiration(String documentId, String assignmentId, String expiresAt) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
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
     */
    public ResendNotificationResponse resendNotification(String documentId, String assignmentId, String signerId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        String sid = requireId(signerId, "Signer ID");
        return call("Failed to resend signer notification",
                () -> http.put("/documents/" + docId + "/assignments/" + asgId + "/signers/" + sid + "/resend", null),
                ResendNotificationResponse.class);
    }

    /**
     * Estimate the credit cost of resending a signer notification, without sending it
     * ({@code POST /documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/estimate-resend-cost}).
     */
    public Map<String, Object> estimateResendCost(String documentId, String assignmentId, String signerId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        String sid = requireId(signerId, "Signer ID");
        return callMap("Failed to estimate resend cost",
                () -> http.post("/documents/" + docId + "/assignments/" + asgId + "/signers/" + sid + "/estimate-resend-cost", null));
    }

    /**
     * Signer-side decline of an assignment. Requires the signer-access-code that was issued
     * to the signer in the invitation flow. A non-blank {@code declineReason} is required.
     *
     * <p>Maps to {@code PUT /documents/{documentId}/assignments/{assignmentId}/reject}.
     */
    public Map<String, Object> decline(String documentId, String assignmentId, String signerAccessCode, String declineReason) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
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
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getWhatsappNotifications(String documentId, String assignmentId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        PaginatedResult<?> result = callList("Failed to fetch WhatsApp notifications",
                () -> http.get("/documents/" + docId + "/assignments/" + asgId + "/whatsapp-notifications"),
                Map.class);
        return (List<Map<String, Object>>) (List<?>) result.getData();
    }

    /**
     * Signer-facing fetch of the document + assignment to be signed.
     *
     * <p>Maps to {@code GET /sign?signer-access-code={code}}.
     */
    public Map<String, Object> getForSigner(String signerAccessCode) {
        requireId(signerAccessCode, "Signer access code");
        return callMap("Failed to fetch signer assignment",
                () -> http.get("/sign?signer-access-code=" + encode(signerAccessCode)));
    }

    /**
     * Signer-facing submission of completed assignment items.
     *
     * <p>Maps to {@code POST /documents/{documentId}/assignments/{assignmentId}?signer-access-code={code}}.
     *
     * @param items the completed items, each typically {@code {itemId, fieldId, pageId, value}}
     */
    public Map<String, Object> sign(String documentId, String assignmentId, String signerAccessCode,
                                    List<Map<String, Object>> items) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        requireId(signerAccessCode, "Signer access code");
        String json = serialise(items != null ? items : List.of());
        return callMap("Failed to submit signature",
                () -> http.post(
                        "/documents/" + docId + "/assignments/" + asgId + "?signer-access-code=" + encode(signerAccessCode),
                        json));
    }

    static Map<String, Object> buildAssignmentPayload(CreateAssignmentRequest request, boolean allowSignersWithoutId) {
        List<SignerReference> signers = request.getSigners();
        if (signers == null || signers.isEmpty()) {
            throw new ValidationException("At least one signer is required");
        }

        List<Map<String, Object>> normalisedSigners = signers.stream()
                .map(ref -> normaliseSignerRef(ref, allowSignersWithoutId))
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("method", request.getMethod() != null ? request.getMethod() : "virtual");
        body.put("signers", normalisedSigners);
        if (request.getMessage() != null) body.put("message", request.getMessage());
        if (request.getExpiresAt() != null) body.put("expires_at", request.getExpiresAt());
        if (request.getCopyReceivers() != null) body.put("copy_receivers", request.getCopyReceivers());
        if (request.getEntries() != null) body.put("entries", request.getEntries());
        return body;
    }

    private static Map<String, Object> normaliseSignerRef(SignerReference ref, boolean allowWithoutId) {
        Map<String, Object> map = new HashMap<>();
        if (ref.getId() != null && !ref.getId().isBlank()) {
            map.put("id", ref.getId());
        } else if (!allowWithoutId) {
            throw new ValidationException("Invalid signer reference: id is required");
        }
        if (ref.getVerificationMethod() != null) map.put("verification_method", ref.getVerificationMethod());
        if (ref.getNotificationMethods() != null) map.put("notification_methods", ref.getNotificationMethods());
        if (ref.getStep() != null) map.put("step", ref.getStep());
        return map;
    }
}
