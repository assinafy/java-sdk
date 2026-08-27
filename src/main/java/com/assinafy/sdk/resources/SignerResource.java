package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.DocumentListItem;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.enums.DocumentArtifactName;
import com.assinafy.sdk.request.CreateSignerRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.UpdateSignerRequest;
import com.assinafy.sdk.util.ResponseHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Manages account signers and signer-access-code signing operations. */
public class SignerResource extends BaseResource {

    private static final String DEFAULT_ARTIFACT = DocumentArtifactName.CERTIFICATED.getValue();

    /**
     * Create signer operations bound to a default account and logger.
     *
     * @param http HTTP transport
     * @param defaultAccountId default account ID
     * @param logger diagnostic logger
     */
    public SignerResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    /**
     * Create signer operations bound to a default account.
     *
     * @param http HTTP transport
     * @param defaultAccountId default account ID
     */
    public SignerResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    /**
     * Create signer operations without a default account.
     *
     * @param http HTTP transport
     */
    public SignerResource(ApiHttpClient http) {
        super(http);
    }

    /**
     * Create a signer in the default account.
     *
     * @param request signer profile; {@code full_name} is required
     * @return the created signer
     */
    public Signer create(CreateSignerRequest request) {
        return create(request, null);
    }

    /**
     * Create a signer ({@code POST /accounts/{id}/signers}). This method always issues the POST;
     * use {@link #findOrCreate(CreateSignerRequest, String)} when reuse by email is intended. A
     * supplied CPF/CNPJ is applied through the documented signer update after creation; if that
     * update fails, the newly created signer is deleted. Request metadata is not sent because
     * signer creation does not define a metadata field.
     *
     * @param request signer profile; {@code full_name} is required
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the created signer
     */
    public Signer create(CreateSignerRequest request, String accountId) {
        String account = accountId(accountId);
        validateCreateRequest(request);
        Signer signer = postSigner(request, account);
        return applyGovernmentId(request, signer, account);
    }

    private Signer postSigner(CreateSignerRequest request, String accountId) {
        String id = pathSegment(accountId, "Account ID");
        logInfo("Creating signer", Map.of("hasEmail", request.getEmail() != null));
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("full_name", request.getFullName());
        if (request.getEmail() != null) createBody.put("email", request.getEmail());
        if (request.getWhatsappPhoneNumber() != null) {
            createBody.put("whatsapp_phone_number", request.getWhatsappPhoneNumber());
        }
        String body = serialise(createBody);
        return requireSignerId(call("Failed to create signer",
                        () -> http.post("/accounts/" + id + "/signers", body), Signer.class),
                "Signer creation");
    }

    private Signer applyGovernmentId(CreateSignerRequest request, Signer signer,
                                     String accountId) {
        if (request.getCpf() != null && !request.getCpf().isBlank()) {
            try {
                return update(signer.getId(), UpdateSignerRequest.builder()
                        .governmentId(request.getCpf())
                        .build(), accountId);
            } catch (RuntimeException error) {
                try {
                    delete(signer.getId(), accountId);
                } catch (RuntimeException cleanupError) {
                    error.addSuppressed(cleanupError);
                }
                throw error;
            }
        }
        return signer;
    }

    /**
     * Return an exact case-insensitive email match from the default account, or create the signer
     * when none exists. A request without an email is always created. An existing match is returned
     * unchanged; request name, phone, and CPF/CNPJ apply only when a signer is created.
     *
     * @param request signer profile; {@code full_name} is required
     * @return an existing email match or the created signer
     */
    public Signer findOrCreate(CreateSignerRequest request) {
        return findOrCreate(request, null);
    }

    /**
     * Return an exact case-insensitive email match, or create the signer when none exists. If a
     * concurrent request creates the same email after the lookup, a duplicate 4xx response is
     * resolved with one final lookup. An existing match is returned unchanged; request name, phone,
     * and CPF/CNPJ apply only when a signer is created.
     *
     * @param request signer profile; {@code full_name} is required
     * @param accountId explicit account ID, or {@code null} for the default
     * @return an existing email match or the created signer
     */
    public Signer findOrCreate(CreateSignerRequest request, String accountId) {
        validateCreateRequest(request);
        String account = accountId(accountId);
        String email = request.getEmail();
        if (email == null) return create(request, account);

        Signer existing = findByEmail(email, account);
        if (existing != null) {
            logInfo("Using existing signer", Map.of());
            return existing;
        }

        Signer created;
        try {
            created = postSigner(request, account);
        } catch (ApiException e) {
            if (isDuplicateCreate(e)) {
                Signer duplicate = findByEmail(email, account);
                if (duplicate != null) {
                    logInfo("Signer already exists, using existing signer", Map.of());
                    return duplicate;
                }
            }
            throw e;
        }
        return applyGovernmentId(request, created, account);
    }

    private static boolean isDuplicateCreate(ApiException error) {
        if (error.getStatusCode() == 409) return true;
        if (error.getStatusCode() != 400 || error.getMessage() == null) return false;
        String message = error.getMessage().toLowerCase(Locale.ROOT);
        return message.contains("already exists") || message.contains("já existe");
    }

    private static void validateCreateRequest(CreateSignerRequest request) {
        if (request == null || request.getFullName() == null || request.getFullName().isBlank()) {
            throw new ValidationException("Signer full_name is required");
        }
        String email = request.getEmail();
        if (email != null) requireEmail(email);
    }

    /**
     * Fetch a signer from the default account.
     *
     * @param signerId signer ID
     * @return the signer
     */
    public Signer get(String signerId) {
        return get(signerId, null);
    }

    /**
     * Fetch a signer ({@code GET /accounts/{id}/signers/{signerId}}).
     *
     * @param signerId signer ID
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the signer
     */
    public Signer get(String signerId, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String sid = pathSegment(signerId, "Signer ID");
        return call("Failed to fetch signer", () -> http.get("/accounts/" + id + "/signers/" + sid), Signer.class);
    }

    /**
     * List signers for the default account.
     *
     * @return paginated signers
     */
    public PaginatedResult<Signer> list() {
        return list(new ListParams(), null);
    }

    /**
     * List signers for the default account.
     *
     * @param params paging and search options; {@code null} sends no query parameters
     * @return paginated signers
     */
    public PaginatedResult<Signer> list(ListParams params) {
        return list(params, null);
    }

    /**
     * List signers ({@code GET /accounts/{id}/signers}).
     *
     * @param params paging and search options; {@code null} sends no query parameters
     * @param accountId explicit account ID, or {@code null} for the default
     * @return paginated signers
     */
    public PaginatedResult<Signer> list(ListParams params, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list signers", () -> http.get("/accounts/" + id + "/signers", queryParams), Signer.class);
    }

    /**
     * Update a signer in the default account.
     *
     * @param signerId signer ID
     * @param request fields to update; {@code null} sends an empty object
     * @return the updated signer
     */
    public Signer update(String signerId, UpdateSignerRequest request) {
        return update(signerId, request, null);
    }

    /**
     * Update a signer ({@code PUT /accounts/{id}/signers/{signerId}}).
     *
     * @param signerId signer ID
     * @param request fields to update; {@code null} sends an empty object
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the updated signer
     */
    public Signer update(String signerId, UpdateSignerRequest request, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String sid = pathSegment(signerId, "Signer ID");
        String body = serialise(signerBody(request));
        return requireSignerId(call("Failed to update signer",
                        () -> http.put("/accounts/" + id + "/signers/" + sid, body), Signer.class),
                "Signer update");
    }

    private static Signer requireSignerId(Signer signer, String operation) {
        if (signer == null || signer.getId() == null || signer.getId().isBlank()) {
            throw new AssinafyException(operation + " succeeded but no signer ID was returned");
        }
        return signer;
    }

    /**
     * Delete a signer from the default account.
     *
     * @param signerId signer ID
     */
    public void delete(String signerId) {
        delete(signerId, null);
    }

    /**
     * Delete a signer ({@code DELETE /accounts/{id}/signers/{signerId}}).
     *
     * @param signerId signer ID
     * @param accountId explicit account ID, or {@code null} for the default
     */
    public void delete(String signerId, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String sid = pathSegment(signerId, "Signer ID");
        callVoid("Failed to delete signer", () -> http.delete("/accounts/" + id + "/signers/" + sid));
    }

    /**
     * Find an exact case-insensitive email match in the default account.
     *
     * @param email signer email
     * @return the matching signer, or {@code null}
     */
    public Signer findByEmail(String email) {
        return findByEmail(email, null);
    }

    /**
     * Find an exact case-insensitive email match using the documented signer list/search operation.
     *
     * @param email signer email
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the matching signer, or {@code null}
     */
    public Signer findByEmail(String email, String accountId) {
        requireEmail(email);
        try {
            ListParams params = ListParams.builder().search(email).perPage(100).build();
            PaginatedResult<Signer> result = list(params, accountId);
            String lower = email.toLowerCase(Locale.ROOT);
            return result.getData().stream()
                    .filter(s -> s.getEmail() != null && s.getEmail().toLowerCase(Locale.ROOT).equals(lower))
                    .findFirst()
                    .orElse(null);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) return null;
            throw e;
        }
    }

    /** Serialise a signer DTO and digit-strip its CPF/CNPJ wire field. */
    private Map<String, Object> signerBody(Object dto) {
        Map<String, Object> body = toMap(dto);
        for (String key : List.of("cpf", "government_id")) {
            Object value = body.get(key);
            if (value instanceof String s) {
                body.put(key, s.replaceAll("\\D", ""));
            }
        }
        return body;
    }

    /**
     * Fetch the signer identified by a signer access code ({@code GET /signers/self}).
     *
     * @param signerAccessCode signer query credential
     * @return the signer profile
     */
    public Signer getSelf(String signerAccessCode) {
        requireId(signerAccessCode, "Signer access code");
        return call("Failed to fetch signer self info",
                () -> http.get(withAccessCode("/signers/self", signerAccessCode)),
                Signer.class);
    }

    /**
     * Record that the signer accepted the terms of use ({@code PUT /signers/accept-terms}).
     * Authenticated by the signer access code, passed as the {@code signer-access-code} query
     * parameter; the endpoint takes no request body and returns no payload.
     *
     * @param signerAccessCode signer query credential
     */
    public void acceptTerms(String signerAccessCode) {
        requireId(signerAccessCode, "Signer access code");
        callVoid("Failed to accept terms",
                () -> http.put(withAccessCode("/signers/accept-terms", signerAccessCode), null));
    }

    /**
     * Signer-self confirmation/update of their data before signing, returning the server-normalised
     * {@link Signer}.
     *
     * <p>Maps to {@code PUT /documents/{documentId}/signers/confirm-data?signer-access-code={code}}.
     * The {@code data} map may carry {@code full_name}, {@code email}, and {@code government_id}.
     * Additional keys are forwarded unchanged.
     *
     * @param documentId document ID
     * @param signerAccessCode signer query credential
     * @param data fields to confirm; {@code null} sends an empty object
     * @return the server-normalized signer
     */
    public Signer confirmSignerData(String documentId, String signerAccessCode, Map<String, Object> data) {
        String docId = pathSegment(documentId, "Document ID");
        requireId(signerAccessCode, "Signer access code");
        Map<String, Object> body = data != null ? new HashMap<>(data) : new HashMap<>();
        String json = serialise(body);
        return call("Failed to confirm signer data",
                () -> http.put(withAccessCode("/documents/" + docId + "/signers/confirm-data", signerAccessCode), json),
                Signer.class);
    }

    /**
     * Submit the OTP verification code sent to the signer ({@code POST /verify}). Authenticated by
     * the signer access code (passed as the {@code signer-access-code} query parameter); the body
     * carries only {@code verification-code}.
     *
     * @param signerAccessCode signer query credential
     * @param verificationCode one-time verification code
     * @return the response data map, normally empty
     */
    public Map<String, Object> verifyEmail(String signerAccessCode, String verificationCode) {
        requireId(signerAccessCode, "Signer access code");
        requireId(verificationCode, "Verification code");
        String json = serialise(Map.of("verification-code", verificationCode));
        return callMap("Failed to verify email",
                () -> http.post(withAccessCode("/verify", signerAccessCode), json));
    }

    /**
     * Upload the signer's signature/initials image ({@code POST /signature}). Both {@code type}
     * (e.g. {@code signature} or {@code initial}) and {@code reuse} are optional per the docs.
     *
     * @param signerAccessCode signer query credential
     * @param type signature type, or {@code null} to omit it
     * @param imageData non-empty PNG or JPEG bytes
     */
    public void uploadSignature(String signerAccessCode, String type, byte[] imageData) {
        uploadSignature(signerAccessCode, type, imageData, null);
    }

    /**
     * Upload the signer's signature/initials image with the documented {@code reuse} flag. When
     * {@code reuse} is non-null it sets the signer's {@code is_signature_reusable} flag; when null
     * the flag is left unchanged.
     *
     * @param signerAccessCode signer query credential
     * @param type signature type, or {@code null} to omit it
     * @param imageData non-empty PNG or JPEG bytes
     * @param reuse whether future signatures may reuse the image, or {@code null} to omit the flag
     */
    public void uploadSignature(String signerAccessCode, String type, byte[] imageData, Boolean reuse) {
        requireId(signerAccessCode, "Signer access code");
        if (imageData == null || imageData.length == 0) {
            throw new ValidationException("Signature image data is empty");
        }
        boolean png = imageData.length >= 4 && (imageData[0] & 0xff) == 0x89
                && imageData[1] == 'P' && imageData[2] == 'N' && imageData[3] == 'G';
        boolean jpeg = imageData.length >= 3 && (imageData[0] & 0xff) == 0xff
                && (imageData[1] & 0xff) == 0xd8 && (imageData[2] & 0xff) == 0xff;
        if (!png && !jpeg) throw new ValidationException("Signature image must be PNG or JPEG");
        String path = withAccessCode("/signature", signerAccessCode);
        if (type != null && !type.isBlank()) path = path + "&type=" + encode(type);
        if (reuse != null) path = path + "&reuse=" + reuse;
        logInfo("Uploading signature", Map.of("type", type != null ? type : ""));
        String finalPath = path;
        callVoid("Failed to upload signature",
                () -> http.postSignature(finalPath, imageData));
    }

    /**
     * Download a stored signature or initials image ({@code GET /signature/{type}}).
     *
     * @param signerAccessCode signer query credential
     * @param type signature type path value
     * @return image bytes
     */
    public byte[] downloadSignature(String signerAccessCode, String type) {
        requireId(signerAccessCode, "Signer access code");
        String signatureType = pathSegment(type, "Signature type");
        return callBinary("Failed to download signature",
                () -> http.getBinary(withAccessCode("/signature/" + signatureType, signerAccessCode)));
    }

    /**
     * Fetch the signer's current document as a map.
     *
     * @param signerId signer ID
     * @param signerAccessCode signer query credential
     * @return document response fields
     */
    public Map<String, Object> getCurrentDocument(String signerId, String signerAccessCode) {
        String sid = pathSegment(signerId, "Signer ID");
        requireId(signerAccessCode, "Signer access code");
        return callMap("Failed to fetch signer's current document",
                () -> http.get(withAccessCode("/signers/" + sid + "/document", signerAccessCode)));
    }

    /**
     * Fetch the signer's current document as a typed model.
     *
     * @param signerId signer ID
     * @param signerAccessCode signer query credential
     * @return document details
     */
    public DocumentDetails getCurrentDocumentTyped(String signerId, String signerAccessCode) {
        return ResponseHandler.convert(
                getCurrentDocument(signerId, signerAccessCode), DocumentDetails.class);
    }

    /**
     * List a signer's documents with default paging.
     *
     * @param signerId signer ID
     * @param signerAccessCode signer query credential
     * @return paginated documents
     */
    public PaginatedResult<DocumentListItem> listDocuments(String signerId, String signerAccessCode) {
        return listDocuments(signerId, signerAccessCode, null);
    }

    /**
     * List the documents assigned to a signer ({@code GET /signers/{signerId}/documents}). The
     * endpoint documents only {@code page}/{@code per-page} paging (supply via {@link ListParams});
     * for server-side text search use {@link #searchDocuments(String, String, String)}.
     *
     * @param signerId signer ID
     * @param signerAccessCode signer query credential
     * @param params paging options; {@code null} sends only the access code
     * @return paginated documents
     */
    public PaginatedResult<DocumentListItem> listDocuments(String signerId, String signerAccessCode, ListParams params) {
        String sid = pathSegment(signerId, "Signer ID");
        requireId(signerAccessCode, "Signer access code");
        Map<String, Object> query = params != null ? new HashMap<>(params.toQueryParams()) : new HashMap<>();
        query.put("signer-access-code", signerAccessCode);
        return callList("Failed to list signer's documents",
                () -> http.get("/signers/" + sid + "/documents", query),
                DocumentListItem.class);
    }

    /**
     * Search the documents a signer is party to, returning a compact representation
     * ({@code GET /signers/{signerId}/documents/search}). Authenticated by the signer access code.
     *
     * @param signerId signer ID
     * @param signerAccessCode signer query credential
     * @param search free-text query matched against the signer's documents
     * @return paginated matching documents
     */
    public PaginatedResult<DocumentListItem> searchDocuments(String signerId, String signerAccessCode, String search) {
        String sid = pathSegment(signerId, "Signer ID");
        requireId(signerAccessCode, "Signer access code");
        Map<String, Object> query = new HashMap<>();
        query.put("signer-access-code", signerAccessCode);
        if (search != null && !search.isBlank()) query.put("search", search);
        return callList("Failed to search signer's documents",
                () -> http.get("/signers/" + sid + "/documents/search", query),
                DocumentListItem.class);
    }

    /**
     * Download a signer document using an access-code query.
     *
     * @param signerId signer ID
     * @param documentId document ID
     * @param artifactName artifact name, or {@code null} for {@code certificated}
     * @param signerAccessCode signer query credential
     * @return artifact bytes
     */
    public byte[] downloadDocument(String signerId, String documentId, String artifactName, String signerAccessCode) {
        String sid = pathSegment(signerId, "Signer ID");
        String docId = pathSegment(documentId, "Document ID");
        String artifact = pathSegment(artifactName != null ? artifactName : DEFAULT_ARTIFACT, "Artifact name");
        requireId(signerAccessCode, "Signer access code");
        return callBinary("Failed to download signer document",
                () -> http.getBinary(withAccessCode(
                        "/signers/" + sid + "/documents/" + docId + "/download/" + artifact,
                        signerAccessCode)));
    }

    /**
     * Download a signer document artifact from the documented public endpoint. No signer access
     * code is required. Artifact names are {@code original}, {@code certificated},
     * {@code certificate-page}, {@code pades}, or {@code bundle}.
     *
     * @param signerId signer ID
     * @param documentId document ID
     * @param artifactName artifact name, or {@code null} for {@code certificated}
     * @return artifact bytes
     */
    public byte[] downloadDocument(String signerId, String documentId, String artifactName) {
        String sid = pathSegment(signerId, "Signer ID");
        String docId = pathSegment(documentId, "Document ID");
        String artifact = pathSegment(artifactName != null ? artifactName : DEFAULT_ARTIFACT, "Artifact name");
        return callBinary("Failed to download signer document",
                () -> http.getBinary("/signers/" + sid + "/documents/" + docId + "/download/" + artifact));
    }

    /**
     * Sign multiple eligible documents ({@code PUT /signers/documents/sign-multiple}).
     *
     * @param signerAccessCode signer query credential
     * @param documentIds document ID list
     * @return the response data map, normally wrapping an empty array
     */
    public Map<String, Object> signMultiple(String signerAccessCode, List<String> documentIds) {
        requireId(signerAccessCode, "Signer access code");
        validateDocumentIds(documentIds);
        String json = serialise(Map.of("document_ids", documentIds));
        return callMap("Failed to sign multiple documents",
                () -> http.put(withAccessCode("/signers/documents/sign-multiple", signerAccessCode), json));
    }

    /**
     * Decline multiple documents ({@code PUT /signers/documents/decline-multiple}).
     *
     * @param signerAccessCode signer query credential
     * @param documentIds document ID list
     * @param declineReason non-blank decline reason
     * @return the response data map, normally wrapping an empty array
     */
    public Map<String, Object> declineMultiple(String signerAccessCode, List<String> documentIds, String declineReason) {
        requireId(signerAccessCode, "Signer access code");
        validateDocumentIds(documentIds);
        requireId(declineReason, "Decline reason");
        Map<String, Object> body = new HashMap<>();
        body.put("document_ids", documentIds);
        body.put("decline_reason", declineReason);
        String json = serialise(body);
        return callMap("Failed to decline multiple documents",
                () -> http.put(withAccessCode("/signers/documents/decline-multiple", signerAccessCode), json));
    }

    private static void validateDocumentIds(List<String> documentIds) {
        if (documentIds == null) {
            throw new ValidationException("Document IDs are required");
        }
        if (documentIds.stream().anyMatch(id -> id == null)) {
            throw new ValidationException("Document IDs must not be null");
        }
    }
}
