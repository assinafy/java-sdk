# Changelog

All notable changes to `com.assinafy:assinafy-sdk` will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.7.0] - 2026-08-27

### Removed

- **`WebhookVerifier`**, `AssinafyClient.webhookVerifier()`, and
  `AssinafyClientOptions.webhookSecret`. Assinafy publishes no webhook signature header, no signing
  scheme, and nowhere on the subscription to register a shared secret, so there is nothing in a
  delivery for a client library to verify. The class implemented a conventional
  HMAC-SHA256-over-raw-body check against a secret the platform never issues, which invited callers
  to treat a `false` result as evidence of forgery when it only ever meant "no secret configured".
  Authenticate deliveries at a trusted network boundary instead, and re-read the affected entity
  through the API before acting on it.

  Migration: deserialize the delivery body into `WebhookPayload` with your own Jackson mapper
  (`FAIL_ON_UNKNOWN_PROPERTIES=false`), then read `getEvent()`, `getSubject()`, and `getObject()`
  directly. `WebhookPayload` is unchanged.

## [1.6.0] - 2026-08-27

Supersedes the unreleased 1.5.2 source; every change below is relative to the published 1.5.1
artifact. One Java type now models each API resource, signer/assignment placement rules are
single-sourced, request fields the API never accepted are gone, and the release pipeline publishes
signed build provenance. 299 unit tests pass and the opt-in `LiveApiSmokeIT` runs 25 flows.

### Removed

- **`DocumentListItem`, `DocumentUploadResponse`, and `DocumentDetails` are replaced by a single
  `Document`.** The API returns one document schema from upload, list, search, get, rename, and
  create-from-template, so the SDK now returns one type from all of them. `Document` carries the
  union of the previous three field sets; a field a given response does not populate is `null`.
  Migration: replace all three type names with `Document`. `Document.getAssignment()` is typed
  `Assignment` everywhere, so `DocumentUploadResponse.getAssignmentDetails()` is gone — call
  `getAssignment()`.
- **`WorkspaceListItem` is replaced by `Workspace`; `TemplateListItem` is replaced by `Template`.**
  Each pair carried identical fields. `workspaces().list()` now returns `PaginatedResult<Workspace>`
  and `templates().list()` returns `PaginatedResult<Template>`.
- **`DocumentResource.confirmSignerData(...)`**, deprecated in 1.5.1, is removed. Use
  `signers().confirmSignerData(...)`, which returns the server-normalized `Signer`.
- **`CreateSignerRequest.metadata` and `UploadAndRequestSignaturesRequest.SignerEntry.metadata`.**
  Signer creation has no metadata field, so these values were accepted and then dropped. Removing
  them turns a silent no-op into a compile error.
- **`Signer.metadata`.** The signer payload has no metadata field, so the accessor always returned
  `null`.
- **`AssignmentSigner.getNotificationHistoryEntries()` / `setNotificationHistoryEntries(...)`.**
  `getNotificationHistory()` is now typed `List<NotificationHistoryEntry>` directly, so the
  converting accessors have no purpose. Migration: rename `getNotificationHistoryEntries()` to
  `getNotificationHistory()`.

### Changed

- **`signers().create(...)` always issues the create request.** In 1.5.1 it silently returned an
  existing signer when the email matched. Use `signers().findOrCreate(...)` for the reusing
  behavior; it returns an exact case-insensitive email match unchanged.
- **`publicDocuments().getBasicInfo(...)`** returns `Document` instead of `Map<String,Object>`.
- **`users().get()`** accepts both the `{user, accounts}` and the flat user response shapes.
- Statistics field names follow the API: `signature_requests_notification_email`,
  `..._notification_whatsapp`, `..._notification_bypass`, `..._verification_email`,
  `..._verification_whatsapp`, `..._verification_bypass`, and
  `..._verification_digital_certificate`, plus `signature_requests_viewed`. The former
  `getSignatureRequestsEmail()` and `getSignatureRequestsWhatsapp()` remain as deprecated aliases.
- A supplied `CreateSignerRequest.cpf` is persisted through the signer `government_id` update after
  creation; if that update fails, the new signer is deleted.
- `uploadAndRequestSignatures(...)` reconciles an indeterminate assignment or signer create response
  before rolling back, and retains resources whose outcome cannot be established rather than
  deleting a request that may already have been dispatched.

### Added

- **`documents().appendTagIds(...)` and `replaceTagIds(...)`** resolve workspace tag IDs before
  changing a document. `appendTags(...)` and `replaceTags(...)` continue to take tag names.
- **`signers().findOrCreate(...)`** — reuse-by-email creation, split out of `create`.
- **`authentication().changePasswordResult(...)`, `requestPasswordResetResult(...)`, and
  `resetPasswordResult(...)`** return the response payload; the `void` forms remain.
- `AssignmentMethod` is the source of the accepted `method` vocabulary, so the enum and the
  validation cannot drift apart.

### Fixed

- Signer-facing assignment calls (`sign`, `decline`, `getForSigner`) build the `signer-access-code`
  query through the same helper as every other signer route, so encoding is identical everywhere.
- Assignment and template signer placement share one implementation of the delivery-method
  vocabulary and the signing-order rules (all-or-nothing steps, contiguous from 1, a
  `DigitalCertificate` signer alone in its step), removing two divergent copies.

### Build and documentation

- Releases publish to GitHub Packages with `actions/attest-build-provenance` and create a GitHub
  release carrying the jars, so every published artifact has verifiable provenance.
- The release job runs only after the verification job succeeds; pull-request runs supersede each
  other while pushes, tags, and the scheduled sandbox run always finish.
- `README.md` is reorganized as a single read-through guide, and `docs/API_REFERENCE.md` documents
  every operation with its full request and response payload.

## [1.5.1] - 2026-08-20

### Build and documentation

- Tagged releases publish the jar, sources, and Javadoc to GitHub Packages. `README.md` documents
  the `~/.m2/settings.xml` credentials and the repository entry consumers need.
- The release job verifies that the pushed tag matches the project version before deploying.

## [1.5.0] - 2026-07-18

Fixes three non-functional signer/public endpoints, fills the remaining documented endpoint gaps,
and adds request/response payload documentation. 195 unit tests pass; the opt-in `LiveApiSmokeIT`
runs 18 flows.

### Fixed

- **`SignerResource.acceptTerms`** put the access code in the JSON body and PUT to a bare path, so
  the endpoint (which authenticates via the `signer-access-code` **query** parameter) rejected the
  call with 400 "access-code parameter missing". It now sends the code as the query parameter with
  no body. Return type changed from `Signer` to `void` (the 200 response carries no `data`).
- **`SignerResource.verifyEmail`** likewise put the access code in the body. It now sends
  `signer-access-code` as the query parameter and only `verification-code` in the body.
- **`PublicDocumentResource.sendToken`** sent `{recipient, channel}`; the documented body is a
  single `email` field. New signature `sendToken(documentId, email)`; the old
  `sendToken(documentId, recipient, channel)` is retained as `@Deprecated` (delegates, channel
  ignored).
- **`SignerResource.create`** duplicate-email recovery only caught HTTP 409, but the live API
  returns **400** for a duplicate email. The fallback now re-queries by email on any 4xx and
  returns the existing signer, keeping `create()` idempotent.
- **`DocumentResource.download(id, artifact)`** now URL-encodes the artifact name (consistent with
  the signer-scoped download).
- `FieldResource.validate`/`validateMultiple` now reuse the shared `withAccessCode` helper.

### Added

- **`DocumentResource.rename(documentId, name)`** — `PATCH /documents/{id}` (rename).
- **`DocumentResource.search(ListParams)`** — `GET /accounts/{id}/documents/search` (lightweight
  compact search).
- **`SignerResource.searchDocuments(signerId, accessCode, search)`** —
  `GET /signers/{signerId}/documents/search`.
- **`AssignmentResource.list(ListParams)`** — `GET /assignments`. Documented caveat: the endpoint
  resolves the account from an interactive session and is **not** available to API-key clients
  (returns 400 "account context required"); use with a session/Bearer token.
- **`WorkspaceResource.getTheme` / `downloadLogo` / `uploadLogo` / `deleteLogo`** — the Accounts
  `theme` and `logo` operations, plus a new **`AccountTheme`** model.
- **`WorkspaceResource.delete(accountId, force)`** — sends the documented `{"force": true}` body to
  cancel a blocking paid subscription and delete immediately.
- **`SignerResource.uploadSignature(code, type, image, reuse)`** — the documented `reuse` query
  parameter (sets `is_signature_reusable`); `type` is now optional per the docs.
- **`notification_sender_type`** on `CreateWorkspaceRequest`/`UpdateWorkspaceRequest` and the
  `Workspace` model (plus `roles`, `is_delete_allowed`); **`is_signature_reusable`** on `Signer`.
- Transport: `patch`, `delete(path, body)` and `postFile` (single-file multipart) on
  `ApiHttpClient`/`OkHttpApiClient`.

### Changed

- **`SignerResource.confirmSignerData`** now returns the server-normalised `Signer` (was `void`);
  its Javadoc lists the documented body fields (`full_name`, `email`, `government_id`).
- **`PublicDocumentResource.getBasicInfo`** now returns the typed `DocumentDetails` (was
  `Map<String,Object>`) — the payload is the same document shape as `documents().details()`.
- `AssinafyClient.uploadAndRequestSignatures`, `WebhookResource.register`, the `AssignmentResource`
  and `FieldResource` CRUD methods, and the new methods above all gained Javadoc conveying their
  route, defaults and request/response contract.

### Behavior that intentionally differs from the written docs

Live behavior is authoritative in these places; the SDK follows it:

- **Document tag attach/replace take tag _names_ (auto-created), not IDs.** The API reference labels
  the `tags` array "Tag IDs", but attaching by name links the existing tag while an ID string
  creates a tag named after the ID. `appendTags`/`replaceTags` correctly send names; Javadoc now
  notes the discrepancy.
- **Field create/update accept more than the docs list:** `is_active` is honored on create, and
  `type`/`is_required` are mutable on update. The DTO fields were kept.
- **Assignment cost estimation requires at least one signer** (server-side) even for `collect`.
- **Deleting a tag still attached to a document returns 409** unless `force=true` — the Javadoc
  claim is accurate.

### Build and documentation

- Bumped `actions/checkout` to `v7` (`setup-java@v5`, `upload-artifact@v7` already current); build
  targets Java 25 (current LTS).
- README: corrected the changed method examples, documented the new endpoints, and added a
  **Request / Response Payloads** section with real wire shapes for the core operations.

## [1.4.1] - 2026-06-05

A non-breaking release focused on correctness, robustness, and test coverage.
175 unit tests pass, now including wire-level `OkHttpApiClient` tests backed by
MockWebServer; the opt-in `LiveApiSmokeIT` runs 16 flows and also exercises
binary downloads.

### Fixed

- **Binary downloads no longer return the error body as file bytes.**
  `OkHttpApiClient.getBinary` ignored the HTTP status, so a 4xx/5xx (e.g.
  downloading the `certificated` artifact of an unsigned document, or a missing
  document) handed the JSON error envelope back as if it were the PDF/JPEG. It now
  throws `ApiException` (with the server message) on any non-2xx, so
  `documents().download/thumbnail/downloadPage` and
  `signers().downloadSignature/downloadDocument` fail loudly instead of producing a
  corrupt file. Verified against the live sandbox.
- **`ResponseHandler.handleVoid`** now also honors an in-body error envelope
  (`{status,…}`) on an HTTP 200, consistent with the typed/list/map handlers.
- **`WebhookVerifier.verify(String, String)`** returns `false` (fail-closed) on a
  `null` payload instead of throwing a `NullPointerException`.
- **`BaseResource.serialise`** failures now throw `AssinafyException` instead of a
  raw `RuntimeException`, so every SDK error is catchable as `AssinafyException`.

### Added

- **`AuthenticationException`** (401/403) and **`RateLimitException`** (429),
  subtypes of `ApiException` — `ApiException.fromResponse` returns the most specific
  type so callers can react to auth/rate-limit without switching on the status code.
  Existing `catch (ApiException)` handlers are unaffected.
- **`DocumentArtifacts.thumbnail`** — the inline thumbnail URL the API returns on
  every document (previously silently dropped); reachable via
  `getArtifacts().getThumbnail()` with no extra round-trip.
- **`RenameTagRequest.builder().clearColor()`** — sends an explicit `"color": null`
  so a tag's colour can be cleared (the documented tri-state); a plain builder still
  leaves the colour unchanged.
- **`WebhookPayload.getPayloadRaw()`** — the raw payload exactly as delivered
  (object, array, or null).
- `WebhookDispatch.resource` field.

### Changed / hardened

- The response `ObjectMapper` now enables `ACCEPT_EMPTY_STRING_AS_NULL_OBJECT`, so a
  typed-object field returned as `""` (e.g. an activity `origin`) coerces to `null`
  rather than failing the whole parse.
- **`WebhookPayload.payload`** is now stored untyped internally so an empty-array
  payload (`[]`, delivered by some event types) no longer breaks `extractEvent`.
  `getPayload()` still returns `Map<String,Object>` (object-shaped, else `null`) —
  no source change for callers.
- `WebhookVerifier` Javadoc and the README webhook section now state plainly that the
  platform does not document a signature scheme, and that `verify() == false` does
  not by itself indicate forgery.
- `FieldResource.create` validates the documented-required `type`/`name` client-side
  (consistent with `TagResource`/`WebhookResource`/`SignerResource`).
- Internal DRY/KISS cleanups with no behavioural change: a shared
  `withAccessCode`/`execute`/`toMap` helper in `BaseResource`, single-source base-URL
  normalisation, and the `certificated` literal sourced from the existing enums.

### Documentation

- Fixed two non-compiling README examples (`ResendEmailResponse` →
  `ResendNotificationResponse`; `new CreateDocumentFromTemplateRequest(...)` → the
  builder form) and documented previously-undocumented methods (`activities`,
  `verify`, `isFullySigned`, `getSigningProgress`, `waitUntilReady`, `downloadPage`,
  `estimateCostFromTemplate`, `estimateResendCost`).
- Added Javadoc to the public `DocumentResource` methods; deprecated the
  never-populated `DocumentDetails.download_url`/`download_final_url` (use
  `getArtifacts()`); documented that signer `cpf`/`metadata` are sent for sibling-SDK
  parity but not persisted/returned by the current API.

## [1.4.0] - 2026-05-27

Adds the missing Tag and document-tag surfaces and API-key management, fixes
several response-parsing bugs, and tightens model conformance. 111 unit tests
pass and the opt-in `LiveApiSmokeIT` exercises 16 read/write flows.

This release contains source-breaking changes (corrected public types). See
**Changed** below for migration notes.

### Added

- **`TagResource`** (`client.tags()`): `list`, `create`, `rename`, `delete`
  (with a `force` overload) for workspace tags — `GET/POST /accounts/{id}/tags`,
  `PUT/DELETE /accounts/{id}/tags/{tagId}`. New `Tag` model and `CreateTagRequest`
  / `RenameTagRequest`.
- **Document tags** on `DocumentResource`: `listTags`, `replaceTags`,
  `appendTags`, `detachTag` — `GET/PUT/POST /accounts/{id}/documents/{docId}/tags`
  and `DELETE .../tags/{tagId}`.
- **`ApiKeyResource`** (`client.apiKeys()`): `get` (masked), `create(password)`
  (full key, one-time), `delete` — `/users/api-keys`. New `ApiKey` model.
- **`AssignmentResource`** signer-flow endpoints: `getForSigner(accessCode)`
  (`GET /sign`) and `sign(documentId, assignmentId, accessCode, items)`
  (`POST /documents/{id}/assignments/{id}`).
- **`SignerResource.confirmSignerData(documentId, accessCode, data)`** — the
  signer self-service confirm-data endpoint, now properly URL-encoded.
- **`ListParams`** typed filters: `status`, `method`, `tags`, `includeInactive`,
  `includeStandard` (previously only reachable via the untyped `extra()` map).
- **`SignerReference.step` / `TemplateSigner.step`** for sequential signing order.
- **`CreateDocumentFromTemplateRequest.tags`** (auto-creates tags by name).
- New models: `Tag`, `ApiKey`, `SigningUrl`, `AssignmentSigner`, `AssignmentItem`,
  `ActivityOrigin`, `TemplatePage`, `TemplateFieldPlacement`,
  `ResendNotificationResponse`.
- Richer model fields verified against live responses: `DocumentDetails`
  (`template_id`, `tags`, `declined_by`, typed `pages`), `DocumentListItem`
  (`artifacts`, `signing_url`, `pages`, `tags`, `decline_reason`, `declined_by`),
  `Template`/`TemplateListItem` (`document_name`, `message`, `pages`, `roles`,
  `tags`, `default_document_tags`), `TemplateRole` (`assignment_type`,
  `created_at`, `updated_at`), `Assignment` (`resource`).
- `AssinafyClientOptions.SANDBOX_BASE_URL` constant.

### Changed (breaking)

- **`Assignment.signingUrls`** is now `List<SigningUrl>` (was `Map<String,String>`).
  The API returns an array of `{signer_id, url}` objects; the old type silently
  parsed to `null` and could throw on assignment responses.
- **`Assignment.signers`** is now `List<AssignmentSigner>` (was `List<Signer>`),
  exposing `verification_method`, `notification_methods`, `step`, `notified`,
  `completed` and `notification_history`. `Assignment.items` is now
  `List<AssignmentItem>`. The legacy `expiration` field was removed (use
  `expiresAt`).
- **`SignerResource.acceptTerms`** now returns `Signer` (was `Map<String,Object>`).
- **`FieldResource.validate`** returns `FieldValidationResult`; `validateMultiple`
  returns `List<FieldValidationResult>` (were `Map<String,Object>`). This also
  fixes `validateMultiple` mangling its JSON-array response.
- **`AssignmentResource.getWhatsappNotifications`** returns
  `List<Map<String,Object>>` (was `Map<String,Object>`); the API returns an array.
- **`AssignmentResource.resendNotification`** returns `ResendNotificationResponse`
  (renamed from `ResendEmailResponse`, since it applies to any channel).
- **`DocumentActivity.origin`** is now an `ActivityOrigin` object (`ip`,
  `user-agent`) instead of `String`, matching the live payload (the old `String`
  type would throw at runtime when `origin` was populated); added `payload`.
- **`DocumentUploadResponse.declinedBy`** is now `Object` (was `String`), matching
  the documented `object|null` shape.
- **`WebhookPayload`** now models the documented envelope: added `origin`,
  `subject`, `created_at`; removed the undocumented `type` and `data` fields
  (`getEventData` now returns the `object` entity, falling back to `payload`).
- **`WebhookSubscription`** dropped the phantom `id` and `created_at` fields
  (the API returns only `events`, `is_active`, `url`, `email`, `updated_at`).
- **`WebhookDispatch.createdAt`/`updatedAt`** are now `String` (ISO-8601) per the
  documented Dispatch object (were `Long`).

### Fixed

- **`SignerResource.create`** no longer requires an email: `email` is optional per
  the API, so WhatsApp-only signers can now be created. `full_name` is required
  (matches the documented contract). The email-based dedupe pre-check is skipped
  when no email is supplied.
- **`AssignmentResource.resetExpiration(..., null)`** no longer throws
  `NullPointerException`; passing `null` correctly clears the expiration.
- **`confirmSignerData`** now URL-encodes the `signer-access-code` query parameter
  (it previously appended it raw).
- **`decline` / `declineMultiple`** now require a non-blank `decline_reason`
  (documented as required) instead of silently omitting it.

### Deprecated

- **`DocumentResource.confirmSignerData`** — moved to
  `SignerResource.confirmSignerData` (signer self-service). The old method remains
  (now correctly encoded) but is deprecated.
- **`WebhookResource.delete`** — the `DELETE /accounts/{id}/webhooks/subscriptions`
  route is not served by the live API (returns 404). Use `inactivate()` to stop
  delivery.

### Notes

- The documented user-account/auth surface is intentionally limited to API-key
  management (`client.apiKeys()`). Login, social login, and password
  change/reset are web-app concerns and are out of scope for this server SDK.
- `TemplateResource.get(id)` is retained but the single-template GET path is not
  separately specified in the docs; verify availability before relying on it.

## [1.3.0] - 2026-05-11

Fixes a pagination bug, removes a broken endpoint, and adds the missing
Field Definition, Public Document, signer-self-service, and assignment
decline / WhatsApp-notification endpoints. 82 unit tests pass and a new
`LiveApiSmokeIT` exercises 12 read/write flows.

### Added

- **`FieldResource`** (`client.fields()`):
  - `create` / `list` / `get` / `update` / `delete` for field definitions.
  - `validate` and `validateMultiple` (with optional `signer-access-code`).
  - `listTypes` (`GET /field-types`).
- **`PublicDocumentResource`** (`client.publicDocuments()`):
  - `getBasicInfo` (`GET /public/documents/{id}`).
  - `sendToken` (`PUT /public/documents/{id}/send-token`).
- **`SignerResource` self-service additions**:
  - `getCurrentDocument`, `listDocuments`, `downloadDocument` (signer-scoped).
  - `signMultiple`, `declineMultiple` (bulk signer actions).
- **`AssignmentResource`**:
  - `decline(documentId, assignmentId, signerAccessCode, reason)` — signer-side
    reject (`PUT /documents/{id}/assignments/{id}/reject`).
  - `getWhatsappNotifications(documentId, assignmentId)` — delivery state.
- **`Signer` model**: `has_signature` and `has_initial` fields.
- New models: `FieldDefinition`, `FieldType`, `FieldValidationResult`.
- New request types: `CreateFieldRequest`, `UpdateFieldRequest`.
- `OkHttpApiClient.postSignature` now auto-detects PNG vs JPEG from the byte
  prefix and sets the correct `Content-Type`.

### Changed

- **`ListParams.toQueryParams`** now emits `per-page` (hyphen) instead of
  `per_page` (underscore). The Assinafy API silently ignored the underscore
  form, so all paginated calls were previously returning the default page size
  regardless of the configured `perPage`.
- Signer self-service endpoints (`getSelf`, `uploadSignature`,
  `downloadSignature`) now URL-encode the `signer-access-code` and `type`
  query parameters.

### Removed

- **`AssignmentResource.cancel(...)`** — the previous implementation called
  `/accounts/{id}/signature-requests/{docId}/cancel`, which is not a real
  endpoint (returns 404). The API does not document a sender-side cancel; use
  `DocumentResource.delete` or let the assignment expire.

### Fixed

- Stray indented closing braces in `AssignmentResource`, `WebhookResource`,
  and `WorkspaceResource`.
- Removed unused `ResponseHandler` imports across resources.
- `TemplateResource.list` now passes an empty map (not `null`) for query
  parameters when none are supplied, matching peer resources.

## [1.2.0] - 2026-05-06

Initial Java port of the SDK. API parity with the TypeScript and PHP SDKs.

### Added

- `AssinafyClient` with builder-style `AssinafyClientOptions`.
- Resources: `DocumentResource`, `SignerResource`, `WorkspaceResource`,
  `AssignmentResource`, `WebhookResource`, `TemplateResource`.
- High-level `uploadAndRequestSignatures` helper.
- `WebhookVerifier` for HMAC-SHA256 signature verification.
- Typed exceptions: `AssinafyException`, `ApiException`, `ValidationException`,
  `NetworkException`.
- `cpf` field on signer create/update payloads with automatic non-digit
  stripping (mirrors PHP SDK `sanitizeDocument`).
- `PaginatedResult<T>` with parsed `X-Pagination-*` header meta.
