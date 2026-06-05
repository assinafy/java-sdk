package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.DocumentListItem;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.enums.DocumentArtifactName;
import com.assinafy.sdk.request.CreateSignerRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.UpdateSignerRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SignerResource extends BaseResource {

    private static final Pattern EMAIL_RE = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final String DEFAULT_ARTIFACT = DocumentArtifactName.CERTIFICATED.getValue();

    public SignerResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    public SignerResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    public SignerResource(ApiHttpClient http) {
        super(http);
    }

    public Signer create(CreateSignerRequest request) {
        return create(request, null);
    }

    public Signer create(CreateSignerRequest request, String accountId) {
        String id = accountId(accountId);
        if (request == null || request.getFullName() == null || request.getFullName().isBlank()) {
            throw new ValidationException("Signer full_name is required");
        }
        String email = request.getEmail();
        boolean hasEmail = email != null && !email.isBlank();

        // Email is optional per the API (a signer may have only a name + WhatsApp number).
        // When an email is supplied we validate it and reuse an existing signer with the same
        // address to keep create() idempotent.
        if (hasEmail) {
            assertEmail(email);
            Signer existing = findByEmail(email, id);
            if (existing != null) {
                logger.info("Using existing signer", Map.of("email", email));
                return existing;
            }
        }

        logger.info("Creating signer", Map.of("hasEmail", hasEmail));
        try {
            String body = serialise(signerBody(request));
            return call("Failed to create signer", () -> http.post("/accounts/" + id + "/signers", body), Signer.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 409 && hasEmail) {
                Signer duplicate = findByEmail(email, id);
                if (duplicate != null) {
                    logger.info("Signer already exists, using existing signer", Map.of("email", email));
                    return duplicate;
                }
            }
            throw e;
        }
    }

    public Signer get(String signerId) {
        return get(signerId, null);
    }

    public Signer get(String signerId, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        return call("Failed to fetch signer", () -> http.get("/accounts/" + id + "/signers/" + sid), Signer.class);
    }

    public PaginatedResult<Signer> list() {
        return list(new ListParams(), null);
    }

    public PaginatedResult<Signer> list(ListParams params) {
        return list(params, null);
    }

    public PaginatedResult<Signer> list(ListParams params, String accountId) {
        String id = accountId(accountId);
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list signers", () -> http.get("/accounts/" + id + "/signers", queryParams), Signer.class);
    }

    public Signer update(String signerId, UpdateSignerRequest request) {
        return update(signerId, request, null);
    }

    public Signer update(String signerId, UpdateSignerRequest request, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        String body = serialise(signerBody(request));
        return call("Failed to update signer", () -> http.put("/accounts/" + id + "/signers/" + sid, body), Signer.class);
    }

    public void delete(String signerId) {
        delete(signerId, null);
    }

    public void delete(String signerId, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        callVoid("Failed to delete signer", () -> http.delete("/accounts/" + id + "/signers/" + sid));
    }

    public Signer findByEmail(String email) {
        return findByEmail(email, null);
    }

    public Signer findByEmail(String email, String accountId) {
        assertEmail(email);
        try {
            ListParams params = ListParams.builder().search(email).perPage(100).build();
            PaginatedResult<Signer> result = list(params, accountId);
            String lower = email.toLowerCase();
            return result.getData().stream()
                    .filter(s -> s.getEmail() != null && s.getEmail().toLowerCase().equals(lower))
                    .findFirst()
                    .orElse(null);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) return null;
            throw e;
        }
    }

    private void assertEmail(String email) {
        if (email == null || !EMAIL_RE.matcher(email).matches()) {
            throw new ValidationException("Invalid email address", Map.of("email", email != null ? email : ""));
        }
    }

    /**
     * Serialise a signer create/update DTO to a wire map using the DTO's own {@code @JsonProperty}
     * names (single source of truth), then digit-strip {@code cpf} to mirror the PHP/TS SDKs.
     */
    private Map<String, Object> signerBody(Object dto) {
        Map<String, Object> body = toMap(dto);
        Object cpf = body.get("cpf");
        if (cpf instanceof String s) {
            body.put("cpf", s.replaceAll("\\D", ""));
        }
        return body;
    }

    public Signer getSelf(String signerAccessCode) {
        requireId(signerAccessCode, "Signer access code");
        return call("Failed to fetch signer self info",
                () -> http.get(withAccessCode("/signers/self", signerAccessCode)),
                Signer.class);
    }

    public Signer acceptTerms(String signerAccessCode) {
        requireId(signerAccessCode, "Signer access code");
        String json = serialise(Map.of("signer-access-code", signerAccessCode));
        return call("Failed to accept terms",
                () -> http.put("/signers/accept-terms", json),
                Signer.class);
    }

    /**
     * Signer-self confirmation of contact data and terms acceptance.
     *
     * <p>Maps to {@code PUT /documents/{documentId}/signers/confirm-data?signer-access-code={code}}.
     * The {@code data} map may carry {@code email}, {@code whatsapp_phone_number} and
     * {@code has_accepted_terms}.
     */
    public void confirmSignerData(String documentId, String signerAccessCode, Map<String, Object> data) {
        String docId = requireId(documentId, "Document ID");
        requireId(signerAccessCode, "Signer access code");
        Map<String, Object> body = data != null ? new HashMap<>(data) : new HashMap<>();
        String json = serialise(body);
        callVoid("Failed to confirm signer data",
                () -> http.put(withAccessCode("/documents/" + docId + "/signers/confirm-data", signerAccessCode), json));
    }

    public Map<String, Object> verifyEmail(String signerAccessCode, String verificationCode) {
        requireId(signerAccessCode, "Signer access code");
        requireId(verificationCode, "Verification code");
        String json = serialise(Map.of(
                "signer-access-code", signerAccessCode,
                "verification-code", verificationCode
        ));
        return callMap("Failed to verify email",
                () -> http.post("/verify", json));
    }

    public void uploadSignature(String signerAccessCode, String type, byte[] imageData) {
        requireId(signerAccessCode, "Signer access code");
        requireId(type, "Signature type");
        logger.info("Uploading signature", Map.of("signerAccessCode", signerAccessCode, "type", type));
        callVoid("Failed to upload signature",
                () -> http.postSignature(
                        withAccessCode("/signature", signerAccessCode) + "&type=" + encode(type),
                        imageData));
    }

    public byte[] downloadSignature(String signerAccessCode, String type) {
        requireId(signerAccessCode, "Signer access code");
        requireId(type, "Signature type");
        return callBinary("Failed to download signature",
                () -> http.getBinary(withAccessCode("/signature/" + encode(type), signerAccessCode)));
    }

    public Map<String, Object> getCurrentDocument(String signerId, String signerAccessCode) {
        String sid = requireId(signerId, "Signer ID");
        requireId(signerAccessCode, "Signer access code");
        return callMap("Failed to fetch signer's current document",
                () -> http.get(withAccessCode("/signers/" + sid + "/document", signerAccessCode)));
    }

    public PaginatedResult<DocumentListItem> listDocuments(String signerId, String signerAccessCode) {
        return listDocuments(signerId, signerAccessCode, null);
    }

    /**
     * List the documents assigned to a signer, with optional {@code status}, {@code method},
     * {@code search}, {@code sort} and paging filters supplied via {@link ListParams}.
     */
    public PaginatedResult<DocumentListItem> listDocuments(String signerId, String signerAccessCode, ListParams params) {
        String sid = requireId(signerId, "Signer ID");
        requireId(signerAccessCode, "Signer access code");
        Map<String, Object> query = params != null ? new HashMap<>(params.toQueryParams()) : new HashMap<>();
        query.put("signer-access-code", signerAccessCode);
        return callList("Failed to list signer's documents",
                () -> http.get("/signers/" + sid + "/documents", query),
                DocumentListItem.class);
    }

    public byte[] downloadDocument(String signerId, String documentId, String artifactName, String signerAccessCode) {
        String sid = requireId(signerId, "Signer ID");
        String docId = requireId(documentId, "Document ID");
        String artifact = artifactName != null ? artifactName : DEFAULT_ARTIFACT;
        requireId(signerAccessCode, "Signer access code");
        return callBinary("Failed to download signer document",
                () -> http.getBinary(withAccessCode(
                        "/signers/" + sid + "/documents/" + docId + "/download/" + encode(artifact),
                        signerAccessCode)));
    }

    public Map<String, Object> signMultiple(String signerAccessCode, List<String> documentIds) {
        requireId(signerAccessCode, "Signer access code");
        if (documentIds == null || documentIds.isEmpty()) {
            throw new ValidationException("At least one document ID is required");
        }
        String json = serialise(Map.of("document_ids", documentIds));
        return callMap("Failed to sign multiple documents",
                () -> http.put(withAccessCode("/signers/documents/sign-multiple", signerAccessCode), json));
    }

    public Map<String, Object> declineMultiple(String signerAccessCode, List<String> documentIds, String declineReason) {
        requireId(signerAccessCode, "Signer access code");
        if (documentIds == null || documentIds.isEmpty()) {
            throw new ValidationException("At least one document ID is required");
        }
        requireId(declineReason, "Decline reason");
        Map<String, Object> body = new HashMap<>();
        body.put("document_ids", documentIds);
        body.put("decline_reason", declineReason);
        String json = serialise(body);
        return callMap("Failed to decline multiple documents",
                () -> http.put(withAccessCode("/signers/documents/decline-multiple", signerAccessCode), json));
    }
}
