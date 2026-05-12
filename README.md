# Assinafy Java SDK

Java client SDK for the [Assinafy API](https://api.assinafy.com.br/v1/docs) — a Brazilian digital signature platform.

## Requirements

- Java 25+
- Maven 3.9+

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.assinafy</groupId>
    <artifactId>assinafy-sdk</artifactId>
    <version>1.3.0</version>
</dependency>
```

## Quick Start

```java
import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;

AssinafyClient client = new AssinafyClient(
    AssinafyClientOptions.builder()
        .apiKey("your-api-key")
        .accountId("your-account-id")
        .build()
);

// Upload a document and request signatures
byte[] fileData = Files.readAllBytes(Path.of("contract.pdf"));
DocumentUploadResponse doc = client.documents().upload(fileData, "contract.pdf");

// Create signers
Signer signer = client.signers().create(
    CreateSignerRequest.builder()
        .fullName("John Doe")
        .email("john@example.com")
        .build()
);

// Create assignment
Assignment assignment = client.assignments().create(
    doc.getId(),
    CreateAssignmentRequest.builder()
        .method("virtual")
        .signers(List.of(SignerReference.ofId(signer.getId())))
        .message("Please sign this document")
        .build()
);
```

## Authentication

The API supports two authentication methods:

```java
// Preferred: X-Api-Key header
AssinafyClient client = new AssinafyClient(
    AssinafyClientOptions.builder()
        .apiKey("your-api-key")
        .accountId("your-account-id")
        .build()
);

// Legacy: Authorization: Bearer token
AssinafyClient client = new AssinafyClient(
    AssinafyClientOptions.builder()
        .token("jwt-token")
        .accountId("your-account-id")
        .build()
);
```

## Configuration

| Option          | Type     | Default                              | Description                              |
|-----------------|----------|--------------------------------------|------------------------------------------|
| `apiKey`        | String   | —                                    | Preferred credential (X-Api-Key header).  |
| `token`         | String   | —                                    | Legacy access token (Bearer header).      |
| `accountId`     | String   | —                                    | Default workspace/account ID.             |
| `baseUrl`       | String   | `https://api.assinafy.com.br/v1`     | API base URL. Use sandbox for testing.    |
| `webhookSecret`  | String   | —                                    | Shared secret for webhook verification.   |
| `timeoutMs`      | long     | 30000                                | Request timeout in milliseconds.          |
| `logger`        | Logger   | No-op                                | Optional logger instance.                |

### Factory Methods

```java
// Positional factory
AssinafyClient client = AssinafyClient.create("api-key", "account-id");

// With additional options
AssinafyClientOptions extras = AssinafyClientOptions.builder()
    .webhookSecret("secret")
    .timeoutMs(60000)
    .build();
AssinafyClient client = AssinafyClient.create("api-key", "account-id", extras);
```

## Resources

### Documents

```java
// Upload
DocumentUploadResponse doc = client.documents().upload(fileData, "document.pdf");

// List with pagination
PaginatedResult<DocumentListItem> result = client.documents().list(
    ListParams.builder().page(1).perPage(20).build()
);

// Get details
DocumentDetails details = client.documents().details(documentId);

// Download
byte[] pdf = client.documents().download(documentId);
byte[] thumbnail = client.documents().thumbnail(documentId);

// Delete
client.documents().delete(documentId);

// Create from template
DocumentDetails doc = client.documents().createFromTemplate(
    templateId,
    new CreateDocumentFromTemplateRequest(...),
    accountId
);

// Get document statuses
List<DocumentStatusInfo> statuses = client.documents().getStatuses();
```

### Signers

```java
// Create
Signer signer = client.signers().create(
    CreateSignerRequest.builder()
        .fullName("John Doe")
        .email("john@example.com")
        .whatsappPhoneNumber("+5548999990000")
        .build()
);

// Get
Signer signer = client.signers().get(signerId);

// List
PaginatedResult<Signer> signers = client.signers().list(
    ListParams.builder().search("john").build()
);

// Update
client.signers().update(signerId,
    UpdateSignerRequest.builder().fullName("John Updated").build()
);

// Delete
client.signers().delete(signerId);

// Find by email
Signer signer = client.signers().findByEmail("john@example.com");

// Self-service (signer-access-code based)
Signer selfInfo = client.signers().getSelf(signerAccessCode);
Map<String, Object> result = client.signers().acceptTerms(signerAccessCode);
Map<String, Object> verifyResult = client.signers().verifyEmail(signerAccessCode, "123456");
```

### Assignments

```java
// Create
Assignment assignment = client.assignments().create(
    documentId,
    CreateAssignmentRequest.builder()
        .method("virtual")
        .signers(List.of(SignerReference.ofId(signerId)))
        .message("Please sign")
        .expiresAt("2025-12-31T23:59:59Z")
        .build()
);

// Estimate cost
Map<String, Object> cost = client.assignments().estimateCost(documentId, request);

// Reset expiration
Assignment updated = client.assignments().resetExpiration(documentId, assignmentId, "2025-06-30T00:00:00Z");

// Resend notification
ResendEmailResponse res = client.assignments().resendNotification(documentId, assignmentId, signerId);

// Signer-side decline (requires signer-access-code)
Map<String, Object> declined = client.assignments().decline(
        documentId, assignmentId, signerAccessCode, "Unfavorable terms");

// Inspect WhatsApp notification delivery state
Map<String, Object> waState = client.assignments().getWhatsappNotifications(documentId, assignmentId);
```

### Webhooks

```java
// Register
WebhookSubscription sub = client.webhooks().register(
    RegisterWebhookRequest.builder()
        .url("https://example.com/webhook")
        .email("admin@example.com")
        .events(List.of("document_ready", "signer_signed_document"))
        .build()
);

// Get current subscription
WebhookSubscription current = client.webhooks().get();

// Inactivate
client.webhooks().inactivate();

// Delete
client.webhooks().delete();

// List event types
List<WebhookEventTypeInfo> types = client.webhooks().listEventTypes();

// List dispatches
PaginatedResult<WebhookDispatch> dispatches = client.webhooks().listDispatches(
    ListParams.builder().page(1).perPage(20).build()
);

// Retry dispatch
client.webhooks().retryDispatch(dispatchId);
```

### Templates

```java
// List
PaginatedResult<TemplateListItem> templates = client.templates().list();

// Get
Template template = client.templates().get(templateId);
```

### Field Definitions

Field definitions describe the typed inputs that signers fill in during a
`collect`-method assignment. The SDK exposes the full CRUD surface plus the
validation helpers.

```java
// Create
FieldDefinition field = client.fields().create(
    CreateFieldRequest.builder()
        .type("text")
        .name("Address")
        .isRequired(true)
        .build()
);

// List / Get / Update / Delete
PaginatedResult<FieldDefinition> fields = client.fields().list();
FieldDefinition one = client.fields().get(fieldId);
client.fields().update(fieldId, UpdateFieldRequest.builder().isRequired(false).build());
client.fields().delete(fieldId);

// Validate a value (omit signer-access-code when calling as an authenticated user)
Map<String, Object> result = client.fields().validate(fieldId, "400.676.228-36", null);

// Validate multiple in one round-trip
Map<String, Object> bulk = client.fields().validateMultiple(
    List.of(Map.of("field_id", fieldId, "value", "12345")),
    null
);

// Discover supported types
List<FieldType> types = client.fields().listTypes();
```

### Public Documents

Endpoints that do not require auth (useful for embedded signer flows).

```java
// Basic info — anyone can call
Map<String, Object> basic = client.publicDocuments().getBasicInfo(documentId);

// Request a new signer-access-code to be sent to the recipient
Map<String, Object> sent = client.publicDocuments().sendToken(
    documentId, "signer@example.com", "email"
);
```

### Signer Self-Service

These endpoints are used by the signer's browser/app (signer-access-code based).

```java
// Get a signer's own info
Signer me = client.signers().getSelf(signerAccessCode);

// Confirm/accept terms
client.signers().acceptTerms(signerAccessCode);

// Email verification
client.signers().verifyEmail(signerAccessCode, "123456");

// Signature image (PNG or JPEG)
client.signers().uploadSignature(signerAccessCode, "signature", pngBytes);
byte[] image = client.signers().downloadSignature(signerAccessCode, "signature");

// Documents assigned to the signer
Map<String, Object> current = client.signers().getCurrentDocument(signerId, signerAccessCode);
PaginatedResult<DocumentListItem> mine = client.signers().listDocuments(signerId, signerAccessCode);
byte[] pdf = client.signers().downloadDocument(signerId, docId, "certificated", signerAccessCode);

// Bulk sign / decline
client.signers().signMultiple(signerAccessCode, List.of(docId1, docId2));
client.signers().declineMultiple(signerAccessCode, List.of(docId1), "Reason");
```

### Workspaces

```java
// Create
Workspace workspace = client.workspaces().create(
    CreateWorkspaceRequest.builder().name("My Workspace").build()
);

// List
PaginatedResult<WorkspaceListItem> workspaces = client.workspaces().list();

// Get
Workspace workspace = client.workspaces().get(accountId);

// Update
Workspace updated = client.workspaces().update(accountId,
    UpdateWorkspaceRequest.builder().name("New Name").build()
);

// Delete
client.workspaces().delete(accountId);
```

## High-Level Helper

The SDK provides a convenience method that handles the full workflow:

```java
UploadAndRequestSignaturesResult result = client.uploadAndRequestSignatures(
    UploadAndRequestSignaturesRequest.builder()
        .fileData(fileData)
        .fileName("contract.pdf")
        .signers(List.of(
            UploadAndRequestSignaturesRequest.SignerEntry.builder()
                .name("John Doe")
                .email("john@example.com")
                .build()
        ))
        .message("Please sign this contract")
        .waitForReady(true)
        .build()
);

// Result contains:
result.getDocument();      // DocumentUploadResponse
result.getAssignment();    // Assignment
result.getSignerIds();     // List<String>
```

## Webhook Verification

```java
WebhookVerifier verifier = client.webhookVerifier();

// In your webhook handler:
if (!verifier.verify(payload, signatureHeader)) {
    // Invalid signature
    return Response.status(401).build();
}

WebhookPayload event = verifier.extractEvent(payload);
String eventType = verifier.getEventType(event);
Map<String, Object> eventData = verifier.getEventData(event);
```

## Error Handling

The SDK throws typed exceptions:

```java
try {
    client.documents().upload(fileData, "document.pdf");
} catch (ValidationException e) {
    // Invalid input (e.g., file too large, invalid format)
    System.err.println("Validation failed: " + e.getMessage());
    System.err.println("Errors: " + e.getErrors());
} catch (ApiException e) {
    // API returned an error
    System.err.println("API error " + e.getStatusCode() + ": " + e.getMessage());
    System.err.println("Response data: " + e.getResponseData());
} catch (NetworkException e) {
    // Network connectivity issue
    System.err.println("Network error: " + e.getMessage());
} catch (AssinafyException e) {
    // General SDK error
    System.err.println("SDK error: " + e.getMessage());
    System.err.println("Context: " + e.getContext());
}
```

## Pagination

Use `ListParams` for paginated requests:

```java
ListParams params = ListParams.builder()
    .page(1)
    .perPage(25)
    .search("document name")
    .sort("-created_at")  // Descending order
    .build();

PaginatedResult<DocumentListItem> result = client.documents().list(params);
PaginationMeta meta = result.getMeta();
// meta.getCurrentPage()
// meta.getTotal()
// meta.getLastPage()
// meta.getPerPage()
```

## Development

```bash
# Build
mvn compile

# Run unit tests
mvn test

# Run the live API smoke test against the real Assinafy API
ASSINAFY_API_KEY=...  ASSINAFY_ACCOUNT_ID=...  \
    mvn test -Dtest=LiveApiSmokeIT

# Package
mvn package
```

The live smoke test (`src/test/java/com/assinafy/sdk/it/LiveApiSmokeIT.java`)
is excluded from the default `mvn test` run (Surefire skips classes whose names
end in `IT`). When enabled, it exercises read endpoints, uploads a tiny PDF,
estimates an assignment cost, and creates + deletes an ephemeral signer. No
emails are sent at any point.

## Continuous Integration

CI runs on GitHub Actions (`.github/workflows/ci.yml`), since this repo is
mirrored to a public GitHub project. Three jobs:

- **test** — `mvn test` on Temurin JDK 25 (every push and PR to `main`).
- **package** — builds the JAR + sources JAR and uploads them as artifacts.
- **publish** — fires only on `v*` tags. Deploys the artifact to the GitLab
  Maven registry at `git.febacapital.com`.

The publish job needs three GitHub repo secrets:

| Secret                | Description                                                       |
|-----------------------|-------------------------------------------------------------------|
| `GITLAB_PROJECT_ID`   | Numeric project ID for `feba/assinafy/sdk/java` on GitLab.        |
| `GITLAB_DEPLOY_USER`  | Username paired with the deploy token (informational only).        |
| `GITLAB_DEPLOY_TOKEN` | Deploy token with `write_package_registry` scope.                  |

To cut a release: tag `v1.3.0`, push the tag, and the publish job will
upload `assinafy-sdk-1.3.0.jar` and `assinafy-sdk-1.3.0-sources.jar` to the
GitLab Maven registry.

## License

MIT