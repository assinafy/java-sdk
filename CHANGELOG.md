# Changelog

All notable changes to `com.assinafy:assinafy-sdk` will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.4.0] - 2026-05-27

Second full audit pass, verified file-by-file against the documentation at
`https://api.assinafy.com.br/v1/docs` **and** the live API. Adds the missing Tag
and document-tag surfaces and API-key management, fixes several response-parsing
bugs, and tightens model conformance. 111 unit tests pass and the opt-in
`LiveApiSmokeIT` now exercises 16 read/write flows against the real API.

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

Full audit pass against the live API at `https://api.assinafy.com.br/v1/docs`.
Fixes a pagination bug, removes a broken endpoint, and adds the missing
Field Definition, Public Document, signer-self-service, and assignment
decline / WhatsApp-notification endpoints. 82 unit tests pass and a new
`LiveApiSmokeIT` exercises 12 read/write flows against the real API.

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
