package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.DocumentActivity;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.DocumentListItem;
import com.assinafy.sdk.models.DocumentStatusInfo;
import com.assinafy.sdk.models.DocumentUploadResponse;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.SigningProgress;
import com.assinafy.sdk.models.Tag;
import com.assinafy.sdk.models.enums.DocumentArtifactName;
import com.assinafy.sdk.models.enums.DocumentStatus;
import com.assinafy.sdk.request.CreateDocumentFromTemplateRequest;
import com.assinafy.sdk.request.ListParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DocumentResource extends BaseResource {

    private static final long MAX_UPLOAD_BYTES = 25L * 1024 * 1024;

    /** Default artifact for {@link #download(String)} — the signed/certificated PDF. */
    private static final String DEFAULT_ARTIFACT = DocumentArtifactName.CERTIFICATED.getValue();
    private static final String CERTIFICATED = DocumentStatus.CERTIFICATED.getValue();

    private static final Set<String> READY_STATUSES = Set.of(
            "metadata_ready", "pending_signature", CERTIFICATED
    );

    private static final Set<String> FAILED_STATUSES = Set.of(
            "failed", "rejected_by_signer", "rejected_by_user", "expired"
    );

    public DocumentResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    public DocumentResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    /**
     * Upload a PDF and create a document ({@code POST /accounts/{accountId}/documents}). The file
     * must be a PDF (validated by extension), non-empty, and at most 25 MB.
     *
     * @throws com.assinafy.sdk.exceptions.ValidationException if the file is missing, not a PDF, or too large
     */
    public DocumentUploadResponse upload(byte[] fileData, String fileName) {
        return upload(fileData, fileName, null, null);
    }

    /**
     * Upload a PDF and create a document, with optional document metadata and an explicit account.
     *
     * @param fileData  the PDF bytes (non-empty, ≤ 25 MB)
     * @param fileName  the file name (must end in {@code .pdf})
     * @param metadata  optional metadata stored with the document, or {@code null}
     * @param accountId workspace/account ID; falls back to the client default when {@code null}
     * @throws com.assinafy.sdk.exceptions.ValidationException if the file is invalid
     */
    public DocumentUploadResponse upload(byte[] fileData, String fileName, Map<String, Object> metadata, String accountId) {
        validateUpload(fileData, fileName);
        String id = accountId(accountId);
        String metadataJson = null;
        if (metadata != null) {
            metadataJson = serialise(metadata);
        }
        logger.info("Uploading document", Map.of("fileName", fileName, "size", fileData.length));
        String finalMetadata = metadataJson;
        DocumentUploadResponse document = call("Document upload failed",
                () -> http.postMultipart("/accounts/" + id + "/documents", fileName, fileData, fileName, finalMetadata),
                DocumentUploadResponse.class);
        if (document == null || document.getId() == null) {
            throw new ValidationException("Upload succeeded but no document ID was returned");
        }
        logger.info("Document uploaded", Map.of("documentId", document.getId()));
        return document;
    }

    /** List documents in the default workspace ({@code GET /accounts/{accountId}/documents}). */
    public PaginatedResult<DocumentListItem> list() {
        return list(new ListParams(), null);
    }

    public PaginatedResult<DocumentListItem> list(ListParams params) {
        return list(params, null);
    }

    public PaginatedResult<DocumentListItem> list(ListParams params, String accountId) {
        String id = accountId(accountId);
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list documents", () -> http.get("/accounts/" + id + "/documents", queryParams), DocumentListItem.class);
    }

    /** Fetch full document details ({@code GET /documents/{documentId}}), including the assignment. */
    public DocumentDetails details(String documentId) {
        String id = requireId(documentId, "Document ID");
        return call("Failed to fetch document details", () -> http.get("/documents/" + id), DocumentDetails.class);
    }

    /** Alias for {@link #details(String)}. */
    public DocumentDetails get(String documentId) {
        return details(documentId);
    }

    /** Poll {@link #details(String)} until the document is ready, using a 30s timeout and 2s interval. */
    public DocumentDetails waitUntilReady(String documentId) {
        return waitUntilReady(documentId, 30_000, 2_000);
    }

    /**
     * Poll {@link #details(String)} until the document reaches a ready status
     * ({@code metadata_ready}/{@code pending_signature}/{@code certificated}).
     *
     * @param maxWaitMs      maximum time to wait before giving up
     * @param pollIntervalMs delay between status checks
     * @throws com.assinafy.sdk.exceptions.ValidationException if the document enters a failed status or the wait times out
     */
    public DocumentDetails waitUntilReady(String documentId, long maxWaitMs, long pollIntervalMs) {
        String id = requireId(documentId, "Document ID");
        long start = System.currentTimeMillis();
        int attempts = 0;
        logger.info("Waiting for document to be ready", Map.of("documentId", id, "maxWaitMs", maxWaitMs));

        while (System.currentTimeMillis() - start < maxWaitMs) {
            attempts++;
            try {
                DocumentDetails details = this.details(id);
                String status = details.getStatus() != null ? details.getStatus() : "unknown";
                logger.debug("Document status check", Map.of("attempts", attempts, "status", status));
                if (READY_STATUSES.contains(status)) return details;
                if (FAILED_STATUSES.contains(status)) {
                    throw new ValidationException("Document processing failed with status: " + status, Map.of("status", status));
                }
            } catch (ValidationException e) {
                throw e;
            } catch (Exception e) {
                logger.warn("Error checking document status", Map.of("error", e.getMessage() != null ? e.getMessage() : ""));
            }
            sleep(pollIntervalMs);
        }
        throw new ValidationException("Timeout waiting for document to be ready", Map.of("documentId", id, "attempts", attempts));
    }

    /** Download the default ({@code certificated}) artifact as bytes. Throws on a non-2xx response. */
    public byte[] download(String documentId) {
        return download(documentId, DEFAULT_ARTIFACT);
    }

    /**
     * Download a document artifact as bytes ({@code GET /documents/{id}/download/{artifact}}).
     *
     * @param artifactName one of {@code original}, {@code certificated}, {@code certificate-page},
     *                     {@code bundle}; defaults to {@code certificated} when {@code null}
     * @throws com.assinafy.sdk.exceptions.ApiException if the artifact is unavailable or the document is missing
     */
    public byte[] download(String documentId, String artifactName) {
        String id = requireId(documentId, "Document ID");
        String artifact = artifactName != null ? artifactName : DEFAULT_ARTIFACT;
        return callBinary("Failed to download document", () -> http.getBinary("/documents/" + id + "/download/" + artifact));
    }

    /** Download the document thumbnail image as bytes ({@code GET /documents/{id}/thumbnail}). */
    public byte[] thumbnail(String documentId) {
        String id = requireId(documentId, "Document ID");
        return callBinary("Failed to download document thumbnail", () -> http.getBinary("/documents/" + id + "/thumbnail"));
    }

    /** Download a single rendered page as bytes ({@code GET /documents/{id}/pages/{pageId}/download}). */
    public byte[] downloadPage(String documentId, String pageId) {
        String docId = requireId(documentId, "Document ID");
        String pid = requireId(pageId, "Page ID");
        return callBinary("Failed to download page", () -> http.getBinary("/documents/" + docId + "/pages/" + pid + "/download"));
    }

    /** List the document's activity log ({@code GET /documents/{id}/activities}). */
    public List<DocumentActivity> activities(String documentId) {
        String id = requireId(documentId, "Document ID");
        PaginatedResult<DocumentActivity> result = callList("Failed to fetch document activities",
                () -> http.get("/documents/" + id + "/activities"),
                DocumentActivity.class);
        return result != null ? result.getData() : new ArrayList<>();
    }

    /** Delete a document ({@code DELETE /documents/{id}}). */
    public void delete(String documentId) {
        String id = requireId(documentId, "Document ID");
        callVoid("Failed to delete document", () -> http.delete("/documents/" + id));
    }

    /** Create a document from a template ({@code POST /accounts/{id}/templates/{templateId}/documents}). */
    public DocumentDetails createFromTemplate(String templateId, CreateDocumentFromTemplateRequest request) {
        return createFromTemplate(templateId, request, null);
    }

    public DocumentDetails createFromTemplate(String templateId, CreateDocumentFromTemplateRequest request, String accountId) {
        String tmplId = requireId(templateId, "Template ID");
        String accId = accountId(accountId);
        String json = serialise(request);
        logger.info("Creating document from template", Map.of("templateId", tmplId, "accountId", accId));
        return call("Failed to create document from template",
                () -> http.post("/accounts/" + accId + "/templates/" + tmplId + "/documents", json),
                DocumentDetails.class);
    }

    /** Estimate the credit cost of creating a document from a template, without creating it. */
    public Map<String, Object> estimateCostFromTemplate(String templateId, CreateDocumentFromTemplateRequest request) {
        return estimateCostFromTemplate(templateId, request, null);
    }

    public Map<String, Object> estimateCostFromTemplate(String templateId, CreateDocumentFromTemplateRequest request, String accountId) {
        String tmplId = requireId(templateId, "Template ID");
        String accId = accountId(accountId);
        String json = serialise(request);
        return callMap("Failed to estimate cost from template",
                () -> http.post("/accounts/" + accId + "/templates/" + tmplId + "/documents/estimate-cost", json));
    }

    /**
     * Verify a signed document by its signature hash ({@code GET /documents/{hash}/verify}). The
     * returned map carries {@code is_valid} plus, when valid, signing metadata.
     */
    public Map<String, Object> verify(String hash) {
        String h = requireId(hash, "Signature hash");
        return callMap("Failed to verify document", () -> http.get("/documents/" + h + "/verify"));
    }

    /** Convenience: {@code true} if the document is certificated or every signer has completed. */
    public boolean isFullySigned(String documentId) {
        DocumentDetails details = this.details(documentId);
        if (CERTIFICATED.equals(details.getStatus())) return true;
        var summary = details.getAssignment() != null ? details.getAssignment().getSummary() : null;
        if (summary != null && summary.getSignerCount() != null) {
            return summary.getSignerCount() > 0 && summary.getSignerCount().equals(summary.getCompletedCount());
        }
        return false;
    }

    /** Convenience: signed/total/pending counts and a completion percentage derived from the summary. */
    public SigningProgress getSigningProgress(String documentId) {
        DocumentDetails details = this.details(documentId);
        var summary = details.getAssignment() != null ? details.getAssignment().getSummary() : null;
        int total = summary != null && summary.getSignerCount() != null ? summary.getSignerCount() : 0;
        int signed = summary != null && summary.getCompletedCount() != null ? summary.getCompletedCount() : 0;
        int pending = Math.max(total - signed, 0);
        double percentage = total > 0 ? Math.round((double) signed / total * 10_000.0) / 100.0 : 0.0;
        return new SigningProgress(signed, total, pending, percentage);
    }

    /** List the supported document statuses and whether each is deletable ({@code GET /documents/statuses}). */
    public List<DocumentStatusInfo> getStatuses() {
        return callList("Failed to fetch document statuses",
                () -> http.get("/documents/statuses"),
                DocumentStatusInfo.class).getData();
    }

    /**
     * @deprecated This is a signer self-service operation; use
     * {@link com.assinafy.sdk.resources.SignerResource#confirmSignerData(String, String, Map)}
     * (via {@code client.signers().confirmSignerData(...)}) instead. Retained for backwards
     * compatibility.
     */
    @Deprecated
    public void confirmSignerData(String documentId, String signerAccessCode, Map<String, Object> data) {
        String docId = requireId(documentId, "Document ID");
        requireId(signerAccessCode, "Signer access code");
        Map<String, Object> body = data != null ? new HashMap<>(data) : new HashMap<>();
        String json = serialise(body);
        callVoid("Failed to confirm signer data",
                () -> http.put("/documents/" + docId + "/signers/confirm-data?signer-access-code=" + encode(signerAccessCode), json));
    }

    /**
     * List the tags currently attached to a document.
     *
     * <p>{@code GET /accounts/{accountId}/documents/{documentId}/tags}.
     */
    public List<Tag> listTags(String documentId) {
        return listTags(documentId, null);
    }

    public List<Tag> listTags(String documentId, String accountId) {
        String accId = accountId(accountId);
        String docId = requireId(documentId, "Document ID");
        return callList("Failed to list document tags",
                () -> http.get("/accounts/" + accId + "/documents/" + docId + "/tags"),
                Tag.class).getData();
    }

    /**
     * Replace the document's tag set with the supplied tag names. Unknown names are
     * auto-created; an empty list detaches all tags.
     *
     * <p>{@code PUT /accounts/{accountId}/documents/{documentId}/tags}.
     */
    public List<Tag> replaceTags(String documentId, List<String> tagNames) {
        return replaceTags(documentId, tagNames, null);
    }

    public List<Tag> replaceTags(String documentId, List<String> tagNames, String accountId) {
        String accId = accountId(accountId);
        String docId = requireId(documentId, "Document ID");
        String json = serialise(Map.of("tags", tagNames != null ? tagNames : List.of()));
        return callList("Failed to replace document tags",
                () -> http.put("/accounts/" + accId + "/documents/" + docId + "/tags", json),
                Tag.class).getData();
    }

    /**
     * Attach additional tags to a document without removing existing ones. Idempotent;
     * unknown names are auto-created.
     *
     * <p>{@code POST /accounts/{accountId}/documents/{documentId}/tags}.
     */
    public List<Tag> appendTags(String documentId, List<String> tagNames) {
        return appendTags(documentId, tagNames, null);
    }

    public List<Tag> appendTags(String documentId, List<String> tagNames, String accountId) {
        String accId = accountId(accountId);
        String docId = requireId(documentId, "Document ID");
        String json = serialise(Map.of("tags", tagNames != null ? tagNames : List.of()));
        return callList("Failed to append document tags",
                () -> http.post("/accounts/" + accId + "/documents/" + docId + "/tags", json),
                Tag.class).getData();
    }

    /**
     * Detach a single tag from a document (the tag itself is not deleted).
     *
     * <p>{@code DELETE /accounts/{accountId}/documents/{documentId}/tags/{tagId}}.
     */
    public void detachTag(String documentId, String tagId) {
        detachTag(documentId, tagId, null);
    }

    public void detachTag(String documentId, String tagId, String accountId) {
        String accId = accountId(accountId);
        String docId = requireId(documentId, "Document ID");
        String tid = requireId(tagId, "Tag ID");
        callVoid("Failed to detach document tag",
                () -> http.delete("/accounts/" + accId + "/documents/" + docId + "/tags/" + tid));
    }

    private void validateUpload(byte[] fileData, String fileName) {
        if (fileData == null || fileData.length == 0) {
            throw new ValidationException("File data is empty", Map.of("fileName", fileName != null ? fileName : ""));
        }
        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            throw new ValidationException("Only PDF files are supported", Map.of("fileName", fileName != null ? fileName : ""));
        }
        if (fileData.length > MAX_UPLOAD_BYTES) {
            throw new ValidationException("File size exceeds maximum allowed (25MB)",
                    Map.of("fileSize", fileData.length, "maxSize", MAX_UPLOAD_BYTES));
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
