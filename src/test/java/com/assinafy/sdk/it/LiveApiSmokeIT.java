package com.assinafy.sdk.it;

import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.models.DocumentListItem;
import com.assinafy.sdk.models.DocumentStatusInfo;
import com.assinafy.sdk.models.DocumentUploadResponse;
import com.assinafy.sdk.models.FieldDefinition;
import com.assinafy.sdk.models.FieldType;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.TemplateListItem;
import com.assinafy.sdk.models.WebhookEventTypeInfo;
import com.assinafy.sdk.models.WorkspaceListItem;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.CreateSignerRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.SignerReference;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * End-to-end smoke test against the live Assinafy API.
 *
 * <p>This test is opt-in: it only runs when the environment variables
 * {@code ASSINAFY_API_KEY} and {@code ASSINAFY_ACCOUNT_ID} are set.
 * It exercises read endpoints and one safe create-and-cleanup signer
 * cycle. No emails are sent (no assignment is created).
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
        client = AssinafyClient.create(apiKey, accountId);
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
        // Returns null when no subscription exists, otherwise a subscription object.
        var sub = client.webhooks().get();
        // No assertion on value — just no exception.
        assertThat(true).isTrue();
        if (sub != null) {
            assertThat(sub).isNotNull();
        }
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
        DocumentUploadResponse doc = client.documents().upload(pdf, "sdk-it-" + UUID.randomUUID() + ".pdf");
        try {
            assertThat(doc.getId()).isNotBlank();
            // Wait for status to advance past 'uploading' or 'metadata_processing'.
            client.documents().waitUntilReady(doc.getId(), 20_000, 1_500);
            var details = client.documents().details(doc.getId());
            assertThat(details.getId()).isEqualTo(doc.getId());

            // Estimate cost for a 1-signer assignment (no email is sent).
            Map<String, Object> cost = client.assignments().estimateCost(doc.getId(),
                    CreateAssignmentRequest.builder()
                            .method("virtual")
                            .signers(List.of(SignerReference.builder().verificationMethod("Email").build()))
                            .build());
            assertThat(cost).isNotNull();
        } finally {
            try {
                client.documents().delete(doc.getId());
            } catch (Exception ignore) {
                // Best-effort cleanup
            }
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
                        .build()
        );
        try {
            assertThat(created.getId()).isNotBlank();
            assertThat(created.getEmail()).isEqualToIgnoringCase(email);

            Signer fetched = client.signers().get(created.getId());
            assertThat(fetched.getId()).isEqualTo(created.getId());
        } finally {
            try {
                client.signers().delete(created.getId());
            } catch (Exception ignore) {
                // Best-effort cleanup
            }
        }
    }

    /** Returns a tiny syntactically valid one-page PDF for upload testing. */
    private static byte[] minimalPdf() {
        // Minimal PDF 1.4 with one empty page. Reverse-engineered from a hello-world PDF.
        String pdf = "%PDF-1.4\n"
                + "1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
                + "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n"
                + "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]/Resources<<>>/Contents 4 0 R>>endobj\n"
                + "4 0 obj<</Length 44>>stream\n"
                + "BT /F1 24 Tf 100 700 Td (SDK IT) Tj ET\n"
                + "endstream\nendobj\n"
                + "xref\n0 5\n0000000000 65535 f \n0000000010 00000 n \n0000000055 00000 n \n0000000103 00000 n \n0000000178 00000 n \n"
                + "trailer<</Size 5/Root 1 0 R>>\n"
                + "startxref\n264\n%%EOF\n";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte b : pdf.getBytes()) out.write(b);
        return out.toByteArray();
    }
}
