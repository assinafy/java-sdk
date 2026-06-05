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
    <version>1.4.1</version>
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
| `baseUrl`       | String   | `https://api.assinafy.com.br/v1`     | API base URL. Use `AssinafyClientOptions.SANDBOX_BASE_URL` for the sandbox. |
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
    CreateDocumentFromTemplateRequest.builder()
        .name("contract.pdf")
        .signers(List.of(
            TemplateSigner.builder().roleId("role-id").id(signerId).build()
        ))
        .build(),
    accountId
);

// Estimate the credit cost before creating
Map<String, Object> cost = client.documents().estimateCostFromTemplate(templateId, request, accountId);

// Get document statuses
List<DocumentStatusInfo> statuses = client.documents().getStatuses();

// Wait for processing to finish, then download artifacts (PDF/JPEG bytes).
// Download throws ApiException if the artifact is unavailable (e.g. not yet signed).
client.documents().waitUntilReady(documentId);
byte[] page = client.documents().downloadPage(documentId, pageId);
String thumbUrl = details.getArtifacts().getThumbnail(); // inline URL, no extra round-trip

// Activity log, verification and signing-progress helpers
List<DocumentActivity> activity = client.documents().activities(documentId);
Map<String, Object> verification = client.documents().verify(signatureHash); // { is_valid, ... }
boolean done = client.documents().isFullySigned(documentId);
SigningProgress progress = client.documents().getSigningProgress(documentId); // signed/total/pending/%
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

// Create a WhatsApp-only signer (email is optional; full_name is required)
Signer waSigner = client.signers().create(
    CreateSignerRequest.builder()
        .fullName("Maria Silva")
        .whatsappPhoneNumber("+5548999990000")
        .build()
);

// Self-service (signer-access-code based)
Signer selfInfo = client.signers().getSelf(signerAccessCode);
Signer accepted = client.signers().acceptTerms(signerAccessCode);   // returns the signer
Map<String, Object> verifyResult = client.signers().verifyEmail(signerAccessCode, "123456");

// Confirm signer contact data + terms (signer self-service)
client.signers().confirmSignerData(documentId, signerAccessCode,
    Map.of("email", "maria@example.com", "has_accepted_terms", true));
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

// Resend notification (and estimate its cost first)
Map<String, Object> resendCost = client.assignments().estimateResendCost(documentId, assignmentId, signerId);
ResendNotificationResponse res = client.assignments().resendNotification(documentId, assignmentId, signerId);

// Signer-side decline (requires signer-access-code and a non-blank reason)
Map<String, Object> declined = client.assignments().decline(
        documentId, assignmentId, signerAccessCode, "Unfavorable terms");

// Inspect WhatsApp notification delivery state (one entry per tracked notification)
List<Map<String, Object>> waState = client.assignments().getWhatsappNotifications(documentId, assignmentId);

// Clear an assignment's expiration entirely
client.assignments().resetExpiration(documentId, assignmentId, null);

// Signer-side flows (signer-access-code based)
Map<String, Object> toSign = client.assignments().getForSigner(signerAccessCode);
client.assignments().sign(documentId, assignmentId, signerAccessCode,
        List.of(Map.of("itemId", "i1", "fieldId", "f1", "pageId", "p1", "value", "text")));
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
FieldValidationResult result = client.fields().validate(fieldId, "400.676.228-36", null);
if (!Boolean.TRUE.equals(result.getSuccess())) {
    System.err.println(result.getErrorMessage());
}

// Validate multiple in one round-trip
List<FieldValidationResult> bulk = client.fields().validateMultiple(
    List.of(Map.of("field_id", fieldId, "value", "12345")),
    null
);

// Discover supported types
List<FieldType> types = client.fields().listTypes();
```

### Tags

Workspace tags can be created, renamed, and deleted, and attached to documents.

```java
// Workspace-level tag CRUD
Tag tag = client.tags().create(CreateTagRequest.builder().name("Contracts").color("FF0000").build());
PaginatedResult<Tag> tags = client.tags().list();
client.tags().rename(tag.getId(), RenameTagRequest.builder().name("2026 Contracts").build());
client.tags().rename(tag.getId(), RenameTagRequest.builder().clearColor().build()); // sends color:null to clear
client.tags().delete(tag.getId());          // 409 if the tag is still attached…
client.tags().delete(tag.getId(), true);    // …pass force=true to detach + delete

// Document tags (tag names are auto-created if they don't exist)
client.documents().appendTags(documentId, List.of("Urgent"));      // add without removing
client.documents().replaceTags(documentId, List.of("Contracts"));  // replace the whole set
List<Tag> docTags = client.documents().listTags(documentId);
client.documents().detachTag(documentId, tagId);                   // detach one tag
```

### API Key Management

Manage the API key for the authenticated user (`/users/api-keys`). The
generated key is shown in full only once — store it securely and never expose
it to a frontend.

```java
ApiKey current = client.apiKeys().get();          // masked (last 4 chars only), or null
ApiKey rotated = client.apiKeys().create("password");  // full key; invalidates the previous one
client.apiKeys().delete();
```

> The wider user-account/auth surface (login, social login, password
> change/reset) is intentionally **out of scope** for this server-side SDK —
> those are web-app concerns. Only API-key management is provided.

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

> **Caution:** the Assinafy webhook contract does **not** currently publish a signature header or a
> signing scheme, and the subscription has no place to register a shared secret. `verify(...)`
> implements the conventional `HMAC-SHA256(raw body)` pattern for tenants that have an out-of-band
> signing arrangement — it is not a documented platform guarantee. A `verify() == false` result does
> **not** by itself mean a request is forged (it is also `false` when no secret/signature is present).
> Do **not** reject deliveries on `verify() == false` unless you have confirmed your tenant signs with
> this exact scheme; otherwise authenticate webhooks another way and just parse the body.

```java
WebhookVerifier verifier = client.webhookVerifier();

// Parse the event (always safe):
WebhookPayload event = verifier.extractEvent(payload);
String eventType = verifier.getEventType(event);
Map<String, Object> eventData = verifier.getEventData(event);

// Only gate on verify() if your tenant uses the HMAC-SHA256(raw-body) scheme:
if (!verifier.verify(payload, signatureHeader)) {
    return Response.status(401).build();
}
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
} catch (AuthenticationException e) {
    // 401/403 — missing, invalid, or insufficiently-privileged credential (subtype of ApiException)
    System.err.println("Auth error " + e.getStatusCode() + ": " + e.getMessage());
} catch (RateLimitException e) {
    // 429 — back off and retry (subtype of ApiException)
    System.err.println("Rate limited: " + e.getMessage());
} catch (ApiException e) {
    // Any other API error
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

The default `mvn test` run executes 175 offline unit tests (including wire-level
`OkHttpApiClient` tests backed by MockWebServer).

```bash
# Run the live smoke test against the sandbox
ASSINAFY_API_KEY=...  ASSINAFY_ACCOUNT_ID=...  \
    ASSINAFY_BASE_URL=https://sandbox.assinafy.com.br/v1  \
    mvn test -Dtest=LiveApiSmokeIT
```

The live smoke test (`src/test/java/com/assinafy/sdk/it/LiveApiSmokeIT.java`)
is excluded from the default `mvn test` run (Surefire skips classes whose names
end in `IT`) and honors the optional `ASSINAFY_BASE_URL` (defaults to production).
When enabled, it runs 16 read/write flows: it exercises the read endpoints, uploads
a tiny PDF, downloads its artifacts (and asserts an unavailable artifact throws),
estimates an assignment cost, appends/lists/detaches a document tag, validates a
field value, reads the masked API key, and creates + deletes ephemeral signers and
a workspace tag. No emails are sent at any point and every created resource is
cleaned up.

## License

MIT