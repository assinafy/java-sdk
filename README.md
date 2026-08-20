# Assinafy Java SDK

Java client SDK for the [Assinafy API](https://api.assinafy.com.br/v1/docs) — a Brazilian digital signature platform.

See the [complete Java API reference](docs/API_REFERENCE.md) for all 89 official operations,
payload fields, response schemas, status codes, authentication requirements, and compatibility APIs.

## Requirements

- JDK 25 (LTS). The build intentionally enforces Java `>=25,<26`.
- Maven 3.9+

## Installation

Release tags publish to GitHub Packages. GitHub requires authentication to install public Maven
packages, so export your GitHub username and a classic personal access token with `read:packages`:

```bash
export GITHUB_ACTOR=your-github-username
export GITHUB_TOKEN=your-classic-personal-access-token
```

Reference those variables from `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>${env.GITHUB_ACTOR}</username>
            <password>${env.GITHUB_TOKEN}</password>
        </server>
    </servers>
</settings>
```

Then add the GitHub Packages repository and SDK dependency to your project:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/assinafy/java-sdk</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.assinafy</groupId>
    <artifactId>assinafy-sdk</artifactId>
    <version>1.5.1</version>
</dependency>
```

For a source checkout, install the artifact into your local Maven repository first:

```bash
mvn install
```

## Quick Start

```java
import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.DocumentUploadResponse;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.CreateSignerRequest;
import com.assinafy.sdk.request.SignerReference;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class QuickStart {
    private QuickStart() {}

    public static void main(String[] args) throws Exception {
        String apiKey = Objects.requireNonNull(
            System.getenv("ASSINAFY_API_KEY"), "Set ASSINAFY_API_KEY");
        String accountId = Objects.requireNonNull(
            System.getenv("ASSINAFY_ACCOUNT_ID"), "Set ASSINAFY_ACCOUNT_ID");

        AssinafyClient client = new AssinafyClient(
            AssinafyClientOptions.builder()
                .apiKey(apiKey)
                .accountId(accountId)
                .build()
        );

        byte[] fileData = Files.readAllBytes(Path.of("contract.pdf"));
        DocumentUploadResponse document = client.documents().upload(fileData, "contract.pdf");
        client.documents().waitUntilReady(document.getId());

        Signer signer = client.signers().create(
            CreateSignerRequest.builder()
                .fullName("Example Signer")
                .email("signer@example.com")
                .build()
        );

        Assignment assignment = client.assignments().create(
            document.getId(),
            CreateAssignmentRequest.builder()
                .method("virtual")
                .signers(List.of(SignerReference.ofId(signer.getId())))
                .message("Please sign this document")
                .build()
        );
        System.out.println(assignment.getId());
    }
}
```

## Authentication

The API supports two authentication methods:

```java
// Preferred: X-Api-Key header
AssinafyClient apiKeyClient = new AssinafyClient(
    AssinafyClientOptions.builder()
        .apiKey("your-api-key")
        .accountId("your-account-id")
        .build()
);

// Legacy: Authorization: Bearer token
AssinafyClient bearerClient = new AssinafyClient(
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
| `baseUrl`       | String   | `https://api.assinafy.com.br/v1`     | HTTPS API base URL. Plain HTTP is rejected except for loopback testing; use `AssinafyClientOptions.SANDBOX_BASE_URL` for the sandbox. |
| `webhookSecret`  | String   | —                                    | Optional secret for an out-of-band HMAC arrangement; the official API publishes no webhook-signature scheme. |
| `timeoutMs`      | long     | 30000                                | Request timeout in milliseconds.          |
| `logger`        | Logger   | No-op                                | Optional logger instance.                |

### Factory Methods

```java
// Positional factory
AssinafyClient factoryClient = AssinafyClient.create("api-key", "account-id");

// With additional options
AssinafyClientOptions extras = AssinafyClientOptions.builder()
    .webhookSecret("secret")
    .timeoutMs(60000)
    .build();
AssinafyClient customClient = AssinafyClient.create("api-key", "account-id", extras);
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

// Rename (PATCH /documents/{id})
DocumentDetails renamed = client.documents().rename(documentId, "signed-contract.pdf");

// Lightweight search (compact representation; cheaper than list())
PaginatedResult<DocumentListItem> found = client.documents().search(
    ListParams.builder().search("contract").status("metadata_ready").build()
);

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

// Wait for processing to finish, then download raw binary artifact bytes.
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

// Self-service (signer-access-code based; the code is sent as the signer-access-code query param)
Signer selfInfo = client.signers().getSelf(signerAccessCode);
client.signers().acceptTerms(signerAccessCode);   // no body, no return payload
Map<String, Object> verifyResult = client.signers().verifyEmail(signerAccessCode, "123456");

// Confirm/update signer data before signing; returns the server-normalised signer.
// Documented body fields: full_name, email, government_id.
Signer confirmed = client.signers().confirmSignerData(documentId, signerAccessCode,
    Map.of("full_name", "Maria Silva", "email", "maria@example.com"));
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
CostEstimate typedCost = client.assignments().estimateCostTyped(documentId, request);

// List assignments. OpenAPI resolves the current interactive account; when this client has a
// default account ID, the SDK also sends the deployed compatibility accountId query parameter.
PaginatedResult<Assignment> assignments = client.assignments().list(
    ListParams.builder().page(1).perPage(20).build()
);

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

// Deployed compatibility: clear expiration. OpenAPI does not explicitly mark expires_at nullable.
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

// Stop delivery. Prefer inactivate(); delete() is deprecated because its DELETE route is not in
// the official contract and returned 404 on the tested deployment.
client.webhooks().inactivate();

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

// Get (compatibility endpoint; absent from the official OpenAPI contract)
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

// Document tags. append/replace take tag NAMES (auto-created if unknown); an existing name links
// that tag. (The API reference labels these "Tag IDs", but the live API resolves/creates by name —
// verified against the sandbox.) detachTag takes the tag ID from the path.
client.documents().appendTags(documentId, List.of("Urgent"));      // add without removing
client.documents().replaceTags(documentId, List.of("Contracts"));  // replace the whole set
List<Tag> docTags = client.documents().listTags(documentId);
client.documents().detachTag(documentId, tagId);                   // detach one tag (by ID)
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

### Authentication and Users

Login, social-login, password, authenticated-user, user-statistics, and notification-preference
methods are available through `client.authentication()` and `client.users()`. Their complete
signatures and payloads are in the [API reference](docs/API_REFERENCE.md).

Notification preferences use the exact case-sensitive codes published by the API. Updates merge,
so omitted switches keep their current values:

```java
NotificationPreferences preferences = client.users().getNotificationPreferences();
NotificationPreferences updated = client.users().updateNotificationPreferences(Map.of(
    "DocumentCompleted", true,
    "SignerWhatsappFailed", false
));
```

Request: `PUT /users/self/notification-preferences`

```json
{ "DocumentCompleted": true, "SignerWhatsappFailed": false }
```

Response `data` (the API always returns all nine switches):

```json
{
  "DocumentCompleted": true,
  "SignerDeclined": true,
  "DocumentCancelled": true,
  "DocumentAboutToExpire": true,
  "DocumentExpired": true,
  "DocumentExpirationReset": true,
  "DocumentProcessingFailed": true,
  "TemplateProcessingFailed": true,
  "SignerWhatsappFailed": false
}
```

Both notification-preference operations are published in the production OpenAPI contract, but the
sandbox returned application-level `404` responses when checked on 2026-08-20. The SDK methods
remain available for production and future sandbox parity.

### Public Documents

Endpoints that do not require auth (useful for embedded signer flows).

```java
// Basic info — anyone can call (returns the same typed DocumentDetails as documents().details())
DocumentDetails basic = client.publicDocuments().getBasicInfo(documentId);

// Send a one-time access token by email so the recipient can view/sign the document.
// OpenAPI documents optional `email`; the SDK also sends sandbox-required recipient/channel.
Map<String, Object> sent = client.publicDocuments().sendToken(documentId, "signer@example.com");

// Explicit deployed channel form (recipient may be an email address or phone number).
client.publicDocuments().sendToken(documentId, recipient, channel);
```

### Signer Self-Service

These endpoints are used by the signer's browser/app (signer-access-code based).

```java
// Get a signer's own info (includes has_signature/has_initial/is_signature_reusable)
Signer me = client.signers().getSelf(signerAccessCode);

// Accept terms (no body, no return)
client.signers().acceptTerms(signerAccessCode);

// OTP verification (body carries only verification-code)
client.signers().verifyEmail(signerAccessCode, "123456");

// Signature image. The official upload contract accepts image/png only.
// type and reuse are optional documented query inputs; reuse sets is_signature_reusable.
client.signers().uploadSignature(signerAccessCode, "signature", pngBytes);
client.signers().uploadSignature(signerAccessCode, "signature", pngBytes, true); // opt into reuse
byte[] image = client.signers().downloadSignature(signerAccessCode, "signature");

// Documents assigned to the signer
Map<String, Object> current = client.signers().getCurrentDocument(signerId, signerAccessCode);
PaginatedResult<DocumentListItem> mine = client.signers().listDocuments(signerId, signerAccessCode);
PaginatedResult<DocumentListItem> hits =
    client.signers().searchDocuments(signerId, signerAccessCode, "invoice"); // compact search
byte[] pdf = client.signers().downloadDocument(signerId, docId, "certificated", signerAccessCode);

// Bulk sign / decline
client.signers().signMultiple(signerAccessCode, List.of(docId1, docId2));
client.signers().declineMultiple(signerAccessCode, List.of(docId1), "Reason");
```

### Workspaces

```java
// Create (notification_sender_type: "User" (default) or "Account")
Workspace workspace = client.workspaces().create(
    CreateWorkspaceRequest.builder().name("My Workspace").notificationSenderType("Account").build()
);

// List
PaginatedResult<WorkspaceListItem> workspaces = client.workspaces().list();

// Get
Workspace workspace = client.workspaces().get(accountId);

// Update
Workspace updated = client.workspaces().update(accountId,
    UpdateWorkspaceRequest.builder().name("New Name").build()
);

// Delete (pass force=true to also cancel an active paid subscription that would block deletion)
client.workspaces().delete(accountId);
client.workspaces().delete(accountId, true);

// Branding: theme + logo
AccountTheme theme = client.workspaces().getTheme(accountId); // account_name, colors, logo URL
byte[] logo = client.workspaces().downloadLogo(accountId);    // throws ApiException(404) if none set
client.workspaces().uploadLogo(accountId, pngBytes, "logo.png"); // content type auto-detected
client.workspaces().deleteLogo(accountId);
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

## Request / Response Payloads

JSON success responses normally use `{ "status", "message", "data" }`; the SDK returns `data` and
also accepts compatible bare JSON where applicable. A `void` method discards a success envelope and
accepts an empty 2xx body. Binary methods return raw `byte[]` without JSON decoding. A non-2xx HTTP
status or non-2xx numeric envelope status raises `ApiException`. See the
[complete Java API reference](docs/API_REFERENCE.md) for every operation and payload field.

**Upload document** — `documents().upload(...)` → `DocumentUploadResponse` (multipart `file`; the
SDK also sends compatibility `name` and optional `metadata` parts):

```json
{ "data": {
  "resource": "document", "id": "103aa2…", "account_id": "102d…", "template_id": null,
  "name": "contract.pdf", "status": "uploaded",
  "artifacts": { "original": "https://…/download/original" },
  "is_closed": false, "signing_url": "https://app…/sign/103aa2…",
  "decline_reason": null, "declined_by": null, "tags": [],
  "created_at": "2026-07-18T19:03:38Z", "updated_at": "2026-07-18T19:03:38Z", "pages": []
}, "status": 200, "message": "" }
```

Once processing finishes (`status: "metadata_ready"`), `documents().details(id)` also returns a
`thumbnail` artifact and a populated `pages` array (`{ id, number, height, width, download_url }`).

**Create signer** — `signers().create(...)` → `Signer`:

```jsonc
// request
{ "full_name": "John Doe", "email": "john@example.com", "whatsapp_phone_number": "+5548999990000" }
// response data
{ "resource": "signer", "id": "19e6…", "full_name": "John Doe", "email": "john@example.com",
  "whatsapp_phone_number": null, "has_accepted_terms": false }
```

**Create assignment** — `assignments().create(documentId, ...)` → `Assignment`:

```jsonc
// request
{ "method": "virtual", "message": "Please sign",
  "signers": [ { "id": "19e6…", "verification_method": "Email", "notification_methods": ["Email"], "step": 1 } ] }
// response data (abridged)
{ "resource": "assignment", "id": "103aa2…", "sender_email": "sender@example.invalid", "method": "virtual",
  "expires_at": null, "message": "Please sign",
  "signers": [ { "id": "19e6…", "full_name": "John Doe", "email": "john@example.com",
                 "verification_method": "Email", "notification_methods": ["Email"], "step": 1,
                 "notified": true, "completed": false, "notification_history": [] } ],
  "items": [ { "id": "…", "page": null, "signer": { … }, "field": { … }, "value": null, "completed": false } ],
  "summary": { "signer_count": 1, "completed_count": 0, "signers": [ … ] },
  "signing_urls": [ { "signer_id": "19e6…", "url": "https://app…/sign/103aa2…?email=…" } ] }
```

**Estimate cost** — the compatibility methods return `Map<String,Object>`; the corresponding
`estimateCostTyped(...)` methods return `CostEstimate`:

```json
{ "documents": 1, "credits": 0, "needs_extra_document": false, "extra_document_cost": 0,
  "total_credits": 0, "breakdown": [], "document_balance": 87, "credit_balance": 0,
  "has_sufficient_resources": true, "blocking_reason": null, "message": null }
```

**Field definition** — `fields().create/get/update(...)` → `FieldDefinition`:

```json
{ "resource": "field_definition", "id": "102d…", "name": "Address", "type": "text", "regex": null,
  "is_pre_defined": false, "is_active": true, "is_required": true, "is_standard": false,
  "is_read_only": false, "is_visible": true }
```

**Tag** — `tags().create(...)` → `Tag` · **Account theme** — `workspaces().getTheme(...)` → `AccountTheme`:

```jsonc
// Tag
{ "resource": "tag", "id": "103a…", "name": "Contracts", "color": "ff8800",
  "created_at": "2026-05-14T12:00:00Z", "updated_at": "2026-05-14T12:00:00Z" }
// AccountTheme
{ "account_name": "Acme", "primary_color": "2072b9", "secondary_color": "ffffff", "logo": null }
```

**Webhook subscription** — `webhooks().get()` / `register(...)` → `WebhookSubscription`:

```json
{ "events": ["document_ready","signer_signed_document"], "is_active": true,
  "url": "https://example.com/webhook", "email": "admin@example.com",
  "updated_at": "2026-07-18T02:36:02Z" }
```

## Development

```bash
# Offline unit tests
mvn test

# Full offline build, Javadocs, and package verification
mvn clean verify
```

`mvn test` is offline and includes wire-level HTTP tests backed by MockWebServer. The opt-in
`LiveApiSmokeIT` suite performs real sandbox reads and writes. Inject its credentials through your
shell or CI secret store, then run:

```bash
# ASSINAFY_API_KEY and ASSINAFY_ACCOUNT_ID must already be set from a secret store.
export ASSINAFY_API_KEY
export ASSINAFY_ACCOUNT_ID
export ASSINAFY_BASE_URL=https://sandbox.assinafy.com.br/v1
mvn -Plive-api verify
```

The test rejects any base URL other than the exact sandbox URL. The assignment-notification case
requires both `ASSINAFY_TEST_EMAIL_PRIMARY` and `ASSINAFY_TEST_EMAIL_SECONDARY`; the password-reset
case requires only `ASSINAFY_TEST_EMAIL_PRIMARY`. Leave them unset to skip those cases, or set them
to controlled sandbox recipients—the cases send real messages. Writes use unique fixture names and
`finally` cleanup, but cleanup can still fail after a network or service outage; inspect the sandbox
account after an interrupted run.

## License

MIT
