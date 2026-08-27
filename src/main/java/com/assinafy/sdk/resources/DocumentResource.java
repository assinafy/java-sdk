package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.CostEstimate;
import com.assinafy.sdk.models.DocumentActivity;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.DocumentListItem;
import com.assinafy.sdk.models.DocumentStatusInfo;
import com.assinafy.sdk.models.DocumentUploadResponse;
import com.assinafy.sdk.models.DocumentVerification;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.SigningProgress;
import com.assinafy.sdk.models.Tag;
import com.assinafy.sdk.models.enums.DocumentArtifactName;
import com.assinafy.sdk.models.enums.DocumentStatus;
import com.assinafy.sdk.request.CreateDocumentFromTemplateRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.TemplateSigner;
import com.assinafy.sdk.util.ResponseHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Document upload, retrieval, artifact, template, verification, status, and tag operations. */
public class DocumentResource extends BaseResource {

    private static final long MAX_UPLOAD_BYTES = 25L * 1024 * 1024;

    /** Default artifact for {@link #download(String)} — the signed/certificated PDF. */
    private static final String DEFAULT_ARTIFACT = DocumentArtifactName.CERTIFICATED.getValue();
    private static final String CERTIFICATED = DocumentStatus.CERTIFICATED.getValue();
    private static final Set<String> VERIFICATION_METHODS =
            Set.of("Email", "Whatsapp", "DigitalCertificate");
    private static final Set<String> NOTIFICATION_METHODS = Set.of("Email", "Whatsapp");

    private static final Set<String> READY_STATUSES = Set.of(
            "metadata_ready", "pending_signature", CERTIFICATED
    );

    private static final Set<String> FAILED_STATUSES = Set.of(
            "failed", "rejected_by_signer", "rejected_by_user", "expired"
    );

    /**
     * Create document operations with a default account and logger.
     *
     * @param http HTTP transport
     * @param defaultAccountId optional default account ID
     * @param logger diagnostic logger
     */
    public DocumentResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    /**
     * Create document operations with a default account and no-op logging.
     *
     * @param http HTTP transport
     * @param defaultAccountId optional default account ID
     */
    public DocumentResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    /**
     * Upload a PDF and create a document ({@code POST /accounts/{accountId}/documents}). The file
     * must have a PDF file name, be non-empty, and be at most 25 MB. The API validates the document
     * content.
     *
     * @param fileData PDF bytes
     * @param fileName PDF file name
     * @return the uploaded document summary
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
     * @param metadata  optional metadata sent as a multipart field, or {@code null}
     * @param accountId workspace/account ID; falls back to the client default when {@code null}
     * @return the uploaded document summary
     * @throws com.assinafy.sdk.exceptions.ValidationException if the file is invalid
     */
    public DocumentUploadResponse upload(byte[] fileData, String fileName, Map<String, Object> metadata, String accountId) {
        validateUpload(fileData, fileName);
        String id = pathSegment(accountId(accountId), "Account ID");
        String metadataJson = null;
        if (metadata != null) {
            metadataJson = serialise(metadata);
        }
        logInfo("Uploading document", Map.of("size", fileData.length, "hasMetadata", metadata != null));
        String finalMetadata = metadataJson;
        DocumentUploadResponse document = call("Document upload failed",
                () -> http.postMultipart("/accounts/" + id + "/documents", fileName, fileData, fileName, finalMetadata),
                DocumentUploadResponse.class);
        if (document == null || document.getId() == null) {
            throw new ValidationException("Upload succeeded but no document ID was returned");
        }
        logInfo("Document uploaded", Map.of("documentId", document.getId()));
        return document;
    }

    /**
     * List documents in the default account with default paging.
     *
     * @return matching documents and pagination metadata
     */
    public PaginatedResult<DocumentListItem> list() {
        return list(new ListParams(), null);
    }

    /**
     * List documents in the default account.
     *
     * @param params paging and filtering parameters, or {@code null}
     * @return matching documents and pagination metadata
     */
    public PaginatedResult<DocumentListItem> list(ListParams params) {
        return list(params, null);
    }

    /**
     * List documents in an explicit or default account.
     *
     * @param params paging and filtering parameters, or {@code null}
     * @param accountId explicit account ID, or {@code null} for the default
     * @return matching documents and pagination metadata
     */
    public PaginatedResult<DocumentListItem> list(ListParams params, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list documents", () -> http.get("/accounts/" + id + "/documents", queryParams), DocumentListItem.class);
    }

    /**
     * Fetch full document details, including the assignment.
     *
     * @param documentId document ID
     * @return expanded document details
     */
    public DocumentDetails details(String documentId) {
        String id = pathSegment(documentId, "Document ID");
        return call("Failed to fetch document details", () -> http.get("/documents/" + id), DocumentDetails.class);
    }

    /**
     * Fetch full document details as an alias for {@link #details(String)}.
     *
     * @param documentId document ID
     * @return expanded document details
     */
    public DocumentDetails get(String documentId) {
        return details(documentId);
    }

    /**
     * Rename a document ({@code PATCH /documents/{documentId}} with body {@code {"name": ...}}) and
     * return the updated document.
     *
     * @param documentId document ID
     * @param newName nonblank replacement name
     * @return the updated document
     * @throws com.assinafy.sdk.exceptions.ValidationException if {@code newName} is blank
     */
    public DocumentDetails rename(String documentId, String newName) {
        String id = pathSegment(documentId, "Document ID");
        requireId(newName, "Document name");
        String json = serialise(Map.of("name", newName));
        return call("Failed to rename document", () -> http.patch("/documents/" + id, json), DocumentDetails.class);
    }

    /**
     * Lightweight document search ({@code GET /accounts/{accountId}/documents/search}), returning a
     * compact representation without expanded assignments or pages. Honors {@code search},
     * {@code status}, and paging via {@link ListParams}.
     *
     * @param params search, status, and paging parameters, or {@code null}
     * @return matching compact documents and pagination metadata
     */
    public PaginatedResult<DocumentListItem> search(ListParams params) {
        return search(params, null);
    }

    /**
     * Search documents in an explicit or default account.
     *
     * @param params search, status, and paging parameters, or {@code null}
     * @param accountId explicit account ID, or {@code null} for the default
     * @return matching compact documents and pagination metadata
     */
    public PaginatedResult<DocumentListItem> search(ListParams params, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to search documents",
                () -> http.get("/accounts/" + id + "/documents/search", queryParams),
                DocumentListItem.class);
    }

    /**
     * Poll until a document is ready, using a 30-second timeout and 2-second interval.
     *
     * @param documentId document ID
     * @return the first ready document state
     */
    public DocumentDetails waitUntilReady(String documentId) {
        return waitUntilReady(documentId, 30_000, 2_000);
    }

    /**
     * Poll {@link #details(String)} until the document reaches a ready status
     * ({@code metadata_ready}/{@code pending_signature}/{@code certificated}). The first attempt
     * is immediate. The polling deadline is checked between attempts and therefore cannot preempt
     * an in-flight transport call.
     *
     * @param documentId document ID
     * @param maxWaitMs maximum polling budget in milliseconds
     * @param pollIntervalMs delay between status checks
     * @return the first ready document state
     * @throws com.assinafy.sdk.exceptions.ValidationException if the document enters a failed status or the wait times out
     */
    public DocumentDetails waitUntilReady(String documentId, long maxWaitMs, long pollIntervalMs) {
        String id = requireId(documentId, "Document ID");
        if (maxWaitMs <= 0) throw new ValidationException("Maximum wait must be greater than zero");
        if (pollIntervalMs <= 0) throw new ValidationException("Poll interval must be greater than zero");
        long timeoutNanos = TimeUnit.MILLISECONDS.toNanos(maxWaitMs);
        long start = System.nanoTime();
        int attempts = 0;
        logInfo("Waiting for document to be ready", Map.of("documentId", id, "maxWaitMs", maxWaitMs));

        while (true) {
            attempts++;
            try {
                DocumentDetails details = this.details(id);
                String status = details.getStatus() != null ? details.getStatus() : "unknown";
                logDebug("Document status check", Map.of("attempts", attempts, "status", status));
                if (READY_STATUSES.contains(status)) return details;
                if (FAILED_STATUSES.contains(status)) {
                    throw new ValidationException("Document processing failed with status: " + status, Map.of("status", status));
                }
            } catch (ValidationException e) {
                throw e;
            } catch (ApiException e) {
                if (e.getStatusCode() != 404 && e.getStatusCode() < 500) throw e;
                logWarn("API error checking document status", Map.of("statusCode", e.getStatusCode()));
            } catch (NetworkException e) {
                logWarn("Network error checking document status", Map.of());
            } catch (AssinafyException e) {
                throw e;
            }
            long remaining = timeoutNanos - (System.nanoTime() - start);
            if (remaining <= 0) break;
            sleep(Math.min(remaining, TimeUnit.MILLISECONDS.toNanos(pollIntervalMs)));
        }
        throw new ValidationException("Timeout waiting for document to be ready", Map.of("documentId", id, "attempts", attempts));
    }

    /**
     * Download the default certificated artifact as bytes.
     *
     * @param documentId document ID
     * @return artifact bytes
     */
    public byte[] download(String documentId) {
        return download(documentId, DEFAULT_ARTIFACT);
    }

    /**
     * Download a document artifact as bytes ({@code GET /documents/{id}/download/{artifact}}).
     *
     * <p>The request accepts any media type ({@code Accept: }{@literal *}/{@literal *}) because artifacts may be PDFs,
     * ZIP bundles, or other documented binary formats.
     *
     * @param documentId document ID
     * @param artifactName one of {@code original}, {@code certificated}, {@code certificate-page},
     *                     {@code pades}, or {@code bundle}; defaults to {@code certificated} when
     *                     {@code null}
     * @return artifact bytes
     * @throws com.assinafy.sdk.exceptions.ApiException if the artifact is unavailable or the document is missing
     */
    public byte[] download(String documentId, String artifactName) {
        String id = pathSegment(documentId, "Document ID");
        String artifact = pathSegment(artifactName != null ? artifactName : DEFAULT_ARTIFACT, "Artifact name");
        return callBinary("Failed to download document",
                () -> http.getBinary("/documents/" + id + "/download/" + artifact, "*/*"));
    }

    /**
     * Download a document thumbnail.
     *
     * @param documentId document ID
     * @return thumbnail bytes
     */
    public byte[] thumbnail(String documentId) {
        String id = pathSegment(documentId, "Document ID");
        return callBinary("Failed to download document thumbnail", () -> http.getBinary("/documents/" + id + "/thumbnail"));
    }

    /**
     * Download one rendered document page.
     *
     * @param documentId document ID
     * @param pageId page ID
     * @return rendered page bytes
     */
    public byte[] downloadPage(String documentId, String pageId) {
        String docId = pathSegment(documentId, "Document ID");
        String pid = pathSegment(pageId, "Page ID");
        return callBinary("Failed to download page", () -> http.getBinary("/documents/" + docId + "/pages/" + pid + "/download"));
    }

    /**
     * List a document's activity log.
     *
     * @param documentId document ID
     * @return activity entries, or an empty list when the response contains no data
     */
    public List<DocumentActivity> activities(String documentId) {
        String id = pathSegment(documentId, "Document ID");
        PaginatedResult<DocumentActivity> result = callList("Failed to fetch document activities",
                () -> http.get("/documents/" + id + "/activities"),
                DocumentActivity.class);
        return result != null ? result.getData() : new ArrayList<>();
    }

    /**
     * Delete a document.
     *
     * @param documentId document ID
     */
    public void delete(String documentId) {
        String id = pathSegment(documentId, "Document ID");
        callVoid("Failed to delete document", () -> http.delete("/documents/" + id));
    }

    /**
     * Create a document from a template in the default account. See the account-aware overload for
     * the signer validation rules.
     *
     * @param templateId template ID
     * @param request signer assignments and document settings
     * @return the created document
     * @throws ValidationException if signer IDs, roles, delivery methods, or signing order are
     *         invalid
     */
    public DocumentDetails createFromTemplate(String templateId, CreateDocumentFromTemplateRequest request) {
        return createFromTemplate(templateId, request, null);
    }

    /**
     * Create a document from a template in an explicit or default account. At least one template
     * signer is required, and each signer needs nonblank role and signer IDs. Verification methods
     * are {@code Email}, {@code Whatsapp}, or {@code DigitalCertificate}; notification methods,
     * when supplied, may be empty, but every element must be {@code Email} or {@code Whatsapp}; at
     * most one method is permitted per signer. If one signer supplies a step, all must do so and the
     * positive steps must be contiguous from 1. A digital-certificate signer must be alone in its
     * step.
     *
     * @param templateId template ID
     * @param request signer assignments and document settings
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the created document
     * @throws ValidationException if signer IDs, roles, delivery methods, or signing order are
     *         invalid
     */
    public DocumentDetails createFromTemplate(String templateId, CreateDocumentFromTemplateRequest request, String accountId) {
        String tmplId = pathSegment(templateId, "Template ID");
        String accId = pathSegment(accountId(accountId), "Account ID");
        String json = serialise(templatePayload(request, false));
        logInfo("Creating document from template", Map.of("templateId", tmplId, "accountId", accId));
        return call("Failed to create document from template",
                () -> http.post("/accounts/" + accId + "/templates/" + tmplId + "/documents", json),
                DocumentDetails.class);
    }

    /**
     * Estimate the cost of creating a document from a template in the default account.
     *
     * <p>The estimate sends only signer role, verification, and notification fields; it requires
     * nonblank role IDs, validates delivery-method values, and omits signer IDs, signer steps, and
     * creation-only document settings.
     *
     * @param templateId template ID
     * @param request template signer estimate inputs
     * @return the cost breakdown
     * @throws ValidationException if no valid template signer roles are supplied or a delivery
     *         method is invalid
     */
    public Map<String, Object> estimateCostFromTemplate(String templateId, CreateDocumentFromTemplateRequest request) {
        return estimateCostFromTemplate(templateId, request, null);
    }

    /**
     * Return a typed template-document cost estimate for the default account.
     *
     * @param templateId template ID
     * @param request template signer estimate inputs
     * @return the typed cost breakdown
     * @throws ValidationException if no valid template signer roles are supplied or a delivery
     *         method is invalid
     */
    public CostEstimate estimateCostFromTemplateTyped(String templateId, CreateDocumentFromTemplateRequest request) {
        return estimateCostFromTemplateTyped(templateId, request, null);
    }

    /**
     * Estimate the cost of creating a document from a template in an explicit or default account.
     *
     * <p>The estimate requires nonblank role IDs, validates delivery-method values, and omits
     * signer IDs, signer steps, and creation-only document settings.
     *
     * @param templateId template ID
     * @param request template signer estimate inputs
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the cost breakdown
     * @throws ValidationException if no valid template signer roles are supplied or a delivery
     *         method is invalid
     */
    public Map<String, Object> estimateCostFromTemplate(String templateId, CreateDocumentFromTemplateRequest request, String accountId) {
        String tmplId = pathSegment(templateId, "Template ID");
        String accId = pathSegment(accountId(accountId), "Account ID");
        String json = serialise(templatePayload(request, true));
        return callMap("Failed to estimate cost from template",
                () -> http.post("/accounts/" + accId + "/templates/" + tmplId + "/documents/estimate-cost", json));
    }

    /**
     * Return a typed template-document cost estimate for an explicit or default account.
     *
     * @param templateId template ID
     * @param request template signer estimate inputs
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the typed cost breakdown
     * @throws ValidationException if no valid template signer roles are supplied or a delivery
     *         method is invalid
     */
    public CostEstimate estimateCostFromTemplateTyped(
            String templateId, CreateDocumentFromTemplateRequest request, String accountId) {
        return ResponseHandler.convert(
                estimateCostFromTemplate(templateId, request, accountId), CostEstimate.class);
    }

    /**
     * Verify a signed document by its signature hash ({@code GET /documents/{hash}/verify}). The
     * returned map carries {@code is_valid} plus, when valid, signing metadata.
     *
     * @param hash document signature hash
     * @return raw verification result
     */
    public Map<String, Object> verify(String hash) {
        String h = pathSegment(hash, "Signature hash");
        return callMap("Failed to verify document", () -> http.get("/documents/" + h + "/verify"));
    }

    /**
     * Verify a signed document and return a typed result.
     *
     * @param hash document signature hash
     * @return typed verification result
     */
    public DocumentVerification verifyTyped(String hash) {
        return ResponseHandler.convert(verify(hash), DocumentVerification.class);
    }

    /**
     * Determine whether a document is certificated or every signer has completed.
     *
     * @param documentId document ID
     * @return {@code true} when signing is complete
     */
    public boolean isFullySigned(String documentId) {
        DocumentDetails details = this.details(documentId);
        if (CERTIFICATED.equals(details.getStatus())) return true;
        var summary = details.getAssignment() != null ? details.getAssignment().getSummary() : null;
        if (summary != null && summary.getSignerCount() != null) {
            return summary.getSignerCount() > 0 && summary.getSignerCount().equals(summary.getCompletedCount());
        }
        return false;
    }

    /**
     * Calculate signer completion counts and percentage from document details.
     *
     * @param documentId document ID
     * @return signing progress
     */
    public SigningProgress getSigningProgress(String documentId) {
        DocumentDetails details = this.details(documentId);
        var summary = details.getAssignment() != null ? details.getAssignment().getSummary() : null;
        int total = summary != null && summary.getSignerCount() != null ? summary.getSignerCount() : 0;
        int signed = summary != null && summary.getCompletedCount() != null ? summary.getCompletedCount() : 0;
        int pending = Math.max(total - signed, 0);
        double percentage = total > 0 ? Math.round((double) signed / total * 10_000.0) / 100.0 : 0.0;
        return new SigningProgress(signed, total, pending, percentage);
    }

    /**
     * List supported document statuses and whether each is deletable.
     *
     * @return supported status definitions
     */
    public List<DocumentStatusInfo> getStatuses() {
        return callList("Failed to fetch document statuses",
                () -> http.get("/documents/statuses"),
                DocumentStatusInfo.class).getData();
    }

    /**
     * @deprecated This is a signer self-service operation; use
     * {@link com.assinafy.sdk.resources.SignerResource#confirmSignerData(String, String, Map)}
     * (via {@code client.signers().confirmSignerData(...)}) instead. Additional map keys pass
     * through unchanged.
     *
     * @param documentId document ID
     * @param signerAccessCode signer invitation access code
     * @param data signer fields to confirm
     */
    @Deprecated
    public void confirmSignerData(String documentId, String signerAccessCode, Map<String, Object> data) {
        String docId = pathSegment(documentId, "Document ID");
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
     *
     * @param documentId document ID
     * @return attached tags
     */
    public List<Tag> listTags(String documentId) {
        return listTags(documentId, null);
    }

    /**
     * List tags attached to a document in an explicit or default account.
     *
     * @param documentId document ID
     * @param accountId explicit account ID, or {@code null} for the default
     * @return attached tags
     */
    public List<Tag> listTags(String documentId, String accountId) {
        String accId = pathSegment(accountId(accountId), "Account ID");
        String docId = pathSegment(documentId, "Document ID");
        return callList("Failed to list document tags",
                () -> http.get("/accounts/" + accId + "/documents/" + docId + "/tags"),
                Tag.class).getData();
    }

    /**
     * Replace the document's tag set using tag names. Unknown names are created by the API; an
     * empty list detaches all tags.
     *
     * <p>{@code PUT /accounts/{accountId}/documents/{documentId}/tags}.
     *
     * @param documentId document ID
     * @param tagNames replacement tag names; {@code null} detaches all tags
     * @return resulting attached-tag records; use their IDs when detaching
     */
    public List<Tag> replaceTags(String documentId, List<String> tagNames) {
        return replaceTags(documentId, tagNames, null);
    }

    /**
     * Replace document tags in an explicit or default account using tag names.
     *
     * @param documentId document ID
     * @param tagNames replacement tag names; {@code null} detaches all tags
     * @param accountId explicit account ID, or {@code null} for the default
     * @return resulting attached-tag records; use their IDs when detaching
     */
    public List<Tag> replaceTags(String documentId, List<String> tagNames, String accountId) {
        String accId = pathSegment(accountId(accountId), "Account ID");
        String docId = pathSegment(documentId, "Document ID");
        return sendDocumentTags(docId, tagNames != null ? tagNames : List.of(), accId, true);
    }

    /**
     * Replace the document's tag set using workspace tag IDs.
     *
     * @param documentId document ID
     * @param tagIds replacement workspace tag IDs; {@code null} detaches all tags
     * @return resulting attached-tag records; use their IDs when detaching
     * @throws AssinafyException if an ID cannot be resolved before the document is changed
     */
    public List<Tag> replaceTagIds(String documentId, List<String> tagIds) {
        return replaceTagIds(documentId, tagIds, null);
    }

    /**
     * Replace document tags in an explicit or default account using workspace tag IDs.
     *
     * @param documentId document ID
     * @param tagIds replacement workspace tag IDs; {@code null} detaches all tags
     * @param accountId explicit account ID, or {@code null} for the default
     * @return resulting attached-tag records; use their IDs when detaching
     * @throws AssinafyException if an ID cannot be resolved before the document is changed
     */
    public List<Tag> replaceTagIds(String documentId, List<String> tagIds, String accountId) {
        String accId = pathSegment(accountId(accountId), "Account ID");
        String docId = pathSegment(documentId, "Document ID");
        return sendDocumentTags(docId, resolveTagIds(accId, tagIds), accId, true);
    }

    /**
     * Attach additional tags to a document using tag names without removing existing ones.
     * Unknown names are created by the API.
     *
     * <p>{@code POST /accounts/{accountId}/documents/{documentId}/tags}.
     *
     * @param documentId document ID
     * @param tagNames tag names to attach; {@code null} sends an empty list
     * @return resulting attached-tag records; use their IDs when detaching
     */
    public List<Tag> appendTags(String documentId, List<String> tagNames) {
        return appendTags(documentId, tagNames, null);
    }

    /**
     * Append document tags in an explicit or default account using tag names.
     *
     * @param documentId document ID
     * @param tagNames tag names to attach; {@code null} sends an empty list
     * @param accountId explicit account ID, or {@code null} for the default
     * @return resulting attached-tag records; use their IDs when detaching
     */
    public List<Tag> appendTags(String documentId, List<String> tagNames, String accountId) {
        String accId = pathSegment(accountId(accountId), "Account ID");
        String docId = pathSegment(documentId, "Document ID");
        return sendDocumentTags(docId, tagNames != null ? tagNames : List.of(), accId, false);
    }

    /**
     * Attach additional tags to a document using workspace tag IDs.
     *
     * @param documentId document ID
     * @param tagIds workspace tag IDs to attach; {@code null} sends an empty list
     * @return resulting attached-tag records; use their IDs when detaching
     * @throws AssinafyException if an ID cannot be resolved before the document is changed
     */
    public List<Tag> appendTagIds(String documentId, List<String> tagIds) {
        return appendTagIds(documentId, tagIds, null);
    }

    /**
     * Append document tags in an explicit or default account using workspace tag IDs.
     *
     * @param documentId document ID
     * @param tagIds workspace tag IDs to attach; {@code null} sends an empty list
     * @param accountId explicit account ID, or {@code null} for the default
     * @return resulting attached-tag records; use their IDs when detaching
     * @throws AssinafyException if an ID cannot be resolved before the document is changed
     */
    public List<Tag> appendTagIds(String documentId, List<String> tagIds, String accountId) {
        String accId = pathSegment(accountId(accountId), "Account ID");
        String docId = pathSegment(documentId, "Document ID");
        return sendDocumentTags(docId, resolveTagIds(accId, tagIds), accId, false);
    }

    private List<String> resolveTagIds(String accountId, List<String> tagIds) {
        List<String> requested = tagIds != null ? tagIds : List.of();
        if (requested.isEmpty()) return requested;
        Map<String, String> namesById = workspaceTagNames(accountId, requested);
        if (!namesById.keySet().containsAll(requested)) {
            throw new AssinafyException("Unable to resolve one or more workspace tag IDs");
        }
        return requested.stream().map(namesById::get).toList();
    }

    private List<Tag> sendDocumentTags(
            String documentId, List<String> tags, String accountId, boolean replace) {
        String path = "/accounts/" + accountId + "/documents/" + documentId + "/tags";
        String json = serialise(Map.of("tags", tags));
        String label = replace ? "Failed to replace document tags" : "Failed to append document tags";
        return callList(label, () -> replace ? http.put(path, json) : http.post(path, json),
                Tag.class).getData();
    }

    private Map<String, String> workspaceTagNames(String accountId, List<String> tagIds) {
        Map<String, String> names = new HashMap<>();
        int page = 1;
        int lastPage;
        do {
            int currentPage = page;
            PaginatedResult<Tag> result = callList("Failed to resolve workspace tags",
                    () -> http.get("/accounts/" + accountId + "/tags",
                            Map.of("page", currentPage, "per-page", 100)), Tag.class);
            result.getData().stream()
                    .filter(tag -> tagIds.contains(tag.getId()))
                    .forEach(tag -> names.put(tag.getId(), tag.getName()));
            lastPage = result.getMeta() != null && result.getMeta().getLastPage() != null
                    ? result.getMeta().getLastPage() : currentPage;
            page++;
        } while (page <= lastPage && names.size() < tagIds.size());
        return names;
    }

    /**
     * Detach a single tag from a document (the tag itself is not deleted).
     *
     * <p>{@code DELETE /accounts/{accountId}/documents/{documentId}/tags/{tagId}}.
     *
     * @param documentId document ID
     * @param tagId attached-tag ID returned by {@link #listTags(String)} or the attach response
     */
    public void detachTag(String documentId, String tagId) {
        detachTag(documentId, tagId, null);
    }

    /**
     * Detach a tag from a document in an explicit or default account.
     *
     * @param documentId document ID
     * @param tagId attached-tag ID returned by a list or attach operation
     * @param accountId explicit account ID, or {@code null} for the default
     */
    public void detachTag(String documentId, String tagId, String accountId) {
        String accId = pathSegment(accountId(accountId), "Account ID");
        String docId = pathSegment(documentId, "Document ID");
        String tid = pathSegment(tagId, "Tag ID");
        callVoid("Failed to detach document tag",
                () -> http.delete("/accounts/" + accId + "/documents/" + docId + "/tags/" + tid));
    }

    private void validateUpload(byte[] fileData, String fileName) {
        if (fileData == null || fileData.length == 0) {
            throw new ValidationException("File data is empty", Map.of("fileName", fileName != null ? fileName : ""));
        }
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new ValidationException("Only PDF files are supported", Map.of("fileName", fileName != null ? fileName : ""));
        }
        if (fileData.length > MAX_UPLOAD_BYTES) {
            throw new ValidationException("File size exceeds maximum allowed (25MB)",
                    Map.of("fileSize", fileData.length, "maxSize", MAX_UPLOAD_BYTES));
        }
    }

    private Map<String, Object> templatePayload(CreateDocumentFromTemplateRequest request, boolean estimate) {
        if (request == null || request.getSigners() == null || request.getSigners().isEmpty()) {
            throw new ValidationException("At least one template signer is required");
        }
        List<Map<String, Object>> signers = request.getSigners().stream()
                .map(signer -> templateSignerPayload(signer, estimate))
                .toList();
        if (!estimate) validateTemplateSignerSteps(request.getSigners());
        Map<String, Object> body = new HashMap<>();
        body.put("signers", signers);
        if (!estimate) {
            if (request.getName() != null) body.put("name", request.getName());
            if (request.getMessage() != null) body.put("message", request.getMessage());
            if (request.getExpiresAt() != null) body.put("expires_at", request.getExpiresAt());
            if (request.getEditorFields() != null) body.put("editor_fields", request.getEditorFields());
            if (request.getTags() != null) body.put("tags", request.getTags());
        }
        return body;
    }

    private Map<String, Object> templateSignerPayload(TemplateSigner signer, boolean estimate) {
        if (signer == null || signer.getRoleId() == null || signer.getRoleId().isBlank()) {
            throw new ValidationException("Every template signer requires a role ID");
        }
        if (!estimate && (signer.getId() == null || signer.getId().isBlank())) {
            throw new ValidationException("Every template signer requires a signer ID");
        }
        validateDeliveryMethods(signer.getVerificationMethod(), signer.getNotificationMethods(), estimate);
        Map<String, Object> value = new HashMap<>();
        value.put("role_id", signer.getRoleId());
        if (!estimate) value.put("id", signer.getId());
        if (signer.getVerificationMethod() != null) {
            value.put("verification_method", signer.getVerificationMethod());
        }
        if (signer.getNotificationMethods() != null) {
            value.put("notification_methods", signer.getNotificationMethods());
        }
        if (!estimate && signer.getStep() != null) {
            if (signer.getStep() < 1) throw new ValidationException("Template signer step must be positive");
            value.put("step", signer.getStep());
        }
        return value;
    }

    private static void validateDeliveryMethods(
            String verificationMethod, List<String> notificationMethods, boolean estimate) {
        if (verificationMethod != null && !VERIFICATION_METHODS.contains(verificationMethod)) {
            throw new ValidationException("Verification method must be Email, Whatsapp, or DigitalCertificate");
        }
        if (notificationMethods != null && notificationMethods.stream().anyMatch(
                method -> method == null || !NOTIFICATION_METHODS.contains(method))) {
            throw new ValidationException("Notification methods must contain Email or Whatsapp");
        }
        if (!estimate && notificationMethods != null && notificationMethods.size() > 1) {
            throw new ValidationException("A template signer may use only one notification method");
        }
    }

    private static void validateTemplateSignerSteps(List<TemplateSigner> signers) {
        boolean anyStep = false;
        boolean missingStep = false;
        Set<Integer> steps = new HashSet<>();
        Map<Integer, Integer> signersPerStep = new HashMap<>();
        for (TemplateSigner signer : signers) {
            Integer step = signer.getStep();
            anyStep |= step != null;
            missingStep |= step == null;
            int effectiveStep = step != null ? step : 1;
            signersPerStep.merge(effectiveStep, 1, Integer::sum);
            if (step != null) steps.add(step);
        }
        if (anyStep && missingStep) {
            throw new ValidationException("Every template signer must provide a step when signing order is used");
        }
        for (int step = 1; step <= steps.size(); step++) {
            if (!steps.contains(step)) {
                throw new ValidationException("Template signer steps must be contiguous starting at 1");
            }
        }
        for (TemplateSigner signer : signers) {
            if ("DigitalCertificate".equals(signer.getVerificationMethod())) {
                int step = signer.getStep() != null ? signer.getStep() : 1;
                if (signersPerStep.get(step) > 1) {
                    throw new ValidationException("A DigitalCertificate signer must be alone in its step");
                }
            }
        }
    }

    private static void sleep(long nanos) {
        try {
            TimeUnit.NANOSECONDS.sleep(nanos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Interrupted while waiting for document readiness", e);
        }
    }
}
