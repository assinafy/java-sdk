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
import com.assinafy.sdk.resources.AssignmentResource;
import com.assinafy.sdk.resources.DocumentResource;
import com.assinafy.sdk.resources.SignerResource;
import com.assinafy.sdk.resources.TemplateResource;
import com.assinafy.sdk.resources.WebhookResource;
import com.assinafy.sdk.resources.WorkspaceResource;
import com.assinafy.sdk.support.WebhookVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssinafyClient {

    private final DocumentResource documents;
    private final SignerResource signers;
    private final WorkspaceResource workspaces;
    private final AssignmentResource assignments;
    private final WebhookResource webhooks;
    private final TemplateResource templates;
    private final WebhookVerifier webhookVerifier;
    private final Logger logger;
    private final String defaultAccountId;

    public AssinafyClient(AssinafyClientOptions options) {
        if ((options.getApiKey() == null || options.getApiKey().isBlank())
                && (options.getToken() == null || options.getToken().isBlank())) {
            throw new ValidationException(
                    "An API key (options.apiKey) or legacy access token (options.token) is required."
            );
        }

        this.defaultAccountId = options.getAccountId();
        this.logger = options.getLogger() != null ? options.getLogger() : NoOpLogger.INSTANCE;

        String baseUrl = normaliseBaseUrl(
                options.getBaseUrl() != null ? options.getBaseUrl() : AssinafyClientOptions.DEFAULT_BASE_URL
        );

        ApiHttpClient http = new OkHttpApiClient(baseUrl, options.getApiKey(), options.getToken(), options.getTimeoutMs());

        this.documents = new DocumentResource(http, defaultAccountId, this.logger);
        this.signers = new SignerResource(http, defaultAccountId, this.logger);
        this.workspaces = new WorkspaceResource(http, null, this.logger);
        this.assignments = new AssignmentResource(http, defaultAccountId, this.logger);
        this.webhooks = new WebhookResource(http, defaultAccountId, this.logger);
        this.templates = new TemplateResource(http, defaultAccountId, this.logger);
        this.webhookVerifier = new WebhookVerifier(options.getWebhookSecret());
    }

    AssinafyClient(ApiHttpClient http, AssinafyClientOptions options) {
        if ((options.getApiKey() == null || options.getApiKey().isBlank())
                && (options.getToken() == null || options.getToken().isBlank())) {
            throw new ValidationException(
                    "An API key (options.apiKey) or legacy access token (options.token) is required."
            );
        }
        this.defaultAccountId = options.getAccountId();
        this.logger = options.getLogger() != null ? options.getLogger() : NoOpLogger.INSTANCE;
        this.documents = new DocumentResource(http, defaultAccountId, this.logger);
        this.signers = new SignerResource(http, defaultAccountId, this.logger);
        this.workspaces = new WorkspaceResource(http, null, this.logger);
        this.assignments = new AssignmentResource(http, defaultAccountId, this.logger);
        this.webhooks = new WebhookResource(http, defaultAccountId, this.logger);
        this.templates = new TemplateResource(http, defaultAccountId, this.logger);
        this.webhookVerifier = new WebhookVerifier(options.getWebhookSecret());
    }

    public static AssinafyClient create(String apiKey, String accountId) {
        return new AssinafyClient(AssinafyClientOptions.builder()
                .apiKey(apiKey)
                .accountId(accountId)
                .build());
    }

    public static AssinafyClient create(String apiKey, String accountId, AssinafyClientOptions extras) {
        AssinafyClientOptions opts = AssinafyClientOptions.builder()
                .apiKey(apiKey)
                .accountId(accountId)
                .baseUrl(extras.getBaseUrl())
                .webhookSecret(extras.getWebhookSecret())
                .timeoutMs(extras.getTimeoutMs())
                .logger(extras.getLogger())
                .build();
        return new AssinafyClient(opts);
    }

    public UploadAndRequestSignaturesResult uploadAndRequestSignatures(UploadAndRequestSignaturesRequest request) {
        if (request.getSigners() == null || request.getSigners().isEmpty()) {
            throw new ValidationException("At least one signer is required");
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

    public DocumentResource documents() { return documents; }
    public SignerResource signers() { return signers; }
    public WorkspaceResource workspaces() { return workspaces; }
    public AssignmentResource assignments() { return assignments; }
    public WebhookResource webhooks() { return webhooks; }
    public TemplateResource templates() { return templates; }
    public WebhookVerifier webhookVerifier() { return webhookVerifier; }

    private static String normaliseBaseUrl(String url) {
        return url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
