# Assinafy Java SDK

A Java client for the [Assinafy API](https://api.assinafy.com.br/v1/docs), the Brazilian digital
signature platform. It covers all 89 documented operations — document upload and certification,
signer management, signature requests, templates, field definitions, tags, workspaces, webhooks,
and the signer-facing self-service flows — behind typed models, typed exceptions, and a single
thread-safe client.

This README is written to be read top to bottom: install the SDK, understand how it is organized,
walk the signing lifecycle end to end, then reach the surfaces around it. For the per-operation
contract — every parameter, every request and response field, every documented status code — see
the [complete Java API reference](docs/API_REFERENCE.md).

## Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Quick start](#quick-start)
- [How the SDK is organized](#how-the-sdk-is-organized)
- [Authentication](#authentication)
- [Configuration](#configuration)
- [The signing lifecycle](#the-signing-lifecycle)
  - [1. Upload the document](#1-upload-the-document)
  - [2. Wait for processing](#2-wait-for-processing)
  - [3. Resolve the signers](#3-resolve-the-signers)
  - [4. Estimate the cost and request signatures](#4-estimate-the-cost-and-request-signatures)
  - [5. Track progress](#5-track-progress)
  - [6. Download the artifacts](#6-download-the-artifacts)
  - [7. Delete when finished](#7-delete-when-finished)
- [The one-call workflow helper](#the-one-call-workflow-helper)
- [Signer self-service](#signer-self-service)
- [Templates and field definitions](#templates-and-field-definitions)
- [Tags](#tags)
- [Workspaces and branding](#workspaces-and-branding)
- [Users, sessions, and API keys](#users-sessions-and-api-keys)
- [Webhooks](#webhooks)
- [Error handling](#error-handling)
- [Pagination](#pagination)
- [Request and response payloads](#request-and-response-payloads)
- [Development](#development)
- [Releasing](#releasing)
- [License](#license)

## Requirements

- JDK 25 (LTS). The build enforces Java `>=25,<26`.
- Maven Wrapper pinned to Maven 3.9.16; a system Maven installation is not required.

Runtime dependencies are OkHttp and Jackson. The published jar declares
`Automatic-Module-Name: com.assinafy.sdk`.

## Installation

Release tags publish to GitHub Packages, and GitHub requires authentication even for public Maven
packages. Export a GitHub username and a classic personal access token with `read:packages`:

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

Then add the repository and the dependency to your project:

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
    <version>1.7.0</version>
</dependency>
```

Working from a source checkout instead? Install the artifact into your local repository first:

```bash
./mvnw install
```

## Quick start

The shortest path from a PDF on disk to a dispatched signature request:

```java
import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.Document;
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
        Document document = client.documents().upload(fileData, "contract.pdf");
        client.documents().waitUntilReady(document.getId());

        Signer signer = client.signers().findOrCreate(
            CreateSignerRequest.builder()
                .fullName("Example Signer")
                .email("signer@example.invalid")
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

## How the SDK is organized

**One client, many resources.** `AssinafyClient` owns the HTTP transport and exposes one accessor
per API area: `documents()`, `signers()`, `assignments()`, `templates()`, `fields()`, `tags()`,
`workspaces()`, `webhooks()`, `users()`, `apiKeys()`, `authentication()`, and
`publicDocuments()`. The accessors return the instances the client owns, so holding a resource
reference is equivalent to holding the client.

**The client is thread-safe** with its default OkHttp transport and is designed to be created once
and shared. OkHttp releases idle connections and threads on its own, so no explicit shutdown is
required.

**One Java type per API resource.** The API returns the same `Document` schema from upload, list,
search, get, rename, and create-from-template, and the SDK mirrors that: every one of those methods
returns `Document`. A field a particular response does not populate is `null` — a freshly uploaded
document has no `assignment` and no `pages` until processing reaches `metadata_ready`. `Workspace`,
`Template`, `Signer`, and `Assignment` work the same way.

**Envelopes are unwrapped for you.** JSON success bodies are
`{ "status": integer, "message": string, "data": ... }`; SDK methods return the `data` payload. A
`void` method discards a success envelope and also accepts an empty 2xx body. Binary methods return
raw `byte[]` without JSON decoding. A non-2xx HTTP status — or a non-2xx numeric status inside a
200 envelope — raises `ApiException`.

**Account scoping.** Account-scoped operations use the `accountId` configured on the client. Every
one of them also has an overload taking an explicit account ID, so a single client can serve several
workspaces. `workspaces()` always takes the account ID explicitly, because its operations are about
the workspace rather than inside it.

## Authentication

The API accepts two credentials. Prefer the API key for server integrations:

```java
// Preferred: X-Api-Key header
AssinafyClient apiKeyClient = new AssinafyClient(
    AssinafyClientOptions.builder()
        .apiKey("your-api-key")
        .accountId("your-account-id")
        .build()
);

// Authorization: Bearer access token, from authentication().login(...)
AssinafyClient bearerClient = new AssinafyClient(
    AssinafyClientOptions.builder()
        .token("jwt-token")
        .accountId("your-account-id")
        .build()
);
```

When both are configured the API key wins. Signer-facing operations use a third credential, the
signer access code, passed per call rather than configured on the client — see
[Signer self-service](#signer-self-service). Public operations require no credential at all, so
build a credential-free client for them.

## Configuration

| Option          | Type   | Default                          | Description |
|-----------------|--------|----------------------------------|-------------|
| `apiKey`        | String | —                                | Preferred credential, sent as `X-Api-Key`. |
| `token`         | String | —                                | Bearer access token, used when no API key is set. |
| `accountId`     | String | —                                | Default workspace for account-scoped operations. |
| `baseUrl`       | String | `https://api.assinafy.com.br/v1` | HTTPS API base URL. Plain HTTP is rejected except for loopback testing; use `AssinafyClientOptions.SANDBOX_BASE_URL` for the sandbox. |
| `timeoutMs`     | long   | `30000`                          | Call, connect, read, and write timeout in milliseconds. |
| `logger`        | Logger | No-op                            | Structured diagnostic callback. A logger that throws never affects an API call. |

Two factory methods cover the common cases:

```java
AssinafyClient client = AssinafyClient.create("api-key", "account-id");

AssinafyClientOptions extras = AssinafyClientOptions.builder()
    .baseUrl(AssinafyClientOptions.SANDBOX_BASE_URL)
    .timeoutMs(60_000)
    .build();
AssinafyClient sandboxClient = AssinafyClient.create("api-key", "account-id", extras);
```

## The signing lifecycle

A document travels from an uploaded PDF to a certificated file through a fixed sequence. The rest of
this section follows it step by step.

### 1. Upload the document

Upload a PDF (non-empty, name ending in `.pdf`, at most 25 MB) into a workspace. The response
carries the document ID everything else keys off.

```java
byte[] fileData = Files.readAllBytes(Path.of("contract.pdf"));
Document document = client.documents().upload(fileData, "contract.pdf");

// With optional metadata and an explicit workspace
Document scoped = client.documents().upload(
    fileData, "contract.pdf", Map.of("origin", "crm"), accountId);
```

### 2. Wait for processing

Upload is asynchronous. The document moves through `metadata_processing` to `metadata_ready`, at
which point its pages and thumbnail exist. `waitUntilReady` polls `details` until the document
reaches a ready status, throwing `ValidationException` if it enters a failed status or the budget
expires.

```java
Document ready = client.documents().waitUntilReady(document.getId());          // 30s / 2s poll
Document slower = client.documents().waitUntilReady(document.getId(), 120_000, 5_000);
```

Reading and reshaping the document at any point:

```java
Document details = client.documents().details(documentId);   // get(...) is an alias
Document renamed = client.documents().rename(documentId, "signed-contract.pdf");

PaginatedResult<Document> page = client.documents().list(
    ListParams.builder().page(1).perPage(20).status("pending_signature").build()
);

// Lightweight search: compact records without the expanded assignment and pages
PaginatedResult<Document> found = client.documents().search(
    ListParams.builder().search("contract").build()
);

List<DocumentActivity> activity = client.documents().activities(documentId);
List<DocumentStatusInfo> statuses = client.documents().getStatuses();  // which statuses are deletable
```

### 3. Resolve the signers

`create` always issues the create request. Use `findOrCreate` when reusing an existing signer with
the same email address is what you want; it returns an exact case-insensitive match unchanged,
without overwriting the stored name, phone, or CPF/CNPJ. A supplied `cpf` (CPF or CNPJ) is persisted
after creation through the signer's `government_id` update, with non-digits stripped; if that update
fails, the new signer is deleted.

```java
Signer created = client.signers().create(
    CreateSignerRequest.builder()
        .fullName("John Doe")
        .email("john@example.invalid")
        .whatsappPhoneNumber("+5548999990000")
        .cpf("123.456.789-00")
        .build()
);

Signer reused = client.signers().findOrCreate(
    CreateSignerRequest.builder()
        .fullName("Jane Doe")
        .email("jane@example.invalid")
        .build()
);

// Email is optional — a signer may have only a name and a WhatsApp number
Signer whatsappOnly = client.signers().create(
    CreateSignerRequest.builder()
        .fullName("Maria Silva")
        .whatsappPhoneNumber("+5548999990000")
        .build()
);

Signer fetched = client.signers().get(signerId);
Signer byEmail = client.signers().findByEmail("john@example.invalid");   // null when absent
PaginatedResult<Signer> signers = client.signers().list(ListParams.builder().search("john").build());
client.signers().update(signerId, UpdateSignerRequest.builder().fullName("John Updated").build());
client.signers().delete(signerId);
```

### 4. Estimate the cost and request signatures

Creating an assignment starts the signing process and dispatches the notifications, so estimate
first when credit balance matters.

```java
CreateAssignmentRequest request = CreateAssignmentRequest.builder()
    .method("virtual")                                    // or "collect" for placed input fields
    .signers(List.of(SignerReference.ofId(signerId)))
    .message("Please sign")
    .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS)
            .truncatedTo(ChronoUnit.SECONDS).toString())
    .copyReceivers(List.of("watcher@example.invalid"))
    .build();

CostEstimate estimate = client.assignments().estimateCostTyped(documentId, request);
if (Boolean.TRUE.equals(estimate.getHasSufficientResources())) {
    Assignment assignment = client.assignments().create(documentId, request);
}
```

Per-signer delivery and ordering live on `SignerReference`. Verification methods are `Email`,
`Whatsapp`, or `DigitalCertificate`; notification methods are `Email` or `Whatsapp`. `step` controls
signing order: signers sharing a step sign in parallel, and the next step is notified only once the
previous one completes. If any signer supplies a step, all must, and the values must be contiguous
from 1. A `DigitalCertificate` signer must be alone in its step.

```java
SignerReference ordered = SignerReference.builder()
    .id(signerId)
    .verificationMethod("Whatsapp")
    .notificationMethods(List.of("Whatsapp"))
    .step(1)
    .build();
```

Managing a live assignment:

```java
Assignment extended = client.assignments().resetExpiration(
    documentId, assignmentId,
    Instant.now().plus(60, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS).toString());

CostEstimate resendCost = client.assignments().estimateResendCostTyped(documentId, assignmentId, signerId);
ResendNotificationResponse resent = client.assignments().resendNotification(documentId, assignmentId, signerId);

List<WhatsappNotification> delivery =
    client.assignments().getWhatsappNotificationsTyped(documentId, assignmentId);
```

`assignments().list(...)` maps `GET /assignments`, which resolves the workspace from an interactive
session. It is not reachable with API-key authentication; use a bearer token.

### 5. Track progress

```java
Document current = client.documents().details(documentId);
SigningProgress progress = client.documents().getSigningProgress(documentId); // signed/total/pending/%
boolean done = client.documents().isFullySigned(documentId);
List<DocumentActivity> log = client.documents().activities(documentId);
```

For continuous tracking, prefer [webhooks](#webhooks) over polling.

### 6. Download the artifacts

Artifact URLs are inline on the document, and the binary itself is one call away. `download`
defaults to the `certificated` artifact; the alternatives are `original`, `certificate-page`,
`pades` (digital-certificate processes), and `bundle` (a ZIP of the available finals).

```java
String thumbUrl = current.getArtifacts().getThumbnail();      // inline URL, no round-trip

byte[] certificated = client.documents().download(documentId);
byte[] original     = client.documents().download(documentId, "original");
byte[] thumbnail    = client.documents().thumbnail(documentId);
byte[] pageImage    = client.documents().downloadPage(documentId, pageId);
```

A download of an artifact that does not exist yet — the `certificated` file of an unsigned
document, for example — throws `ApiException` rather than returning the error body as file bytes.

Anyone holding the signature hash can verify a finished document without credentials:

```java
DocumentVerification verification = client.documents().verifyTyped(signatureHash);
```

### 7. Delete when finished

Deletion is allowed only in a deletable status; `documents().getStatuses()` returns the rules.

```java
client.documents().delete(documentId);
```

## The one-call workflow helper

`uploadAndRequestSignatures` composes steps 1 through 4: it uploads the PDF, waits for processing
(unless `waitForReady(false)`), resolves each signer, and creates a `virtual` assignment.

```java
UploadAndRequestSignaturesResult result = client.uploadAndRequestSignatures(
    UploadAndRequestSignaturesRequest.builder()
        .fileData(fileData)
        .fileName("contract.pdf")
        .signers(List.of(
            UploadAndRequestSignaturesRequest.SignerEntry.builder()
                .name("John Doe")
                .email("john@example.invalid")
                .build()
        ))
        .message("Please sign this contract")
        .waitForReady(true)
        .build()
);

result.getDocument();      // Document
result.getAssignment();    // Assignment
result.getSignerIds();     // List<String>, in request order
```

It has externally visible side effects — it creates signer records and dispatches notifications —
and blocks by default. Each entry needs a name plus an email or a WhatsApp number; email-bearing
entries must have unique addresses (case-insensitive) and WhatsApp-only entries unique numbers.
WhatsApp-only entries use `Whatsapp` verification and notification automatically. An entry whose
email matches an existing signer reuses that profile unchanged.

Rollback is deliberately conservative. On an ordinary post-upload failure the helper deletes the
uploaded document and every signer it created, attaching cleanup failures to the original exception
as suppressed exceptions. When a create response is indeterminate — a transport failure after the
server may have committed — it reconciles before rolling back, and if the outcome still cannot be
established it retains the resources rather than deleting a request that may already be live,
attaching the identifiers as suppressed diagnostic context. Signers that were reused or recovered
are never modified or deleted.

See [SDK convenience payloads](docs/API_REFERENCE.md#sdk-convenience-payloads) for every field of
the request, the result, and the other Java-only helper types.

## Signer self-service

These operations are what a signer's browser or app calls. They authenticate with the signer access
code from the invitation, which the SDK sends as the `signer-access-code` query parameter — pass it
per call rather than configuring it on the client.

```java
// Who am I, and what am I being asked to sign?
Signer me = client.signers().getSelf(signerAccessCode);
Document toSign = client.assignments().getForSignerTyped(signerAccessCode);

// Terms, identity confirmation, and the one-time code
client.signers().acceptTerms(signerAccessCode);
Signer confirmed = client.signers().confirmSignerData(documentId, signerAccessCode,
    Map.of("full_name", "Maria Silva", "email", "maria@example.invalid"));
client.signers().verifyEmail(signerAccessCode, "123456");

// Signature image. The documented upload contract is image/png; type and reuse are optional,
// and reuse sets the signer's is_signature_reusable flag.
client.signers().uploadSignature(signerAccessCode, "signature", pngBytes);
client.signers().uploadSignature(signerAccessCode, "signature", pngBytes, true);
byte[] stored = client.signers().downloadSignature(signerAccessCode, "signature");

// Sign, or decline with a reason
client.assignments().sign(documentId, assignmentId, signerAccessCode,
    List.of(Map.of("itemId", "i1", "fieldId", "f1", "pageId", "p1", "value", "text")));
client.assignments().decline(documentId, assignmentId, signerAccessCode, "Unfavorable terms");

// The signer's own queue
Document currentDoc = client.signers().getCurrentDocumentTyped(signerId, signerAccessCode);
PaginatedResult<Document> mine = client.signers().listDocuments(signerId, signerAccessCode);
PaginatedResult<Document> hits = client.signers().searchDocuments(signerId, signerAccessCode, "invoice");
byte[] pdf = client.signers().downloadDocument(signerId, documentId, "certificated", signerAccessCode);

// Bulk actions across several pending documents
client.signers().signMultiple(signerAccessCode, List.of(documentId1, documentId2));
client.signers().declineMultiple(signerAccessCode, List.of(documentId1), "Reason");
```

Two operations need no credential at all, which is what makes an embedded signing page possible:

```java
Document basic = client.publicDocuments().getBasicInfo(documentId);
client.publicDocuments().sendToken(documentId, "signer@example.invalid");  // sends { "email": ... }
```

## Templates and field definitions

A template is a prepared document with named roles and pre-placed fields. Creating a document from
one skips upload and processing entirely.

```java
PaginatedResult<Template> templates = client.templates().list();
Template template = client.templates().get(templateId);   // deployment extension; confirm support

CreateDocumentFromTemplateRequest request = CreateDocumentFromTemplateRequest.builder()
    .name("contract.pdf")
    .signers(List.of(TemplateSigner.builder().roleId("role-id").id(signerId).build()))
    .message("Please sign")
    .build();

CostEstimate cost = client.documents().estimateCostFromTemplateTyped(templateId, request);
Document fromTemplate = client.documents().createFromTemplate(templateId, request);
```

Template signers follow the same delivery and ordering rules as assignment signers, with one extra
restriction: a template signer may use only one notification method.

Field definitions describe the typed inputs a signer fills in during a `collect` assignment:

```java
FieldDefinition field = client.fields().create(
    CreateFieldRequest.builder().type("text").name("Address").isRequired(true).build()
);

PaginatedResult<FieldDefinition> fields = client.fields().list(
    ListParams.builder().includeInactive(true).includeStandard(true).build()
);
FieldDefinition one = client.fields().get(fieldId);
client.fields().update(fieldId, UpdateFieldRequest.builder().isRequired(false).build());
client.fields().delete(fieldId);

List<FieldType> types = client.fields().listTypes();

// Validate before submitting. Omit the access code when calling as an authenticated user.
FieldValidationResult result = client.fields().validate(fieldId, "400.676.228-36", null);
List<FieldValidationResult> bulk = client.fields().validateMultiple(
    List.of(Map.of("field_id", fieldId, "value", "12345")), null);
```

## Tags

Tags are workspace-level labels that can be attached to documents.

```java
Tag tag = client.tags().create(CreateTagRequest.builder().name("Contracts").color("FF0000").build());
PaginatedResult<Tag> tags = client.tags().list();
client.tags().rename(tag.getId(), RenameTagRequest.builder().name("2026 Contracts").build());
client.tags().rename(tag.getId(), RenameTagRequest.builder().clearColor().build());  // sends color:null
client.tags().delete(tag.getId());          // 409 while the tag is still attached…
client.tags().delete(tag.getId(), true);    // …force detaches, then deletes
```

Attaching to a document comes in two forms. The ID-based methods resolve workspace tag IDs before
changing the document; the name-based methods send names, and the API creates any name it does not
already know.

```java
List<Tag> attached = client.documents().appendTagIds(documentId, List.of(tag.getId()));
client.documents().replaceTagIds(documentId, List.of(tag.getId()));
client.documents().appendTags(documentId, List.of("2026 Contracts"));
List<Tag> docTags = client.documents().listTags(documentId);
client.documents().detachTag(documentId, attached.getFirst().getId());
```

## Workspaces and branding

Workspace operations always take the account ID explicitly.

```java
Workspace workspace = client.workspaces().create(
    CreateWorkspaceRequest.builder().name("My Workspace")
        .notificationSenderType("Account")     // "User" (default) or "Account"
        .build()
);

PaginatedResult<Workspace> workspaces = client.workspaces().list();
Workspace one = client.workspaces().get(accountId);
client.workspaces().update(accountId, UpdateWorkspaceRequest.builder().name("New Name").build());

client.workspaces().delete(accountId);        // 400 listing blockers if a paid subscription is active
client.workspaces().delete(accountId, true);  // cancels the subscription, then deletes

AccountTheme theme = client.workspaces().getTheme(accountId);
byte[] logo = client.workspaces().downloadLogo(accountId);      // ApiException(404) when unset
client.workspaces().uploadLogo(accountId, pngBytes, "logo.png"); // content type auto-detected
client.workspaces().deleteLogo(accountId);

List<DocumentStatsRow> monthly = client.workspaces().stats(accountId);
List<DocumentStatsRow> daily = client.workspaces().stats(accountId, "daily", "2026-08");
```

## Users, sessions, and API keys

```java
AuthSession session = client.authentication().login("user@example.invalid", "password");
client.authentication().changePassword("user@example.invalid", "old", "new");
client.authentication().requestPasswordReset("user@example.invalid");
client.authentication().resetPassword("user@example.invalid", "token", "new");

AuthUser user = client.users().get();
List<DocumentStatsRow> crossAccount = client.users().stats();   // every accessible workspace
```

Notification preferences use the exact case-sensitive codes the API publishes. Updates merge, so
omitted switches keep their current values, and the response always returns all nine.

```java
NotificationPreferences preferences = client.users().getNotificationPreferences();
NotificationPreferences updated = client.users().updateNotificationPreferences(Map.of(
    "DocumentCompleted", true,
    "SignerWhatsappFailed", false
));
```

An API key is shown in full only once. Store it securely and never expose it to a frontend.

```java
ApiKey current = client.apiKeys().get();              // masked, or null when none exists
ApiKey rotated = client.apiKeys().create("password"); // full key; invalidates the previous one
client.apiKeys().delete();
```

## Webhooks

Register one subscription per workspace, then receive deliveries at your endpoint.

```java
WebhookSubscription sub = client.webhooks().register(
    RegisterWebhookRequest.builder()
        .url("https://example.invalid/webhook")
        .email("admin@example.invalid")
        .events(List.of("document_ready", "signer_signed_document"))
        .build()
);

List<WebhookEventTypeInfo> types = client.webhooks().listEventTypes();
WebhookSubscription active = client.webhooks().get();   // null when none is registered
client.webhooks().inactivate();                         // stop delivery
```

When `events` is null or empty the SDK subscribes to `document_ready`, `document_prepared`,
`signer_signed_document`, `signer_rejected_document`, and `document_processing_failed`; `isActive`
defaults to `true`. Pass explicit values to override either default.

Deliveries are recorded and can be inspected or replayed:

```java
PaginatedResult<WebhookDispatch> dispatches = client.webhooks().listDispatches(
    ListParams.builder().page(1).perPage(20).build()
);
client.webhooks().retryDispatch(dispatchId);
```

On the receiving side, deserialize the delivery body into `WebhookPayload` with your own Jackson
mapper. The SDK models the envelope but does not parse it for you, because a webhook arrives at
your HTTP endpoint rather than through the SDK's transport:

```java
ObjectMapper mapper = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

WebhookPayload event = mapper.readValue(requestBody, WebhookPayload.class);
String eventType = event.getEvent();          // e.g. "signer_signed_document"
Map<String, Object> subject = event.getSubject();  // who acted
Map<String, Object> object = event.getObject();    // the affected entity
```

> **Authenticate deliveries at a trusted network boundary.** Assinafy publishes no webhook signature
> header, no signing scheme, and no place on the subscription to register a shared secret, so there
> is nothing in the delivery for a client library to verify. Restrict your endpoint to the sender's
> network, put it behind a gateway that authenticates the caller, or use an unguessable endpoint
> path — and treat the payload as a notification to act on, not as trusted data. Re-read the
> affected entity through the API (`documents().details(id)`) before acting on anything that
> matters.

## Error handling

Every failure is an unchecked `AssinafyException` or a subtype, so one catch can cover the SDK while
more specific handlers react to particular conditions.

| Exception                 | Raised when |
|---------------------------|-------------|
| `ValidationException`     | Local argument validation failed before any request was sent. |
| `AuthenticationException` | HTTP 401 or 403. Subtype of `ApiException`. |
| `RateLimitException`      | HTTP 429. Subtype of `ApiException`; back off and retry. |
| `ApiException`            | Any other non-2xx status, or a non-2xx status inside a 200 envelope. |
| `NetworkException`        | Transport or I/O failure, including an interrupted poll. |
| `AssinafyException`       | Base type: serialization, decoding, and everything above. |

```java
try {
    client.documents().upload(fileData, "document.pdf");
} catch (ValidationException e) {
    System.err.println("Invalid input: " + e.getMessage() + " " + e.getContext());
} catch (AuthenticationException e) {
    System.err.println("Auth error " + e.getStatusCode() + ": " + e.getMessage());
} catch (RateLimitException e) {
    System.err.println("Rate limited: " + e.getResponseHeader("retry-after"));
} catch (ApiException e) {
    System.err.println("API error " + e.getStatusCode() + ": " + e.getMessage());
    System.err.println("Response data: " + e.getResponseData());
} catch (NetworkException e) {
    System.err.println("Network error: " + e.getMessage());
} catch (AssinafyException e) {
    System.err.println("SDK error: " + e.getMessage() + " " + e.getContext());
}
```

`ApiException` exposes the decoded body through `getResponseData()` and the response headers through
`getResponseHeaders()` / `getResponseHeader(name)`, both as immutable snapshots with case-insensitive
header lookup. `getContext()` on any `AssinafyException` carries structured diagnostic fields.

The SDK does not retry automatically; retry policy belongs to the caller, who knows whether an
operation is safe to repeat.

## Pagination

`ListParams` builds the query for every list endpoint. `page` must be at least 1 and `perPage` must
be between 1 and 100; both are validated locally.

```java
ListParams params = ListParams.builder()
    .page(1)
    .perPage(25)
    .search("contract")
    .sort("updated_at")
    .status("pending_signature")
    .extra("custom", "value")       // anything the endpoint accepts that has no typed setter
    .build();

PaginatedResult<Document> result = client.documents().list(params);
List<Document> data = result.getData();
PaginationMeta meta = result.getMeta();   // null when the response exposes no pagination headers
```

`PaginationMeta` comes from response headers:

| Response header             | Getter             | Meaning |
|-----------------------------|--------------------|---------|
| `X-Pagination-Current-Page` | `getCurrentPage()` | Current page number. |
| `X-Pagination-Page-Count`   | `getLastPage()`    | Total number of pages. |
| `X-Pagination-Per-Page`     | `getPerPage()`     | Requested page size. |
| `X-Pagination-Total-Count`  | `getTotal()`       | Total matching items across all pages, not the current page size. |

## Request and response payloads

The wire shapes below cover the core operations. The
[complete Java API reference](docs/API_REFERENCE.md) documents every operation and every field.

**Upload document** — `documents().upload(...)` → `Document` (multipart `file`, `name`, and
optional `metadata` parts):

```json
{ "data": {
  "resource": "document", "id": "doc_123", "account_id": "account_123", "template_id": null,
  "name": "contract.pdf", "status": "uploaded",
  "artifacts": { "original": "https://example.invalid/download/original" },
  "is_closed": false, "signing_url": "https://example.invalid/sign/doc_123",
  "decline_reason": null, "declined_by": null, "tags": [],
  "created_at": "2026-08-27T19:03:38Z", "updated_at": "2026-08-27T19:03:38Z", "pages": []
}, "status": 200, "message": "" }
```

Once processing finishes (`status: "metadata_ready"`), the same shape also carries a `thumbnail`
artifact and a populated `pages` array of `{ id, number, height, width, download_url }`.

**Create signer** — `signers().create(...)` → `Signer`:

```jsonc
// request
{ "full_name": "John Doe", "email": "john@example.invalid", "whatsapp_phone_number": "+5548999990000" }
// response data
{ "resource": "signer", "id": "signer_123", "full_name": "John Doe", "email": "john@example.invalid",
  "whatsapp_phone_number": "+5548999990000", "has_accepted_terms": false }
```

**Create assignment** — `assignments().create(documentId, ...)` → `Assignment`:

```jsonc
// request
{ "method": "virtual", "message": "Please sign",
  "signers": [ { "id": "signer_123", "verification_method": "Email", "notification_methods": ["Email"], "step": 1 } ] }
// response data (abridged)
{ "resource": "assignment", "id": "assignment_123", "sender_email": "sender@example.invalid", "method": "virtual",
  "expires_at": null, "message": "Please sign",
  "signers": [ { "id": "signer_123", "full_name": "John Doe", "email": "john@example.invalid",
                 "verification_method": "Email", "notification_methods": ["Email"], "step": 1,
                 "notified": true, "completed": false, "notification_history": [] } ],
  "items": [ { "id": "item_123", "page": null, "signer": { … }, "field": { … }, "value": null, "completed": false } ],
  "summary": { "signer_count": 1, "completed_count": 0, "signers": [ … ] },
  "signing_urls": [ { "signer_id": "signer_123", "url": "https://example.invalid/sign/document_123" } ] }
```

**Estimate cost** — the map-returning methods return `Map<String,Object>`; each has a
`...Typed(...)` counterpart returning `CostEstimate`:

```json
{ "documents": 1, "credits": 0, "needs_extra_document": false, "extra_document_cost": 0,
  "total_credits": 0, "breakdown": [], "document_balance": 87, "credit_balance": 0,
  "has_sufficient_resources": true, "blocking_reason": null, "message": null }
```

**Field definition** — `fields().create/get/update(...)` → `FieldDefinition`:

```json
{ "resource": "field_definition", "id": "field_123", "name": "Address", "type": "text", "regex": null,
  "is_pre_defined": false, "is_active": true, "is_required": true, "is_standard": false,
  "is_read_only": false, "is_visible": true }
```

**Tag** — `tags().create(...)` → `Tag` · **Account theme** — `workspaces().getTheme(...)` →
`AccountTheme`:

```jsonc
// Tag
{ "resource": "tag", "id": "tag_123", "name": "Contracts", "color": "ff8800",
  "created_at": "2026-05-14T12:00:00Z", "updated_at": "2026-05-14T12:00:00Z" }
// AccountTheme
{ "account_name": "Acme", "primary_color": "2072b9", "secondary_color": "ffffff", "logo": null }
```

**Webhook subscription** — `webhooks().get()` / `register(...)` → `WebhookSubscription`:

```json
{ "events": ["document_ready","signer_signed_document"], "is_active": true,
  "url": "https://example.invalid/webhook", "email": "admin@example.invalid",
  "updated_at": "2026-08-27T02:36:02Z" }
```

**Notification preferences** — `users().updateNotificationPreferences(...)` →
`NotificationPreferences`:

```jsonc
// request: only the switches you want to change
{ "DocumentCompleted": true, "SignerWhatsappFailed": false }
// response data: always all nine
{ "DocumentCompleted": true, "SignerDeclined": true, "DocumentCancelled": true,
  "DocumentAboutToExpire": true, "DocumentExpired": true, "DocumentExpirationReset": true,
  "DocumentProcessingFailed": true, "TemplateProcessingFailed": true, "SignerWhatsappFailed": false }
```

## Development

```bash
# Unit tests, no live API calls
./mvnw test

# Full build: tests, Javadoc, jar, sources, and Javadoc jars
./mvnw clean verify

# The same verification offline, after dependencies and plugins are cached
./mvnw -o verify
```

The unit suite makes no live Assinafy calls and includes wire-level HTTP tests backed by
MockWebServer. Compilation runs with `-Xlint:all -Werror`, and Javadoc with `doclint:all` and
`failOnWarnings`, so a warning fails the build.

The opt-in `LiveApiSmokeIT` suite performs real sandbox reads and writes. Inject its credentials
from your shell or CI secret store, then run:

```bash
# ASSINAFY_API_KEY and ASSINAFY_ACCOUNT_ID must already be set from a secret store.
export ASSINAFY_API_KEY
export ASSINAFY_ACCOUNT_ID
export ASSINAFY_BASE_URL=https://sandbox.assinafy.com.br/v1
./mvnw -Plive-api verify
```

The live profile rejects any base URL other than the exact sandbox URL. GitHub Actions runs it
weekly and on manual dispatch through the protected `sandbox` environment. Only `ASSINAFY_API_KEY`
and `ASSINAFY_ACCOUNT_ID` are required. The assignment-notification case additionally needs
`ASSINAFY_TEST_EMAIL_PRIMARY` and `ASSINAFY_TEST_EMAIL_SECONDARY`, and the password-reset case needs
`ASSINAFY_TEST_EMAIL_PRIMARY`; leave them unset to skip those cases, or point them at controlled
sandbox recipients, because the cases send real messages. Writes use unique fixture names,
reverse-order cleanup, and retries for transient cleanup failures. Inspect the sandbox account after
an interrupted run, since a process termination or service outage can still prevent cleanup.

## Releasing

Versions follow the minor line: breaking changes are documented in
[`CHANGELOG.md`](CHANGELOG.md) rather than signalled by a major bump.

1. Update the version in `pom.xml` and the dependency snippet in this README.
2. Add the release section to `CHANGELOG.md`.
3. Run `./mvnw clean verify`.
4. Tag the release `v<version>`; the tag must match the project version or the release job fails.

Pushing the tag runs the full verification, publishes the jar, sources, and Javadoc to GitHub
Packages, attests build provenance, and creates the GitHub release with the artifacts attached.

## License

MIT — see [`LICENSE`](LICENSE).
