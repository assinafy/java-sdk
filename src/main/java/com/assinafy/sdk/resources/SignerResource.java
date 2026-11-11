package com.assinafy.sdk.resources;


import com.assinafy.sdk.models.AccountTheme;
import com.assinafy.sdk.models.CertifiedCopy;
import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.Document;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.SignerIdentityVerification;
import com.assinafy.sdk.models.enums.DocumentArtifactName;
import com.assinafy.sdk.request.CreateSignerRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.UpdateSignerRequest;
import com.assinafy.sdk.util.ResponseHandler;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
     * <p>Wire contract, payloads and failures: {@link #create(CreateSignerRequest, String)}.</p>
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
     * supplied CPF/CNPJ is sent as {@code government_id}; the API accepts formatting and normalizes
     * the value on save.
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/accounts/{accountId}/signers</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "full_name": "John Dove",
     *   "email": "signer@example.invalid",
     *   "whatsapp_phone_number": "+5548999990000",
     *   "government_id": "390.533.447-05"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "signer",
     *     "id": "62d6ee35c7741ca4006b9e11",
     *     "full_name": "John Dove",
     *     "email": "signer@example.invalid",
     *     "whatsapp_phone_number": "+5548999990000",
     *     "government_id": "39053344705",
     *     "has_accepted_terms": false
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The created signer; 400 One or more fields failed validation.;
     * 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param request signer profile; {@code full_name} is required
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the created signer
     */
    public Signer create(CreateSignerRequest request, String accountId) {
        String account = accountId(accountId);
        validateCreateRequest(request);
        return postSigner(request, account);
    }

    private Signer postSigner(CreateSignerRequest request, String accountId) {
        String id = pathSegment(accountId, "Account ID");
        logInfo("Creating signer", Map.of("hasEmail", request.getEmail() != null));
        String body = serialise(request);
        return requireSignerId(call("Failed to create signer",
                        () -> http.post("/accounts/" + id + "/signers", body), Signer.class),
                "Signer creation");
    }

    /**
     * Return an exact case-insensitive email match from the default account, or create the signer
     * when none exists. A request without an email is always created. An existing match is returned
     * unchanged; request name, phone, and CPF/CNPJ apply only when a signer is created.
     *
     * <p>Wire payloads and HTTP failures follow {@link #create(CreateSignerRequest, String)}.</p>
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
     * <p>Wire payloads and HTTP failures follow {@link #create(CreateSignerRequest, String)}.</p>
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

        try {
            return postSigner(request, account);
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
        if (request.getWhatsappPhoneNumber() != null) {
            requireWhatsappPhoneNumber(request.getWhatsappPhoneNumber());
        }
    }

    /**
     * Fetch a signer from the default account.
     *
     * <p>Wire contract, payloads and failures: {@link #get(String, String)}.</p>
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
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts/{accountId}/signers/{signerId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>signerId</code> (path, required): The signer ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "signer",
     *     "id": "62d6ee35c7741ca4006b9e11",
     *     "full_name": "John Signer",
     *     "email": null,
     *     "whatsapp_phone_number": null,
     *     "has_accepted_terms": false
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The signer; 404 The requested resource does not exist.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p>Wire contract, payloads and failures: {@link #list(ListParams, String)}.</p>
     *
     * @return paginated signers
     */
    public PaginatedResult<Signer> list() {
        return list(new ListParams(), null);
    }

    /**
     * List signers for the default account.
     *
     * <p>Wire contract, payloads and failures: {@link #list(ListParams, String)}.</p>
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
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts/{accountId}/signers</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>search</code> (query, optional): Filter by full_name or email.</li>
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
     *       "resource": "signer",
     *       "id": "62d6ee35c7741ca4006b9e11",
     *       "full_name": "John Signer",
     *       "email": null,
     *       "whatsapp_phone_number": null,
     *       "has_accepted_terms": false
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 A page of signers; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p>Wire contract, payloads and failures: {@link #update(String, UpdateSignerRequest, String)}.</p>
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
     * <p><strong>HTTP:</strong> <code>PUT /v1/accounts/{accountId}/signers/{signerId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>signerId</code> (path, required): The signer ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "full_name": "John Dove",
     *   "email": "john@example.invalid",
     *   "whatsapp_phone_number": "+5548999990000",
     *   "government_id": "39053344705"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "signer",
     *     "id": "62d6ee35c7741ca4006b9e11",
     *     "full_name": "John Signer",
     *     "email": null,
     *     "whatsapp_phone_number": null,
     *     "has_accepted_terms": false
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The updated signer; 400 One or more fields failed validation.;
     * 404 The requested resource does not exist.; 401 Missing or invalid credentials.; 500 Unexpected
     * server error. Non-2xx HTTP or numeric envelope statuses raise <code>ApiException</code>; 401/403
     * raise <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O
     * failures raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * @param signerId signer ID
     * @param request fields to update; {@code null} sends an empty object
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the updated signer
     */
    public Signer update(String signerId, UpdateSignerRequest request, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String sid = pathSegment(signerId, "Signer ID");
        validateUpdateRequest(request);
        String body = serialise(request != null ? request : Map.of());
        return requireSignerId(call("Failed to update signer",
                        () -> http.put("/accounts/" + id + "/signers/" + sid, body), Signer.class),
                "Signer update");
    }

    private static void validateUpdateRequest(UpdateSignerRequest request) {
        if (request == null) return;
        if (request.getFullName() != null && request.getFullName().isBlank()) {
            throw new ValidationException("Signer full_name must not be blank");
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) requireEmail(request.getEmail());
        if (request.getWhatsappPhoneNumber() != null && !request.getWhatsappPhoneNumber().isEmpty()) {
            requireWhatsappPhoneNumber(request.getWhatsappPhoneNumber());
        }
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
     * <p>Wire contract, payloads and failures: {@link #delete(String, String)}.</p>
     *
     * @param signerId signer ID
     */
    public void delete(String signerId) {
        delete(signerId, null);
    }

    /**
     * Delete a signer ({@code DELETE /accounts/{id}/signers/{signerId}}).
     *
     * <p><strong>HTTP:</strong> <code>DELETE /v1/accounts/{accountId}/signers/{signerId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>signerId</code> (path, required): The signer ID.</li>
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
     * <p>Documented HTTP statuses: 200 Signer deleted; 404 The requested resource does not exist.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p>Wire payloads and HTTP failures follow {@link #list(ListParams, String)}.</p>
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
     * <p>Wire payloads and HTTP failures follow {@link #list(ListParams, String)}.</p>
     *
     * @param email signer email
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the matching signer, or {@code null}
     */
    public Signer findByEmail(String email, String accountId) {
        requireEmail(email);
        try {
            String lower = email.toLowerCase(Locale.ROOT);
            int page = 1;
            while (true) {
                PaginatedResult<Signer> result = list(ListParams.builder().search(email).perPage(50).page(page).build(), accountId);
                Signer match = result.getData().stream()
                        .filter(s -> s.getEmail() != null && s.getEmail().toLowerCase(Locale.ROOT).equals(lower))
                        .findFirst().orElse(null);
                if (match != null) return match;
                if (result.getData().isEmpty() || result.getMeta() == null || result.getMeta().getLastPage() == null
                        || page >= result.getMeta().getLastPage()) return null;
                page++;
            }
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) return null;
            throw e;
        }
    }

    /**
     * Fetch the signer identified by a signer access code ({@code GET /signers/self}).
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/signers/self</code>.
     * <strong>Authentication:</strong> <code>signer-access-code</code> query credential.</p>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "signer",
     *     "id": "62d6ee35c7741ca4006b9e11",
     *     "full_name": "John Signer",
     *     "email": null,
     *     "whatsapp_phone_number": null,
     *     "has_accepted_terms": false,
     *     "has_signature": true,
     *     "has_initial": false,
     *     "is_signature_reusable": false,
     *     "verification_method": "IdDocumentFaceMatchBasic"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The signer; 401 Missing or invalid credentials.; 500 Unexpected
     * server error. Non-2xx HTTP or numeric envelope statuses raise <code>ApiException</code>; 401/403
     * raise <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O
     * failures raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
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
     * Read the current signer's hosted identity-verification state.
     * <p><strong>HTTP:</strong> {@code GET /v1/signers/identity-verification?signer-access-code=...}.
     * The signer access code is the only credential; there is no body. Requires an
     * {@code IdDocumentFaceMatchBasic} assignment. A session can start only before one exists or
     * after it expires before capture. Pending, InReview, Approved, Declined and Abandoned sessions
     * cannot restart; Declined/Abandoned require a new document and assignment.</p>
     * <pre>{"status":200,"message":"","data":{"can_start":false,"status":"Pending",
     * "url":"https://verify.assinafy.com.br/c/session#t=token",
     * "expires_at":"2026-10-20T21:00:00Z","completed_at":null}}</pre>
     * <p>Open the URL unchanged, including its fragment. Never log it or persist it in browser
     * storage. InReview permits signing, but holds certification until account approval.
     * HTTP/envelope errors raise {@code ApiException} (401/403: {@code AuthenticationException},
     * 429: {@code RateLimitException}); I/O failures raise {@code NetworkException}.</p>
     * @param signerAccessCode signer query credential
     * @return the unwrapped verification state
     * @throws ValidationException if the access code is missing
     */
    public SignerIdentityVerification identityVerification(String signerAccessCode) {
        requireId(signerAccessCode, "Signer access code");
        return call("Failed to fetch signer identity verification",
                () -> http.get(withAccessCode("/signers/identity-verification", signerAccessCode)),
                SignerIdentityVerification.class);
    }

    /**
     * Record explicit biometric-processing consent and open a hosted verification session.
     * <p><strong>HTTP:</strong> {@code POST /v1/signers/identity-verification?signer-access-code=...}.
     * Only the signer query credential is sent; JSON body: {@code {"consent":true}}. Pass true
     * only after obtaining the signer's explicit biometric-processing consent.</p>
     * <pre>{"status":200,"message":"","data":{"id":"verification-id",
     * "url":"https://verify.assinafy.com.br/c/session#t=token",
     * "expires_at":"2026-10-20T21:00:00Z"}}</pre>
     * <p>Open the bearer URL unchanged, including its fragment, without logging it or saving it in
     * browser storage. An expired capture session can be replaced; any other existing session
     * returns 400. Resending never replaces a live session; restarting is not billed again.
     * HTTP/envelope errors raise {@code ApiException} (401/403: {@code AuthenticationException},
     * 429: {@code RateLimitException}); I/O failures raise {@code NetworkException}.</p>
     * @param signerAccessCode signer query credential
     * @param consent explicit consent to biometric processing; must be true
     * @return the unwrapped session ID, capture URL and expiry
     * @throws ValidationException if the access code is missing or consent is false
     */
    public SignerIdentityVerification startIdentityVerification(String signerAccessCode, boolean consent) {
        requireId(signerAccessCode, "Signer access code");
        if (!consent) throw new ValidationException("Explicit biometric-processing consent is required");
        String json = serialise(Map.of("consent", true));
        return call("Failed to start signer identity verification",
                () -> http.post(withAccessCode("/signers/identity-verification", signerAccessCode), json),
                SignerIdentityVerification.class);
    }

    /**
     * Record that the signer accepted the terms of use ({@code PUT /signers/accept-terms}).
     * Authenticated by the signer access code, passed as the {@code signer-access-code} query
     * parameter; the endpoint takes no request body and returns no payload.
     *
     * <p><strong>HTTP:</strong> <code>PUT /v1/signers/accept-terms</code>.
     * <strong>Authentication:</strong> <code>signer-access-code</code> query credential.</p>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": ""
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Terms accepted; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param signerAccessCode signer query credential
     */
    public void acceptTerms(String signerAccessCode) {
        requireId(signerAccessCode, "Signer access code");
        callVoid("Failed to accept terms",
                () -> http.put(withAccessCode("/signers/accept-terms", signerAccessCode), null));
    }

    /**
     * Confirm signer data.
     * <p><strong>HTTP:</strong> {@code PUT /v1/documents/{documentId}/signers/confirm-data}.
     * The signer confirms or updates their data before signing. Uses the signer access code. With the code of a document of an envelope, it acts on the signer&#x27;s request on the document the route names, if that document is in the same envelope, carrying the code&#x27;s e-mail or WhatsApp verification there. The code&#x27;s own document, taken out of its envelope, takes the confirmation too while a document of the envelope still awaits signatures: it signs nothing (that document stays cancelled) and opens the reads a certificate or identity-check signer&#x27;s method gates, so the signer reaches the documents left.</p>
     * <p>Authentication: signer access code only.</p>
     * <p>Request: {&quot;email&quot;: &quot;signer@example.invalid&quot;, &quot;whatsapp_phone_number&quot;: &quot;value&quot;, &quot;has_accepted_terms&quot;: true}.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: []}</pre>
     * <p>Statuses: 410 With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer&#x27;s status and asks for the certified copy (`GET /v1/sign`).; 404 The route names a document the code doesn&#x27;t reach: outside the code&#x27;s envelope, or one the signer is not on.; 200 Data confirmed. `data` is an empty list.; 400 An e-mail or WhatsApp number the signer&#x27;s methods need is missing, invalid or not theirs, or the signer&#x27;s step has not started yet (sequential signing).; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param documentId document ID
     * @param signerAccessCode signer query credential
     * @param data email and/or whatsapp_phone_number required by the signing method; optional has_accepted_terms
     * @return null for the current empty confirmation response; a signer when an older deployment returns one
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public Signer confirmSignerData(String documentId, String signerAccessCode, Map<String, Object> data) {
        String docId = pathSegment(documentId, "Document ID");
        requireId(signerAccessCode, "Signer access code");
        Map<String, Object> body = data != null ? new HashMap<>(data) : new HashMap<>();
        String json = serialise(body);
        Object confirmed = call("Failed to confirm signer data",
                () -> http.put(withAccessCode("/documents/" + docId + "/signers/confirm-data", signerAccessCode), json),
                Object.class);
        if (confirmed == null || confirmed instanceof List<?> values && values.isEmpty()) return null;
        return ResponseHandler.convert(confirmed, Signer.class);
    }

    /**
     * Submit the OTP verification code sent to the signer ({@code POST /verify}). Authenticated by
     * the signer access code (passed as the {@code signer-access-code} query parameter); the body
     * carries only {@code verification-code}.
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/verify</code>.
     * <strong>Authentication:</strong> <code>signer-access-code</code> query credential.</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "verification-code": "123456"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": ""
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Code verified; 400 One or more fields failed validation.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
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
     * Begin an ICP-Brasil digital-certificate signature and return the Web PKI operation token
     * ({@code POST /signers/certificate/start?signer-access-code={code}}).
     *
     * <p>Required for a signer whose {@code verification_method} is {@code DigitalCertificate}:
     * the ordinary {@link AssignmentResource#sign(String, String, String, java.util.List) sign}
     * endpoint rejects those signers with {@code 400}. The signer must have confirmed their data
     * and accepted the terms first.
     *
     * <p>The returned token is signed in the browser by the signer's own A1 or A3 certificate
     * through the Web PKI extension, and the signed value is handed back to
     * {@link #completeCertificateSignature(String, String)}.
     *
     * <p>Request body — the access code travels in both the query and the body:
     * <pre>{@code
     * { "signer-access-code": "access-code" }
     * }</pre>
     *
     * <p>Response:
     * <pre>{@code
     * { "status": 200, "message": "", "data": { "token": "web-pki-token" } }
     * }</pre>
     *
     * <p>This route is available on production and sandbox for workspaces with Digital Certificate
     * enabled. It is absent from the published OpenAPI document. Both environments require an
     * actual ICP-Brasil certificate and Web PKI.
     *
     * @param signerAccessCode signer query credential
     * @return the Web PKI operation token the browser must sign
     * @throws ValidationException if the access code is blank, or the response carries no token
     */
    public String startCertificateSignature(String signerAccessCode) {
        requireId(signerAccessCode, "Signer access code");
        String json = serialise(Map.of("signer-access-code", signerAccessCode));
        Map<String, Object> data = callMap("Failed to start certificate signature",
                () -> http.post(withAccessCode("/signers/certificate/start", signerAccessCode), json));
        return requireStringField(data, "token", "Certificate signature start");
    }

    /**
     * Complete an ICP-Brasil digital-certificate signature with the browser-signed Web PKI token
     * ({@code POST /signers/certificate/complete?signer-access-code={code}}).
     *
     * <p>Once this succeeds, downloading the document's {@code pades} artifact yields the qualified
     * PAdES signature.
     *
     * <p>Request body:
     * <pre>{@code
     * { "signer-access-code": "access-code", "token": "signed-web-pki-token" }
     * }</pre>
     *
     * <p>Response:
     * <pre>{@code
     * { "status": 200, "message": "", "data": { "signerName": "Certificate Signer" } }
     * }</pre>
     *
     * <p>This route is available on production and sandbox for workspaces with Digital Certificate
     * enabled. It is absent from the published OpenAPI document. Both environments require an
     * actual ICP-Brasil certificate and Web PKI.
     *
     * @param signerAccessCode signer query credential
     * @param token the Web PKI token from {@link #startCertificateSignature(String)}, after the
     *              browser has signed it with the signer's certificate
     * @return the certificate holder's name as reported by the signature
     * @throws ValidationException if the access code or token is blank, or the response carries no
     *         signer name
     */
    public String completeCertificateSignature(String signerAccessCode, String token) {
        requireId(signerAccessCode, "Signer access code");
        requireId(token, "Certificate token");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("signer-access-code", signerAccessCode);
        body.put("token", token);
        String json = serialise(body);
        Map<String, Object> data = callMap("Failed to complete certificate signature",
                () -> http.post(withAccessCode("/signers/certificate/complete", signerAccessCode), json));
        return requireStringField(data, "signerName", "Certificate signature completion");
    }

    private static String requireStringField(Map<String, Object> data, String field, String label) {
        if (data != null && data.get(field) instanceof String value && !value.isBlank()) {
            return value;
        }
        throw new ValidationException(label + " response did not include " + field);
    }

    /**
     * Upload the signer's signature/initials image ({@code POST /signature}). Both {@code type}
     * (e.g. {@code signature} or {@code initial}) and {@code reuse} are optional per the docs.
     *
     * <p>Wire contract, payloads and failures: {@link #uploadSignature(String, String, byte[], Boolean)}.</p>
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
     * <p><strong>HTTP:</strong> <code>POST /v1/signature</code>.
     * <strong>Authentication:</strong> <code>signer-access-code</code> query credential.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>type</code> (query, optional): Image type, e.g. <code>signature</code> or <code>initial</code>.</li>
     * <li><code>reuse</code> (query, optional): Whether the signer opted to reuse this signature in future processes. When set, updates the signer's <code>is_signature_reusable</code> flag; when omitted, the flag is left unchanged.</li>
     * </ul>
     * <p>Request body: <code>image/png</code>, raw artifact bytes.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": ""
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Signature stored; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p><strong>HTTP:</strong> <code>GET /v1/signature/{signatureType}</code>.
     * <strong>Authentication:</strong> <code>signer-access-code</code> query credential.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>signatureType</code> (path, required): Image type (e.g. <code>signature</code>, <code>initial</code>).</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>image/*</code>, raw artifact bytes.</p>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The signature image; 404 The requested resource does not exist.;
     * 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p><strong>HTTP:</strong> <code>GET /v1/signers/{signerId}/document</code>.
     * <strong>Authentication:</strong> <code>signer-access-code</code> query credential.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>signerId</code> (path, required): The signer ID.</li>
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
     * <p>Documented HTTP statuses: 200 The document with the signer&#x27;s items; 404 The requested
     * resource does not exist.; 401 Missing or invalid credentials.; 500 Unexpected server error.
     * Non-2xx HTTP or numeric envelope statuses raise <code>ApiException</code>; 401/403 raise
     * <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O failures
     * raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
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
     * Reads the signer's document with optional envelope membership.
     * {@code GET /v1/signers/{signerId}/document?signer-access-code=...&expand=envelope}, no body.
     * Payload and failures follow {@link #getCurrentDocument(String, String)}; data additionally
     * includes nullable {@code envelope: {id, name}} when expanded.
     * @param signerId signer ID
     * @param signerAccessCode invitation code
     * @param includeEnvelope whether to include envelope ID and name
     * @return the document
     */
    public Document getCurrentDocumentTyped(String signerId, String signerAccessCode, boolean includeEnvelope) {
        if (!includeEnvelope) return getCurrentDocumentTyped(signerId, signerAccessCode);
        String path = withAccessCode("/signers/" + pathSegment(signerId, "Signer ID") + "/document",
                requireId(signerAccessCode, "Signer access code")) + "&expand=envelope";
        return call("Failed to fetch signer's current document", () -> http.get(path), Document.class);
    }

    /**
     * Fetch the signer's current document as a typed model.
     *
     * <p>Wire contract, payloads and failures: {@link #getCurrentDocument(String, String)}.
     * This method converts the same payload to the declared response model.</p>
     *
     * @param signerId signer ID
     * @param signerAccessCode signer query credential
     * @return document details
     */
    public Document getCurrentDocumentTyped(String signerId, String signerAccessCode) {
        return ResponseHandler.convert(
                getCurrentDocument(signerId, signerAccessCode), Document.class);
    }

    /**
     * List a signer's documents with default paging.
     *
     * <p>Wire contract, payloads and failures: {@link #listDocuments(String, String, ListParams)}.</p>
     *
     * @param signerId signer ID
     * @param signerAccessCode signer query credential
     * @return paginated documents
     */
    public PaginatedResult<Document> listDocuments(String signerId, String signerAccessCode) {
        return listDocuments(signerId, signerAccessCode, null);
    }

    /**
     * List the documents assigned to a signer ({@code GET /signers/{signerId}/documents}),
     * authenticated by the signer access code alone; the workspace API key or bearer token is never
     * sent. Use {@link #listDocumentsAsWorkspace(String, ListParams)} for workspace access. The
     * endpoint documents only {@code page}/{@code per-page} paging (supply via {@link ListParams});
     * for server-side text search use {@link #searchDocuments(String, String, String)}.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/signers/{signerId}/documents</code>.
     * <strong>Authentication:</strong> <code>signer-access-code</code> query credential.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>signerId</code> (path, required): The signer ID.</li>
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
     * <p>Documented HTTP statuses: 200 The signer&#x27;s documents; 401 Missing or invalid credentials.;
     * 500 Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param signerId signer ID
     * @param signerAccessCode signer query credential
     * @param params paging options; {@code null} sends only the access code
     * @return paginated documents
     */
    public PaginatedResult<Document> listDocuments(String signerId, String signerAccessCode, ListParams params) {
        String sid = pathSegment(signerId, "Signer ID");
        requireId(signerAccessCode, "Signer access code");
        Map<String, Object> query = params != null ? new HashMap<>(params.toQueryParams()) : new HashMap<>();
        query.put("signer-access-code", signerAccessCode);
        return callList("Failed to list signer's documents",
                () -> http.get("/signers/" + sid + "/documents", query),
                Document.class);
    }

    /**
     * Search the documents a signer is party to, returning a compact representation
     * ({@code GET /signers/{signerId}/documents/search}), authenticated by the signer access code
     * alone. Use {@link #searchDocumentsAsWorkspace(String, String)} for workspace access.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/signers/{signerId}/documents/search</code>.
     * <strong>Authentication:</strong> <code>signer-access-code</code> query credential.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>signerId</code> (path, required): The signer ID.</li>
     * <li><code>search</code> (query, optional): Search term.</li>
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
     * @param signerId signer ID
     * @param signerAccessCode signer query credential
     * @param search free-text query matched against the signer's documents
     * @return paginated matching documents
     */
    public PaginatedResult<Document> searchDocuments(String signerId, String signerAccessCode, String search) {
        return searchDocuments(signerId, signerAccessCode, search, null);
    }

    /**
     * List a signer's documents with the workspace API key or bearer token, using default paging.
     *
     * <p>Wire contract, payloads and failures: {@link #listDocumentsAsWorkspace(String, ListParams)}.</p>
     *
     * @param signerId ID of a signer in the authenticated workspace
     * @return paginated documents
     * @throws ValidationException if the signer ID is blank
     */
    public PaginatedResult<Document> listDocumentsAsWorkspace(String signerId) {
        return listDocumentsAsWorkspace(signerId, null);
    }

    /**
     * List the documents a signer of the authenticated workspace is party to, using the workspace
     * API key or bearer token instead of a signer access code.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/signers/{signerId}/documents</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>; the signer must belong to
     * that workspace.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>signerId</code> (path, required): The signer ID.</li>
     * <li><code>page</code> (query, optional): Page number.</li>
     * <li><code>per-page</code> (query, optional): Records per page (max 100).</li>
     * </ul>
     * <p>Response and statuses are the same as {@link #listDocuments(String, String, ListParams)}:
     * a <code>data</code> array of documents with <code>X-Pagination-*</code> headers.</p>
     *
     * @param signerId ID of a signer in the authenticated workspace
     * @param params paging options; {@code null} sends none
     * @return paginated documents
     * @throws ValidationException if the signer ID is blank or paging is out of range
     */
    public PaginatedResult<Document> listDocumentsAsWorkspace(String signerId, ListParams params) {
        String sid = pathSegment(signerId, "Signer ID");
        Map<String, Object> query = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list signer's documents",
                () -> http.get("/signers/" + sid + "/documents", query),
                Document.class);
    }

    /**
     * Search the documents a signer of the authenticated workspace is party to, using the
     * workspace API key or bearer token instead of a signer access code.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/signers/{signerId}/documents/search</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>; the signer must belong to
     * that workspace.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>signerId</code> (path, required): The signer ID.</li>
     * <li><code>search</code> (query, optional): Search term.</li>
     * </ul>
     * <p>Response and statuses are the same as {@link #searchDocuments(String, String, String)}.</p>
     *
     * @param signerId ID of a signer in the authenticated workspace
     * @param search free-text query, or {@code null} to omit it
     * @return matching documents
     * @throws ValidationException if the signer ID is blank
     */
    public PaginatedResult<Document> searchDocumentsAsWorkspace(String signerId, String search) {
        return searchDocumentsAsWorkspace(signerId, search, null);
    }

    /**
     * Download a signer document using an access-code query.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/signers/{signerId}/documents/{documentId}/download/{artifactName}</code>.
     * <strong>Authentication:</strong> Public (no SDK credential).</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>signerId</code> (path, required): The signer ID.</li>
     * <li><code>documentId</code> (path, required): Document ID.</li>
     * <li><code>artifactName</code> (path, required): Artifact type.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/pdf</code>, raw artifact bytes.</p>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The artifact binary; 404 The requested resource does not exist.;
     * 500 Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p>Wire contract, payloads and failures: {@link #downloadDocument(String, String, String, String)}.</p>
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
     * <p><strong>HTTP:</strong> <code>PUT /v1/signers/documents/sign-multiple</code>.
     * <strong>Authentication:</strong> <code>signer-access-code</code> query credential.</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "document_ids": [
     *     "document_ids_example"
     *   ]
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
     * <p>Documented HTTP statuses: 200 Signing result; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p><strong>HTTP:</strong> <code>PUT /v1/signers/documents/decline-multiple</code>.
     * <strong>Authentication:</strong> <code>signer-access-code</code> query credential.</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "document_ids": [
     *     "document_ids_example"
     *   ],
     *   "decline_reason": "Unfavorable terms."
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
     * <p>Documented HTTP statuses: 200 Decline result; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param signerAccessCode signer query credential
     * @param documentIds document ID list
     * @param declineReason non-blank decline reason
     * @return the response data map, normally wrapping an empty array
     */
    public Map<String, Object> declineMultiple(String signerAccessCode, List<String> documentIds, String declineReason) {
        requireId(signerAccessCode, "Signer access code");
        validateDocumentIds(documentIds);
        requireText(declineReason, "Decline reason", 2000);
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
    /**
     * Workspace brand for the signing page.
     * <p><strong>HTTP:</strong> {@code GET /v1/sign/theme}.
     * The brand of the workspace that sent the document: its name, its colors (`primary_color` is the button background, `secondary_color` the button text, 6-digit hex without `#`) and the address of its logo, or `null` when it has none. The logo address carries no access code: append `signer-access-code` to it, as to every signer call. Available before verification and after the document is deleted, so every screen of the signing page can carry the brand. Uses the signer access code.</p>
     * <p>Authentication: signer access code only.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;account_name&quot;: &quot;value&quot;, &quot;primary_color&quot;: &quot;value&quot;, &quot;secondary_color&quot;: &quot;value&quot;, &quot;logo&quot;: &quot;value&quot;}}</pre>
     * <p>Statuses: 200 The brand; 400 With a signer access code: the signer&#x27;s step has not started yet (sequential signing).; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param signerAccessCode signer invitation access code
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public AccountTheme getTheme(String signerAccessCode) {
        requireId(signerAccessCode, "Signer access code");
        return call("Failed to fetch signing theme", () -> http.get(withAccessCode("/sign/theme", signerAccessCode)), AccountTheme.class);
    }

    /**
     * Workspace logo for the signing page.
     * <p><strong>HTTP:</strong> {@code GET /v1/sign/logo}.
     * The logo image of the workspace that sent the document. Available before verification and after the document is deleted. Uses the signer access code.</p>
     * <p>Authentication: signer access code only.</p>
     * <p>Request: none.</p>
     * <p>Success (image/*):</p><pre>&quot;(binary bytes)&quot;</pre>
     * <p>Statuses: 200 The logo image; 400 With a signer access code: the signer&#x27;s step has not started yet (sequential signing).; 401 Missing or invalid credentials.; 404 The workspace has no logo.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param signerAccessCode signer invitation access code
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public byte[] downloadLogo(String signerAccessCode) {
        requireId(signerAccessCode, "Signer access code");
        return callBinary("Failed to download signing logo", () -> http.getBinary(withAccessCode("/sign/logo", signerAccessCode), "*/*"));
    }

    /**
     * Send the certified copy again.
     * <p><strong>HTTP:</strong> {@code POST /v1/sign/certified-copy}.
     * Sends the signer their certified document again, with a download link valid for 30 minutes. It goes on the channel of the signature request (e-mail when the request used e-mail, otherwise WhatsApp) and only to the e-mail or number the signing messages went to, never to a changed signer record. Works with the signer&#x27;s access code after they signed, including once the code is closed and after the workspace is deleted. E-mail: once every 2 minutes and 10 times in 24 hours. WhatsApp: once per signature request; after that the signer must ask the sender. `certified_copy` on `GET /v1/sign` says whether it is available.</p>
     * <p>Authentication: signer access code only.</p>
     * <p>Request: {&quot;document_id&quot;: &quot;value&quot;}.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;available&quot;: true, &quot;notification_method&quot;: &quot;Email&quot;, &quot;resends_left&quot;: 1, &quot;sent_to&quot;: &quot;value&quot;}}</pre>
     * <p>Statuses: 200 Sent; 404 `document_id` names a document outside the code&#x27;s envelope, or one the signer is not on.; 409 The signer has not signed, the document is not certified yet, or the request has no e-mail or WhatsApp to send to (ask the sender).; 429 E-mail: asked again within 2 minutes or more than 10 times in 24 hours. WhatsApp: already re-sent once (ask the sender).; 500 The message could not be sent; nothing was used up, so it can be asked again.; 400 The signer&#x27;s step has not started yet (sequential signing), or, with the code of a document of an envelope, `document_id` is no document id.; 401 Missing or invalid credentials.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param signerAccessCode signer invitation access code, including a closed code
     * @param documentId optional other document of the same envelope
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public CertifiedCopy resendCertifiedCopy(String signerAccessCode, String documentId) {
        requireId(signerAccessCode, "Signer access code");
        if (documentId != null) requireId(documentId, "Document ID");
        String json = serialise(documentId == null ? Map.of() : Map.of("document_id", documentId));
        return call("Failed to resend certified copy", () -> http.post(withAccessCode("/sign/certified-copy", signerAccessCode), json), CertifiedCopy.class);
    }

    /**
     * Download the certified copy from a sent link.
     * <p><strong>HTTP:</strong> {@code GET /v1/signers/downloads/{token}}.
     * The link in the completion and re-send messages. Valid 7 days in the completion message and 30 minutes when re-sent. Add `/pades` for the PAdES version on documents that have one. An expired or unknown link is redirected (`302`) to the expired-link page; for a known link the redirect carries `?token=`, with which the page can ask for a new one (`POST /v1/signers/downloads/{token}/resend`).</p>
     * <p>Authentication: public token only.</p>
     * <p>Request: none.</p>
     * <p>Success (application/pdf):</p><pre>&quot;(binary bytes)&quot;</pre>
     * <p>Statuses: 200 The certified PDF; 302 Expired or unknown link: redirected to the expired-link page.; 404 `/pades` on a document that has no PAdES version.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param token download-link token
     * @param pades true for the original PAdES signature artifact
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public byte[] downloadCertifiedCopy(String token, boolean pades) {
        String path = "/signers/downloads/" + pathSegment(token, "Download token") + (pades ? "/pades" : "");
        return callBinary("Failed to download certified copy", () -> http.getBinary(path, "*/*"));
    }

    /**
     * Send the certified copy again from an expired link.
     * <p><strong>HTTP:</strong> {@code POST /v1/signers/downloads/{token}/resend}.
     * Used by the page an expired download link leads to. Works with the link&#x27;s token even after it expired, and on the same terms as `POST /v1/sign/certified-copy`: the request&#x27;s channel, only the e-mail or number of the signing, the same limits. Copy receivers&#x27; links have no signature request (`409`).</p>
     * <p>Authentication: public token only.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: {&quot;available&quot;: true, &quot;notification_method&quot;: &quot;Email&quot;, &quot;resends_left&quot;: 1, &quot;sent_to&quot;: &quot;value&quot;}}</pre>
     * <p>Statuses: 200 Sent; 404 Unknown link.; 409 Nothing can be sent from here (copy receiver, Bypass signer, no recorded contact, or the document is not certified): ask the sender.; 429 E-mail: asked again within 2 minutes or more than 10 times in 24 hours. WhatsApp: already re-sent once.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param token known download-link token, including an expired token
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public CertifiedCopy resendCertifiedCopyFromToken(String token) {
        String path = "/signers/downloads/" + pathSegment(token, "Download token") + "/resend";
        return call("Failed to resend certified copy", () -> http.post(path, null), CertifiedCopy.class);
    }

    /**
     * Search signer's documents.
     * <p><strong>HTTP:</strong> {@code GET /v1/signers/{signerId}/documents/search}.
     * Search the documents a signer is party to (compact representation). Accepts either the signer access code or the account&#x27;s own credentials (API key or bearer token) — with account credentials, the signer must belong to that account&#x27;s workspace. With the signer access code, `signerId` must be the code&#x27;s own signer (otherwise `403`), only documents whose signing step has been reached are listed (for the code of a document of an envelope, only that envelope&#x27;s documents), and each assignment leaves out `signing_urls`.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: [{&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;account_id&quot;: &quot;value&quot;, &quot;template_id&quot;: &quot;value&quot;, &quot;name&quot;: &quot;value&quot;, &quot;status&quot;: &quot;value&quot;, &quot;artifacts&quot;: {}, &quot;is_closed&quot;: true, &quot;signing_url&quot;: &quot;value&quot;, &quot;verification_code&quot;: &quot;value&quot;, &quot;verification_url&quot;: &quot;value&quot;, &quot;decline_reason&quot;: &quot;value&quot;, &quot;declined_by&quot;: {&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;full_name&quot;: &quot;value&quot;, &quot;email&quot;: &quot;signer@example.invalid&quot;, &quot;whatsapp_phone_number&quot;: &quot;value&quot;, &quot;government_id&quot;: &quot;value&quot;, &quot;has_accepted_terms&quot;: true}, &quot;assignment&quot;: {&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;document_id&quot;: &quot;value&quot;, &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;, &quot;sender_email&quot;: &quot;signer@example.invalid&quot;, &quot;method&quot;: &quot;virtual&quot;, &quot;expires_at&quot;: &quot;2026-12-31T12:00:00Z&quot;, &quot;message&quot;: &quot;value&quot;, &quot;signers&quot;: [{&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;full_name&quot;: &quot;value&quot;, &quot;email&quot;: &quot;signer@example.invalid&quot;, &quot;whatsapp_phone_number&quot;: &quot;value&quot;, &quot;government_id&quot;: &quot;value&quot;, &quot;has_accepted_terms&quot;: true, &quot;verification_method&quot;: &quot;value&quot;, &quot;notification_methods&quot;: [&quot;value&quot;], &quot;step&quot;: 1, &quot;notified&quot;: true, &quot;identity_verification_status&quot;: &quot;Pending&quot;, &quot;completed&quot;: true, &quot;notification_history&quot;: [{&quot;event&quot;: &quot;value&quot;, &quot;status&quot;: &quot;sent&quot;, &quot;error_code&quot;: &quot;MailboxFull&quot;, &quot;error_message&quot;: &quot;value&quot;, &quot;sent_at&quot;: &quot;2026-12-31T12:00:00Z&quot;, &quot;failed_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}]}], &quot;copy_receivers&quot;: [{}], &quot;items&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;page&quot;: {&quot;id&quot;: &quot;value&quot;, &quot;number&quot;: 1, &quot;height&quot;: 1, &quot;width&quot;: 1, &quot;download_url&quot;: &quot;value&quot;}, &quot;signer&quot;: {}, &quot;field&quot;: {}, &quot;display_settings&quot;: &quot;value&quot;, &quot;value&quot;: &quot;value&quot;, &quot;completed&quot;: true}], &quot;summary&quot;: {&quot;signer_count&quot;: 1, &quot;completed_count&quot;: 1, &quot;signers&quot;: [{}]}, &quot;signing_urls&quot;: [{&quot;signer_id&quot;: &quot;value&quot;, &quot;url&quot;: &quot;value&quot;, &quot;embedded&quot;: true}]}, &quot;pages&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;number&quot;: 1, &quot;height&quot;: 1, &quot;width&quot;: 1, &quot;download_url&quot;: &quot;value&quot;}], &quot;envelope_id&quot;: &quot;value&quot;, &quot;envelope&quot;: {&quot;id&quot;: &quot;value&quot;, &quot;name&quot;: &quot;value&quot;}, &quot;has_contract&quot;: true, &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;, &quot;updated_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}]}</pre>
     * <p>Statuses: 410 With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer&#x27;s status and asks for the certified copy (`GET /v1/sign`).; 200 Matching documents; 403 The access code belongs to another signer; or, with a user&#x27;s token or key, a role, a membership or an OAuth scope refuses it.; 404 The signer is not in the caller&#x27;s workspace.; 400 With a signer access code, the signer&#x27;s step has not started yet (sequential signing); or `envelope_id` is not text, an entry of `errors`.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param signerId signer ID
     * @param signerAccessCode signer invitation code
     * @param search optional search text
     * @param envelopeId envelope to filter, or null for all
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public PaginatedResult<Document> searchDocuments(String signerId, String signerAccessCode, String search, String envelopeId) {
        String path = "/signers/" + pathSegment(signerId, "Signer ID") + "/documents/search";
        Map<String, Object> query = new HashMap<>();
        query.put("signer-access-code", requireId(signerAccessCode, "Signer access code"));
        if (search != null && !search.isBlank()) query.put("search", search);
        if (envelopeId != null) query.put("envelope_id", requireId(envelopeId, "Envelope ID"));
        return callList("Failed to search signer's documents", () -> http.get(path, query), Document.class);
    }

    /**
     * Search signer's documents.
     * <p><strong>HTTP:</strong> {@code GET /v1/signers/{signerId}/documents/search}.
     * Search the documents a signer is party to (compact representation). Accepts either the signer access code or the account&#x27;s own credentials (API key or bearer token) — with account credentials, the signer must belong to that account&#x27;s workspace. With the signer access code, `signerId` must be the code&#x27;s own signer (otherwise `403`), only documents whose signing step has been reached are listed (for the code of a document of an envelope, only that envelope&#x27;s documents), and each assignment leaves out `signing_urls`.</p>
     * <p>Authentication: configured API key or bearer token.</p>
     * <p>Request: none.</p>
     * <p>Success (application/json):</p><pre>{&quot;status&quot;: 200, &quot;message&quot;: &quot;&quot;, &quot;data&quot;: [{&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;account_id&quot;: &quot;value&quot;, &quot;template_id&quot;: &quot;value&quot;, &quot;name&quot;: &quot;value&quot;, &quot;status&quot;: &quot;value&quot;, &quot;artifacts&quot;: {}, &quot;is_closed&quot;: true, &quot;signing_url&quot;: &quot;value&quot;, &quot;verification_code&quot;: &quot;value&quot;, &quot;verification_url&quot;: &quot;value&quot;, &quot;decline_reason&quot;: &quot;value&quot;, &quot;declined_by&quot;: {&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;full_name&quot;: &quot;value&quot;, &quot;email&quot;: &quot;signer@example.invalid&quot;, &quot;whatsapp_phone_number&quot;: &quot;value&quot;, &quot;government_id&quot;: &quot;value&quot;, &quot;has_accepted_terms&quot;: true}, &quot;assignment&quot;: {&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;document_id&quot;: &quot;value&quot;, &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;, &quot;sender_email&quot;: &quot;signer@example.invalid&quot;, &quot;method&quot;: &quot;virtual&quot;, &quot;expires_at&quot;: &quot;2026-12-31T12:00:00Z&quot;, &quot;message&quot;: &quot;value&quot;, &quot;signers&quot;: [{&quot;resource&quot;: &quot;value&quot;, &quot;id&quot;: &quot;value&quot;, &quot;full_name&quot;: &quot;value&quot;, &quot;email&quot;: &quot;signer@example.invalid&quot;, &quot;whatsapp_phone_number&quot;: &quot;value&quot;, &quot;government_id&quot;: &quot;value&quot;, &quot;has_accepted_terms&quot;: true, &quot;verification_method&quot;: &quot;value&quot;, &quot;notification_methods&quot;: [&quot;value&quot;], &quot;step&quot;: 1, &quot;notified&quot;: true, &quot;identity_verification_status&quot;: &quot;Pending&quot;, &quot;completed&quot;: true, &quot;notification_history&quot;: [{&quot;event&quot;: &quot;value&quot;, &quot;status&quot;: &quot;sent&quot;, &quot;error_code&quot;: &quot;MailboxFull&quot;, &quot;error_message&quot;: &quot;value&quot;, &quot;sent_at&quot;: &quot;2026-12-31T12:00:00Z&quot;, &quot;failed_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}]}], &quot;copy_receivers&quot;: [{}], &quot;items&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;page&quot;: {&quot;id&quot;: &quot;value&quot;, &quot;number&quot;: 1, &quot;height&quot;: 1, &quot;width&quot;: 1, &quot;download_url&quot;: &quot;value&quot;}, &quot;signer&quot;: {}, &quot;field&quot;: {}, &quot;display_settings&quot;: &quot;value&quot;, &quot;value&quot;: &quot;value&quot;, &quot;completed&quot;: true}], &quot;summary&quot;: {&quot;signer_count&quot;: 1, &quot;completed_count&quot;: 1, &quot;signers&quot;: [{}]}, &quot;signing_urls&quot;: [{&quot;signer_id&quot;: &quot;value&quot;, &quot;url&quot;: &quot;value&quot;, &quot;embedded&quot;: true}]}, &quot;pages&quot;: [{&quot;id&quot;: &quot;value&quot;, &quot;number&quot;: 1, &quot;height&quot;: 1, &quot;width&quot;: 1, &quot;download_url&quot;: &quot;value&quot;}], &quot;envelope_id&quot;: &quot;value&quot;, &quot;envelope&quot;: {&quot;id&quot;: &quot;value&quot;, &quot;name&quot;: &quot;value&quot;}, &quot;has_contract&quot;: true, &quot;created_at&quot;: &quot;2026-12-31T12:00:00Z&quot;, &quot;updated_at&quot;: &quot;2026-12-31T12:00:00Z&quot;}]}</pre>
     * <p>Statuses: 410 With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer&#x27;s status and asks for the certified copy (`GET /v1/sign`).; 200 Matching documents; 403 The access code belongs to another signer; or, with a user&#x27;s token or key, a role, a membership or an OAuth scope refuses it.; 404 The signer is not in the caller&#x27;s workspace.; 400 With a signer access code, the signer&#x27;s step has not started yet (sequential signing); or `envelope_id` is not text, an entry of `errors`.; 401 Missing or invalid credentials.; 500 Unexpected server error.. Non-2xx HTTP/envelope responses raise ApiException; 401/403 use AuthenticationException, 429 uses RateLimitException, and I/O failures use NetworkException.</p>
     * @param signerId signer ID
     * @param search optional search text
     * @param envelopeId envelope to filter, or null for all
     * @return the unwrapped response; binary operations return raw bytes
     * @throws com.assinafy.sdk.exceptions.ValidationException if local arguments are invalid
     */
    public PaginatedResult<Document> searchDocumentsAsWorkspace(String signerId, String search, String envelopeId) {
        String path = "/signers/" + pathSegment(signerId, "Signer ID") + "/documents/search";
        Map<String, Object> query = new HashMap<>();
        if (search != null && !search.isBlank()) query.put("search", search);
        if (envelopeId != null) query.put("envelope_id", requireId(envelopeId, "Envelope ID"));
        return callList("Failed to search signer's documents", () -> http.get(path, query), Document.class);
    }

}
