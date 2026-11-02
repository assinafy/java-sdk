# Assinafy Java SDK

*[Leia em português](README.md) · English*

A thread-safe Java client for all 118 [Assinafy API](https://api.assinafy.com.br/v1/docs) operations,
including documents, signatures, templates, contracts, webhooks, OAuth 2.1, and signer-facing flows.
The [complete contract](docs/API_REFERENCE.md) documents every route, payload, response, and status.

## Requirements and installation

- JDK 25 LTS (`>=25,<26`) and TLS 1.2 or later.
- Maven Wrapper 3.10.0 to build the project.
- OkHttp and Jackson as runtime dependencies.

```xml
<dependency>
  <groupId>com.assinafy</groupId>
  <artifactId>assinafy-sdk</artifactId>
  <version>2.3.0</version>
</dependency>
```

Gradle: `implementation("com.assinafy:assinafy-sdk:2.3.0")`.

## Complete signing flow

Use an API key to act on your own workspace. Keep credentials in environment variables and share one
`AssinafyClient` instance across the application.

```java
import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.models.Document;
import com.assinafy.sdk.models.UploadAndRequestSignaturesResult;
import com.assinafy.sdk.request.UploadAndRequestSignaturesRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

AssinafyClient client = AssinafyClient.create(
    System.getenv("ASSINAFY_API_KEY"),
    System.getenv("ASSINAFY_ACCOUNT_ID")
);

UploadAndRequestSignaturesResult result = client.uploadAndRequestSignatures(
    UploadAndRequestSignaturesRequest.builder()
        .fileData(Files.readAllBytes(Path.of("contract.pdf")))
        .fileName("contract.pdf")
        .signers(List.of(
            UploadAndRequestSignaturesRequest.SignerEntry.builder()
                .name("Example Signer")
                .email("signer@example.invalid")
                .build()
        ))
        .message("Please sign this document")
        .expiresAt(OffsetDateTime.now().plusDays(7).toString())
        .build()
);

String documentId = result.getDocument().getId();
String assignmentId = result.getAssignment().getId();

// After the certification webhook:
Document finalDocument = client.documents().details(documentId);
if ("certificated".equals(finalDocument.getStatus())) {
    Files.write(Path.of("signed-contract.pdf"), client.documents().download(documentId));
    Files.write(Path.of("certificate.pdf"),
        client.documents().download(documentId, "certificate-page"));
}
```

The helper validates before upload, sends the PDF, waits for metadata, reuses or creates signers, and
creates a `virtual` assignment. After a later failure it attempts to remove only resources whose
creation was confirmed. Create a `collect` assignment directly for positioned fields. Always:

1. Persist IDs and estimate cost with `assignments().estimateCostTyped(...)`.
2. Treat initial processing (`metadata_ready`) and final certification (`certificated`) as separate stages.
3. Consume webhooks idempotently and wait for `certificated` before downloading final artifacts.
4. Store `original`, `certificated`, `certificate-page`, `pades`, or `bundle` before deleting the document.

Deadlines accept an ISO 8601 date or date-time at least one hour ahead. A date ends at 23:59 UTC; a
date-time without an offset is UTC; an explicit offset is preserved.

## Verification and notifications

| Verification | Requirement | Notification |
|---|---|---|
| `Email` | Valid email | zero or `Email` |
| `Whatsapp` | E.164 number, such as `+15555550123` | zero or `Whatsapp` |
| `DigitalCertificate` | Enabled feature, CPF/CNPJ, and `virtual` assignment | zero, `Email`, or `Whatsapp` |
| `IdDocumentFaceMatchBasic` | Enabled feature, CPF, and explicit biometric consent | zero, `Email`, or `Whatsapp` |

Each signer accepts at most one channel. `step` defines a contiguous order starting at 1; signers in
the same step sign in parallel. Cost estimation accepts an explicitly empty notification list.

Biometrics require consent before `startIdentityVerification(accessCode, true)`. Do not log, store in
browser storage, or alter the capture URL fragment. Evidence exists only during review and must not
be cached.

ICP-Brasil A1/A3 signing uses `startCertificateSignature` and `completeCertificateSignature` with Web
PKI. Allow one certificate signer per step; the ordinary signing endpoint does not complete this
method. Confirm `certificated` before download. Complete flows and payloads are in the
[reference](docs/API_REFERENCE.md#deployed-extensions).

## OAuth 2.1 for marketplace applications

Use Authorization Code + PKCE with an HTTPS redirect. `oauth().createAuthorizationUrl(...)` returns
the URL, `state`, PKCE verifier, issuer, and `nonce`; keep the entire object server-side. At the
callback, `readAuthorizationCallback(...)` validates parameters and returns the code for
`exchangeCode(...)`.

Access tokens last one hour. Every refresh rotates the refresh token and begins a fresh 30 days:
serialize refreshes per connection and save the new token set atomically. Never retry a refresh whose
result is uncertain; reconnect instead. Revoke the latest token, and never log tokens, PKCE, `state`,
`nonce`, consent URLs, or signing URLs. `offline_access` is not returned in `scope`.

## Webhooks

Create endpoints with a public HTTPS URL. Accounts have one endpoint or up to three on paid plans.
Enable `signing_enabled` and immediately store `WebhookEndpoint.getSecret()` when creation or an
update returns it. OAuth cannot read the secret but can rotate it with `webhooks:write`; rotation
invalidates the previous secret immediately.

```java
byte[] rawBody = requestBodyBytes;
if (!com.assinafy.sdk.util.WebhookSignature.verify(secret, requestHeaders, rawBody)) {
    throw new SecurityException("Invalid webhook signature");
}
```

Verify the raw body before deserializing, deduplicate by `webhook-id`, check `account_id`, and process
asynchronously. Return 2xx quickly: the API makes up to six attempts about ten minutes apart and holds
an endpoint after ten consecutive undelivered events.

## Surfaces and errors

The client exposes `documents()`, `signers()`, `assignments()`, `contracts()`, `templates()`,
`fields()`, `tags()`, `webhooks()`, `workspaces()`, `users()`, `apiKeys()`, `authentication()`,
`oauth()`, and `publicDocuments()`. Account operations use the configured `accountId` and offer an
explicit-account overload.

Success methods return envelope `data`; OAuth returns flat JSON and downloads return `byte[]`.
Invalid input raises `ValidationException` before the network. Remote failures use
`AuthenticationException`, `RateLimitException`, `ApiException`, `OAuthException`, or
`NetworkException`. See the [operation reference](docs/API_REFERENCE.md) for every documented
request/response field and status code.

## Development

```bash
export JAVA_HOME=/usr/local/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home
./mvnw clean verify
```

Live tests are opt-in: `./mvnw -Plive-api verify` with sandbox `ASSINAFY_*` variables.
License: [MIT](LICENSE).
