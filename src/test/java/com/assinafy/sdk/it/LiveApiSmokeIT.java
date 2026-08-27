package com.assinafy.sdk.it;

import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;
import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.exceptions.RateLimitException;
import com.assinafy.sdk.models.ApiKey;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.Document;
import com.assinafy.sdk.models.DocumentStatusInfo;
import com.assinafy.sdk.models.FieldDefinition;
import com.assinafy.sdk.models.FieldType;
import com.assinafy.sdk.models.FieldValidationResult;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.Tag;
import com.assinafy.sdk.models.Template;
import com.assinafy.sdk.models.UploadAndRequestSignaturesResult;
import com.assinafy.sdk.models.WebhookEventTypeInfo;
import com.assinafy.sdk.models.Workspace;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.CreateFieldRequest;
import com.assinafy.sdk.request.CreateSignerRequest;
import com.assinafy.sdk.request.CreateTagRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.RenameTagRequest;
import com.assinafy.sdk.request.SignerReference;
import com.assinafy.sdk.request.UpdateFieldRequest;
import com.assinafy.sdk.request.UpdateSignerRequest;
import com.assinafy.sdk.request.UploadAndRequestSignaturesRequest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.*;

/**
 * End-to-end smoke test against the Assinafy sandbox API.
 *
 * <p>This test is opt-in: it only runs when the environment variables
 * {@code ASSINAFY_API_KEY} and {@code ASSINAFY_ACCOUNT_ID} are set.
 * It exercises reads and isolated create/delete lifecycles. Tests that send invitations use
 * the optional {@code ASSINAFY_TEST_EMAIL_PRIMARY/SECONDARY} environment variables.
 *
 * <p>Run with:
 * <pre>
 *   ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... \
 *     mvn test -Dtest=LiveApiSmokeIT
 * </pre>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LiveApiSmokeIT {

    private static final int RECONCILE_ATTEMPTS = 5;
    private static final long RECONCILE_DELAY_MS = 500;
    private static final long DOCUMENT_DELETE_TIMEOUT_MS = 30_000;
    private static final long DOCUMENT_DELETE_POLL_MS = 1_000;
    private static final long MAX_RETRY_DELAY_MS = 10_000;

    private static String apiKey;
    private static String accountId;
    private static AssinafyClient client;

    @BeforeAll
    static void setUp() {
        apiKey = System.getenv("ASSINAFY_API_KEY");
        accountId = System.getenv("ASSINAFY_ACCOUNT_ID");
        Assumptions.assumeTrue(
                apiKey != null && !apiKey.isBlank() && accountId != null && !accountId.isBlank(),
                "Set ASSINAFY_API_KEY and ASSINAFY_ACCOUNT_ID to run live API tests"
        );
        String baseUrl = System.getenv().getOrDefault(
                "ASSINAFY_BASE_URL", AssinafyClientOptions.SANDBOX_BASE_URL);
        if (!AssinafyClientOptions.SANDBOX_BASE_URL.equals(baseUrl)) {
            throw new IllegalArgumentException("LiveApiSmokeIT only permits the Assinafy sandbox URL");
        }
        client = new AssinafyClient(AssinafyClientOptions.builder()
                .apiKey(apiKey).accountId(accountId).baseUrl(baseUrl).build());
    }

    @Test
    @Order(1)
    void listsWorkspacesIncludesTheConfiguredAccount() {
        PaginatedResult<Workspace> result = client.workspaces().list();
        assertThat(result.getData()).isNotEmpty();
        assertThat(result.getData())
                .extracting(Workspace::getId)
                .contains(accountId);
    }

    @Test
    @Order(2)
    void getsWorkspaceById() {
        var ws = client.workspaces().get(accountId);
        assertThat(ws.getId()).isEqualTo(accountId);
        assertThat(ws.getName()).isNotBlank();
    }

    @Test
    @Order(3)
    void listsDocumentsWithPaginationMeta() {
        PaginatedResult<Document> result =
                client.documents().list(ListParams.builder().perPage(2).page(1).build());
        // Even with empty data the meta should be populated when per-page works correctly.
        assertThat(result.getMeta()).as("pagination meta proves per-page is honored").isNotNull();
        assertThat(result.getMeta().getPerPage()).isEqualTo(2);
    }

    @Test
    @Order(4)
    void getsDocumentStatuses() {
        List<DocumentStatusInfo> statuses = client.documents().getStatuses();
        assertThat(statuses).isNotEmpty();
        assertThat(statuses).extracting(DocumentStatusInfo::getCode).contains("certificated");
    }

    @Test
    @Order(5)
    void listsTemplates() {
        PaginatedResult<Template> templates = client.templates().list();
        // Workspaces may have zero templates; just check it doesn't error.
        assertThat(templates.getData()).isNotNull();
    }

    @Test
    @Order(6)
    void listsWebhookEventTypes() {
        List<WebhookEventTypeInfo> types = client.webhooks().listEventTypes();
        assertThat(types).isNotEmpty();
    }

    @Test
    @Order(7)
    void getsWebhookSubscriptionWithoutErrors() {
        // get() maps a 404 to null (callOptional); otherwise it returns a well-formed subscription.
        assertThatCode(() -> {
            var sub = client.webhooks().get();
            if (sub != null) {
                // When present, the documented fields are populated (events is always returned).
                assertThat(sub.getEvents()).isNotNull();
                assertThat(sub.getIsActive()).isNotNull();
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @Order(8)
    void listsFieldDefinitions() {
        PaginatedResult<FieldDefinition> fields = client.fields().list();
        assertThat(fields.getData()).isNotNull();
    }

    @Test
    @Order(9)
    void listsFieldTypes() {
        List<FieldType> types = client.fields().listTypes();
        assertThat(types).isNotEmpty();
        assertThat(types).extracting(FieldType::getType).anyMatch("cpf"::equalsIgnoreCase);
    }

    @Test
    @Order(10)
    void listsSigners() {
        PaginatedResult<Signer> signers = client.signers().list(ListParams.builder().perPage(5).build());
        assertThat(signers.getData()).isNotNull();
    }

    @Test
    @Order(11)
    void uploadsTinyPdfAndCleansItUp() {
        byte[] pdf = minimalPdf();
        String tagName = "sdk-it-doctag-" + UUID.randomUUID().toString().substring(0, 8);
        String fileName = "sdk-it-" + UUID.randomUUID() + ".pdf";
        AtomicReference<String> documentId = new AtomicReference<>();
        AtomicReference<String> tagId = new AtomicReference<>();
        AtomicBoolean documentCreateAttempted = new AtomicBoolean();
        AtomicBoolean tagCreateAttempted = new AtomicBoolean();
        try (SandboxCleanup cleanup = new SandboxCleanup()) {
            cleanup.add(() -> deleteDocument(
                    documentId.get(), fileName, documentCreateAttempted.get()));
            cleanup.add(() -> deleteTag(tagId.get(), tagName, tagCreateAttempted.get()));
            documentCreateAttempted.set(true);
            Document doc = client.documents().upload(pdf, fileName);
            documentId.set(doc.getId());
            assertThat(doc.getId()).isNotBlank();
            // Wait for status to advance past 'uploading' or 'metadata_processing'.
            client.documents().waitUntilReady(doc.getId(), 20_000, 1_500);
            var details = client.documents().details(doc.getId());
            assertThat(details.getId()).isEqualTo(doc.getId());
            assertThat(details.getTags()).as("tags are always present (possibly empty)").isNotNull();
            assertThat(details.getArtifacts()).isNotNull();
            assertThat(details.getArtifacts().getThumbnail())
                    .as("the inline thumbnail artifact URL is exposed").isNotBlank();

            // Binary downloads: the available 'original' artifact returns real PDF bytes...
            byte[] original = client.documents().download(doc.getId(), "original");
            assertThat(original).isNotEmpty();
            assertThat(new String(original, 0, Math.min(5, original.length))).startsWith("%PDF");
            assertThat(client.documents().thumbnail(doc.getId())).isNotEmpty();

            // Rename (PATCH /documents/{id}) round-trips through the SDK.
            var renamed = client.documents().rename(doc.getId(), "sdk-it-renamed.pdf");
            assertThat(renamed.getName()).isEqualTo("sdk-it-renamed.pdf");

            // Lightweight search (GET /accounts/{id}/documents/search) returns a compact page.
            var searchResult = client.documents().search(
                    ListParams.builder().search("sdk-it-renamed").perPage(10).build());
            assertThat(searchResult.getData()).as("search returns a (possibly empty) page").isNotNull();

            // An unavailable artifact is surfaced as an API error.
            assertThatThrownBy(() -> client.documents().download(doc.getId(), "certificated"))
                    .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class);

            // Document tags: create, attach by name and ID, replace, list, and detach.
            tagCreateAttempted.set(true);
            Tag createdTag = client.tags().create(
                    CreateTagRequest.builder().name(tagName).build());
            tagId.set(createdTag.getId());
            List<Tag> attached = client.documents().appendTags(doc.getId(), List.of(tagName));
            assertThat(attached).extracting(Tag::getName).contains(tagName);
            Tag added = waitForDocumentTag(doc.getId(), tagName, true);
            client.documents().detachTag(doc.getId(), added.getId());
            waitForDocumentTag(doc.getId(), tagName, false);
            assertThat(client.documents().appendTagIds(doc.getId(), List.of(createdTag.getId())))
                    .extracting(Tag::getName).contains(tagName);
            assertThat(client.documents().replaceTags(doc.getId(), List.of(tagName)))
                    .extracting(Tag::getName).contains(tagName);
            assertThat(client.documents().replaceTagIds(doc.getId(), List.of(createdTag.getId())))
                    .extracting(Tag::getName).contains(tagName);
            Tag addedById = waitForDocumentTag(doc.getId(), tagName, true);
            client.documents().detachTag(doc.getId(), addedById.getId());
            waitForDocumentTag(doc.getId(), tagName, false);

            // Estimate cost for a 1-signer assignment (no email is sent).
            Map<String, Object> cost = client.assignments().estimateCost(doc.getId(),
                    CreateAssignmentRequest.builder()
                            .method("virtual")
                            .signers(List.of(SignerReference.builder().verificationMethod("Email").build()))
                            .build());
            assertThat(cost).isNotNull();
        }
    }

    @Test
    @Order(12)
    void createsAndDeletesEphemeralSigner() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "sdk-it-" + suffix + "@example.invalid";
        String fullName = "SDK IT " + suffix;
        AtomicReference<String> signerId = new AtomicReference<>();
        AtomicBoolean createAttempted = new AtomicBoolean();
        try (SandboxCleanup cleanup = new SandboxCleanup()) {
            cleanup.add(() -> deleteSigner(
                    signerId.get(), email, fullName, createAttempted.get()));
            createAttempted.set(true);
            Signer created = client.signers().create(
                    CreateSignerRequest.builder()
                            .fullName(fullName)
                            .email(email)
                            .whatsappPhoneNumber("+5548999990000")
                            .cpf("400.676.228-36")
                            .build()
            );
            signerId.set(created.getId());
            assertThat(created.getId()).isNotBlank();
            assertThat(created.getEmail()).isEqualToIgnoringCase(email);

            Signer fetched = client.signers().get(created.getId());
            assertThat(fetched.getId()).isEqualTo(created.getId());
        }
    }

    @Test
    @Order(13)
    void createsWhatsappOnlySignerWithoutEmail() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String fullName = "SDK WA " + suffix;
        AtomicReference<String> signerId = new AtomicReference<>();
        AtomicBoolean createAttempted = new AtomicBoolean();
        try (SandboxCleanup cleanup = new SandboxCleanup()) {
            cleanup.add(() -> deleteSigner(
                    signerId.get(), null, fullName, createAttempted.get()));
            createAttempted.set(true);
            Signer created = client.signers().create(
                    CreateSignerRequest.builder()
                            .fullName(fullName)
                            .whatsappPhoneNumber("+5548999990000")
                            .build()
            );
            signerId.set(created.getId());
            assertThat(created.getId()).isNotBlank();
        }
    }

    @Test
    @Order(14)
    void getsMaskedApiKey() {
        ApiKey key = client.apiKeys().get();
        // A key exists for the configured credentials; only the tail is visible.
        assertThat(key).isNotNull();
        assertThat(key.getApiKey()).isNotBlank();
    }

    @Test
    @Order(15)
    void validatesFieldValueReturningTypedResult() {
        // Use the predefined "E-mail" field definition from the account.
        PaginatedResult<FieldDefinition> fields =
                client.fields().list(ListParams.builder().perPage(50).build());
        FieldDefinition emailField = fields.getData().stream()
                .filter(f -> "email".equalsIgnoreCase(f.getType()))
                .findFirst()
                .orElse(null);
        Assumptions.assumeTrue(emailField != null, "No email field definition available");

        FieldValidationResult ok = client.fields().validate(emailField.getId(), "john@example.com", null);
        assertThat(ok.getSuccess()).isTrue();

        FieldValidationResult bad = client.fields().validate(emailField.getId(), "not-an-email", null);
        assertThat(bad.getSuccess()).isFalse();
        assertThat(bad.getErrorMessage()).isNotBlank();
    }

    @Test
    @Order(16)
    void tagLifecycleCreateRenameDelete() {
        String name = "sdk-it-tag-" + UUID.randomUUID().toString().substring(0, 8);
        AtomicReference<String> tagId = new AtomicReference<>();
        AtomicBoolean createAttempted = new AtomicBoolean();
        try (SandboxCleanup cleanup = new SandboxCleanup()) {
            cleanup.add(() -> deleteTag(tagId.get(), name, createAttempted.get()));
            createAttempted.set(true);
            Tag created = client.tags().create(
                    CreateTagRequest.builder().name(name).color("FF0000").build());
            tagId.set(created.getId());
            assertThat(created.getId()).isNotBlank();
            assertThat(created.getName()).isEqualTo(name);

            Tag renamed = client.tags().rename(created.getId(),
                    RenameTagRequest.builder().name(name + "-renamed").build());
            assertThat(renamed.getName()).isEqualTo(name + "-renamed");
        }
    }

    @Test
    @Order(17)
    void fieldLifecycleCreateGetUpdateDelete() {
        String name = "sdk-it-field-" + UUID.randomUUID().toString().substring(0, 8);
        AtomicReference<String> fieldId = new AtomicReference<>();
        AtomicBoolean createAttempted = new AtomicBoolean();
        try (SandboxCleanup cleanup = new SandboxCleanup()) {
            cleanup.add(() -> deleteField(fieldId.get(), name, createAttempted.get()));
            createAttempted.set(true);
            FieldDefinition created = client.fields().create(
                    CreateFieldRequest.builder().type("text").name(name).build());
            fieldId.set(created.getId());
            assertThat(client.fields().get(created.getId()).getName()).isEqualTo(name);

            FieldDefinition updated = client.fields().update(created.getId(),
                    UpdateFieldRequest.builder().name(name + "-updated").build());
            assertThat(updated.getName()).isEqualTo(name + "-updated");
        }
    }

    @Test
    @Order(18)
    void getsAccountTheme() {
        var theme = client.workspaces().getTheme(accountId);
        assertThat(theme).isNotNull();
        // account_name is always populated; colors/logo may be null depending on branding.
        assertThat(theme.getAccountName()).isNotBlank();
    }

    @Test
    @Order(19)
    void listsAssignmentsWithSandboxAccountContext() {
        assertThat(client.assignments().list(ListParams.builder().perPage(1).build()).getData())
                .isNotNull();
    }

    @Test
    @Order(20)
    void getsAuthenticatedUserAndProbesDocumentedSandboxRoutes() {
        assertThat(client.users().get().getId()).isNotBlank();

        probeDocumentedRoute(() -> client.workspaces().stats(accountId));
        probeDocumentedRoute(() -> client.users().stats());
        probeDocumentedRoute(() -> client.users().getNotificationPreferences());
    }

    @Test
    @Order(21)
    void assignmentEmailLifecycleWithConfiguredRecipients() {
        String primaryEmail = System.getenv("ASSINAFY_TEST_EMAIL_PRIMARY");
        String secondaryEmail = System.getenv("ASSINAFY_TEST_EMAIL_SECONDARY");
        Assumptions.assumeTrue(primaryEmail != null && !primaryEmail.isBlank()
                        && secondaryEmail != null && !secondaryEmail.isBlank(),
                "Set both ASSINAFY_TEST_EMAIL_PRIMARY and ASSINAFY_TEST_EMAIL_SECONDARY");

        try (SandboxCleanup cleanup = new SandboxCleanup()) {
            Signer existingPrimary = client.signers().findByEmail(primaryEmail);
            Signer primary = existingPrimary;
            if (primary == null) {
                String fullName = "SDK Integration Primary "
                        + UUID.randomUUID().toString().substring(0, 8);
                AtomicReference<String> signerId = new AtomicReference<>();
                AtomicBoolean createAttempted = new AtomicBoolean();
                cleanup.add(() -> deleteSigner(
                        signerId.get(), primaryEmail, fullName, createAttempted.get()));
                createAttempted.set(true);
                primary = client.signers().create(CreateSignerRequest.builder()
                        .fullName(fullName).email(primaryEmail).build());
                signerId.set(primary.getId());
                primary = client.signers().update(primary.getId(), UpdateSignerRequest.builder()
                        .fullName(fullName)
                        .governmentId("400.676.228-36")
                        .build());
            }

            Signer existingSecondary = client.signers().findByEmail(secondaryEmail);
            Signer secondary = existingSecondary;
            if (secondary == null) {
                String fullName = "SDK Integration Secondary "
                        + UUID.randomUUID().toString().substring(0, 8);
                AtomicReference<String> signerId = new AtomicReference<>();
                AtomicBoolean createAttempted = new AtomicBoolean();
                cleanup.add(() -> deleteSigner(
                        signerId.get(), secondaryEmail, fullName, createAttempted.get()));
                createAttempted.set(true);
                secondary = client.signers().create(CreateSignerRequest.builder()
                        .fullName(fullName).email(secondaryEmail).build());
                signerId.set(secondary.getId());
            }

            String fileName = "sdk-it-assignment-" + UUID.randomUUID() + ".pdf";
            AtomicReference<String> documentId = new AtomicReference<>();
            AtomicBoolean documentCreateAttempted = new AtomicBoolean();
            cleanup.add(() -> deleteDocument(
                    documentId.get(), fileName, documentCreateAttempted.get()));
            documentCreateAttempted.set(true);
            Document document = client.documents().upload(minimalPdf(), fileName);
            documentId.set(document.getId());
            client.documents().waitUntilReady(document.getId(), 30_000, 1_500);

            CreateAssignmentRequest request = CreateAssignmentRequest.builder()
                    .method("virtual")
                    .signers(List.of(
                            SignerReference.builder().id(primary.getId())
                                    .verificationMethod("Email")
                                    .notificationMethods(List.of("Email")).build(),
                            SignerReference.builder().id(secondary.getId())
                                    .verificationMethod("Email")
                                    .notificationMethods(List.of("Email")).build()))
                    .message("Assinafy SDK sandbox integration test")
                    .expiresAt(expirationInDays(7))
                    .build();

            assertThat(client.assignments().estimateCost(document.getId(), request)).isNotNull();
            Assignment assignment = client.assignments().create(document.getId(), request);
            assertThat(assignment.getId()).isNotBlank();
            assertThat(assignment.getSigners()).hasSize(2);

            client.assignments().resetExpiration(document.getId(), assignment.getId(),
                    expirationInDays(8));
            assertThat(client.assignments().estimateResendCost(
                    document.getId(), assignment.getId(), primary.getId())).isNotNull();
            assertThat(client.assignments().resendNotification(
                    document.getId(), assignment.getId(), primary.getId()).getDocumentId())
                    .isEqualTo(document.getId());
            assertThat(client.assignments().getWhatsappNotifications(
                    document.getId(), assignment.getId())).isNotNull();
            client.publicDocuments().sendToken(document.getId(), secondaryEmail, "email");
            assertThat(client.documents().activities(document.getId())).isNotNull();
        }
    }

    @Test
    @Order(22)
    void requestsPasswordResetForConfiguredSandboxIdentity() {
        String email = System.getenv("ASSINAFY_TEST_EMAIL_PRIMARY");
        Assumptions.assumeTrue(email != null && !email.isBlank(),
                "Set ASSINAFY_TEST_EMAIL_PRIMARY");
        assertThatCode(() -> client.authentication().requestPasswordReset(email))
                .doesNotThrowAnyException();
    }

    @Test
    @Order(23)
    void uploadsAndRequestsSignaturesWithConfiguredRecipient() {
        String email = System.getenv("ASSINAFY_TEST_EMAIL_PRIMARY");
        Assumptions.assumeTrue(email != null && !email.isBlank(),
                "Set ASSINAFY_TEST_EMAIL_PRIMARY");

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String fullName = "SDK Workflow " + suffix;
        String fileName = "sdk-it-workflow-" + UUID.randomUUID() + ".pdf";
        AtomicReference<String> createdSignerId = new AtomicReference<>();
        AtomicReference<String> workflowSignerId = new AtomicReference<>();
        AtomicReference<String> documentId = new AtomicReference<>();
        AtomicBoolean signerCreateAttempted = new AtomicBoolean();
        AtomicBoolean workflowAttempted = new AtomicBoolean();
        try (SandboxCleanup cleanup = new SandboxCleanup()) {
            cleanup.add(() -> deleteSigner(
                    createdSignerId.get(), email, fullName, signerCreateAttempted.get()));
            cleanup.add(() -> deleteSigner(
                    workflowSignerId.get(), email, fullName, workflowAttempted.get()));
            cleanup.add(() -> deleteDocument(
                    documentId.get(), fileName, workflowAttempted.get()));

            Signer expectedSigner = client.signers().findByEmail(email);
            if (expectedSigner == null) {
                signerCreateAttempted.set(true);
                expectedSigner = client.signers().create(CreateSignerRequest.builder()
                        .fullName(fullName)
                        .email(email)
                        .build());
                createdSignerId.set(expectedSigner.getId());
            }
            assertThat(expectedSigner.getId()).isNotBlank();

            workflowAttempted.set(true);
            UploadAndRequestSignaturesResult result = client.uploadAndRequestSignatures(
                    UploadAndRequestSignaturesRequest.builder()
                            .fileData(minimalPdf())
                            .fileName(fileName)
                            .signers(List.of(
                                    UploadAndRequestSignaturesRequest.SignerEntry.builder()
                                            .name(fullName)
                                            .email(email)
                                            .build()))
                            .message("Assinafy SDK sandbox workflow test")
                            .build());

            assertThat(result.getDocument()).isNotNull();
            documentId.set(result.getDocument().getId());
            assertThat(documentId.get()).isNotBlank();
            assertThat(result.getAssignment()).isNotNull();
            assertThat(result.getAssignment().getId()).isNotBlank();
            String resultSignerId = result.getSignerIds().getFirst();
            if (!expectedSigner.getId().equals(resultSignerId)) {
                workflowSignerId.set(resultSignerId);
            }
            assertThat(result.getSignerIds()).containsExactly(expectedSigner.getId());
            assertThat(resultSignerId).isNotBlank();
        }
    }

    @Test
    @Order(24)
    void leavesNoNamedSandboxTestResources() {
        assertThat(client.documents().search(
                        ListParams.builder().search("sdk-it-").perPage(100).build()).getData())
                .extracting(Document::getName)
                .noneMatch(name -> name != null && name.startsWith("sdk-it-"));
        assertThat(client.tags().list(
                        ListParams.builder().search("sdk-it-").perPage(100).build()).getData())
                .extracting(Tag::getName)
                .noneMatch(name -> name != null && name.startsWith("sdk-it-"));
        assertThat(client.fields().list(
                        ListParams.builder().search("sdk-it-").perPage(100).build()).getData())
                .extracting(FieldDefinition::getName)
                .noneMatch(name -> name != null && name.startsWith("sdk-it-"));
    }

    private static void probeDocumentedRoute(Runnable operation) {
        try {
            operation.run();
        } catch (ApiException error) {
            assertThat(error.getStatusCode())
                    .as("optional route returns 404 when unavailable")
                    .isEqualTo(404);
        }
    }

    private static String expirationInDays(long days) {
        return Instant.now().plus(days, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.SECONDS).toString();
    }

    private static Tag waitForDocumentTag(String documentId, String tagName, boolean present) {
        for (int attempt = 0; attempt < RECONCILE_ATTEMPTS; attempt++) {
            Tag match = client.documents().listTags(documentId).stream()
                    .filter(tag -> tagName.equals(tag.getName()))
                    .findFirst().orElse(null);
            if (present == (match != null)) return match;
            if (attempt + 1 < RECONCILE_ATTEMPTS) sleepCleanup(RECONCILE_DELAY_MS);
        }
        throw new AssertionError(present
                ? "Attached tag was not returned by the document"
                : "Detached tag remained on the document");
    }

    private static void deleteDocument(String documentId, String fileName, boolean createAttempted) {
        if (documentId != null) {
            long deadline = System.nanoTime()
                    + TimeUnit.MILLISECONDS.toNanos(DOCUMENT_DELETE_TIMEOUT_MS);
            RuntimeException lastFailure = null;
            int notFoundAttempts = 0;
            while (true) {
                try {
                    String status = client.documents().details(documentId).getStatus();
                    notFoundAttempts = 0;
                    lastFailure = null;
                    if (List.of("uploading", "uploaded", "metadata_processing", "certificating")
                            .contains(status)) {
                        if (!pauseBefore(deadline, DOCUMENT_DELETE_POLL_MS)) break;
                        continue;
                    }
                    client.documents().delete(documentId);
                    return;
                } catch (RuntimeException error) {
                    if (error instanceof ApiException api && api.getStatusCode() == 404) {
                        lastFailure = error;
                        if (++notFoundAttempts >= RECONCILE_ATTEMPTS
                                && !documentExists(documentId, fileName)) return;
                        if (!pauseBefore(deadline, RECONCILE_DELAY_MS)) break;
                        continue;
                    }
                    notFoundAttempts = 0;
                    if (!isRetryableDocumentCleanup(error)) throw error;
                    lastFailure = error;
                    if (!pauseBefore(deadline, DOCUMENT_DELETE_POLL_MS)) break;
                }
            }
            if (lastFailure != null) throw lastFailure;
            throw new AssertionError("Document did not reach a deletable status before cleanup timed out");
        }
        if (!createAttempted) return;
        reconcileCleanup(() -> client.documents().search(
                        ListParams.builder().search(fileName).perPage(100).build())
                .getData().stream()
                .filter(document -> fileName.equals(document.getName()))
                .findFirst()
                .map(document -> {
                    deleteDocument(document.getId(), fileName, true);
                    return true;
                })
                .orElse(false));
    }

    private static boolean isRetryableDocumentCleanup(RuntimeException error) {
        if (error instanceof NetworkException) return true;
        if (!(error instanceof ApiException api)) return false;
        int status = api.getStatusCode();
        return status == 400 || status == 409 || status == 423 || status == 429 || status >= 500;
    }

    private static void deleteSigner(
            String signerId, String email, String fullName, boolean createAttempted) {
        String search = email != null ? email : fullName;
        if (signerId != null) {
            deleteKnownResource(
                    () -> client.signers().delete(signerId),
                    () -> client.signers().list(
                                    ListParams.builder().search(search).perPage(100).build())
                            .getData().stream()
                            .filter(signer -> signerId.equals(signer.getId()))
                            .findFirst()
                            .map(signer -> {
                                client.signers().delete(signer.getId());
                                return true;
                            })
                            .orElse(false));
            return;
        }
        if (!createAttempted) return;
        reconcileCleanup(() -> client.signers().list(
                        ListParams.builder().search(search).perPage(100).build())
                .getData().stream()
                .filter(signer -> email == null
                        || signer.getEmail() != null && email.equalsIgnoreCase(signer.getEmail()))
                .filter(signer -> fullName == null || fullName.equals(signer.getFullName()))
                .findFirst()
                .map(signer -> {
                    client.signers().delete(signer.getId());
                    return true;
                })
                .orElse(false));
    }

    private static void deleteTag(String tagId, String name, boolean createAttempted) {
        if (tagId != null) {
            deleteKnownResource(
                    () -> client.tags().delete(tagId, true),
                    () -> client.tags().list(
                                    ListParams.builder().search(name).perPage(100).build())
                            .getData().stream()
                            .filter(tag -> tagId.equals(tag.getId()))
                            .findFirst()
                            .map(tag -> {
                                client.tags().delete(tag.getId(), true);
                                return true;
                            })
                            .orElse(false));
            return;
        }
        if (!createAttempted) return;
        reconcileCleanup(() -> client.tags().list(
                        ListParams.builder().search(name).perPage(100).build())
                .getData().stream()
                .filter(tag -> name.equals(tag.getName()))
                .findFirst()
                .map(tag -> {
                    client.tags().delete(tag.getId(), true);
                    return true;
                })
                .orElse(false));
    }

    private static void deleteField(String fieldId, String name, boolean createAttempted) {
        if (fieldId != null) {
            deleteKnownResource(
                    () -> client.fields().delete(fieldId),
                    () -> client.fields().list(
                                    ListParams.builder().search(name).perPage(100).build())
                            .getData().stream()
                            .filter(field -> fieldId.equals(field.getId()))
                            .findFirst()
                            .map(field -> {
                                client.fields().delete(field.getId());
                                return true;
                            })
                            .orElse(false));
            return;
        }
        if (!createAttempted) return;
        reconcileCleanup(() -> client.fields().list(
                        ListParams.builder().search(name).perPage(100).build())
                .getData().stream()
                .filter(field -> name.equals(field.getName()))
                .findFirst()
                .map(field -> {
                    client.fields().delete(field.getId());
                    return true;
                })
                .orElse(false));
    }

    private static void reconcileCleanup(BooleanSupplier deleteIfFound) {
        for (int attempt = 0; attempt < RECONCILE_ATTEMPTS; attempt++) {
            if (deleteIfFound.getAsBoolean()) return;
            if (attempt + 1 < RECONCILE_ATTEMPTS) sleepCleanup(RECONCILE_DELAY_MS);
        }
    }

    private static boolean documentExists(String documentId, String fileName) {
        return client.documents().search(ListParams.builder().search(fileName).perPage(100).build())
                .getData().stream()
                .anyMatch(document -> documentId.equals(document.getId()));
    }

    private static void deleteKnownResource(Runnable delete, BooleanSupplier deleteIfFound) {
        try {
            delete.run();
        } catch (ApiException error) {
            if (error.getStatusCode() != 404) throw error;
            reconcileCleanup(deleteIfFound);
        }
    }

    private static boolean pauseBefore(long deadlineNanos, long delayMs) {
        long remaining = deadlineNanos - System.nanoTime();
        if (remaining <= 0) return false;
        sleepCleanup(Math.min(delayMs,
                Math.max(1, TimeUnit.NANOSECONDS.toMillis(remaining))));
        return true;
    }

    private static void sleepCleanup(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted during sandbox cleanup", interrupted);
        }
    }

    private static void retryCleanup(Runnable operation) {
        int retryAttempts = 0;
        int notFoundAttempts = 0;
        while (true) {
            try {
                operation.run();
                return;
            } catch (RuntimeException error) {
                if (error instanceof ApiException api && api.getStatusCode() == 404) {
                    if (++notFoundAttempts >= RECONCILE_ATTEMPTS) return;
                    sleepCleanup(RECONCILE_DELAY_MS);
                    continue;
                }
                notFoundAttempts = 0;
                boolean retryable = error instanceof RateLimitException
                        || error instanceof NetworkException
                        || error instanceof ApiException api && api.getStatusCode() >= 500;
                if (!retryable || retryAttempts++ == 2) throw error;
                long delayMs = 1_250;
                if (error instanceof RateLimitException rateLimit) {
                    String retryAfter = rateLimit.getResponseHeader("retry-after");
                    try {
                        if (retryAfter != null) {
                            long seconds = Math.min(MAX_RETRY_DELAY_MS / 1_000,
                                    Math.max(1, Long.parseLong(retryAfter)));
                            delayMs = Math.min(MAX_RETRY_DELAY_MS, seconds * 1_000 + 250);
                        }
                    } catch (NumberFormatException ignored) {
                        // Retry-After may be an HTTP date; use the short sandbox fallback.
                    }
                }
                sleepCleanup(delayMs);
            }
        }
    }

    private static final class SandboxCleanup implements AutoCloseable {
        private final List<Runnable> actions = new ArrayList<>();

        void add(Runnable action) {
            actions.add(action);
        }

        @Override
        public void close() {
            Throwable failure = null;
            for (int i = actions.size() - 1; i >= 0; i--) {
                try {
                    retryCleanup(actions.get(i));
                } catch (Throwable error) {
                    if (failure == null) failure = error;
                    else failure.addSuppressed(error);
                }
            }
            if (failure != null) throw new AssertionError("Sandbox cleanup failed", failure);
        }
    }

    /** Returns a tiny syntactically valid one-page PDF for upload testing. */
    private static byte[] minimalPdf() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeAscii(out, "%PDF-1.4\n");
        List<String> objects = List.of(
                "<</Type/Catalog/Pages 2 0 R>>",
                "<</Type/Pages/Kids[3 0 R]/Count 1>>",
                "<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]/Resources<<>>/Contents 4 0 R>>",
                "<</Length 0>>stream\n\nendstream"
        );
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            writeAscii(out, (i + 1) + " 0 obj\n" + objects.get(i) + "\nendobj\n");
        }
        int xref = out.size();
        writeAscii(out, "xref\n0 5\n0000000000 65535 f \n");
        offsets.forEach(offset -> writeAscii(out,
                String.format(Locale.ROOT, "%010d 00000 n \n", offset)));
        writeAscii(out, "trailer<</Size 5/Root 1 0 R>>\nstartxref\n" + xref + "\n%%EOF\n");
        return out.toByteArray();
    }

    private static void writeAscii(ByteArrayOutputStream out, String value) {
        out.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
    }
}
