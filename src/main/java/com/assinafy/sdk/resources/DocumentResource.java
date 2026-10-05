package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.CostEstimate;
import com.assinafy.sdk.models.Document;
import com.assinafy.sdk.models.DocumentActivity;
import com.assinafy.sdk.models.DocumentStatusInfo;
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
     * <p>Wire contract, payloads and failures: {@link #upload(byte[], String, Map, String)}.</p>
     *
     * @param fileData PDF bytes
     * @param fileName PDF file name
     * @return the uploaded document summary
     * @throws com.assinafy.sdk.exceptions.ValidationException if the file is missing, not a PDF, or too large
     */
    public Document upload(byte[] fileData, String fileName) {
        return upload(fileData, fileName, null, null);
    }

    /**
     * Upload a PDF and create a document, with optional document metadata and an explicit account.
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/accounts/{accountId}/documents</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * </ul>
     * <p>Request body: <code>multipart/form-data</code>. Illustrative payload with the documented
     * fields; optional fields may be absent or null.</p>
     * <pre>{
     *   "file": "file.pdf (binary bytes)"
     * }</pre>
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
     * <p>Documented HTTP statuses: 200 The created document; 400 One or more fields failed validation.;
     * 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param fileData  the PDF bytes (non-empty, ≤ 25 MB)
     * @param fileName  the file name (must end in {@code .pdf})
     * @param metadata  optional metadata sent as a multipart field, or {@code null}
     * @param accountId workspace/account ID; falls back to the client default when {@code null}
     * @return the uploaded document summary
     * @throws com.assinafy.sdk.exceptions.ValidationException if the file is invalid
     */
    public Document upload(byte[] fileData, String fileName, Map<String, Object> metadata, String accountId) {
        validateUpload(fileData, fileName);
        String id = pathSegment(accountId(accountId), "Account ID");
        String metadataJson = null;
        if (metadata != null) {
            metadataJson = serialise(metadata);
        }
        logInfo("Uploading document", Map.of("size", fileData.length, "hasMetadata", metadata != null));
        String finalMetadata = metadataJson;
        Document document = call("Document upload failed",
                () -> http.postMultipart("/accounts/" + id + "/documents", fileName, fileData, fileName, finalMetadata),
                Document.class);
        if (document == null || document.getId() == null || document.getId().isBlank()) {
            throw new AssinafyException("Upload succeeded but no document ID was returned");
        }
        logInfo("Document uploaded", Map.of("documentId", document.getId()));
        return document;
    }

    /**
     * List documents in the default account with default paging.
     *
     * <p>Wire contract, payloads and failures: {@link #list(ListParams, String)}.</p>
     *
     * @return matching documents and pagination metadata
     */
    public PaginatedResult<Document> list() {
        return list(new ListParams(), null);
    }

    /**
     * List documents in the default account.
     *
     * <p>Wire contract, payloads and failures: {@link #list(ListParams, String)}.</p>
     *
     * @param params paging and filtering parameters, or {@code null}
     * @return matching documents and pagination metadata
     */
    public PaginatedResult<Document> list(ListParams params) {
        return list(params, null);
    }

    /**
     * List documents in an explicit or default account.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts/{accountId}/documents</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>status</code> (query, optional): Status filter, e.g. <code>pending_signature</code>.</li>
     * <li><code>method</code> (query, optional): Signature method filter.</li>
     * <li><code>search</code> (query, optional): Partial match on document.name, signer.full_name, signer.email.</li>
     * <li><code>tags</code> (query, optional): Comma-separated tag IDs; returns documents having ALL listed tags.</li>
     * <li><code>sort</code> (query, optional): Sort by <code>name</code> or <code>updated_at</code>.</li>
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
     *       "resource": "document",
     *       "id": "615601fab04c0a3147bb1246",
     *       "account_id": "d199996981dbd199996981db",
     *       "template_id": null,
     *       "name": "document.pdf",
     *       "status": "metadata_ready",
     *       "artifacts": {
     *         "original": "https://api.assinafy.com.br/v1/documents/doc1/download/original"
     *       },
     *       "is_closed": false,
     *       "signing_url": "https://api.assinafy.com.br/v1/sign/doc1",
     *       "decline_reason": null,
     *       "declined_by": null,
     *       "tags": [
     *         {
     *           "id": "id_example",
     *           "name": "name_example"
     *         }
     *       ],
     *       "assignment": null,
     *       "pages": [
     *         {
     *           "id": "615601faf166d6d1d8e7dc30",
     *           "number": 1,
     *           "height": 2100,
     *           "width": 1275,
     *           "download_url": "https://api.assinafy.com.br/v1/documents/doc1/pages/1a/download"
     *         }
     *       ],
     *       "created_at": "2026-06-03T03:54:16Z",
     *       "updated_at": "2026-06-03T03:54:16Z"
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 A page of documents; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param params paging and filtering parameters, or {@code null}
     * @param accountId explicit account ID, or {@code null} for the default
     * @return matching documents and pagination metadata
     */
    public PaginatedResult<Document> list(ListParams params, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list documents", () -> http.get("/accounts/" + id + "/documents", queryParams), Document.class);
    }

    /**
     * Fetch full document details, including the assignment.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/documents/{documentId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
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
     * <p>Documented HTTP statuses: 200 The document; 404 The requested resource does not exist.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param documentId document ID
     * @return expanded document details
     */
    public Document details(String documentId) {
        String id = pathSegment(documentId, "Document ID");
        return call("Failed to fetch document details", () -> http.get("/documents/" + id), Document.class);
    }

    /**
     * Fetch full document details as an alias for {@link #details(String)}.
     *
     * <p>Wire payloads and HTTP failures follow {@link #details(String)}.</p>
     *
     * @param documentId document ID
     * @return expanded document details
     */
    public Document get(String documentId) {
        return details(documentId);
    }

    /**
     * Rename a document ({@code PATCH /documents/{documentId}} with body {@code {"name": ...}}) and
     * return the updated document.
     *
     * <p><strong>HTTP:</strong> <code>PATCH /v1/documents/{documentId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "name": "Service agreement.pdf"
     * }</pre>
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
     * <p>Documented HTTP statuses: 200 The updated document; 400 One or more fields failed validation.;
     * 404 The requested resource does not exist.; 401 Missing or invalid credentials.; 500 Unexpected
     * server error. Non-2xx HTTP or numeric envelope statuses raise <code>ApiException</code>; 401/403
     * raise <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O
     * failures raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * @param documentId document ID
     * @param newName nonblank replacement name
     * @return the updated document
     * @throws com.assinafy.sdk.exceptions.ValidationException if {@code newName} is blank
     */
    public Document rename(String documentId, String newName) {
        String id = pathSegment(documentId, "Document ID");
        requireText(newName, "Document name", 255);
        String json = serialise(Map.of("name", newName));
        return call("Failed to rename document", () -> http.patch("/documents/" + id, json), Document.class);
    }

    /**
     * Lightweight document search ({@code GET /accounts/{accountId}/documents/search}), returning a
     * compact representation without expanded assignments or pages. Honors {@code search},
     * {@code status}, and paging via {@link ListParams}.
     *
     * <p>Wire contract, payloads and failures: {@link #search(ListParams, String)}.</p>
     *
     * @param params search, status, and paging parameters, or {@code null}
     * @return matching compact documents and pagination metadata
     */
    public PaginatedResult<Document> search(ListParams params) {
        return search(params, null);
    }

    /**
     * Search documents in an explicit or default account.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts/{accountId}/documents/search</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>search</code> (query, optional): Search term.</li>
     * <li><code>status</code> (query, optional): string.</li>
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
     *       "resource": "document",
     *       "id": "615601fab04c0a3147bb1246",
     *       "account_id": "d199996981dbd199996981db",
     *       "template_id": null,
     *       "name": "document.pdf",
     *       "status": "metadata_ready",
     *       "artifacts": {
     *         "original": "https://api.assinafy.com.br/v1/documents/doc1/download/original"
     *       },
     *       "is_closed": false,
     *       "signing_url": "https://api.assinafy.com.br/v1/sign/doc1",
     *       "decline_reason": null,
     *       "declined_by": null,
     *       "tags": [
     *         {
     *           "id": "id_example",
     *           "name": "name_example"
     *         }
     *       ],
     *       "assignment": null,
     *       "pages": [
     *         {
     *           "id": "615601faf166d6d1d8e7dc30",
     *           "number": 1,
     *           "height": 2100,
     *           "width": 1275,
     *           "download_url": "https://api.assinafy.com.br/v1/documents/doc1/pages/1a/download"
     *         }
     *       ],
     *       "created_at": "2026-06-03T03:54:16Z",
     *       "updated_at": "2026-06-03T03:54:16Z"
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Matching documents; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param params search, status, and paging parameters, or {@code null}
     * @param accountId explicit account ID, or {@code null} for the default
     * @return matching compact documents and pagination metadata
     */
    public PaginatedResult<Document> search(ListParams params, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to search documents",
                () -> http.get("/accounts/" + id + "/documents/search", queryParams),
                Document.class);
    }

    /**
     * Poll until a document is ready, using a 30-second timeout and 2-second interval.
     *
     * <p>Wire payloads and HTTP failures follow {@link #details(String)}.</p>
     *
     * @param documentId document ID
     * @return the first ready document state
     */
    public Document waitUntilReady(String documentId) {
        return waitUntilReady(documentId, 30_000, 2_000);
    }

    /**
     * Poll {@link #details(String)} until the document reaches a ready status
     * ({@code metadata_ready}/{@code pending_signature}/{@code certificated}). The first attempt
     * is immediate. The polling deadline is checked between attempts and therefore cannot preempt
     * an in-flight transport call.
     *
     * <p>Wire payloads and HTTP failures follow {@link #details(String)}.</p>
     *
     * @param documentId document ID
     * @param maxWaitMs maximum polling budget in milliseconds
     * @param pollIntervalMs delay between status checks
     * @return the first ready document state
     * @throws com.assinafy.sdk.exceptions.ValidationException if the document enters a failed status or the wait times out
     */
    public Document waitUntilReady(String documentId, long maxWaitMs, long pollIntervalMs) {
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
                Document details = this.details(id);
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
     * <p>Wire contract, payloads and failures: {@link #download(String, String)}.</p>
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
     * <p><strong>HTTP:</strong> <code>GET /v1/documents/{documentId}/download/{artifactName}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * <li><code>artifactName</code> (path, required): Artifact type.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/pdf</code>, raw artifact bytes.</p>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The artifact binary; 404 The requested resource does not exist.;
     * 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p><strong>HTTP:</strong> <code>GET /v1/documents/{documentId}/thumbnail</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>image/*</code>, raw artifact bytes.</p>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The thumbnail image; 404 The requested resource does not exist.;
     * 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p><strong>HTTP:</strong> <code>GET /v1/documents/{documentId}/pages/{pageId}/download</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * <li><code>pageId</code> (path, required): The page ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>image/*</code>, raw artifact bytes.</p>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The page image; 404 The requested resource does not exist.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p><strong>HTTP:</strong> <code>GET /v1/documents/{documentId}/activities</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     {
     *       "id": 4,
     *       "event": "assignment_created",
     *       "message": "Assignment created by John Smith.",
     *       "payload": null,
     *       "origin": null,
     *       "created_at": "2022-07-19T19:28:13Z"
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Document activities; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p><strong>HTTP:</strong> <code>DELETE /v1/documents/{documentId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * </ul>
     * <p>Request body: none.</p>
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
     * <p>Documented HTTP statuses: 200 Document deleted; 404 The requested resource does not exist.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p>Wire contract, payloads and failures: {@link #createFromTemplate(String,
     * CreateDocumentFromTemplateRequest, String)}.</p>
     *
     * @param templateId template ID
     * @param request signer assignments and document settings
     * @return the created document
     * @throws ValidationException if signer IDs, roles, delivery methods, or signing order are
     *         invalid
     */
    public Document createFromTemplate(String templateId, CreateDocumentFromTemplateRequest request) {
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
     * <p><strong>HTTP:</strong> <code>POST /v1/accounts/{accountId}/templates/{templateId}/documents</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>templateId</code> (path, required): The template ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "signers": [
     *     {
     *       "role_id": "fa8c14f32d732271e071998246e",
     *       "id": "fa8c140cb49b79f940aab95fddd",
     *       "verification_method": "Email",
     *       "notification_methods": [
     *         "notification_methods_example"
     *       ],
     *       "step": 1
     *     }
     *   ],
     *   "editor_fields": [
     *     {
     *       "field_id": "fa8c14f3af99d2846d1789de4ba",
     *       "value": "Field value"
     *     }
     *   ],
     *   "name": "sample-contract-one-page.pdf",
     *   "message": "Message to the signers",
     *   "expires_at": "2030-10-05T12:00:00Z",
     *   "tags": [
     *     "tags_example"
     *   ]
     * }</pre>
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
     * <p>Documented HTTP statuses: 200 The created document; 400 One or more fields failed validation.;
     * 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param templateId template ID
     * @param request signer assignments and document settings
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the created document
     * @throws ValidationException if signer IDs, roles, delivery methods, or signing order are
     *         invalid
     */
    public Document createFromTemplate(String templateId, CreateDocumentFromTemplateRequest request, String accountId) {
        String tmplId = pathSegment(templateId, "Template ID");
        String accId = pathSegment(accountId(accountId), "Account ID");
        String json = serialise(templatePayload(request, false));
        logInfo("Creating document from template", Map.of("templateId", tmplId, "accountId", accId));
        return call("Failed to create document from template",
                () -> http.post("/accounts/" + accId + "/templates/" + tmplId + "/documents", json),
                Document.class);
    }

    /**
     * Estimate the cost of creating a document from a template in the default account.
     *
     * <p>The estimate sends only signer role, verification, and notification fields; it requires
     * nonblank role IDs, validates delivery-method values, and omits signer IDs, signer steps, and
     * creation-only document settings.
     *
     * <p>Wire contract, payloads and failures: {@link #estimateCostFromTemplate(String,
     * CreateDocumentFromTemplateRequest, String)}.</p>
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
     * <p>Wire contract, payloads and failures: {@link #estimateCostFromTemplateTyped(String,
     * CreateDocumentFromTemplateRequest, String)}.</p>
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
     * <p><strong>HTTP:</strong> <code>POST
     * /v1/accounts/{accountId}/templates/{templateId}/documents/estimate-cost</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>templateId</code> (path, required): The template ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "signers": [
     *     {
     *       "role_id": "fa8c14f32d732271e071998246e",
     *       "verification_method": "Whatsapp",
     *       "notification_methods": [
     *         "notification_methods_example"
     *       ]
     *     }
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
     * <p>Documented HTTP statuses: 200 Cost estimate; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p>Wire contract, payloads and failures: {@link #estimateCostFromTemplate(String,
     * CreateDocumentFromTemplateRequest, String)}.
     * This method converts the same payload to the declared response model.</p>
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
     * <p><strong>HTTP:</strong> <code>GET /v1/documents/{documentSignatureHash}/verify</code>.
     * <strong>Authentication:</strong> Public (no SDK credential).</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentSignatureHash</code> (path, required): The document signature hash.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "hash": "FE32EDDADE7CBDDCBB934E7402047450B0E59C02",
     *     "id": null,
     *     "agreement_code": null,
     *     "status": null,
     *     "page_count": null,
     *     "signer_count": null,
     *     "completed_count": null,
     *     "completed_at": null,
     *     "verified_at": "2023-01-27T19:27:46Z",
     *     "is_valid": true,
     *     "message": ""
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Verification result; 500 Unexpected server error. Non-2xx HTTP or
     * numeric envelope statuses raise <code>ApiException</code>; 401/403 raise
     * <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O failures
     * raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
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
     * <p>Wire contract, payloads and failures: {@link #verify(String)}.
     * This method converts the same payload to the declared response model.</p>
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
     * <p>Wire payloads and HTTP failures follow {@link #details(String)}.</p>
     *
     * @param documentId document ID
     * @return {@code true} when signing is complete
     */
    public boolean isFullySigned(String documentId) {
        Document details = this.details(documentId);
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
     * <p>Wire payloads and HTTP failures follow {@link #details(String)}.</p>
     *
     * @param documentId document ID
     * @return signing progress
     */
    public SigningProgress getSigningProgress(String documentId) {
        Document details = this.details(documentId);
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
     * <p><strong>HTTP:</strong> <code>GET /v1/documents/statuses</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     {
     *       "code": "metadata_ready",
     *       "deletable": true
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Supported statuses; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @return supported status definitions
     */
    public List<DocumentStatusInfo> getStatuses() {
        return callList("Failed to fetch document statuses",
                () -> http.get("/documents/statuses"),
                DocumentStatusInfo.class).getData();
    }

    /**
     * List the tags currently attached to a document.
     *
     * <p>{@code GET /accounts/{accountId}/documents/{documentId}/tags}.
     *
     * <p>Wire contract, payloads and failures: {@link #listTags(String, String)}.</p>
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
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts/{accountId}/documents/{documentId}/tags</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>documentId</code> (path, required): The document ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     {
     *       "resource": "tag",
     *       "id": "fa8c09f3e709a8a1c82d69b1454",
     *       "name": "Contracts",
     *       "color": null,
     *       "created_at": "2026-05-14T12:00:00Z",
     *       "updated_at": "2026-05-14T12:00:00Z"
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Attached tags; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p>Wire payloads and HTTP failures follow {@link #replaceTagIds(String, java.util.List, String)}.
     * This overload accepts names directly, without ID resolution.</p>
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
     * <p>Wire payloads and HTTP failures follow {@link #replaceTagIds(String, java.util.List, String)}.
     * This overload accepts names directly, without ID resolution.</p>
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
     * <p>Wire contract, payloads and failures: {@link #replaceTagIds(String, List, String)}.</p>
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
     * <p><strong>HTTP:</strong> <code>PUT /v1/accounts/{accountId}/documents/{documentId}/tags</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>documentId</code> (path, required): The document ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "tags": [
     *     "tags_example"
     *   ]
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     {
     *       "resource": "tag",
     *       "id": "fa8c09f3e709a8a1c82d69b1454",
     *       "name": "Contracts",
     *       "color": null,
     *       "created_at": "2026-05-14T12:00:00Z",
     *       "updated_at": "2026-05-14T12:00:00Z"
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Updated tags; 401 Missing or invalid credentials.; 500 Unexpected
     * server error. Non-2xx HTTP or numeric envelope statuses raise <code>ApiException</code>; 401/403
     * raise <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O
     * failures raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
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
     * <p>Wire payloads and HTTP failures follow {@link #appendTagIds(String, java.util.List, String)}.
     * This overload accepts names directly, without ID resolution.</p>
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
     * <p>Wire payloads and HTTP failures follow {@link #appendTagIds(String, java.util.List, String)}.
     * This overload accepts names directly, without ID resolution.</p>
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
     * <p>Wire contract, payloads and failures: {@link #appendTagIds(String, List, String)}.</p>
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
     * <p><strong>HTTP:</strong> <code>POST /v1/accounts/{accountId}/documents/{documentId}/tags</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>documentId</code> (path, required): The document ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "tags": [
     *     "tags_example"
     *   ]
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     {
     *       "resource": "tag",
     *       "id": "fa8c09f3e709a8a1c82d69b1454",
     *       "name": "Contracts",
     *       "color": null,
     *       "created_at": "2026-05-14T12:00:00Z",
     *       "updated_at": "2026-05-14T12:00:00Z"
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Attached tags; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p>Wire contract, payloads and failures: {@link #detachTag(String, String, String)}.</p>
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
     * <p><strong>HTTP:</strong> <code>DELETE /v1/accounts/{accountId}/documents/{documentId}/tags/{tagId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>documentId</code> (path, required): The document ID.</li>
     * <li><code>tagId</code> (path, required): The tag ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "detached": true
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Tag detached; 401 Missing or invalid credentials.; 500 Unexpected
     * server error. Non-2xx HTTP or numeric envelope statuses raise <code>ApiException</code>; 401/403
     * raise <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O
     * failures raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
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
            requireExpiration(request.getExpiresAt());
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
        SigningRules.validateDeliveryMethods(signer.getVerificationMethod(), signer.getNotificationMethods());
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

    private static void validateTemplateSignerSteps(List<TemplateSigner> signers) {
        SigningRules.validateSigningOrder(signers.stream()
                .map(signer -> new SigningRules.Placement(signer.getStep(), signer.getVerificationMethod()))
                .toList(), "template signer");
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
