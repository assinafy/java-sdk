# Changelog

All notable changes to `com.assinafy:assinafy-sdk` will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
