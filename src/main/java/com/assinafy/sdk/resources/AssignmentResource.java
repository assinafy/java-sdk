package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.ResendEmailResponse;
import com.assinafy.sdk.request.CreateAssignmentRequest;
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

    public Assignment create(String documentId, CreateAssignmentRequest request) {
        String docId = requireId(documentId, "Document ID");
        Map<String, Object> body = buildAssignmentPayload(request, false);
        logger.info("Creating assignment", Map.of("documentId", docId, "signers", request.getSigners() != null ? request.getSigners().size() : 0));
        String json = serialise(body);
        return call("Failed to create assignment", () -> http.post("/documents/" + docId + "/assignments", json), Assignment.class);
    }

    public Map<String, Object> estimateCost(String documentId, CreateAssignmentRequest request) {
        String docId = requireId(documentId, "Document ID");
        Map<String, Object> body = buildAssignmentPayload(request, true);
        String json = serialise(body);
        return callMap("Failed to estimate assignment cost", () -> http.post("/documents/" + docId + "/assignments/estimate-cost", json));
    }

    public Assignment resetExpiration(String documentId, String assignmentId, String expiresAt) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        String json = serialise(Map.of("expires_at", expiresAt));
        return call("Failed to update assignment expiration",
                () -> http.put("/documents/" + docId + "/assignments/" + asgId + "/reset-expiration", json),
                Assignment.class);
    }

    public ResendEmailResponse resendNotification(String documentId, String assignmentId, String signerId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        String sid = requireId(signerId, "Signer ID");
        return call("Failed to resend signer notification",
                () -> http.put("/documents/" + docId + "/assignments/" + asgId + "/signers/" + sid + "/resend", null),
                ResendEmailResponse.class);
    }

    public Map<String, Object> estimateResendCost(String documentId, String assignmentId, String signerId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        String sid = requireId(signerId, "Signer ID");
        return callMap("Failed to estimate resend cost",
                () -> http.post("/documents/" + docId + "/assignments/" + asgId + "/signers/" + sid + "/estimate-resend-cost", null));
    }

    /**
     * Signer-side decline of an assignment. Requires the signer-access-code that was issued
     * to the signer in the invitation flow.
     *
     * <p>Maps to {@code PUT /documents/{documentId}/assignments/{assignmentId}/reject}.
     */
    public Map<String, Object> decline(String documentId, String assignmentId, String signerAccessCode, String declineReason) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        requireId(signerAccessCode, "Signer access code");
        Map<String, Object> body = new HashMap<>();
        if (declineReason != null && !declineReason.isBlank()) {
            body.put("decline_reason", declineReason);
        }
        String json = serialise(body);
        return callMap("Failed to decline assignment",
                () -> http.put(
                        "/documents/" + docId + "/assignments/" + asgId + "/reject?signer-access-code=" + encode(signerAccessCode),
                        json));
    }

    /**
     * Inspect WhatsApp notification delivery status for an assignment.
     *
     * <p>Maps to {@code GET /documents/{documentId}/assignments/{assignmentId}/whatsapp-notifications}.
     */
    public Map<String, Object> getWhatsappNotifications(String documentId, String assignmentId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        return callMap("Failed to fetch WhatsApp notifications",
                () -> http.get("/documents/" + docId + "/assignments/" + asgId + "/whatsapp-notifications"));
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
        return map;
    }
}
