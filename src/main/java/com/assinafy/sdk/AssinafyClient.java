package com.assinafy.sdk;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.http.OkHttpApiClient;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.Document;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.UploadAndRequestSignaturesResult;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.CreateSignerRequest;
import com.assinafy.sdk.request.SignerReference;
import com.assinafy.sdk.request.UpdateSignerRequest;
import com.assinafy.sdk.request.UploadAndRequestSignaturesRequest;
import com.assinafy.sdk.resources.ApiKeyResource;
import com.assinafy.sdk.resources.AssignmentResource;
import com.assinafy.sdk.resources.AuthenticationResource;
import com.assinafy.sdk.resources.BaseResource;
import com.assinafy.sdk.resources.DocumentResource;
import com.assinafy.sdk.resources.FieldResource;
import com.assinafy.sdk.resources.PublicDocumentResource;
import com.assinafy.sdk.resources.SignerResource;
import com.assinafy.sdk.resources.TagResource;
import com.assinafy.sdk.resources.TemplateResource;
import com.assinafy.sdk.resources.UserResource;
import com.assinafy.sdk.resources.WebhookResource;
import com.assinafy.sdk.resources.WorkspaceResource;
import com.assinafy.sdk.support.WebhookVerifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Entry point for Assinafy API operations. Credentials are optional for login and public
 * document endpoints; authenticated and account-scoped operations require the corresponding
 * credential/account configuration.
 *
 * <p>The client is thread-safe when used with its default OkHttp transport. Resource accessors
 * return the instances owned by this client.
 */
public class AssinafyClient {

    private static final int RECONCILIATION_ATTEMPTS = 5;
    private static final long RETRY_DELAY_MS = 500;

    private final DocumentResource documents;
    private final SignerResource signers;
    private final WorkspaceResource workspaces;
    private final AssignmentResource assignments;
    private final WebhookResource webhooks;
    private final TemplateResource templates;
    private final FieldResource fields;
    private final TagResource tags;
    private final PublicDocumentResource publicDocuments;
    private final ApiKeyResource apiKeys;
    private final AuthenticationResource authentication;
    private final UserResource users;
    private final WebhookVerifier webhookVerifier;
    private final Logger logger;
    private final String defaultAccountId;

    /**
     * Build a client from {@code options}, constructing the default OkHttp-backed transport.
     *
     * @param options authentication, endpoint, timeout, webhook, and logging options
     * @throws NullPointerException if {@code options} is {@code null}
     * @throws ValidationException if the configured timeout is not positive
     * @throws IllegalArgumentException if the configured base URL is invalid
     */
    public AssinafyClient(AssinafyClientOptions options) {
        this(buildHttp(options), options);
    }

    /**
     * Build a client over a caller-supplied transport. Used for testing with a stub
     * {@link ApiHttpClient}.
     */
    AssinafyClient(ApiHttpClient http, AssinafyClientOptions options) {
        Objects.requireNonNull(http, "http");
        Objects.requireNonNull(options, "options");

        this.defaultAccountId = options.getAccountId();
        this.logger = options.getLogger() != null ? options.getLogger() : NoOpLogger.INSTANCE;

        this.documents = new DocumentResource(http, defaultAccountId, this.logger);
        this.signers = new SignerResource(http, defaultAccountId, this.logger);
        // Workspace operations take an explicit account ID, so no default is bound.
        this.workspaces = new WorkspaceResource(http, null, this.logger);
        this.assignments = new AssignmentResource(http, defaultAccountId, this.logger);
        this.webhooks = new WebhookResource(http, defaultAccountId, this.logger);
        this.templates = new TemplateResource(http, defaultAccountId, this.logger);
        this.fields = new FieldResource(http, defaultAccountId, this.logger);
        this.tags = new TagResource(http, defaultAccountId, this.logger);
        this.publicDocuments = new PublicDocumentResource(http, this.logger);
        this.apiKeys = new ApiKeyResource(http, this.logger);
        this.authentication = new AuthenticationResource(http, this.logger);
        this.users = new UserResource(http, this.logger);
        this.webhookVerifier = new WebhookVerifier(options.getWebhookSecret());
    }

    /**
     * Create a client using an API key, account, and all other default options.
     *
     * @param apiKey API key sent in the {@code X-Api-Key} header
     * @param accountId default account for account-scoped operations
     * @return a configured client
     */
    public static AssinafyClient create(String apiKey, String accountId) {
        return new AssinafyClient(AssinafyClientOptions.builder()
                .apiKey(apiKey)
                .accountId(accountId)
                .build());
    }

    /**
     * Create a client using an API key and account while copying the remaining options.
     *
     * @param apiKey API key sent in the {@code X-Api-Key} header
     * @param accountId default account for account-scoped operations
     * @param extras optional base URL, token, webhook secret, timeout, and logger settings;
     *               {@code null} uses defaults
     * @return a configured client
     */
    public static AssinafyClient create(String apiKey, String accountId, AssinafyClientOptions extras) {
        if (extras == null) return create(apiKey, accountId);
        AssinafyClientOptions opts = AssinafyClientOptions.builder()
                .apiKey(apiKey)
                .accountId(accountId)
                .token(extras.getToken())
                .baseUrl(extras.getBaseUrl())
                .webhookSecret(extras.getWebhookSecret())
                .timeoutMs(extras.getTimeoutMs())
                .logger(extras.getLogger())
                .build();
        return new AssinafyClient(opts);
    }

    private static ApiHttpClient buildHttp(AssinafyClientOptions options) {
        Objects.requireNonNull(options, "options");
        if (options.getTimeoutMs() <= 0) {
            throw new ValidationException("Timeout must be greater than zero");
        }
        // OkHttpApiClient normalises the base URL in its constructor (single source of truth).
        String baseUrl = options.getBaseUrl() != null ? options.getBaseUrl() : AssinafyClientOptions.DEFAULT_BASE_URL;
        return new OkHttpApiClient(baseUrl, options.getApiKey(), options.getToken(), options.getTimeoutMs());
    }

    /**
     * High-level convenience flow that uploads a document and requests signatures in one call.
     *
     * <p>Performs, in order: (1) upload the PDF ({@code documents().upload}); (2) unless
     * {@link UploadAndRequestSignaturesRequest#isWaitForReady()} is false, block polling until the
     * document is ready (default; may take seconds); (3) create one persistent {@code Signer}
     * resource per {@link UploadAndRequestSignaturesRequest.SignerEntry} (existing signers are
     * reused by email); (4) persist a supplied CPF/CNPJ through the signer's documented
     * {@code government_id} update; (5) create a {@code virtual} assignment for those signers,
     * which sends the signature-request notifications. WhatsApp-only entries automatically use
     * WhatsApp for verification and notification.
     *
     * <p>This method has externally-visible side effects (it creates signer resources and dispatches
     * notifications) and blocks by default. At least one signer is required. If a step after upload
     * fails, the method deletes the uploaded document and signer records known to have been created
     * by this invocation; cleanup failures are attached to the original exception as suppressed
     * exceptions. The remote operations are not transactional: a transport failure after the
     * server commits an upload or signer can leave a resource whose ID was never returned. An
     * ambiguous assignment response is reconciled through document details before rollback. If its
     * outcome remains indeterminate, the document and signers are retained to avoid deleting a
     * successfully dispatched request, and reconciliation IDs are attached to the failure as
     * suppressed diagnostic context. CPF/CNPJ is applied only to signers whose create response
     * returned a valid ID. A signer recovered after an indeterminate create response is never
     * updated or deleted; if that entry includes CPF/CNPJ, the workflow fails before assignment
     * creation. Reused signer profiles are also left unchanged. Signers must have unique
     * case-insensitive email addresses, or unique WhatsApp numbers when no email is supplied.
     *
     * @param request document, assignment, signer, and polling settings
     * @return the created document, the assignment, and the signer IDs
     * @throws ValidationException if the request is absent, contains no signers, or contains a
     *                             signer without a name or delivery channel, with an invalid email,
     *                             or with a duplicate email/WhatsApp-only recipient
     */
    public UploadAndRequestSignaturesResult uploadAndRequestSignatures(UploadAndRequestSignaturesRequest request) {
        if (request == null) throw new ValidationException("Workflow request is required");
        if (request.getSigners() == null || request.getSigners().isEmpty()) {
            throw new ValidationException("At least one signer is required");
        }
        Set<String> emails = new HashSet<>();
        Set<String> whatsappNumbers = new HashSet<>();
        for (UploadAndRequestSignaturesRequest.SignerEntry signer : request.getSigners()) {
            if (signer == null || signer.getName() == null || signer.getName().isBlank()) {
                throw new ValidationException("Every signer requires a name");
            }
            if (signer.getEmail() != null) {
                BaseResource.requireEmail(signer.getEmail());
                if (!emails.add(signer.getEmail().toLowerCase(Locale.ROOT))) {
                    throw new ValidationException("Every workflow signer must be unique");
                }
            }
            if (signer.getEmail() == null && (signer.getWhatsappPhoneNumber() == null
                    || signer.getWhatsappPhoneNumber().isBlank())) {
                throw new ValidationException("Every signer requires an email or WhatsApp number");
            }
            if (signer.getEmail() == null && !whatsappNumbers.add(signer.getWhatsappPhoneNumber())) {
                throw new ValidationException("Every workflow signer must be unique");
            }
        }

        logInfo("Starting upload + signature workflow", Map.of("signerCount", request.getSigners().size()));

        Document document = documents.upload(
                request.getFileData(),
                request.getFileName(),
                request.getMetadata(),
                request.getAccountId()
        );

        List<String> signerIds = new ArrayList<>();
        List<String> createdSignerIds = new ArrayList<>();
        List<String> indeterminateSignerIds = new ArrayList<>();
        boolean cleanupAllowed = true;
        try {
            if (request.isWaitForReady()) {
                documents.waitUntilReady(document.getId());
            }

            List<SignerReference> signerRefs = new ArrayList<>();
            for (UploadAndRequestSignaturesRequest.SignerEntry entry : request.getSigners()) {
                CreateSignerRequest signerRequest = CreateSignerRequest.builder()
                        .fullName(entry.getName())
                        .email(entry.getEmail())
                        .whatsappPhoneNumber(entry.getWhatsappPhoneNumber())
                        .build();
                ResolvedSigner resolved = resolveSigner(signerRequest, request.getAccountId());
                Signer signer = resolved.signer();
                if (signer == null || signer.getId() == null || signer.getId().isBlank()) {
                    throw new ValidationException("Signer response did not include an ID");
                }
                if (resolved.ownership() == SignerOwnership.CREATED) {
                    createdSignerIds.add(signer.getId());
                } else if (resolved.ownership() == SignerOwnership.INDETERMINATE) {
                    indeterminateSignerIds.add(signer.getId());
                    if (entry.getCpf() != null && !entry.getCpf().isBlank()) {
                        throw new AssinafyException(
                                "Signer creation outcome is indeterminate; government ID was not changed",
                                Map.of("signerId", signer.getId()));
                    }
                }
                if (resolved.ownership() == SignerOwnership.CREATED
                        && entry.getCpf() != null && !entry.getCpf().isBlank()) {
                    signers.update(signer.getId(), UpdateSignerRequest.builder()
                            .governmentId(entry.getCpf())
                            .build(), request.getAccountId());
                }
                signerIds.add(signer.getId());

                SignerReference.Builder reference = SignerReference.builder().id(signer.getId());
                if (entry.getEmail() == null) {
                    reference.verificationMethod("Whatsapp")
                            .notificationMethods(List.of("Whatsapp"));
                }
                signerRefs.add(reference.build());
            }

            CreateAssignmentRequest assignmentRequest = CreateAssignmentRequest.builder()
                    .method("virtual")
                    .signers(signerRefs)
                    .message(request.getMessage())
                    .expiresAt(request.getExpiresAt())
                    .copyReceivers(request.getCopyReceivers())
                    .build();

            Assignment assignment;
            try {
                assignment = assignments.create(document.getId(), assignmentRequest);
            } catch (RuntimeException createFailure) {
                boolean ambiguous = isAmbiguousAssignmentFailure(createFailure);
                assignment = ambiguous ? recoverAssignment(document.getId(), createFailure) : null;
                if (ambiguous && assignment == null) {
                    cleanupAllowed = false;
                    createFailure.addSuppressed(new AssinafyException(
                            "Assignment outcome is indeterminate; workflow resources were retained",
                            Map.of("documentId", document.getId(),
                                    "signerIds", List.copyOf(signerIds))));
                }
                if (assignment == null) throw createFailure;
            }
            UploadAndRequestSignaturesResult result =
                    new UploadAndRequestSignaturesResult(document, assignment, signerIds);
            logInfo("Upload + signature workflow completed", Map.of("documentId", document.getId()));
            return result;
        } catch (RuntimeException failure) {
            if (cleanupAllowed) {
                try {
                    deleteDocumentAfterProcessing(document.getId());
                } catch (RuntimeException cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
                for (int i = createdSignerIds.size() - 1; i >= 0; i--) {
                    try {
                        deleteSignerAfterCreate(createdSignerIds.get(i), request.getAccountId());
                    } catch (RuntimeException cleanupFailure) {
                        failure.addSuppressed(cleanupFailure);
                    }
                }
            }
            if (!indeterminateSignerIds.isEmpty()) {
                failure.addSuppressed(new AssinafyException(
                        "Signer ownership is indeterminate; signer records were retained",
                        Map.of("signerIds", List.copyOf(indeterminateSignerIds))));
            }
            throw failure;
        }
    }

    private ResolvedSigner resolveSigner(CreateSignerRequest request, String accountId) {
        if (request.getEmail() != null) {
            Signer existing = signers.findByEmail(request.getEmail(), accountId);
            if (existing != null) return new ResolvedSigner(existing, SignerOwnership.REUSED);
        }
        try {
            return new ResolvedSigner(signers.create(request, accountId), SignerOwnership.CREATED);
        } catch (RuntimeException createFailure) {
            boolean duplicate = isDuplicateSignerCreateFailure(createFailure);
            boolean ambiguous = isAmbiguousSignerCreateFailure(createFailure);
            if (request.getEmail() != null && (duplicate || ambiguous)) {
                Signer recovered = recoverSigner(request.getEmail(), accountId, createFailure);
                if (recovered != null) {
                    return new ResolvedSigner(recovered, duplicate
                            ? SignerOwnership.REUSED : SignerOwnership.INDETERMINATE);
                }
            }
            throw createFailure;
        }
    }

    private Assignment recoverAssignment(String documentId, RuntimeException createFailure) {
        for (int attempt = 1; attempt <= RECONCILIATION_ATTEMPTS; attempt++) {
            try {
                Assignment assignment = documents.details(documentId).getAssignment();
                if (assignment != null && assignment.getId() != null && !assignment.getId().isBlank()) {
                    return assignment;
                }
            } catch (RuntimeException recoveryFailure) {
                if (!isTransientRecoveryFailure(recoveryFailure) || attempt == RECONCILIATION_ATTEMPTS) {
                    createFailure.addSuppressed(recoveryFailure);
                    return null;
                }
            }
            if (attempt < RECONCILIATION_ATTEMPTS && !pauseForRetry(createFailure, "assignment reconciliation")) {
                return null;
            }
        }
        return null;
    }

    private Signer recoverSigner(String email, String accountId, RuntimeException createFailure) {
        for (int attempt = 1; attempt <= RECONCILIATION_ATTEMPTS; attempt++) {
            try {
                Signer signer = signers.findByEmail(email, accountId);
                if (signer != null) return signer;
            } catch (RuntimeException lookupFailure) {
                if (!isTransientRecoveryFailure(lookupFailure) || attempt == RECONCILIATION_ATTEMPTS) {
                    createFailure.addSuppressed(lookupFailure);
                    return null;
                }
            }
            if (attempt < RECONCILIATION_ATTEMPTS && !pauseForRetry(createFailure, "signer reconciliation")) {
                return null;
            }
        }
        return null;
    }

    private boolean isAmbiguousAssignmentFailure(RuntimeException failure) {
        if (failure instanceof ValidationException) return false;
        if (failure instanceof ApiException api) return api.getStatusCode() >= 500;
        return failure instanceof NetworkException || failure instanceof AssinafyException;
    }

    private boolean isDuplicateSignerCreateFailure(RuntimeException failure) {
        if (failure instanceof ApiException api) {
            if (api.getStatusCode() == 409) return true;
            String message = api.getMessage() != null ? api.getMessage().toLowerCase(Locale.ROOT) : "";
            return api.getStatusCode() == 400 && (message.contains("already")
                    || message.contains("exist") || message.contains("duplic") || message.contains("já"));
        }
        return false;
    }

    private boolean isAmbiguousSignerCreateFailure(RuntimeException failure) {
        if (failure instanceof ApiException api) return api.getStatusCode() >= 500;
        return failure instanceof AssinafyException && !(failure instanceof ValidationException);
    }

    private boolean isTransientRecoveryFailure(RuntimeException failure) {
        if (failure instanceof ApiException api) {
            return api.getStatusCode() == 404 || api.getStatusCode() == 429 || api.getStatusCode() >= 500;
        }
        return failure instanceof NetworkException;
    }

    private void deleteDocumentAfterProcessing(String documentId) {
        RuntimeException waitFailure = null;
        boolean observed = false;
        try {
            documents.waitUntilReady(documentId, 60_000, 1_000);
            observed = true;
        } catch (RuntimeException failure) {
            waitFailure = failure;
        }
        for (int attempt = 0; ; attempt++) {
            try {
                documents.delete(documentId);
                return;
            } catch (RuntimeException deleteFailure) {
                if (observed && deleteFailure instanceof ApiException api && api.getStatusCode() == 404) return;
                boolean retryable = deleteFailure instanceof NetworkException
                        || deleteFailure instanceof ApiException api
                        && (api.getStatusCode() == 400 || api.getStatusCode() == 404
                        || api.getStatusCode() == 409
                        || api.getStatusCode() == 429 || api.getStatusCode() >= 500);
                if (!retryable || attempt == 4) {
                    if (waitFailure != null) deleteFailure.addSuppressed(waitFailure);
                    throw deleteFailure;
                }
                pauseForRetryOrThrow("workflow cleanup");
            }
        }
    }

    private void deleteSignerAfterCreate(String signerId, String accountId) {
        for (int attempt = 0; ; attempt++) {
            try {
                signers.delete(signerId, accountId);
                return;
            } catch (RuntimeException deleteFailure) {
                boolean retryable = deleteFailure instanceof NetworkException
                        || deleteFailure instanceof ApiException api
                        && (api.getStatusCode() == 404 || api.getStatusCode() == 429
                        || api.getStatusCode() >= 500);
                if (!retryable || attempt == 4) throw deleteFailure;
                pauseForRetryOrThrow("workflow cleanup");
            }
        }
    }

    private boolean pauseForRetry(RuntimeException originalFailure, String action) {
        try {
            pauseForRetryOrThrow(action);
            return true;
        } catch (NetworkException interrupted) {
            originalFailure.addSuppressed(interrupted);
            return false;
        }
    }

    private void pauseForRetryOrThrow(String action) {
        try {
            TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Interrupted during " + action, interrupted);
        }
    }

    private void logInfo(String message, Map<String, Object> context) {
        try {
            logger.info(message, context);
        } catch (RuntimeException ignored) {
            // Diagnostic callbacks must not change API behavior.
        }
    }

    private enum SignerOwnership { CREATED, REUSED, INDETERMINATE }

    private record ResolvedSigner(Signer signer, SignerOwnership ownership) {}

    /** {@return document operations bound to the default account} */
    public DocumentResource documents() { return documents; }

    /** {@return signer operations bound to the default account} */
    public SignerResource signers() { return signers; }

    /** {@return workspace operations, whose methods take an explicit account ID where required} */
    public WorkspaceResource workspaces() { return workspaces; }

    /** {@return assignment operations bound to the default account} */
    public AssignmentResource assignments() { return assignments; }

    /** {@return webhook operations bound to the default account} */
    public WebhookResource webhooks() { return webhooks; }

    /** {@return template operations bound to the default account} */
    public TemplateResource templates() { return templates; }

    /** {@return field-definition operations bound to the default account} */
    public FieldResource fields() { return fields; }

    /** {@return tag operations bound to the default account} */
    public TagResource tags() { return tags; }

    /** {@return unauthenticated document and signer-code operations} */
    public PublicDocumentResource publicDocuments() { return publicDocuments; }

    /** {@return API-key management operations} */
    public ApiKeyResource apiKeys() { return apiKeys; }

    /** {@return login, social-login, and password-management operations} */
    public AuthenticationResource authentication() { return authentication; }

    /** {@return authenticated-user settings and notification preferences} */
    public UserResource users() { return users; }

    /** {@return the webhook signature verifier configured with this client's webhook secret} */
    public WebhookVerifier webhookVerifier() { return webhookVerifier; }
}
