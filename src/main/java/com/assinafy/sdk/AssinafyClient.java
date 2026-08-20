package com.assinafy.sdk;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.http.OkHttpApiClient;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.DocumentUploadResponse;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.UploadAndRequestSignaturesResult;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.CreateSignerRequest;
import com.assinafy.sdk.request.SignerReference;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Entry point for Assinafy API operations. Credentials are optional for login and public
 * document endpoints; authenticated and account-scoped operations require the corresponding
 * credential/account configuration.
 *
 * <p>The client is thread-safe when used with its default OkHttp transport. Resource accessors
 * return the instances owned by this client.
 */
public class AssinafyClient {

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
     * reused by email); (4) create a {@code virtual} assignment for those signers, which sends the
     * signature-request notifications.
     *
     * <p>This method has externally-visible side effects (it creates signer resources and dispatches
     * notifications) and blocks by default. At least one signer is required.
     *
     * @param request document, assignment, signer, and polling settings
     * @return the created document, the assignment, and the signer IDs
     * @throws ValidationException if the request is absent, contains no signers, or contains a
     *                             signer without a name or with an invalid email
     */
    public UploadAndRequestSignaturesResult uploadAndRequestSignatures(UploadAndRequestSignaturesRequest request) {
        if (request == null) throw new ValidationException("Workflow request is required");
        if (request.getSigners() == null || request.getSigners().isEmpty()) {
            throw new ValidationException("At least one signer is required");
        }
        for (UploadAndRequestSignaturesRequest.SignerEntry signer : request.getSigners()) {
            if (signer == null || signer.getName() == null || signer.getName().isBlank()) {
                throw new ValidationException("Every signer requires a name");
            }
            if (signer.getEmail() != null) BaseResource.requireEmail(signer.getEmail());
        }

        logger.info("Starting upload + signature workflow", Map.of("signerCount", request.getSigners().size()));

        DocumentUploadResponse document = documents.upload(
                request.getFileData(),
                request.getFileName(),
                request.getMetadata(),
                request.getAccountId()
        );

        if (request.isWaitForReady()) {
            documents.waitUntilReady(document.getId());
        }

        List<String> signerIds = new ArrayList<>();
        for (UploadAndRequestSignaturesRequest.SignerEntry entry : request.getSigners()) {
            CreateSignerRequest signerRequest = CreateSignerRequest.builder()
                    .fullName(entry.getName())
                    .email(entry.getEmail())
                    .whatsappPhoneNumber(entry.getWhatsappPhoneNumber())
                    .cpf(entry.getCpf())
                    .metadata(entry.getMetadata())
                    .build();
            Signer created = signers.create(signerRequest, request.getAccountId());
            signerIds.add(created.getId());
        }

        List<SignerReference> signerRefs = signerIds.stream()
                .map(SignerReference::ofId)
                .toList();

        CreateAssignmentRequest assignmentRequest = CreateAssignmentRequest.builder()
                .method("virtual")
                .signers(signerRefs)
                .message(request.getMessage())
                .expiresAt(request.getExpiresAt())
                .copyReceivers(request.getCopyReceivers())
                .build();

        Assignment assignment = assignments.create(document.getId(), assignmentRequest);

        logger.info("Upload + signature workflow completed", Map.of("documentId", document.getId()));

        return new UploadAndRequestSignaturesResult(document, assignment, signerIds);
    }

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
