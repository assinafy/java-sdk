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
import com.assinafy.sdk.request.CreateDocumentFromTemplateRequest;
import com.assinafy.sdk.request.ListParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DocumentResource extends BaseResource {

    private static final long MAX_UPLOAD_BYTES = 25L * 1024 * 1024;

    private static final Set<String> READY_STATUSES = Set.of(
            "metadata_ready", "pending_signature", "certificated"
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

    public DocumentUploadResponse upload(byte[] fileData, String fileName) {
        return upload(fileData, fileName, null, null);
    }

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

    public DocumentDetails details(String documentId) {
        String id = requireId(documentId, "Document ID");
        return call("Failed to fetch document details", () -> http.get("/documents/" + id), DocumentDetails.class);
    }

    public DocumentDetails get(String documentId) {
        return details(documentId);
    }

    public DocumentDetails waitUntilReady(String documentId) {
        return waitUntilReady(documentId, 30_000, 2_000);
    }

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

    public byte[] download(String documentId) {
        return download(documentId, "certificated");
    }

    public byte[] download(String documentId, String artifactName) {
        String id = requireId(documentId, "Document ID");
        String artifact = artifactName != null ? artifactName : "certificated";
        return callBinary("Failed to download document", () -> http.getBinary("/documents/" + id + "/download/" + artifact));
    }

    public byte[] thumbnail(String documentId) {
        String id = requireId(documentId, "Document ID");
        return callBinary("Failed to download document thumbnail", () -> http.getBinary("/documents/" + id + "/thumbnail"));
    }

    public byte[] downloadPage(String documentId, String pageId) {
        String docId = requireId(documentId, "Document ID");
        String pid = requireId(pageId, "Page ID");
        return callBinary("Failed to download page", () -> http.getBinary("/documents/" + docId + "/pages/" + pid + "/download"));
    }

    public List<DocumentActivity> activities(String documentId) {
        String id = requireId(documentId, "Document ID");
        PaginatedResult<DocumentActivity> result = callList("Failed to fetch document activities",
                () -> http.get("/documents/" + id + "/activities"),
                DocumentActivity.class);
        return result != null ? result.getData() : new ArrayList<>();
    }

    public void delete(String documentId) {
        String id = requireId(documentId, "Document ID");
        callVoid("Failed to delete document", () -> http.delete("/documents/" + id));
    }

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

    public Map<String, Object> verify(String hash) {
        String h = requireId(hash, "Signature hash");
        return callMap("Failed to verify document", () -> http.get("/documents/" + h + "/verify"));
    }

    public boolean isFullySigned(String documentId) {
        DocumentDetails details = this.details(documentId);
        if ("certificated".equals(details.getStatus())) return true;
        var summary = details.getAssignment() != null ? details.getAssignment().getSummary() : null;
        if (summary != null && summary.getSignerCount() != null) {
            return summary.getSignerCount() > 0 && summary.getSignerCount().equals(summary.getCompletedCount());
        }
        return false;
    }

    public SigningProgress getSigningProgress(String documentId) {
        DocumentDetails details = this.details(documentId);
        var summary = details.getAssignment() != null ? details.getAssignment().getSummary() : null;
        int total = summary != null && summary.getSignerCount() != null ? summary.getSignerCount() : 0;
        int signed = summary != null && summary.getCompletedCount() != null ? summary.getCompletedCount() : 0;
        int pending = Math.max(total - signed, 0);
        double percentage = total > 0 ? Math.round((double) signed / total * 10_000.0) / 100.0 : 0.0;
        return new SigningProgress(signed, total, pending, percentage);
    }

    public List<DocumentStatusInfo> getStatuses() {
        return callList("Failed to fetch document statuses",
                () -> http.get("/documents/statuses"),
                DocumentStatusInfo.class).getData();
    }

    public void confirmSignerData(String documentId, String signerAccessCode, Map<String, Object> data) {
        String docId = requireId(documentId, "Document ID");
        requireId(signerAccessCode, "Signer access code");
        Map<String, Object> body = new HashMap<>(data);
        String json = serialise(body);
        callVoid("Failed to confirm signer data",
                () -> http.put("/documents/" + docId + "/signers/confirm-data?signer-access-code=" + signerAccessCode, json));
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
