package com.assinafy.sdk.it;

import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;
import com.assinafy.sdk.models.ApiKey;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.DocumentListItem;
import com.assinafy.sdk.models.DocumentStatusInfo;
import com.assinafy.sdk.models.DocumentUploadResponse;
import com.assinafy.sdk.models.FieldDefinition;
import com.assinafy.sdk.models.FieldType;
import com.assinafy.sdk.models.FieldValidationResult;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.Tag;
import com.assinafy.sdk.models.TemplateListItem;
import com.assinafy.sdk.models.WebhookEventTypeInfo;
import com.assinafy.sdk.models.WorkspaceListItem;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.CreateSignerRequest;
import com.assinafy.sdk.request.CreateTagRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.RenameTagRequest;
import com.assinafy.sdk.request.SignerReference;
import com.assinafy.sdk.request.UpdateSignerRequest;
import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.RateLimitException;
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

import static org.assertj.core.api.Assertions.*;

/**
 * End-to-end smoke test against the Assinafy sandbox API.
 *
 * <p>This test is opt-in: it only runs when the environment variables
 * {@code ASSINAFY_API_KEY} and {@code ASSINAFY_ACCOUNT_ID} are set.
 * It exercises reads and isolated create/delete lifecycles. Tests that send invitations require
 * the two optional {@code ASSINAFY_TEST_EMAIL_PRIMARY/SECONDARY} environment variables.
 *
 * <p>Run with:
 * <pre>
 *   ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... \
 *     mvn test -Dtest=LiveApiSmokeIT
 * </pre>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LiveApiSmokeIT {

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
        PaginatedResult<WorkspaceListItem> result = client.workspaces().list();
        assertThat(result.getData()).isNotEmpty();
        assertThat(result.getData())
                .extracting(WorkspaceListItem::getId)
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
        PaginatedResult<DocumentListItem> result =
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
        PaginatedResult<TemplateListItem> templates = client.templates().list();
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
        DocumentUploadResponse doc = client.documents().upload(pdf, "sdk-it-" + UUID.randomUUID() + ".pdf");
        String createdTagId = null;
        try {
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

            // ...while an unavailable artifact (no certificate yet) now throws instead of
            // silently returning the JSON error body as bytes (the getBinary status-check fix).
            assertThatThrownBy(() -> client.documents().download(doc.getId(), "certificated"))
                    .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class);

            // Document tags: append, list, detach (auto-creates the workspace tag by name).
            client.documents().appendTags(doc.getId(), List.of(tagName));
            List<Tag> docTags = client.documents().listTags(doc.getId());
            Tag added = docTags.stream().filter(t -> tagName.equals(t.getName())).findFirst().orElse(null);
            assertThat(added).as("appended tag is listed on the document").isNotNull();
            createdTagId = added.getId();
            client.documents().detachTag(doc.getId(), added.getId());
            assertThat(client.documents().listTags(doc.getId()))
                    .extracting(Tag::getName).doesNotContain(tagName);

            // Estimate cost for a 1-signer assignment (no email is sent).
            Map<String, Object> cost = client.assignments().estimateCost(doc.getId(),
                    CreateAssignmentRequest.builder()
                            .method("virtual")
                            .signers(List.of(SignerReference.builder().verificationMethod("Email").build()))
                            .build());
            assertThat(cost).isNotNull();
        } finally {
            if (createdTagId != null) {
                String tagId = createdTagId;
                retryCleanup(() -> client.tags().delete(tagId, true));
            }
            retryCleanup(() -> client.documents().delete(doc.getId()));
        }
    }

    @Test
    @Order(12)
    void createsAndDeletesEphemeralSigner() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "sdk-it-" + suffix + "@example.invalid";

        Signer created = client.signers().create(
                CreateSignerRequest.builder()
                        .fullName("SDK IT " + suffix)
                        .email(email)
                        .whatsappPhoneNumber("+5548999990000")
                        .cpf("400.676.228-36")
                        .build()
        );
        try {
            assertThat(created.getId()).isNotBlank();
            assertThat(created.getEmail()).isEqualToIgnoringCase(email);

            Signer fetched = client.signers().get(created.getId());
            assertThat(fetched.getId()).isEqualTo(created.getId());
        } finally {
            retryCleanup(() -> client.signers().delete(created.getId()));
        }
    }

    @Test
    @Order(13)
    void createsWhatsappOnlySignerWithoutEmail() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Signer created = client.signers().create(
                CreateSignerRequest.builder()
                        .fullName("SDK WA " + suffix)
                        .whatsappPhoneNumber("+5548999990000")
                        .build()
        );
        try {
            assertThat(created.getId()).isNotBlank();
        } finally {
            retryCleanup(() -> client.signers().delete(created.getId()));
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
        Tag created = client.tags().create(CreateTagRequest.builder().name(name).color("FF0000").build());
        try {
            assertThat(created.getId()).isNotBlank();
            assertThat(created.getName()).isEqualTo(name);

            Tag renamed = client.tags().rename(created.getId(),
                    RenameTagRequest.builder().name(name + "-renamed").build());
            assertThat(renamed.getName()).isEqualTo(name + "-renamed");
        } finally {
            retryCleanup(() -> client.tags().delete(created.getId(), true));
        }
    }

    @Test
    @Order(17)
    void getsAccountTheme() {
        var theme = client.workspaces().getTheme(accountId);
        assertThat(theme).isNotNull();
        // account_name is always populated; colors/logo may be null depending on branding.
        assertThat(theme.getAccountName()).isNotBlank();
    }

    @Test
    @Order(18)
    void listsAssignmentsWithSandboxAccountContext() {
        assertThat(client.assignments().list(ListParams.builder().perPage(1).build()).getData())
                .isNotNull();
    }

    @Test
    @Order(19)
    void getsAuthenticatedUserAndProbesDocumentedSandboxRoutes() {
        assertThat(client.users().get().getId()).isNotBlank();

        probeDocumentedRoute(() -> client.workspaces().stats(accountId));
        probeDocumentedRoute(() -> client.users().stats());
        probeDocumentedRoute(() -> client.users().getNotificationPreferences());
    }

    @Test
    @Order(20)
    void assignmentEmailLifecycleWithConfiguredRecipients() {
        String primaryEmail = System.getenv("ASSINAFY_TEST_EMAIL_PRIMARY");
        String secondaryEmail = System.getenv("ASSINAFY_TEST_EMAIL_SECONDARY");
        Assumptions.assumeTrue(primaryEmail != null && !primaryEmail.isBlank()
                        && secondaryEmail != null && !secondaryEmail.isBlank(),
                "Set both ASSINAFY_TEST_EMAIL_PRIMARY and ASSINAFY_TEST_EMAIL_SECONDARY");

        Signer primary = null;
        Signer secondary = null;
        DocumentUploadResponse document = null;
        boolean deletePrimary = false;
        boolean deleteSecondary = false;
        try {
            Signer existingPrimary = client.signers().findByEmail(primaryEmail);
            primary = client.signers().create(CreateSignerRequest.builder()
                    .fullName("SDK Integration Primary").email(primaryEmail).build());
            deletePrimary = existingPrimary == null;
            if (deletePrimary) {
                primary = client.signers().update(primary.getId(), UpdateSignerRequest.builder()
                        .fullName("SDK Integration Primary")
                        .governmentId("400.676.228-36")
                        .build());
            }

            Signer existingSecondary = client.signers().findByEmail(secondaryEmail);
            secondary = client.signers().create(CreateSignerRequest.builder()
                    .fullName("SDK Integration Secondary").email(secondaryEmail).build());
            deleteSecondary = existingSecondary == null;

            document = client.documents().upload(minimalPdf(), "sdk-it-assignment-" + UUID.randomUUID() + ".pdf");
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
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS).toString())
                    .build();

            assertThat(client.assignments().estimateCost(document.getId(), request)).isNotNull();
            Assignment assignment = client.assignments().create(document.getId(), request);
            assertThat(assignment.getId()).isNotBlank();
            assertThat(assignment.getSigners()).hasSize(2);

            client.assignments().resetExpiration(document.getId(), assignment.getId(),
                    Instant.now().plus(8, ChronoUnit.DAYS).toString());
            assertThat(client.assignments().estimateResendCost(
                    document.getId(), assignment.getId(), primary.getId())).isNotNull();
            assertThat(client.assignments().resendNotification(
                    document.getId(), assignment.getId(), primary.getId()).getDocumentId())
                    .isEqualTo(document.getId());
            assertThat(client.assignments().getWhatsappNotifications(
                    document.getId(), assignment.getId())).isNotNull();
            client.publicDocuments().sendToken(document.getId(), secondaryEmail);
            assertThat(client.documents().activities(document.getId())).isNotNull();
        } finally {
            Throwable cleanupFailure = null;
            if (document != null) {
                try {
                    DocumentUploadResponse createdDocument = document;
                    retryCleanup(() -> client.documents().delete(createdDocument.getId()));
                } catch (Throwable failure) {
                    cleanupFailure = failure;
                }
            }
            if (deletePrimary && primary != null) {
                try {
                    Signer createdPrimary = primary;
                    retryCleanup(() -> client.signers().delete(createdPrimary.getId()));
                } catch (Throwable failure) {
                    if (cleanupFailure == null) cleanupFailure = failure;
                    else cleanupFailure.addSuppressed(failure);
                }
            }
            if (deleteSecondary && secondary != null) {
                try {
                    Signer createdSecondary = secondary;
                    retryCleanup(() -> client.signers().delete(createdSecondary.getId()));
                } catch (Throwable failure) {
                    if (cleanupFailure == null) cleanupFailure = failure;
                    else cleanupFailure.addSuppressed(failure);
                }
            }
            if (cleanupFailure != null) throw new AssertionError("Sandbox cleanup failed", cleanupFailure);
        }
    }

    @Test
    @Order(21)
    void requestsPasswordResetForConfiguredSandboxIdentity() {
        String email = System.getenv("ASSINAFY_TEST_EMAIL_PRIMARY");
        Assumptions.assumeTrue(email != null && !email.isBlank(),
                "Set ASSINAFY_TEST_EMAIL_PRIMARY");
        assertThatCode(() -> client.authentication().requestPasswordReset(email))
                .doesNotThrowAnyException();
    }

    private static void probeDocumentedRoute(Runnable operation) {
        try {
            operation.run();
        } catch (ApiException error) {
            assertThat(error.getStatusCode())
                    .as("documented route may be absent from the current sandbox deployment")
                    .isEqualTo(404);
        }
    }

    private static void retryCleanup(Runnable operation) {
        for (int attempt = 0; ; attempt++) {
            try {
                operation.run();
                return;
            } catch (RateLimitException error) {
                if (attempt == 2) throw error;
                long seconds = 5;
                String retryAfter = error.getResponseHeader("retry-after");
                try {
                    if (retryAfter != null) seconds = Math.max(1, Long.parseLong(retryAfter));
                } catch (NumberFormatException ignored) {
                    // Retry-After can be an HTTP date; the short sandbox fallback is sufficient.
                }
                try {
                    Thread.sleep(seconds * 1_000 + 250);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError("Interrupted during sandbox cleanup", interrupted);
                }
            }
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
