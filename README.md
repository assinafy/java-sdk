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
    <version>1.2.0</version>
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

// Cancel (uses undocumented endpoint)
Object cancelResult = client.assignments().cancel(documentId, "Reason for cancellation");
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

# Run tests
mvn test

# Package
mvn package
```

## License

MIT