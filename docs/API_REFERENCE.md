# Assinafy Java SDK API reference

This is the Java mapping of the official production OpenAPI document published at <https://api.assinafy.com.br/v1/docs/openapi.json>. It covers all **147** documented operations, plus the routes listed under [Deployed extensions](#deployed-extensions).

## Conventions

- Build a client with `new AssinafyClient(AssinafyClientOptions.builder()...build())`; each operation names its client accessor and exact current public signature.
- Custom API base URLs must use HTTPS. Plain HTTP is accepted only for loopback hosts used in local tests, so credentials are not sent over cleartext networks.
- Authenticated operations accept either `X-Api-Key` (recommended for server integrations) or `Authorization: Bearer <JWT>`. Signer-facing operations use the `signer-access-code` query credential. The default transport omits workspace credentials on public, login, two-factor verification, OAuth grant, and signer-access-code requests.
- JSON success bodies use `{ "status": integer, "message": string, "data": ... }`. The SDK returns `data`. A Java `void` method discards the success envelope and also accepts an empty 2xx body. Binary methods return raw `byte[]`, not JSON.
- A `!` after an inline JSON field name means required. `?` after a type means explicitly nullable. “Required: no” means the OpenAPI schema does not require the field; it does not imply the server always omits it.
- Linked component schemas are part of the operation payload; follow the link for every nested field. List methods return `PaginatedResult<T>` when pagination headers are exposed.
- Raw `Map<String,Object>` methods expose object-valued `data` fields directly. A scalar or array `data` value is returned as `{ "data": value }`; missing data or an empty success body becomes an empty map. Typed alternatives are listed beside their map-returning methods.
- `confirmSignerData(...)` returns null for the current empty-list success response, or a `Signer` for older object responses. Supply the contact fields required by the signing method.
- `UpdateSignerRequest.email("")` and `whatsappPhoneNumber("")` on the builder clear the contact field; null omits it and leaves it unchanged.
- Operation request tables describe the production OpenAPI wire contract. Additional Java helpers are identified at the end of this reference.
- Non-2xx responses and non-2xx numeric envelope statuses throw `ApiException`; 401/403 use `AuthenticationException`, 429 uses `RateLimitException`, I/O failures use `NetworkException`, and local argument failures use `ValidationException`. An RFC 6749 `{error, error_description}` body from an OAuth endpoint throws `OAuthException`, a subtype of `ApiException`.
- A `DigitalCertificate` signer requires a `virtual` assignment. Direct `collect` creation and estimation fail locally with `ValidationException.getErrors().get("code") == AssignmentResource.DIGITAL_CERTIFICATE_REQUIRES_VIRTUAL_METHOD` (`digital_certificate_requires_virtual_method`). For templates, the API derives the method from placed fields and rejects certificate signers when the result is `collect`.
- Every rate-limited response carries `X-Rate-Limit-Limit`, `X-Rate-Limit-Remaining` and `X-Rate-Limit-Reset`, and a `429` adds `Retry-After` (seconds). `ApiException` (and so `RateLimitException`) returns them from `getRateLimitLimit()`, `getRateLimitRemaining()`, `getRateLimitReset()` and `getRetryAfter()`, each an `OptionalInt`.
- Every method that takes a signer access code rejects a null or blank code with `ValidationException` and sends only that code — never the workspace API key or bearer token. Routes that also accept workspace credentials have separate `...AsWorkspace` methods.
- The OAuth endpoints are the deliberate exception to the envelope rule. RFC 6749 §5.1/§5.2, OIDC Core §5.3.2 and RFC 8615 all require flat JSON, so `/oauth/token`, `/oauth/revoke`, `/oauth/userinfo` and the `.well-known` documents are neither wrapped nor unwrapped — the SDK decodes them as-is.

## API operations

### 1. Get account

- **Java:** `client.workspaces()` — `WorkspaceResource: public Workspace get(String accountId)`; `client.workspaces()` — `WorkspaceResource: public Workspace get(String accountId, boolean includeSigningFeatures)`
- **HTTP:** `GET /v1/accounts/{accountId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Retrieve a workspace account the user belongs to.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `expand` | query | string | no | `signing_features`: add which signing features the workspace can use. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Account](#schema-account)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Account](#schema-account) | no | no |  |

Documented statuses: `200` The account; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 2. Update account

- **Java:** `client.workspaces()` — `WorkspaceResource: public Workspace update(String accountId, UpdateWorkspaceRequest request)`
- **HTTP:** `PUT /v1/accounts/{accountId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Update a workspace account's profile.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: **required**.

`application/json`: object{`name`: string; `notification_sender_type`: string enum[User, Account]}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | string | no | no |  |
| `notification_sender_type` | string enum[User, Account] | no | no | Who signers see as the notification sender for documents in this account. `User` (default) shows the document owner's name; `Account` shows this account's name. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Account](#schema-account)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Account](#schema-account) | no | no |  |

Documented statuses: `200` The updated account; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 3. Delete account

- **Java:** `client.workspaces()` — `WorkspaceResource: public void delete(String accountId)`; `client.workspaces()` — `WorkspaceResource: public void delete(String accountId, boolean force)`
- **HTTP:** `DELETE /v1/accounts/{accountId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Delete a workspace account.  By default the request fails with `400` when the workspace has an active paid subscription — the `restrictions` array in the response lists each blocker by code so you can address them individually before retrying. Pass `force: true` to cancel any active paid subscription automatically and proceed with immediate deletion.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: **optional**.

`application/json`: object{`force`: boolean}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `force` | boolean | no | no | When `true`, cancels any active paid subscription on this workspace and proceeds with deletion immediately. Defaults to `false`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no |  |

Documented statuses: `200` Account deleted; `400` Deletion blocked by active restrictions. Each `restrictions` entry describes one blocker; resolve them individually, or retry with `force: true` to cancel blocking subscriptions/documents automatically.; `404` The requested resource does not exist.; `403` Authenticated but not allowed to perform this action.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 4. Get account theme

- **Java:** `client.workspaces()` — `WorkspaceResource: public AccountTheme getTheme(String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/theme`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Retrieve account theme information (branding name, colors, and logo URL).

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [AccountTheme](#schema-accounttheme)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [AccountTheme](#schema-accounttheme) | no | no |  |

Documented statuses: `200` The theme; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 5. Get account usage

- **Java:** `client.workspaces()` — `WorkspaceResource: public AccountUsage getUsage(String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/usage`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** The workspace's plan and what is left of it, for a plan and credit meter. Nothing about prices or payments: those stay in **Get current subscription**. Not available to third-party OAuth applications (`403`), whatever their scopes: this is billing data. Assinafy's own apps can read it with the `account:read` scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [AccountUsage](#schema-accountusage)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [AccountUsage](#schema-accountusage) | no | no |  |

Documented statuses: `200` The usage; `403` Authenticated but not allowed to perform this action.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 6. Download account logo

- **Java:** `client.workspaces()` — `WorkspaceResource: public byte[] downloadLogo(String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/logo`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Download the account logo image binary.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: none.

Success `200` `image/*`: string (binary).


Documented statuses: `200` The logo image; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 7. Upload account logo

- **Java:** `client.workspaces()` — `WorkspaceResource: public void uploadLogo(String accountId, byte[] imageData, String fileName)`
- **HTTP:** `POST /v1/accounts/{accountId}/logo`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Upload or replace the account logo image.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: **required**.

`multipart/form-data`: object{`file!`: string (binary)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `file` | string (binary) | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |

Documented statuses: `200` Logo updated; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 8. Delete account logo

- **Java:** `client.workspaces()` — `WorkspaceResource: public void deleteLogo(String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/logo`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Remove the account logo image.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |

Documented statuses: `200` Logo deleted; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 9. List my accounts

- **Java:** `client.workspaces()` — `WorkspaceResource: public PaginatedResult<Workspace> list()`
- **HTTP:** `GET /v1/accounts`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List the workspace accounts the authenticated user belongs to.  Called with an OAuth application token, this returns exactly one workspace: the one the user chose when they authorized the application. Use its `id` as the `{accountId}` segment of every other endpoint — a token is bound to a single workspace, and any request naming a different one is refused. This endpoint needs no particular scope.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[Account](#schema-account)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[Account](#schema-account)> | no | no |  |

Documented statuses: `200` The user's accounts; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 10. Create account

- **Java:** `client.workspaces()` — `WorkspaceResource: public Workspace create(CreateWorkspaceRequest request)`
- **HTTP:** `POST /v1/accounts`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Create a new workspace account owned by the authenticated user.

Parameters: none.

Request body: **required**.

`application/json`: object{`name!`: string; `notification_sender_type`: string enum[User, Account]}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | string | yes | no |  |
| `notification_sender_type` | string enum[User, Account] | no | no | Who signers see as the notification sender for documents in this account. `User` (default) shows the document owner's name; `Account` shows this account's name. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Account](#schema-account)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Account](#schema-account) | no | no |  |

Documented statuses: `200` The created account; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 11. List document activities

- **Java:** `client.documents()` — `DocumentResource: public List<DocumentActivity> activities(String documentId)`; `client.documents()` — `DocumentResource: public PaginatedResult<DocumentActivity> activities(String documentId, ListParams params)`
- **HTTP:** `GET /v1/documents/{documentId}/activities`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List the activities recorded for a document, newest first (`sort=id` for oldest first). Paged: 20 per page by default; the `X-Pagination-Total-Count`, `X-Pagination-Page-Count`, `X-Pagination-Current-Page` and `X-Pagination-Per-Page` headers describe the full list, and a page past the end returns an empty `data`. Each entry carries an event-specific `payload` snapshot (keys vary per event) and the request `origin` (`ip`, `user-agent`).

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `sort` | query | string | no | `-id` (default, newest first) or `id`. |
| `page` | query | integer | no | Page number. |
| `per-page` | query | integer | no | Records per page: 20 by default, at most 50; a larger value answers 50 a page. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[DocumentActivity](#schema-documentactivity)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[DocumentActivity](#schema-documentactivity)> | no | no |  |

Documented statuses: `200` Document activities; `403` Authenticated but not allowed to perform this action.; `404` The document is not in the workspace.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 12. List assignments

- **Java:** `client.assignments()` — `AssignmentResource: public PaginatedResult<Assignment> list(ListParams params)`; `client.assignments()` — `AssignmentResource: public PaginatedResult<Assignment> list(ListParams params, String accountId)`; `client.assignments()` — `AssignmentResource: public PaginatedResult<Assignment> list()`
- **HTTP:** `GET /v1/assignments`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List the workspace's assignments, oldest first. With an OAuth token the workspace is the token's; otherwise pass `accountId`. Each assignment carries its `document_id`.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | query | string | no | The workspace ID. Required unless the request uses an OAuth token. |
| `sort` | query | string | no | `created_at` (default, oldest first), `expires_at` or `id`; prefix with `-` for descending (`-created_at`, newest first). Unknown values are ignored. |
| `page` | query | integer | no | Page number. |
| `per-page` | query | integer | no | Records per page: 20 by default, at most 50; a larger value answers 50 a page. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[Assignment](#schema-assignment)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[Assignment](#schema-assignment)> | no | no |  |

Documented statuses: `200` A page of assignments; `400` Neither `accountId` nor an OAuth token names the workspace.; `403` Authenticated but not allowed to perform this action.; `404` The workspace does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 13. Create assignment (request signatures)

- **Java:** `client.assignments()` — `AssignmentResource: public Assignment create(String documentId, CreateAssignmentRequest request)`
- **HTTP:** `POST /v1/documents/{documentId}/assignments`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Request signatures on a document. Use `method: virtual` to sign without input fields, or `method: collect` to place input fields on specific pages.  **Digital certificate signing works only on `virtual` assignments.** A `DigitalCertificate` signer cannot sign placed fields, so a `collect` assignment with one is refused with `400` before anything is charged.  A document takes one assignment: a retry after a lost response answers `400` and charges nothing. Read the document to see whether the first request went through.  For **virtual**, the document may be in `uploaded`, `metadata_processing` or `metadata_ready`; it is promoted to `pending_signature` automatically once metadata processing completes. For **collect**, the document must be in `metadata_ready` (fields reference specific pages).  `step` controls signing order: signers sharing a step sign in parallel, and the next step is notified only after the previous step completes. If supplied, every signer must supply it and values must be contiguous starting at 1.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |

Request body: **required**.

`application/json`: object{`method!`: string enum[virtual, collect]; `signers!`: array<object{`id!`: string; `verification_method`: string enum[Email, Whatsapp, DigitalCertificate, IdDocumentFaceMatchBasic]; `notification_methods`: array<string enum[Email, Whatsapp]>; `embedded`: boolean; `step`: integer}>; `entries`: array<object{`page_id`: string; `fields`: array<object{`signer_id`: string; `field_id`: string; `display_settings`: [DisplaySettings](#schema-displaysettings)}>}>; `message`: string; `expires_at`: string (date-time); `copy_receivers`: array<string>; `max_total_credits`: number}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `method` | string enum[virtual, collect] | yes | no | `virtual` signs without input fields; `collect` places input fields on pages. Signers with `DigitalCertificate` verification need `virtual` (`collect` returns `400`). |
| `signers` | array<object{`id!`: string; `verification_method`: string enum[Email, Whatsapp, DigitalCertificate, IdDocumentFaceMatchBasic]; `notification_methods`: array<string enum[Email, Whatsapp]>; `embedded`: boolean; `step`: integer}> | yes | no |  |
| `entries` | array<object{`page_id`: string; `fields`: array<object{`signer_id`: string; `field_id`: string; `display_settings`: [DisplaySettings](#schema-displaysettings)}>}> | no | no | Required for `collect`: field placements per page. In `collect`, every signer listed in `signers` must appear once and own at least one field here; otherwise `400`. |
| `message` | string | no | no | Text included in the invitation email. |
| `expires_at` | string (date-time) | no | no | ISO 8601; default is no expiration. Must be at least one hour in the future. A UTC offset in the value is honored (`2026-12-31T23:59:59-03:00` is `2027-01-01T02:59:59Z`); a value without one is read as UTC, and a date alone (`2026-12-31`) means 23:59 UTC that day. |
| `copy_receivers` | array<string> | no | no | Signer IDs that only receive a copy. |
| `max_total_credits` | number | no | no | Optional ceiling on what this request may spend: refused with `409` and code `CreditCeilingExceeded`, nothing charged, when its `total_credits` is above this. Send the `total_credits` of the last estimate. A value that is not a number of credits, zero or more, answers `400`; numeric text counts, as a form-encoded body sends it. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Assignment](#schema-assignment)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Assignment](#schema-assignment) | no | no |  |

Documented statuses: `200` The created assignment; `400` The request was rejected because of its current state or invalid input.; `409` The request sent `max_total_credits` and costs more now: the client estimates again and decides. Nothing was created or charged.; `403` The workspace's plan lacks a verification or notification method sent (`PlanRestricted`), or an OAuth app's token lacks `documents:write` (`InsufficientScope`); also a deactivated membership or a role.; `404` The document is not in the workspace.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 14. Estimate assignment cost

- **Java:** `client.assignments()` — `AssignmentResource: public Map<String, Object> estimateCost(String documentId, CreateAssignmentRequest request)`; `client.assignments()` — `AssignmentResource: public CostEstimate estimateCostTyped(String documentId, CreateAssignmentRequest request)`
- **HTTP:** `POST /v1/documents/{documentId}/assignments/estimate-cost`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Estimate the cost of creating an assignment without creating it, returning a cost breakdown and the current account balances. Signer IDs are not required — only the verification/notification method affects cost. Each assignment consumes 1 document from the plan allowance; if exhausted, an extra document is charged from credits (`needs_extra_document` = true). `blocking_reason` may be `PendingPayment`, `InsufficientDocuments` or `InsufficientCredits`.  ### Pricing  Per-unit costs (in credits) used to build the estimate:  &#124; Item &#124; Cost &#124; &#124;------&#124;------&#124; &#124; Extra document &#124; 1 credit &#124; &#124; Email notification &#124; 0 credits &#124; &#124; WhatsApp notification &#124; 0.45 credits &#124; &#124; Digital certificate signature (per signer) &#124; 0.5 credits &#124; &#124; Identity document verification (per signer) &#124; depends on the plan, see the table below &#124;  &#124; Plan &#124; Price per signer &#124; &#124;------&#124;------------------&#124; &#124; Free &#124; 1.5 credits &#124; &#124; Standard &#124; 1 credit &#124; &#124; Pro &#124; 0.5 credits &#124; &#124; Other plans and enterprise contracts &#124; 0.25 credits &#124;  `Email` and `Whatsapp` verification are not priced separately — their only line in the `breakdown` is their notification. A `Whatsapp`-verified signer therefore shows up as a WhatsApp notification, because that channel is mandatory for that verification method. A `DigitalCertificate` signer adds the digital-certificate signature cost **on top of** its notification cost; it appears in the `breakdown` under the `SignatureDigitalCertificate` code. An `IdDocumentFaceMatchBasic` signer likewise adds your plan's verification price on top of its notification, under the `VerificationIdDocumentFaceMatchBasic` code.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |

Request body: **required**.

`application/json`: object{`method`: string enum[virtual, collect]; `signers`: array<object{`verification_method`: string enum[Email, Whatsapp, DigitalCertificate, IdDocumentFaceMatchBasic]; `notification_methods`: array<string enum[Email, Whatsapp]>}>; `entries`: array<object>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `method` | string enum[virtual, collect] | no | no | Send the method you will create the assignment with. With `collect`, a `DigitalCertificate` signer returns `400`, because certificate signing works only on `virtual` assignments. |
| `signers` | array<object{`verification_method`: string enum[Email, Whatsapp, DigitalCertificate, IdDocumentFaceMatchBasic]; `notification_methods`: array<string enum[Email, Whatsapp]>}> | no | no | Required for `virtual`; each entry may be `{}` to default to Email. |
| `entries` | array<object> | no | no | Required for `collect`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [CostEstimate](#schema-costestimate)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [CostEstimate](#schema-costestimate) | no | no |  |

Documented statuses: `200` Cost estimate and balances; `400` The request was rejected because of its current state or invalid input.; `403` The workspace's plan lacks a verification or notification method sent (`PlanRestricted`), or an OAuth app's token lacks `documents:write` (`InsufficientScope`); also a deactivated membership or a role.; `404` The document is not in the workspace.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 15. Resend signature request

- **Java:** `client.assignments()` — `AssignmentResource: public ResendNotificationResponse resendNotification(String documentId, String assignmentId, String signerId)`
- **HTTP:** `PUT /v1/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/resend`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Resend the signature-request notification to a signer of the assignment who has not signed yet, while the document is pending signature. Paid notification methods are charged for each message that goes out.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `assignmentId` | path | string | yes | The assignment ID. |
| `signerId` | path | string | yes | The signer ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`is_sent`: boolean; `document_id`: string; `signer_id`: string}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`is_sent`: boolean; `document_id`: string; `signer_id`: string} | no | no |  |

Documented statuses: `200` Resend result; `400` The document is not pending signature, the signer is not part of the assignment, has already signed, or waits for an earlier signing step; or the account lacks credits for the reminder.; `404` The assignment or signer was not found on this document.; `403` Authenticated but not allowed to perform this action.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 16. Estimate resend cost

- **Java:** `client.assignments()` — `AssignmentResource: public Map<String, Object> estimateResendCost(String documentId, String assignmentId, String signerId)`; `client.assignments()` — `AssignmentResource: public ResendCostEstimate estimateResendCostTyped(String documentId, String assignmentId, String signerId)`
- **HTTP:** `POST /v1/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/estimate-resend-cost`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Estimate the cost of resending the signature request to a signer. The response has its own shape (`total`, not `total_credits`), because a reminder uses no document.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `assignmentId` | path | string | yes | The assignment ID. |
| `signerId` | path | string | yes | The signer ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [ResendCostEstimate](#schema-resendcostestimate)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [ResendCostEstimate](#schema-resendcostestimate) | no | no |  |

Documented statuses: `200` Cost estimate; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 17. Reset assignment expiration

- **Java:** `client.assignments()` — `AssignmentResource: public Assignment resetExpiration(String documentId, String assignmentId, String expiresAt)`
- **HTTP:** `PUT /v1/documents/{documentId}/assignments/{assignmentId}/reset-expiration`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Set a new expiration date for an assignment, or remove its expiration by sending `expires_at: null`. Not allowed once the document is closed.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `assignmentId` | path | string | yes | The assignment ID. |

Request body: **required**.

`application/json`: object{`expires_at!`: string or null (date-time)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `expires_at` | string or null (date-time) | yes | yes | New expiration date (ISO 8601). Must be at least one hour in the future. Send `null` to remove the expiration — the key itself is required. A UTC offset in the value is honored (`2026-12-31T23:59:59-03:00` is `2027-01-01T02:59:59Z`); a value without one is read as UTC, and a date alone (`2026-12-31`) means 23:59 UTC that day. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Assignment](#schema-assignment)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Assignment](#schema-assignment) | no | no |  |

Documented statuses: `200` The updated assignment; `400` One or more fields failed validation.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 18. Signer identity review

- **Java:** `client.assignments().identityVerification(documentId, assignmentId, signerId)` → `SignerIdentityReview`.
- **HTTP:** `GET /v1/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/identity-verification`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** The signer's identity-document verification as the account's reviewer sees it. When the provider sends a verification to review, the account decides it with **Decide signer identity review**. The signer can still view and sign meanwhile; the review holds the document's certification instead (status `pending_identity_review` once every signer has signed). Evidence is a summary kept by Assinafy (no images or biometrics). `evidence_available` is false when the provider keeps none or it could not be fetched yet. While the review is open, `images` lists the evidence images the reviewer can open with **Signer identity review image**. A verification the provider approved directly, without a review, shows no evidence here (`evidence_available` is false); its evidence is on the `signer_identity_verified` activity. For a document of an envelope, the signer's one check of the whole envelope, read from any of its documents they are on. Reachable with the `documents:read` OAuth scope; the images and the decision are not available to third-party OAuth applications (`403`): the review is made by a person, in Assinafy or its apps.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `assignmentId` | path | string | yes | The assignment ID. |
| `signerId` | path | string | yes | The signer ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [SignerIdentityReview](#schema-signeridentityreview)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [SignerIdentityReview](#schema-signeridentityreview) | no | no |  |

Documented statuses: `200` Identity review; `403` Authenticated but not allowed to perform this action.; `404` Assignment, signer or verification not found.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 19. Signer identity review image

- **Java:** `client.assignments().identityVerificationImage(documentId, assignmentId, signerId, name)` → `byte[]`.
- **HTTP:** `GET /v1/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/identity-verification/images/{name}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** One evidence image of a signer identity verification that is `InReview`, for the account's reviewer: the document photos (or the digital document PDF) and the face photo. Fetched from the verification provider on each request and never stored by Assinafy. Available only while the review is open; once it is decided, every image returns 404. `name` must be one of the review's `images`. Not available to third-party OAuth applications (`403`), whatever their scopes: the images are for the person reviewing. Assinafy's own apps can open them with the `documents:read` scope, for the user signed in to them.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `assignmentId` | path | string | yes | The assignment ID. |
| `signerId` | path | string | yes | The signer ID. |
| `name` | path | string enum[document_front, document_back, document_pdf, face] | yes | The image, as listed in the review's `images`. |

Request body: none.

Success `200` `image/jpeg`: string (binary).


Success `200` `image/png`: string (binary).


Success `200` `application/pdf`: string (binary).


Documented statuses: `200` The image bytes (`image/jpeg`, `image/png`) or the document PDF (`application/pdf`). Sent with `Cache-Control: no-store`.; `403` Authenticated but not allowed to perform this action.; `404` Assignment, signer or verification not found, the review is not open, or there is no such image.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 20. Decide signer identity review

- **Java:** `client.assignments().reviewIdentityVerification(documentId, assignmentId, signerId, decision, note)` → `SignerIdentityReview`.
- **HTTP:** `POST /v1/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/identity-verification/review`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Approves or rejects a signer whose identity verification is `InReview`, while the document is `pending_signature` or `pending_identity_review`. Approved: once every signer has signed and no other signer is under review, the document is certificated; the certificate states the approval by review and every difference found. Rejected: the document is cancelled (`rejected_by_user`, with the reviewer as the user) and every signer and copy receiver gets the cancellation notice; the note is never shown to them. The signer gets no message about the decision itself. For a document of an envelope, it decides the signer's one check of the whole envelope, from any of its documents they are on, and is recorded on each of them: approving releases each once signed, and rejecting cancels the whole envelope as its cancel does, each person told once. The decision is also reported to the verification provider. Not available to third-party OAuth applications (`403`), whatever their scopes: the certificate names the person who reviewed. Assinafy's own apps can decide it with the `documents:write` scope, and the reviewer is the user signed in to them.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `assignmentId` | path | string | yes | The assignment ID. |
| `signerId` | path | string | yes | The signer ID. |

Request body: **required**.

`application/json`: object{`decision!`: string enum[Approved, Rejected]; `note`: string or null}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `decision` | string enum[Approved, Rejected] | yes | no |  |
| `note` | string or null | no | yes | Optional. Recorded in the activity log and sent to the provider; never shown to the signer. maxLength: `2000`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [SignerIdentityReview](#schema-signeridentityreview)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [SignerIdentityReview](#schema-signeridentityreview) | no | no |  |

Documented statuses: `200` Decided; `400` Invalid decision, the verification is not `InReview`, or the document is no longer awaiting signatures (for a document of an envelope, none of the signer's documents of it is).; `403` Authenticated but not allowed to perform this action.; `404` Assignment, signer or verification not found.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 21. Login

- **Java:** `client.authentication()` — `AuthenticationResource: public AuthSession login(String email, String password)`
- **HTTP:** `POST /v1/login`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** Authenticate with email and password and receive a JWT access token.

Parameters: none.

Request body: **required**.

`application/json`: object{`email!`: string (email); `password!`: string (password)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `email` | string (email) | yes | no |  |
| `password` | string (password) | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [AuthSession](#schema-authsession)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [AuthSession](#schema-authsession) | no | no |  |

Documented statuses: `200` Access token, user and accounts; `400` One or more fields failed validation.; `500` Unexpected server error..

### 22. Request password reset

- **Java:** `client.authentication()` — `AuthenticationResource: public void requestPasswordReset(String email)`; `client.authentication()` — `AuthenticationResource: public Map<String, Object> requestPasswordResetResult(String email)`
- **HTTP:** `PUT /v1/authentication/request-password-reset`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** Send the user an email with instructions to reset their password. Used when the password was forgotten or never set.

Parameters: none.

Request body: **required**.

`application/json`: object{`email!`: string (email)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `email` | string (email) | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`email`: string (email)}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`email`: string (email)} | no | no |  |

Documented statuses: `200` Reset email sent; `500` Unexpected server error..

### 23. Reset password

- **Java:** `client.authentication()` — `AuthenticationResource: public void resetPassword(String email, String token, String newPassword)`; `client.authentication()` — `AuthenticationResource: public Map<String, Object> resetPasswordResult(String email, String token, String newPassword)`
- **HTTP:** `PUT /v1/authentication/reset-password`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** Reset the user's password using the token received by email.

Parameters: none.

Request body: **required**.

`application/json`: object{`email!`: string (email); `token`: string; `new_password!`: string (password)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `email` | string (email) | yes | no |  |
| `token` | string | no | no | Token received by email. |
| `new_password` | string (password) | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`email`: string (email)}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`email`: string (email)} | no | no |  |

Documented statuses: `200` Password reset; `400` One or more fields failed validation.; `500` Unexpected server error..

### 24. Change password

- **Java:** `client.authentication()` — `AuthenticationResource: public void changePassword(String email, String password, String newPassword)`; `client.authentication()` — `AuthenticationResource: public Map<String, Object> changePasswordResult(String email, String password, String newPassword)`
- **HTTP:** `PUT /v1/authentication/change-password`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Change the authenticated user's password. The body's `email` must be their own address (any letter case); any other address is refused (`403`), whether or not it has an account, and an `email` that is not text answers `400`.

Parameters: none.

Request body: **required**.

`application/json`: object{`email!`: string (email); `password!`: string (password); `new_password!`: string (password)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `email` | string (email) | yes | no |  |
| `password` | string (password) | yes | no | The current password. |
| `new_password` | string (password) | yes | no | The new password. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`email`: string (email)}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`email`: string (email)} | no | no |  |

Documented statuses: `200` Password changed; `400` One or more fields failed validation.; `403` Authenticated but not allowed to perform this action.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 25. Complete a two-factor login

- **Java:** `client.authentication()` — `AuthenticationResource: public AuthSession verifyMfa(String mfaToken, String code)`. The code is trimmed before sending.
- **HTTP:** `POST /v1/authentication/mfa/verify`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** Exchanges the `mfa_token` returned by login for an access token. `code` is either the 6-digit code from the authenticator app or one of the recovery codes issued at enrollment. The challenge is single-use and expires 5 minutes after login.

Parameters: none.

Request body: **required**.

`application/json`: object{`mfa_token!`: string; `code!`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `mfa_token` | string | yes | no | The token returned by the login response. |
| `code` | string | yes | no | A 6-digit authenticator code, or a recovery code such as ABCD-EFGH-JKMN. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [AuthSession](#schema-authsession)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [AuthSession](#schema-authsession) | no | no |  |

Documented statuses: `200` Access token, user and accounts; `400` One or more fields failed validation.; `401` The challenge expired, was already used, or too many codes were tried; `500` Unexpected server error..

### 26. List contracts

- **Java:** `client.contracts()` — `ContractResource: public PaginatedResult<DocumentContract> list()`; `public PaginatedResult<DocumentContract> list(ListParams params)`; `public PaginatedResult<DocumentContract> list(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/contracts`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List the contract terms tracked on the workspace's documents, most recently updated first. Every filter is optional and only narrows the list. To catch up after a webhook that never arrived, page this list until the contracts are older than your last sync: every change to the terms, by a user or by the hourly run, moves `updated_at`. Each document's history of contract events is at `GET /v1/documents/{documentId}/activities`.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `status` | query | array<string enum[draft, active, expired, terminated]> | no | Contract status; several, comma-separated, match any of them. |
| `counterparty` | query | string | no | Part of the counterparty name, or its exact CPF/CNPJ. |
| `search` | query | string | no | Part of the document's name. |
| `end_date_from` | query | string (date) | no | Contracts ending on or after this date. |
| `end_date_to` | query | string (date) | no | Contracts ending on or before this date. |
| `ending_within_days` | query | integer | no | Contracts ending from today (the API's calendar day, Brasília time) to that many days later, both included; `0` is today. Any status: add `status=active` for the ones still in force. |
| `external_system` | query | string | no | System of an external reference, e.g. `hubspot`. |
| `external_id` | query | string | no | ID of an external reference in that system. |
| `sort` | query | string | no | `end_date` or `updated_at`; prefix with `-` for descending (`-updated_at`, the default). Contracts without an end date come last either way. |
| `expand` | query | string | no | `document` embeds each contract's document. |
| `page` | query | integer | no | Page number. |
| `per-page` | query | integer | no | Records per page: 20 by default, at most 50; a larger value answers 50 a page. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[DocumentContract](#schema-documentcontract)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[DocumentContract](#schema-documentcontract)> | no | no |  |

Documented statuses: `200` A page of contracts; `400` The request breaks a contract rule. `errors` has one entry per problem found: the `field` as sent (`null` when the request as a whole is refused), a stable `code` to translate, and the `message` in the platform's language; `message` at the top is the first entry's. The contract's own rules on the terms, and the external references' items, report every field that breaks one, an entry per field (its first problem); the other checks (the body, its fields, the contract's state, a renewal's or a link's conditions) stop at the first problem they find.  &#124; Code &#124; Field &#124; Refused when &#124; &#124;------&#124;-------&#124;--------------&#124; &#124; `InvalidBody` &#124; — &#124; The body is not a JSON object (a list, a bare value), or a `PUT` of contract terms sends no attribute. &#124; &#124; `UnknownField` &#124; the attribute &#124; The endpoint does not take the attribute; one entry per attribute. &#124; &#124; `NotSingleValue` &#124; the attribute &#124; On a `PUT` of contract terms, a list sent for a term that takes one value (every term but `metadata` and `external_references`). On a link, the same list answers the term's own code. &#124; &#124; `Required` &#124; `parent_document_id`, `relation_type`, `external_references[i].system`, `external_references[i].id` &#124; A first link without `parent_document_id` (a pending link that was removed meanwhile) or `relation_type`; an `external_references` item without `system` or `id` (`i` is its place in the list sent, from 0). &#124; &#124; `Invalid` &#124; the field &#124; The value is not of the field's type or form: `status` not one of the four; `currency` not three letters; `auto_renew` not a boolean; `owner_id` not a user id; `value` not a number; `counterparty_name` holding HTML; `metadata` not an object; `external_references` not a list, an item's `system` (`external_references[i].system`) with characters other than letters, digits, `_`, `-` and `.`, or its `id` (`external_references[i].id`) with an emoji or another 4-byte character; `parent_document_id` not text; `relation_type` not `amendment`, `renewal` or `termination`; `term` not a whole number from 1; `reason` not text. On the list: a filter sent as a list, a `status` not one of the four, `ending_within_days` not a whole number. &#124; &#124; `TooSmall`, `TooBig` &#124; the field &#124; `value` below 0 or above 9999999999999.99; `renewal_term_months` outside 1 to 600; `notice_period_days` (and the list's `ending_within_days`) outside 0 to 3650. &#124; &#124; `TooLong` &#124; the field &#124; `counterparty_name` over 255 characters, counting each emoji as stored (several characters); `counterparty_taxpayer_id` over 20; `reason` over 500; `external_references[i].system` over 64 or `external_references[i].id` over 255. &#124; &#124; `TooMany` &#124; `external_references` &#124; More than 20 items. &#124; &#124; `TooManyDecimals` &#124; `value` &#124; More than two decimal places. &#124; &#124; `InvalidDate` &#124; `start_date`, `end_date`, `updated_at`; the list's `end_date_from`, `end_date_to` &#124; Not a date as `YYYY-MM-DD` (an impossible one such as `2026-02-30` included); `updated_at` not a date and time as the API writes it (`2026-10-23T12:00:00Z`, or with an offset). &#124; &#124; `DateTooEarly` &#124; `start_date`, `end_date` &#124; Before the year 1000. &#124; &#124; `DateTooLate` &#124; `end_date` &#124; A renewal by the term would pass 9999-12-31. &#124; &#124; `EndBeforeStart` &#124; `end_date` &#124; Before `start_date`. &#124; &#124; `InvalidTaxpayerId` &#124; `counterparty_taxpayer_id` &#124; Not a valid CPF or CNPJ (formatted or not). &#124; &#124; `NotAMember` &#124; `owner_id` &#124; Not an active member of the workspace. &#124; &#124; `TooManyKeys` &#124; `metadata` &#124; More than 50 keys. &#124; &#124; `InvalidKey` &#124; `metadata` &#124; A key that is not 1 to 40 letters, digits, `_`, `-` or `.`. &#124; &#124; `InvalidValue` &#124; `metadata` &#124; A value that is not plain text of at most 500 characters. &#124; &#124; `NumberUnderDateKey` &#124; `metadata` &#124; A number under a key ending in `_at`, or `stuck_since`: those keys hold text only. &#124; &#124; `ListKeys` &#124; `metadata` &#124; Keys `0`, `1`, `2`…, which would read back as a list. &#124; &#124; `DuplicateReference` &#124; `external_references` &#124; Two items the database takes for one (accents, `ß` and `ss`); items equal case aside are merged, not refused. &#124; &#124; `StatusNotSettable` &#124; `status` &#124; A tracked contract sent `expired` or `terminated`, or a closed one sent `draft` or `active`: a contract is closed by terminating it and reopened by renewing it. &#124; &#124; `CannotReturnToDraft` &#124; `status` &#124; An `active` contract sent `draft`. &#124; &#124; `ExpiredNeedsPastEndDate` &#124; `end_date` &#124; New terms with status `expired` and no `end_date` before today. &#124; &#124; `TerminatedNeedsEndDate` &#124; `end_date` &#124; New terms with status `terminated` and no `end_date`. &#124; &#124; `UnsignableDocument` &#124; — &#124; New terms, or a first link, on a cancelled, declined or expired document. Terms or a link already on one stay editable. &#124; &#124; `DocumentIsAmendment` &#124; — &#124; Terms of its own for a document linked as an amendment: its terms are the changes it makes. &#124; &#124; `DocumentHasOwnTerms` &#124; — &#124; A link for a document that tracks a contract of its own: a document is a contract or an amendment, never both. &#124; &#124; `ParentHasNoTerms` &#124; `parent_document_id` &#124; The parent named has no contract terms, nor amends a contract that has. &#124; &#124; `ParentTerminated` &#124; `parent_document_id` &#124; The parent contract is terminated. &#124; &#124; `NoTermChanged` &#124; — &#124; An `amendment` that changes no term, also by dropping its last one. A `renewal` or `termination` may change none. &#124; &#124; `TermNotAllowed` &#124; the term &#124; A term the relation type cannot change: a `renewal` changes only `end_date` and `value`, a `termination` only `end_date`. &#124; &#124; `AmendmentApplied` &#124; — &#124; The amendment applied when its document was signed and can no longer change. &#124; &#124; `TermOrEndDateRequired` &#124; — &#124; A renewal sends neither `term` nor `end_date`. &#124; &#124; `TermMismatch` &#124; `term` &#124; Not the contract's current term: the renewal already happened. &#124; &#124; `ContractTerminated` &#124; — &#124; A renewal of a terminated contract. &#124; &#124; `EndDateRequired` &#124; `end_date` &#124; A renewal by the term, or a `renewal` link without `end_date`, of a contract without an end date. &#124; &#124; `EndDateNotAfterCurrent` &#124; `end_date` &#124; A renewal, or a `renewal` link, to a date not after the current end date. &#124; &#124; `AlreadyTerminated` &#124; — &#124; A termination of a contract already terminated. &#124;  A body that is not valid JSON is refused before it reaches these endpoints, with the platform's `400` and no `errors`. A link is also checked against what applying it today would give the parent, so a delta the contract would refuse comes back with the contract's own codes.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 27. Get a document's contract terms

- **Java:** `client.contracts()` — `ContractResource: public ContractRecord get(String documentId)`; `public ContractRecord get(String documentId, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/documents/{documentId}/contract`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** The contract terms tracked on a document, with `amendments`: the documents that amend, renew or terminate it. For a document that is itself an amendment, its link and delta (`DocumentContractAmendment`) instead. Readable even after the workspace's plan stops including contract tracking.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `documentId` | path | string | yes | Document ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [DocumentContract](#schema-documentcontract) or [DocumentContractAmendment](#schema-documentcontractamendment)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [DocumentContract](#schema-documentcontract) or [DocumentContractAmendment](#schema-documentcontractamendment) | no | no |  |

Documented statuses: `200` The contract terms, or the amendment; `404` The document is not in the workspace (`Document not found.`), or carries no contract terms (on a read: neither its own nor those of a contract it amends whose document still exists). Only the second has a `code`, `NoContractTerms`, so a document without terms can be told from one that is not there.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 28. Set a document's contract terms

- **Java:** `client.contracts()` — `ContractResource: public ContractRecord set(String documentId, SetContractRequest request)`; `public ContractRecord set(String documentId, SetContractRequest request, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/documents/{documentId}/contract`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Creates the document's contract terms, or changes only the attributes sent (see the request body for what `null` does). `external_references`, when sent, replaces the whole list. Records a `document_contract_updated` event when the terms change; a retry that changes nothing records none. The body must be a JSON object with at least one attribute. With `parent_document_id`, the document is instead linked as an amendment, renewal or termination of that contract (or of any document in its family, resolved to the root) and the other attributes are the terms it changes; a later `PUT` on a pending amendment merges into that delta (a `null` drops a key) and `parent_document_id: null` unlinks it; every change to the link records `document_contract_amendment_updated` on this document. The delta applies to the parent once the document is certificated (at once, if it already is), in signing order (the order the documents' certifications were recorded in, fixed for each once it is; a certificated document with none recorded waits, and is placed by its certification time an hour on): instruments signed earlier apply first, one that cannot holds the later ones back, and an instrument signed before one already applied is blocked instead of applied over it. Applying records `document_contract_amended`; an applied amendment is read-only. A cancelled, declined or expired document takes no new terms and no new link; terms or a link already on one stay and can still change. Send the `updated_at` you read with the write to have it refused (`409`) if the contract or link changed meanwhile. Contract tracking comes with every standard plan, the free plan too; `403` when the workspace's current subscription lacks it.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `documentId` | path | string | yes | Document ID. |

Request body: **required**.

`application/json`: [DocumentContractInput](#schema-documentcontractinput).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | string enum[draft, active, expired, terminated] | no | no | On creation any status, so a contract that ended before it was tracked is registered as it ended: `expired` needs an `end_date` in the past, `terminated` its `end_date`; neither is walked by the lifecycle. Afterwards only `draft` or `active` (a contract in force never goes back to `draft`): `expired` and `terminated` are set by the lifecycle and the terminate endpoint; a closed contract is reopened by renewing it. |
| `counterparty_name` | string or null | no | yes | Emoji are kept; other characters outside the 3-byte range are dropped. maxLength: `255`. |
| `counterparty_taxpayer_id` | string or null | no | yes | A valid CPF or CNPJ, formatted or not. |
| `value` | number or null | no | yes | Two decimal places at most. minimum: `0`. maximum: `9999999999999.99`. |
| `currency` | string or null | no | yes | Three letters, such as `BRL` or `USD`, stored in capitals; `BRL` when a `value` is set without one. |
| `start_date` | string or null (date) | no | yes | `YYYY-MM-DD`, from the year 1000. |
| `end_date` | string or null (date) | no | yes | `YYYY-MM-DD`, on or after `start_date`. |
| `auto_renew` | boolean | no | no | Whether the hourly run starts the next term the day after `end_date`, by `renewal_term_months`, instead of expiring the contract. |
| `renewal_term_months` | integer or null | no | yes | How far a renewal by the term moves `end_date`; 12 when unset. minimum: `1`. maximum: `600`. |
| `notice_period_days` | integer or null | no | yes | Days before `end_date` the notice period starts (`document_contract_renewal_notice`); none when unset. minimum: `0`. maximum: `3650`. |
| `owner_id` | string or null | no | yes | An active member of this workspace. |
| `metadata` | object or null; values string | no | yes | Your own fields: up to 50 keys of 1 to 40 letters, digits, `_`, `-` or `.`, each holding plain text of at most 500 characters; no number under a key ending in `_at`, or `stuck_since`. |
| `external_references` | array or null | no | yes | Replaces the whole list; `[]` or `null` clears it. Two items whose `system` and `id` compare equal case aside are one reference (the first sent is kept); a pair the database takes for one beyond that (accents, `ß` and `ss`) answers `400`. maxItems: `20`. |
| `parent_document_id` | string or null | no | yes | Links the document as an amendment of this contract document, or of any document in its family (resolved to the root); `null` unlinks a pending amendment. With it, the other attributes are the terms the instrument changes, not terms of its own; on a pending amendment a `null` value drops that term from the delta rather than clearing it on the parent. |
| `relation_type` | string enum[amendment, renewal, termination] | no | no | Required on the first link. `amendment` may change `value`, `currency`, `start_date` (effective from, display only), `end_date`, `auto_renew`, `renewal_term_months`, `notice_period_days`, `counterparty_name`, `counterparty_taxpayer_id` and `metadata`; `renewal` only `end_date` and `value`; `termination` only `end_date` (the effective date). |
| `updated_at` | string (date-time) | no | no | Optional precondition: the `updated_at` the client read the contract (or this document's amendment link) with, as the API returned it. If the contract or link changed since, nothing is written and the answer is `409` with `code: ContractChanged`. Not sent, or `null`: the write applies whatever changed meanwhile (last write wins). Compared to the second; a fraction of a second, as a JavaScript date prints it, is read and ignored. New terms and a first link have nothing to have read, so sending one answers `409`; on a pending amendment, send the link's own `updated_at` (the GET on that document). |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [DocumentContract](#schema-documentcontract) or [DocumentContractAmendment](#schema-documentcontractamendment)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [DocumentContract](#schema-documentcontract) or [DocumentContractAmendment](#schema-documentcontractamendment) | no | no |  |

Documented statuses: `200` The contract terms as saved, or the amendment as linked; `400` The request breaks a contract rule. `errors` has one entry per problem found: the `field` as sent (`null` when the request as a whole is refused), a stable `code` to translate, and the `message` in the platform's language; `message` at the top is the first entry's. The contract's own rules on the terms, and the external references' items, report every field that breaks one, an entry per field (its first problem); the other checks (the body, its fields, the contract's state, a renewal's or a link's conditions) stop at the first problem they find.  &#124; Code &#124; Field &#124; Refused when &#124; &#124;------&#124;-------&#124;--------------&#124; &#124; `InvalidBody` &#124; — &#124; The body is not a JSON object (a list, a bare value), or a `PUT` of contract terms sends no attribute. &#124; &#124; `UnknownField` &#124; the attribute &#124; The endpoint does not take the attribute; one entry per attribute. &#124; &#124; `NotSingleValue` &#124; the attribute &#124; On a `PUT` of contract terms, a list sent for a term that takes one value (every term but `metadata` and `external_references`). On a link, the same list answers the term's own code. &#124; &#124; `Required` &#124; `parent_document_id`, `relation_type`, `external_references[i].system`, `external_references[i].id` &#124; A first link without `parent_document_id` (a pending link that was removed meanwhile) or `relation_type`; an `external_references` item without `system` or `id` (`i` is its place in the list sent, from 0). &#124; &#124; `Invalid` &#124; the field &#124; The value is not of the field's type or form: `status` not one of the four; `currency` not three letters; `auto_renew` not a boolean; `owner_id` not a user id; `value` not a number; `counterparty_name` holding HTML; `metadata` not an object; `external_references` not a list, an item's `system` (`external_references[i].system`) with characters other than letters, digits, `_`, `-` and `.`, or its `id` (`external_references[i].id`) with an emoji or another 4-byte character; `parent_document_id` not text; `relation_type` not `amendment`, `renewal` or `termination`; `term` not a whole number from 1; `reason` not text. On the list: a filter sent as a list, a `status` not one of the four, `ending_within_days` not a whole number. &#124; &#124; `TooSmall`, `TooBig` &#124; the field &#124; `value` below 0 or above 9999999999999.99; `renewal_term_months` outside 1 to 600; `notice_period_days` (and the list's `ending_within_days`) outside 0 to 3650. &#124; &#124; `TooLong` &#124; the field &#124; `counterparty_name` over 255 characters, counting each emoji as stored (several characters); `counterparty_taxpayer_id` over 20; `reason` over 500; `external_references[i].system` over 64 or `external_references[i].id` over 255. &#124; &#124; `TooMany` &#124; `external_references` &#124; More than 20 items. &#124; &#124; `TooManyDecimals` &#124; `value` &#124; More than two decimal places. &#124; &#124; `InvalidDate` &#124; `start_date`, `end_date`, `updated_at`; the list's `end_date_from`, `end_date_to` &#124; Not a date as `YYYY-MM-DD` (an impossible one such as `2026-02-30` included); `updated_at` not a date and time as the API writes it (`2026-10-23T12:00:00Z`, or with an offset). &#124; &#124; `DateTooEarly` &#124; `start_date`, `end_date` &#124; Before the year 1000. &#124; &#124; `DateTooLate` &#124; `end_date` &#124; A renewal by the term would pass 9999-12-31. &#124; &#124; `EndBeforeStart` &#124; `end_date` &#124; Before `start_date`. &#124; &#124; `InvalidTaxpayerId` &#124; `counterparty_taxpayer_id` &#124; Not a valid CPF or CNPJ (formatted or not). &#124; &#124; `NotAMember` &#124; `owner_id` &#124; Not an active member of the workspace. &#124; &#124; `TooManyKeys` &#124; `metadata` &#124; More than 50 keys. &#124; &#124; `InvalidKey` &#124; `metadata` &#124; A key that is not 1 to 40 letters, digits, `_`, `-` or `.`. &#124; &#124; `InvalidValue` &#124; `metadata` &#124; A value that is not plain text of at most 500 characters. &#124; &#124; `NumberUnderDateKey` &#124; `metadata` &#124; A number under a key ending in `_at`, or `stuck_since`: those keys hold text only. &#124; &#124; `ListKeys` &#124; `metadata` &#124; Keys `0`, `1`, `2`…, which would read back as a list. &#124; &#124; `DuplicateReference` &#124; `external_references` &#124; Two items the database takes for one (accents, `ß` and `ss`); items equal case aside are merged, not refused. &#124; &#124; `StatusNotSettable` &#124; `status` &#124; A tracked contract sent `expired` or `terminated`, or a closed one sent `draft` or `active`: a contract is closed by terminating it and reopened by renewing it. &#124; &#124; `CannotReturnToDraft` &#124; `status` &#124; An `active` contract sent `draft`. &#124; &#124; `ExpiredNeedsPastEndDate` &#124; `end_date` &#124; New terms with status `expired` and no `end_date` before today. &#124; &#124; `TerminatedNeedsEndDate` &#124; `end_date` &#124; New terms with status `terminated` and no `end_date`. &#124; &#124; `UnsignableDocument` &#124; — &#124; New terms, or a first link, on a cancelled, declined or expired document. Terms or a link already on one stay editable. &#124; &#124; `DocumentIsAmendment` &#124; — &#124; Terms of its own for a document linked as an amendment: its terms are the changes it makes. &#124; &#124; `DocumentHasOwnTerms` &#124; — &#124; A link for a document that tracks a contract of its own: a document is a contract or an amendment, never both. &#124; &#124; `ParentHasNoTerms` &#124; `parent_document_id` &#124; The parent named has no contract terms, nor amends a contract that has. &#124; &#124; `ParentTerminated` &#124; `parent_document_id` &#124; The parent contract is terminated. &#124; &#124; `NoTermChanged` &#124; — &#124; An `amendment` that changes no term, also by dropping its last one. A `renewal` or `termination` may change none. &#124; &#124; `TermNotAllowed` &#124; the term &#124; A term the relation type cannot change: a `renewal` changes only `end_date` and `value`, a `termination` only `end_date`. &#124; &#124; `AmendmentApplied` &#124; — &#124; The amendment applied when its document was signed and can no longer change. &#124; &#124; `TermOrEndDateRequired` &#124; — &#124; A renewal sends neither `term` nor `end_date`. &#124; &#124; `TermMismatch` &#124; `term` &#124; Not the contract's current term: the renewal already happened. &#124; &#124; `ContractTerminated` &#124; — &#124; A renewal of a terminated contract. &#124; &#124; `EndDateRequired` &#124; `end_date` &#124; A renewal by the term, or a `renewal` link without `end_date`, of a contract without an end date. &#124; &#124; `EndDateNotAfterCurrent` &#124; `end_date` &#124; A renewal, or a `renewal` link, to a date not after the current end date. &#124; &#124; `AlreadyTerminated` &#124; — &#124; A termination of a contract already terminated. &#124;  A body that is not valid JSON is refused before it reaches these endpoints, with the platform's `400` and no `errors`. A link is also checked against what applying it today would give the parent, so a delta the contract would refuse comes back with the contract's own codes.; `403` Contract tracking is not available to the workspace (`PlanRestricted`): its current subscription lacks it (a custom contract that leaves it out, or an administrator's removal), or it is turned off for the whole service. It comes with every standard plan, the free plan too.; `409` The write sent the `updated_at` it read the contract (or the amendment link) with, and the contract changed since: by another user, an integration or the hourly run. Nothing was written. Read it again, check the change still applies, and send it with the new `updated_at`.; `404` The document is not in the workspace, the `parent_document_id` names a document outside it, or `parent_document_id: null` is sent for a document that is not an amendment.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 29. List the contracts related to a document

- **Java:** `client.contracts()` — `ContractResource: public DocumentContract listRelated(String documentId)`; `public DocumentContract listRelated(String documentId, String accountId)`; `client.contracts()` — `ContractResource: public DocumentContract listRelated(String documentId, String accountId, boolean includeDocument)`
- **HTTP:** `GET /v1/accounts/{accountId}/documents/{documentId}/contract/related`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** The family the document belongs to, seen from its root: the contract terms with every amendment, renewal and termination document linked to them (`amendments`). Works from the contract document and from any of its amendments alike.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `documentId` | path | string | yes | Document ID. |
| `expand` | query | string enum[document] | no | `document` embeds the root contract's document, its `name` included, so an amendment's page names its parent without a second read; each entry of `amendments` already carries its own `document_name`. Without it the answer is as before. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [DocumentContract](#schema-documentcontract)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [DocumentContract](#schema-documentcontract) | no | no |  |

Documented statuses: `200` The root contract and its family; `404` The document is not in the workspace (`Document not found.`), or carries no contract terms (on a read: neither its own nor those of a contract it amends whose document still exists). Only the second has a `code`, `NoContractTerms`, so a document without terms can be told from one that is not there.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 30. Renew a contract

- **Java:** `client.contracts()` — `ContractResource: public DocumentContract renew(String documentId, RenewContractRequest request)`; `public DocumentContract renew(String documentId, RenewContractRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/documents/{documentId}/contract/renew`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Starts the contract's next term: `end_date` moves by `renewal_term_months` (12 when unset), or to the `end_date` sent, which must be after the current one. A `value` sent replaces the current one. Reopens an expired contract; a terminated one returns `400`. Steps `term`, so the lifecycle alerts run again for the new term. Records a `document_contract_renewed` event. Contract tracking comes with every standard plan, the free plan too; `403` when the workspace's current subscription lacks it. The body names the `term` the renewal starts from (the contract's current one) or the `end_date` it renews to, so a renewal answered with a `5xx` error, or whose answer was lost, is safe to send again: the repeat is refused with `400`, as the contract is then in its next term and that date is no longer after the current one (or with `409`, when it sends the `updated_at` it read).

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `documentId` | path | string | yes | Document ID. |

Request body: **optional**.

`application/json`: object{`end_date`: string or null (date); `value`: number or null; `term`: integer; `updated_at`: string (date-time)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `end_date` | string or null (date) | no | yes | The new end date, after the current one. Absent, empty or `null`: the current end date plus the renewal term, stepped until it is in the future; `term` is then required. |
| `value` | number or null | no | yes | The new value; unchanged when not sent, cleared by `null`. |
| `term` | integer | no | no | The term this renewal starts from, the contract's current `term`. Required unless `end_date` is sent; for a contract in another term the renewal is refused, so a repeated request never renews twice. minimum: `1`. |
| `updated_at` | string (date-time) | no | no | Optional precondition: the contract's `updated_at` as read. Changed since: `409`, nothing renewed. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [DocumentContract](#schema-documentcontract)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [DocumentContract](#schema-documentcontract) | no | no |  |

Documented statuses: `200` The renewed contract; `400` The request breaks a contract rule. `errors` has one entry per problem found: the `field` as sent (`null` when the request as a whole is refused), a stable `code` to translate, and the `message` in the platform's language; `message` at the top is the first entry's. The contract's own rules on the terms, and the external references' items, report every field that breaks one, an entry per field (its first problem); the other checks (the body, its fields, the contract's state, a renewal's or a link's conditions) stop at the first problem they find.  &#124; Code &#124; Field &#124; Refused when &#124; &#124;------&#124;-------&#124;--------------&#124; &#124; `InvalidBody` &#124; — &#124; The body is not a JSON object (a list, a bare value), or a `PUT` of contract terms sends no attribute. &#124; &#124; `UnknownField` &#124; the attribute &#124; The endpoint does not take the attribute; one entry per attribute. &#124; &#124; `NotSingleValue` &#124; the attribute &#124; On a `PUT` of contract terms, a list sent for a term that takes one value (every term but `metadata` and `external_references`). On a link, the same list answers the term's own code. &#124; &#124; `Required` &#124; `parent_document_id`, `relation_type`, `external_references[i].system`, `external_references[i].id` &#124; A first link without `parent_document_id` (a pending link that was removed meanwhile) or `relation_type`; an `external_references` item without `system` or `id` (`i` is its place in the list sent, from 0). &#124; &#124; `Invalid` &#124; the field &#124; The value is not of the field's type or form: `status` not one of the four; `currency` not three letters; `auto_renew` not a boolean; `owner_id` not a user id; `value` not a number; `counterparty_name` holding HTML; `metadata` not an object; `external_references` not a list, an item's `system` (`external_references[i].system`) with characters other than letters, digits, `_`, `-` and `.`, or its `id` (`external_references[i].id`) with an emoji or another 4-byte character; `parent_document_id` not text; `relation_type` not `amendment`, `renewal` or `termination`; `term` not a whole number from 1; `reason` not text. On the list: a filter sent as a list, a `status` not one of the four, `ending_within_days` not a whole number. &#124; &#124; `TooSmall`, `TooBig` &#124; the field &#124; `value` below 0 or above 9999999999999.99; `renewal_term_months` outside 1 to 600; `notice_period_days` (and the list's `ending_within_days`) outside 0 to 3650. &#124; &#124; `TooLong` &#124; the field &#124; `counterparty_name` over 255 characters, counting each emoji as stored (several characters); `counterparty_taxpayer_id` over 20; `reason` over 500; `external_references[i].system` over 64 or `external_references[i].id` over 255. &#124; &#124; `TooMany` &#124; `external_references` &#124; More than 20 items. &#124; &#124; `TooManyDecimals` &#124; `value` &#124; More than two decimal places. &#124; &#124; `InvalidDate` &#124; `start_date`, `end_date`, `updated_at`; the list's `end_date_from`, `end_date_to` &#124; Not a date as `YYYY-MM-DD` (an impossible one such as `2026-02-30` included); `updated_at` not a date and time as the API writes it (`2026-10-23T12:00:00Z`, or with an offset). &#124; &#124; `DateTooEarly` &#124; `start_date`, `end_date` &#124; Before the year 1000. &#124; &#124; `DateTooLate` &#124; `end_date` &#124; A renewal by the term would pass 9999-12-31. &#124; &#124; `EndBeforeStart` &#124; `end_date` &#124; Before `start_date`. &#124; &#124; `InvalidTaxpayerId` &#124; `counterparty_taxpayer_id` &#124; Not a valid CPF or CNPJ (formatted or not). &#124; &#124; `NotAMember` &#124; `owner_id` &#124; Not an active member of the workspace. &#124; &#124; `TooManyKeys` &#124; `metadata` &#124; More than 50 keys. &#124; &#124; `InvalidKey` &#124; `metadata` &#124; A key that is not 1 to 40 letters, digits, `_`, `-` or `.`. &#124; &#124; `InvalidValue` &#124; `metadata` &#124; A value that is not plain text of at most 500 characters. &#124; &#124; `NumberUnderDateKey` &#124; `metadata` &#124; A number under a key ending in `_at`, or `stuck_since`: those keys hold text only. &#124; &#124; `ListKeys` &#124; `metadata` &#124; Keys `0`, `1`, `2`…, which would read back as a list. &#124; &#124; `DuplicateReference` &#124; `external_references` &#124; Two items the database takes for one (accents, `ß` and `ss`); items equal case aside are merged, not refused. &#124; &#124; `StatusNotSettable` &#124; `status` &#124; A tracked contract sent `expired` or `terminated`, or a closed one sent `draft` or `active`: a contract is closed by terminating it and reopened by renewing it. &#124; &#124; `CannotReturnToDraft` &#124; `status` &#124; An `active` contract sent `draft`. &#124; &#124; `ExpiredNeedsPastEndDate` &#124; `end_date` &#124; New terms with status `expired` and no `end_date` before today. &#124; &#124; `TerminatedNeedsEndDate` &#124; `end_date` &#124; New terms with status `terminated` and no `end_date`. &#124; &#124; `UnsignableDocument` &#124; — &#124; New terms, or a first link, on a cancelled, declined or expired document. Terms or a link already on one stay editable. &#124; &#124; `DocumentIsAmendment` &#124; — &#124; Terms of its own for a document linked as an amendment: its terms are the changes it makes. &#124; &#124; `DocumentHasOwnTerms` &#124; — &#124; A link for a document that tracks a contract of its own: a document is a contract or an amendment, never both. &#124; &#124; `ParentHasNoTerms` &#124; `parent_document_id` &#124; The parent named has no contract terms, nor amends a contract that has. &#124; &#124; `ParentTerminated` &#124; `parent_document_id` &#124; The parent contract is terminated. &#124; &#124; `NoTermChanged` &#124; — &#124; An `amendment` that changes no term, also by dropping its last one. A `renewal` or `termination` may change none. &#124; &#124; `TermNotAllowed` &#124; the term &#124; A term the relation type cannot change: a `renewal` changes only `end_date` and `value`, a `termination` only `end_date`. &#124; &#124; `AmendmentApplied` &#124; — &#124; The amendment applied when its document was signed and can no longer change. &#124; &#124; `TermOrEndDateRequired` &#124; — &#124; A renewal sends neither `term` nor `end_date`. &#124; &#124; `TermMismatch` &#124; `term` &#124; Not the contract's current term: the renewal already happened. &#124; &#124; `ContractTerminated` &#124; — &#124; A renewal of a terminated contract. &#124; &#124; `EndDateRequired` &#124; `end_date` &#124; A renewal by the term, or a `renewal` link without `end_date`, of a contract without an end date. &#124; &#124; `EndDateNotAfterCurrent` &#124; `end_date` &#124; A renewal, or a `renewal` link, to a date not after the current end date. &#124; &#124; `AlreadyTerminated` &#124; — &#124; A termination of a contract already terminated. &#124;  A body that is not valid JSON is refused before it reaches these endpoints, with the platform's `400` and no `errors`. A link is also checked against what applying it today would give the parent, so a delta the contract would refuse comes back with the contract's own codes.; `403` Contract tracking is not available to the workspace (`PlanRestricted`): its current subscription lacks it (a custom contract that leaves it out, or an administrator's removal), or it is turned off for the whole service. It comes with every standard plan, the free plan too.; `409` The write sent the `updated_at` it read the contract (or the amendment link) with, and the contract changed since: by another user, an integration or the hourly run. Nothing was written. Read it again, check the change still applies, and send it with the new `updated_at`.; `404` The document is not in the workspace (`Document not found.`), or carries no contract terms (on a read: neither its own nor those of a contract it amends whose document still exists). Only the second has a `code`, `NoContractTerms`, so a document without terms can be told from one that is not there.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 31. Terminate a contract

- **Java:** `client.contracts()` — `ContractResource: public DocumentContract terminate(String documentId)`; `public DocumentContract terminate(String documentId, TerminateContractRequest request)`; `public DocumentContract terminate(String documentId, TerminateContractRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/documents/{documentId}/contract/terminate`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Closes the contract: its status becomes `terminated` and the lifecycle job stops alerting on it. Records a `document_contract_terminated` event carrying the `reason`. Contract tracking comes with every standard plan, the free plan too; `403` when the workspace's current subscription lacks it.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `documentId` | path | string | yes | Document ID. |

Request body: **optional**.

`application/json`: object{`reason`: string or null; `updated_at`: string (date-time)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `reason` | string or null | no | yes | Kept in the `document_contract_terminated` event; HTML is stripped. maxLength: `500`. |
| `updated_at` | string (date-time) | no | no | Optional precondition: the contract's `updated_at` as read. Changed since: `409`, nothing terminated. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [DocumentContract](#schema-documentcontract)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [DocumentContract](#schema-documentcontract) | no | no |  |

Documented statuses: `200` The terminated contract; `400` The request breaks a contract rule. `errors` has one entry per problem found: the `field` as sent (`null` when the request as a whole is refused), a stable `code` to translate, and the `message` in the platform's language; `message` at the top is the first entry's. The contract's own rules on the terms, and the external references' items, report every field that breaks one, an entry per field (its first problem); the other checks (the body, its fields, the contract's state, a renewal's or a link's conditions) stop at the first problem they find.  &#124; Code &#124; Field &#124; Refused when &#124; &#124;------&#124;-------&#124;--------------&#124; &#124; `InvalidBody` &#124; — &#124; The body is not a JSON object (a list, a bare value), or a `PUT` of contract terms sends no attribute. &#124; &#124; `UnknownField` &#124; the attribute &#124; The endpoint does not take the attribute; one entry per attribute. &#124; &#124; `NotSingleValue` &#124; the attribute &#124; On a `PUT` of contract terms, a list sent for a term that takes one value (every term but `metadata` and `external_references`). On a link, the same list answers the term's own code. &#124; &#124; `Required` &#124; `parent_document_id`, `relation_type`, `external_references[i].system`, `external_references[i].id` &#124; A first link without `parent_document_id` (a pending link that was removed meanwhile) or `relation_type`; an `external_references` item without `system` or `id` (`i` is its place in the list sent, from 0). &#124; &#124; `Invalid` &#124; the field &#124; The value is not of the field's type or form: `status` not one of the four; `currency` not three letters; `auto_renew` not a boolean; `owner_id` not a user id; `value` not a number; `counterparty_name` holding HTML; `metadata` not an object; `external_references` not a list, an item's `system` (`external_references[i].system`) with characters other than letters, digits, `_`, `-` and `.`, or its `id` (`external_references[i].id`) with an emoji or another 4-byte character; `parent_document_id` not text; `relation_type` not `amendment`, `renewal` or `termination`; `term` not a whole number from 1; `reason` not text. On the list: a filter sent as a list, a `status` not one of the four, `ending_within_days` not a whole number. &#124; &#124; `TooSmall`, `TooBig` &#124; the field &#124; `value` below 0 or above 9999999999999.99; `renewal_term_months` outside 1 to 600; `notice_period_days` (and the list's `ending_within_days`) outside 0 to 3650. &#124; &#124; `TooLong` &#124; the field &#124; `counterparty_name` over 255 characters, counting each emoji as stored (several characters); `counterparty_taxpayer_id` over 20; `reason` over 500; `external_references[i].system` over 64 or `external_references[i].id` over 255. &#124; &#124; `TooMany` &#124; `external_references` &#124; More than 20 items. &#124; &#124; `TooManyDecimals` &#124; `value` &#124; More than two decimal places. &#124; &#124; `InvalidDate` &#124; `start_date`, `end_date`, `updated_at`; the list's `end_date_from`, `end_date_to` &#124; Not a date as `YYYY-MM-DD` (an impossible one such as `2026-02-30` included); `updated_at` not a date and time as the API writes it (`2026-10-23T12:00:00Z`, or with an offset). &#124; &#124; `DateTooEarly` &#124; `start_date`, `end_date` &#124; Before the year 1000. &#124; &#124; `DateTooLate` &#124; `end_date` &#124; A renewal by the term would pass 9999-12-31. &#124; &#124; `EndBeforeStart` &#124; `end_date` &#124; Before `start_date`. &#124; &#124; `InvalidTaxpayerId` &#124; `counterparty_taxpayer_id` &#124; Not a valid CPF or CNPJ (formatted or not). &#124; &#124; `NotAMember` &#124; `owner_id` &#124; Not an active member of the workspace. &#124; &#124; `TooManyKeys` &#124; `metadata` &#124; More than 50 keys. &#124; &#124; `InvalidKey` &#124; `metadata` &#124; A key that is not 1 to 40 letters, digits, `_`, `-` or `.`. &#124; &#124; `InvalidValue` &#124; `metadata` &#124; A value that is not plain text of at most 500 characters. &#124; &#124; `NumberUnderDateKey` &#124; `metadata` &#124; A number under a key ending in `_at`, or `stuck_since`: those keys hold text only. &#124; &#124; `ListKeys` &#124; `metadata` &#124; Keys `0`, `1`, `2`…, which would read back as a list. &#124; &#124; `DuplicateReference` &#124; `external_references` &#124; Two items the database takes for one (accents, `ß` and `ss`); items equal case aside are merged, not refused. &#124; &#124; `StatusNotSettable` &#124; `status` &#124; A tracked contract sent `expired` or `terminated`, or a closed one sent `draft` or `active`: a contract is closed by terminating it and reopened by renewing it. &#124; &#124; `CannotReturnToDraft` &#124; `status` &#124; An `active` contract sent `draft`. &#124; &#124; `ExpiredNeedsPastEndDate` &#124; `end_date` &#124; New terms with status `expired` and no `end_date` before today. &#124; &#124; `TerminatedNeedsEndDate` &#124; `end_date` &#124; New terms with status `terminated` and no `end_date`. &#124; &#124; `UnsignableDocument` &#124; — &#124; New terms, or a first link, on a cancelled, declined or expired document. Terms or a link already on one stay editable. &#124; &#124; `DocumentIsAmendment` &#124; — &#124; Terms of its own for a document linked as an amendment: its terms are the changes it makes. &#124; &#124; `DocumentHasOwnTerms` &#124; — &#124; A link for a document that tracks a contract of its own: a document is a contract or an amendment, never both. &#124; &#124; `ParentHasNoTerms` &#124; `parent_document_id` &#124; The parent named has no contract terms, nor amends a contract that has. &#124; &#124; `ParentTerminated` &#124; `parent_document_id` &#124; The parent contract is terminated. &#124; &#124; `NoTermChanged` &#124; — &#124; An `amendment` that changes no term, also by dropping its last one. A `renewal` or `termination` may change none. &#124; &#124; `TermNotAllowed` &#124; the term &#124; A term the relation type cannot change: a `renewal` changes only `end_date` and `value`, a `termination` only `end_date`. &#124; &#124; `AmendmentApplied` &#124; — &#124; The amendment applied when its document was signed and can no longer change. &#124; &#124; `TermOrEndDateRequired` &#124; — &#124; A renewal sends neither `term` nor `end_date`. &#124; &#124; `TermMismatch` &#124; `term` &#124; Not the contract's current term: the renewal already happened. &#124; &#124; `ContractTerminated` &#124; — &#124; A renewal of a terminated contract. &#124; &#124; `EndDateRequired` &#124; `end_date` &#124; A renewal by the term, or a `renewal` link without `end_date`, of a contract without an end date. &#124; &#124; `EndDateNotAfterCurrent` &#124; `end_date` &#124; A renewal, or a `renewal` link, to a date not after the current end date. &#124; &#124; `AlreadyTerminated` &#124; — &#124; A termination of a contract already terminated. &#124;  A body that is not valid JSON is refused before it reaches these endpoints, with the platform's `400` and no `errors`. A link is also checked against what applying it today would give the parent, so a delta the contract would refuse comes back with the contract's own codes.; `403` Contract tracking is not available to the workspace (`PlanRestricted`): its current subscription lacks it (a custom contract that leaves it out, or an administrator's removal), or it is turned off for the whole service. It comes with every standard plan, the free plan too.; `409` The write sent the `updated_at` it read the contract (or the amendment link) with, and the contract changed since: by another user, an integration or the hourly run. Nothing was written. Read it again, check the change still applies, and send it with the new `updated_at`.; `404` The document is not in the workspace (`Document not found.`), or carries no contract terms (on a read: neither its own nor those of a contract it amends whose document still exists). Only the second has a `code`, `NoContractTerms`, so a document without terms can be told from one that is not there.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 32. List documents

- **Java:** `client.documents()` — `DocumentResource: public PaginatedResult<Document> list()`; `client.documents()` — `DocumentResource: public PaginatedResult<Document> list(ListParams params)`; `client.documents()` — `DocumentResource: public PaginatedResult<Document> list(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/documents`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List documents of the workspace, most recently updated first. Every row includes its `assignment` and `pages`; an `expand` parameter is ignored. For a lighter list without them, use `GET /v1/accounts/{accountId}/documents/search`.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `status` | query | string | no | Status filter, e.g. `pending_signature`. |
| `envelope_id` | query | string | no | Only the members of this envelope. Anything but text answers `400`. |
| `method` | query | string enum[virtual, collect] | no | Signature method filter. |
| `search` | query | string | no | Partial match on document.name, signer.full_name, signer.email. |
| `tags` | query | string | no | Comma-separated tag IDs; returns documents having ALL listed tags. |
| `sort` | query | string | no | `name` or `updated_at`, ascending; prefix with `-` for descending (`-updated_at`, the default). Other values are ignored. |
| `page` | query | integer | no | Page number. |
| `per-page` | query | integer | no | Records per page: 20 by default, at most 50; a larger value answers 50 a page. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[Document](#schema-document)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[Document](#schema-document)> | no | no |  |

Documented statuses: `200` A page of documents; `400` `envelope_id` refused as sent: not text, an entry of `errors`.; `403` Authenticated but not allowed to perform this action.; `404` The workspace does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 33. Upload and create document

- **Java:** `client.documents()` — `DocumentResource: public Document upload(byte[] fileData, String fileName)`; `client.documents()` — `DocumentResource: public Document upload(byte[] fileData, String fileName, Map<String, Object> metadata, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/documents`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Create a document from an uploaded file. Maximum file size 25MB; maximum 2000 pages.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: **required**.

`multipart/form-data`: object{`file!`: string (binary)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `file` | string (binary) | yes | no | The PDF file to upload. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Document](#schema-document)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Document](#schema-document) | no | no |  |

Documented statuses: `200` The created document; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 34. Search documents (lightweight)

- **Java:** `client.documents()` — `DocumentResource: public PaginatedResult<Document> search(ListParams params)`; `client.documents()` — `DocumentResource: public PaginatedResult<Document> search(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/documents/search`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Search documents of the workspace, returning a compact representation (no expanded assignment/pages).

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `search` | query | string | no | Search term. |
| `status` | query | string | no |  |
| `envelope_id` | query | string | no | Only the members of this envelope. Anything but text answers `400`. |
| `sort` | query | string | no | `name` or `updated_at`, ascending; prefix with `-` for descending (`-updated_at`, the default). Other values are ignored. |
| `page` | query | integer | no | Page number. |
| `per-page` | query | integer | no | Records per page: 20 by default, at most 50; a larger value answers 50 a page. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[Document](#schema-document)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[Document](#schema-document)> | no | no |  |

Documented statuses: `200` Matching documents; `400` `envelope_id` refused as sent: not text, an entry of `errors`.; `403` Authenticated but not allowed to perform this action.; `404` The workspace does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 35. List document statuses

- **Java:** `client.documents()` — `DocumentResource: public List<DocumentStatusInfo> getStatuses()`
- **HTTP:** `GET /v1/documents/statuses`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** The supported document statuses and whether a document in each status can be deleted.  &#124; Status &#124; Deletable &#124; Description &#124; &#124;--------&#124;-----------&#124;-------------&#124; &#124; `uploading` &#124; no &#124; The document upload is in process. &#124; &#124; `uploaded` &#124; no &#124; The document has been uploaded. &#124; &#124; `metadata_processing` &#124; no &#124; The initial processing is under way. &#124; &#124; `metadata_ready` &#124; yes &#124; The initial processing has been completed. &#124; &#124; `expired` &#124; yes &#124; The signature deadline has been reached. &#124; &#124; `certificating` &#124; no &#124; The document has been signed and is being certificated. &#124; &#124; `certificated` &#124; no &#124; The document is certificated. &#124; &#124; `rejected_by_signer` &#124; yes &#124; A signer declined signing the document. &#124; &#124; `pending_signature` &#124; yes &#124; The document is waiting for signatures. &#124; &#124; `pending_identity_review` &#124; yes &#124; Every signer has signed; the document waits for the account to review a signer's identity before it is certificated. Deleting it cancels it and notifies the signers. &#124; &#124; `rejected_by_user` &#124; yes &#124; The signature process was cancelled by a user. &#124; &#124; `failed` &#124; yes &#124; The document processing has failed. &#124;

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[DocumentStatus](#schema-documentstatus)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[DocumentStatus](#schema-documentstatus)> | no | no |  |

Documented statuses: `200` Supported statuses; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 36. Get document

- **Java:** `client.documents()` — `DocumentResource: public Document details(String documentId)`; `client.documents()` — `DocumentResource: public Document details(String documentId, boolean includeEnvelope)`
- **HTTP:** `GET /v1/documents/{documentId}`
- **Auth:** Bearer token or `X-Api-Key` or `signer-access-code` query credential
- **Contract notes:** Get a document by its ID. `decline_reason` is only present when the access token belongs to the document's creator. With a signer access code (`signer-access-code`), only documents the signer has been sent are found, and `assignment` holds only the signer's own items, without `signers`, `signing_urls`, `copy_receivers` or `summary`.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `expand` | query | array<string enum[envelope_id, envelope]> | no | `envelope_id` adds the id of the envelope the document was sent in, `envelope` its id and name; each is `null` for a document sent on its own. Send both separated by a comma. Read by the workspace only; without it the document reads as it always has. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Document](#schema-document)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Document](#schema-document) | no | no |  |

Documented statuses: `200` The document; `404` The requested resource does not exist.; `403` Authenticated but not allowed to perform this action.; `400` With a signer access code: the signer's step has not started yet (sequential signing).; `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 37. Delete document

- **Java:** `client.documents()` — `DocumentResource: public void delete(String documentId)`
- **HTTP:** `DELETE /v1/documents/{documentId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Delete a document by its ID. Only documents in a deletable status can be removed (see GET /v1/documents/statuses). A document of an envelope, or one taken out of it, can't be deleted while any of the envelope's documents is open: cancel the envelope (`PUT /v1/accounts/{accountId}/envelopes/{envelopeId}/cancel`) first.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no |  |

Documented statuses: `200` Document deleted; `404` The requested resource does not exist.; `409` The document belongs to an envelope that is still open (`EnvelopeOpen`).; `400` The document is certificated, or still being uploaded or processed.; `403` Authenticated but not allowed to perform this action.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 38. Rename document

- **Java:** `client.documents()` — `DocumentResource: public Document rename(String documentId, String newName)`
- **HTTP:** `PATCH /v1/documents/{documentId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Update a document's name. Only allowed before any assignment is created (i.e. while the document is in `uploaded` or `metadata_ready` status and has no signers yet); once the signature process has started or the document is certificated, the name is locked. The name is normalized: diacritics are removed and unsupported characters are replaced with dashes.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |

Request body: **required**.

`application/json`: object{`name!`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | string | yes | no |  maxLength: `255`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Document](#schema-document)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Document](#schema-document) | no | no |  |

Documented statuses: `200` The updated document; `400` One or more fields failed validation.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 39. Download document artifact

- **Java:** `client.documents()` — `DocumentResource: public byte[] download(String documentId)`; `client.documents()` — `DocumentResource: public byte[] download(String documentId, String artifactName)`
- **HTTP:** `GET /v1/documents/{documentId}/download/{artifactName}`
- **Auth:** Bearer token or `X-Api-Key` or `signer-access-code` query credential
- **Contract notes:** Download a document artifact. With a signer access code only `original` is available (`403` otherwise): signers get the certified copy by e-mail or WhatsApp. Artifact types: original, certificated, certificate-page, pades, bundle. The pades artifact (signers' ICP-Brasil signatures + platform certification box) is only present on documents that had digital-certificate signers; `bundle` is a zip of the original, certificated and certificate-page artifacts, plus the pades artifact on documents that have one.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `artifactName` | path | string enum[original, certificated, certificate-page, pades, bundle] | yes | Artifact type. |

Request body: none.

Success `200` `application/pdf`: string (binary).


Documented statuses: `200` The artifact binary; `403` A signer access code asked for an artifact other than `original`; or, with a user's token or key, a deactivated membership, a role or an OAuth scope refuses it.; `404` The requested resource does not exist.; `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `400` With a signer access code: the signer's step has not started yet (sequential signing).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 40. Verify a signed document

- **Java:** `client.documents()` — `DocumentResource: public Map<String, Object> verify(String hash)`; `client.documents()` — `DocumentResource: public DocumentVerification verifyTyped(String hash)`
- **HTTP:** `GET /v1/documents/{documentSignatureHash}/verify`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** Verify a document by its signature hash (found on a signed document) and return its certification details. Always returns `200`: when the hash is not found or the document is not signed, `is_valid` is `false`, the other fields are `null`, and `message` explains why. Public endpoint.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentSignatureHash` | path | string | yes | The document signature hash. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [DocumentVerification](#schema-documentverification)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [DocumentVerification](#schema-documentverification) | no | no |  |

Documented statuses: `200` Verification result; `500` Unexpected server error..

### 41. List document tags

- **Java:** `client.documents()` — `DocumentResource: public List<Tag> listTags(String documentId)`; `client.documents()` — `DocumentResource: public List<Tag> listTags(String documentId, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/documents/{documentId}/tags`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List the tags attached to a document.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `documentId` | path | string | yes | The document ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[Tag](#schema-tag)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[Tag](#schema-tag)> | no | no |  |

Documented statuses: `200` Attached tags; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 42. Replace document tags

- **Java:** `client.documents()` — `DocumentResource: public List<Tag> replaceTagIds(String documentId, List<String> tagIds)`; `client.documents()` — `DocumentResource: public List<Tag> replaceTagIds(String documentId, List<String> tagIds, String accountId)`; name-based convenience overloads: `replaceTags(String documentId, List<String> tagNames)` and `replaceTags(String documentId, List<String> tagNames, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/documents/{documentId}/tags`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Replace the full set of tags attached to a document.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `documentId` | path | string | yes | The document ID. |

Request body: **required**.

`application/json`: object{}.


Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[Tag](#schema-tag)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[Tag](#schema-tag)> | no | no |  |

Documented statuses: `200` Updated tags; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 43. Attach document tags

- **Java:** `client.documents()` — `DocumentResource: public List<Tag> appendTagIds(String documentId, List<String> tagIds)`; `client.documents()` — `DocumentResource: public List<Tag> appendTagIds(String documentId, List<String> tagIds, String accountId)`; name-based convenience overloads: `appendTags(String documentId, List<String> tagNames)` and `appendTags(String documentId, List<String> tagNames, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/documents/{documentId}/tags`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Attach one or more tags to a document.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `documentId` | path | string | yes | The document ID. |

Request body: **required**.

`application/json`: object{}.


Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[Tag](#schema-tag)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[Tag](#schema-tag)> | no | no |  |

Documented statuses: `200` Attached tags; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 44. Detach document tag

- **Java:** `client.documents()` — `DocumentResource: public void detachTag(String documentId, String tagId)`; `client.documents()` — `DocumentResource: public void detachTag(String documentId, String tagId, String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/documents/{documentId}/tags/{tagId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Detach a single tag from a document.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `documentId` | path | string | yes | The document ID. |
| `tagId` | path | string | yes | The tag ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`detached`: boolean}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`detached`: boolean} | no | no |  |

Documented statuses: `200` Tag detached; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 45. List embed origins

- **Java:** `client.embed()` — `EmbedResource: public List<EmbedOrigin> listOrigins(String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/embed-origins`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List the origins allowed to embed the workspace's signing pages, oldest first. Returns `403` with code `PlanRestricted` when the workspace's plan does not include embedded signing. Not available to OAuth applications.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[EmbedOrigin](#schema-embedorigin)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[EmbedOrigin](#schema-embedorigin)> | no | no |  |

Documented statuses: `200` The allowed origins; `403` Authenticated but not allowed to perform this action.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 46. Add embed origin

- **Java:** `client.embed()` — `EmbedResource: public EmbedOrigin addOrigin(String origin, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/embed-origins`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Allow one more origin to embed the workspace's signing pages. An origin is an exact `scheme://host[:port]`: `https` (or `http` for `localhost` and `127.0.0.1` only), with no path, query, fragment, credentials or wildcard. The host is lowercased and a trailing `/` and the default port are dropped. Returns `400` when the origin is invalid, already registered, or the workspace already has 10 origins. Returns `403` with code `PlanRestricted` when the workspace's plan does not include embedded signing. Not available to OAuth applications.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: **required**.

`application/json`: object{`origin!`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `origin` | string | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [EmbedOrigin](#schema-embedorigin)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [EmbedOrigin](#schema-embedorigin) | no | no |  |

Documented statuses: `200` The origin added; `400` One or more fields failed validation.; `403` Authenticated but not allowed to perform this action.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 47. Update embed origin

- **Java:** `client.embed()` — `EmbedResource: public EmbedOrigin updateOrigin(String originId, String origin, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/embed-origins/{originId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Replace one allowed origin with another, keeping its `id`. The same rules as **Add embed origin** apply. Returns `400` when the new origin is invalid or another entry already has it. Returns `403` with code `PlanRestricted` when the workspace's plan does not include embedded signing. Not available to OAuth applications.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `originId` | path | string | yes | The embed origin ID. |

Request body: **required**.

`application/json`: object{`origin!`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `origin` | string | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [EmbedOrigin](#schema-embedorigin)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [EmbedOrigin](#schema-embedorigin) | no | no |  |

Documented statuses: `200` The updated origin; `400` One or more fields failed validation.; `403` Authenticated but not allowed to perform this action.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 48. Remove embed origin

- **Java:** `client.embed()` — `EmbedResource: public void deleteOrigin(String originId, String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/embed-origins/{originId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Stop allowing an origin to embed the workspace's signing pages. Removing the last origin disables embedding: new sessions return `409` with code `embed_origins_missing`. Returns `403` with code `PlanRestricted` when the workspace's plan does not include embedded signing. Not available to OAuth applications.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `originId` | path | string | yes | The embed origin ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no |  |

Documented statuses: `200` Origin removed; `403` Authenticated but not allowed to perform this action.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 49. Create an embed session

- **Java:** `client.embed()` — `EmbedResource: public EmbedSession createSession(String documentId, String assignmentId, String signerId)`
- **HTTP:** `POST /v1/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/embed-sessions`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Mint a single-use embed URL for a signer created with `embedded: true`. Load the URL in an iframe on one of the workspace's embed origins within 5 minutes; opening it spends the token, so mint a fresh one each time the signer opens the page. The signer's access code is never returned. Refused with `400` (`embed_not_enabled_for_signer`) for a signer that is not embedded, `409` when the signer already finished (`already_signed`), the document is still being uploaded or processed (`document_not_ready`: retry in a moment), the document can no longer be signed or the signer's step has not started (`document_unavailable`), or the workspace has no embed origin (`embed_origins_missing`), `403` with code `PlanRestricted` when the plan does not include embedded signing, and `429` after 30 sessions for one signer in an hour.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `assignmentId` | path | string | yes | The assignment ID. |
| `signerId` | path | string | yes | The signer ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [EmbedSession](#schema-embedsession)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [EmbedSession](#schema-embedsession) | no | no |  |

Documented statuses: `200` The embed session; `400` The request was rejected because of its current state or invalid input.; `403` Authenticated but not allowed to perform this action.; `404` The document, assignment or signer was not found.; `409` The signer can not sign right now (`already_signed`, `document_not_ready`, `document_unavailable`), or the workspace has no embed origin (`embed_origins_missing`).; `429` More than 30 sessions for this signer in the last hour.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 50. Exchange an embed session

- **Java:** `client.embed()` — `EmbedResource: public EmbedSessionExchange exchangeSession(String token, String parentOrigin)`
- **HTTP:** `POST /v1/embed/sessions/{token}/exchange`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** Called by Assinafy's embed page, not by integrations. Public: the token is the credential. Trades a session token for the signer's access code, once; the token is spent even when the call is refused with `409`. An unknown, expired or already used token returns `410` with code `embed_session_invalid`. Returns `409` when the signer or document can no longer sign.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `token` | path | string | yes | The 64-character session token from the embed URL. |

Request body: **optional**.

`application/json`: object{`parent_origin`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `parent_origin` | string | no | no | The origin of the page hosting the iframe, recorded by the service. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [EmbedSessionExchange](#schema-embedsessionexchange)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [EmbedSessionExchange](#schema-embedsessionexchange) | no | no |  |

Documented statuses: `200` The signing session; `409` The signer or document can not sign right now (`already_signed`, `document_not_ready`, `document_unavailable`).; `410` Unknown, expired or already used token (`embed_session_invalid`).; `500` Unexpected server error..

### 51. List envelopes

- **Java:** `client.envelopes()` — `EnvelopeResource: public PaginatedResult<DocumentEnvelope> list(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/envelopes`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** The workspace's envelopes, the most recent first: each with its documents in order, their statuses and signers, and the counts by status.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `page` | query | integer | no | Page number. |
| `per-page` | query | integer | no | Records per page: 20 by default, at most 50; a larger value answers 50 a page. |
| `search` | query | string | no | Part of the envelope's name. |
| `status` | query | string enum[open, closed] | no | `open`: envelopes with a document still open; `closed`: every document signed, declined, cancelled, expired or failed. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[DocumentEnvelope](#schema-documentenvelope)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[DocumentEnvelope](#schema-documentenvelope)> | no | no |  |

Documented statuses: `200` The workspace's envelopes; `400` A filter refused as sent, an entry of `errors` each: `search` (not text), `status` (other than `open` or `closed`).; `403` An OAuth app's token lacks `documents:read` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 52. Create envelope

- **Java:** `client.envelopes()` — `EnvelopeResource: public DocumentEnvelope create(CreateEnvelopeRequest request, String accountId, String idempotencyKey)`
- **HTTP:** `POST /v1/accounts/{accountId}/envelopes`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Send several documents to the same signers at once. Each document gets its own assignment and keeps its own status, certificate and downloads; one invitation goes to each signer, through the methods sent for them, for all the documents they are on, from the first of them. A send that fails is recorded on that signer's request (`notification_history`) and doesn't undo the envelope: `200` means the envelope was created, not that every invitation was delivered. The envelope is charged as one document, whatever its documents, each signer's notifications and identity check once for the whole envelope, and each digital-certificate signature on every document it is made on. Every document must be uploaded and processed (`metadata_ready`) with no assignment, and every listed signer must be on at least one document. Each document is signed on its virtual page or, with `collect`, on the fields placed on its pages, as on an assignment; a digital-certificate signer signs only virtual documents, alone on each. Needs the Envelopes feature on the workspace's plan (`403` with code `PlanRestricted` otherwise). A refused request creates and charges nothing; `errors` names every field at fault.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `Idempotency-Key` | header | string | no | Any text, case-sensitive and new for each envelope you mean to create (a UUID, say). Retrying with the same key after a lost response answers the envelope the first request created, and creates and charges nothing, whatever the body says. A key belongs to its workspace and doesn't expire. `409` with code `IdempotencyKeyInUse` if the first request is still running: try again. An empty value counts as no key, as on template documents. |

Request body: **required**.

`application/json`: [EnvelopeCreateRequest](#schema-envelopecreaterequest).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | string | yes | no | What the invitation calls what is being signed, in place of a document's name. `<` is refused (`400`). minLength: `1`. maxLength: `255`. |
| `message` | string or null | no | yes | Optional message sent to every signer; their invitation adds the names of the documents they are on. |
| `max_total_credits` | number | no | no | Optional ceiling on what this request may spend, as on an assignment: refused with `409` and code `CreditCeilingExceeded`, nothing created or charged, when the envelope's total credits are above it. A value that is not a number of credits, zero or more, answers `400`; numeric text counts, as a form-encoded body sends it. The estimate ignores it. minimum: `0`. |
| `signers` | array<object{`id!`: string; `verification_method`: string enum[Email, Whatsapp, DigitalCertificate, IdDocumentFaceMatchBasic]; `notification_methods`: array<string enum[Email, Whatsapp]>; `embedded`: boolean}> | yes | no | Every signer of the envelope, once, by id: up to 20. They all sign at once: an envelope takes no `step`. minItems: `1`. maxItems: `20`. |
| `documents` | array<[EnvelopeDocumentRequest](#schema-envelopedocumentrequest)> | yes | no | The documents, in the order the envelope lists them: up to 20. minItems: `1`. maxItems: `20`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [DocumentEnvelope](#schema-documentenvelope)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [DocumentEnvelope](#schema-documentenvelope) | no | no |  |

Documented statuses: `200` The created envelope; `400` Refused as sent; nothing was created or charged. Each field at fault is an entry of `errors` (a list refused as a whole is not read item by item), an item of a list named by its index (`documents[1].signer_ids`): `name`; `message` (not text); `signers` and `documents` (missing, empty, not a list, or more than 20 signers or 20 documents; `documents` also when its documents hold more than 2,000 fields between them, a virtual document's being one per signer, or more than 100 copy receivers); `signers[i].id` (missing, listed twice, not a signer of the workspace, or on no document); `signers[i].verification_method` (not a code); `signers[i].notification_methods` (not a list of codes); `signers[i].step` (sent at all); `documents[i].id` (missing, listed twice, or a document that can't take the assignment: not found, not `metadata_ready`, or already assigned); `documents[i].method` (other than `virtual` or `collect`); `documents[i].entries` (missing for `collect`, or not a list of pages with fields naming their `signer_id`); `documents[i].expires_at` (not text, a number or null); `documents[i].signer_ids` (missing, not a list of ids, a signer not listed once in `signers`, for `collect` not exactly the signers on its fields, or a digital-certificate signer on a `collect` document or with another signer). A page or field the assignment doesn't find on a `collect` document, a method code the platform doesn't know, a verification and notification that don't pair, a signer without the e-mail or WhatsApp number their method needs, an `embedded` other than `true`, `false`, `1` or `0` (`null` reads as left out, as on an assignment) or set for a signer verified other than by Bypass, Email or WhatsApp, a balance too low for the envelope, an `expires_at` or `copy_receiver_ids` the assignment refuses, and a document another request assigned or put back into processing meanwhile answer `400` with `message` only, as on an assignment.; `403` The workspace's plan lacks the Envelopes feature, or a signer's method or embedded signing the plan lacks (`PlanRestricted`); or an OAuth app's token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; `404` The requested resource does not exist.; `409` The envelope costs more than `max_total_credits` (`CreditCeilingExceeded`), or another request with this `Idempotency-Key` is still running (`IdempotencyKeyInUse`).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 53. Estimate envelope cost

- **Java:** `client.envelopes()` — `EnvelopeResource: public CostEstimate estimateCost(CreateEnvelopeRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/envelopes/estimate-cost`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** What creating the envelope would cost, creating and charging nothing. Takes the creation body and checks it as creation does (`max_total_credits` aside); answers as an assignment's estimate: the envelope counts as one document (`documents` is 1, an extra one when the plan's allowance doesn't cover it), each signer's notifications and identity check count once for the whole envelope, and each digital-certificate signature on every document it is made on, as creation charges them.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: **required**.

`application/json`: [EnvelopeCreateRequest](#schema-envelopecreaterequest).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | string | yes | no | What the invitation calls what is being signed, in place of a document's name. `<` is refused (`400`). minLength: `1`. maxLength: `255`. |
| `message` | string or null | no | yes | Optional message sent to every signer; their invitation adds the names of the documents they are on. |
| `max_total_credits` | number | no | no | Optional ceiling on what this request may spend, as on an assignment: refused with `409` and code `CreditCeilingExceeded`, nothing created or charged, when the envelope's total credits are above it. A value that is not a number of credits, zero or more, answers `400`; numeric text counts, as a form-encoded body sends it. The estimate ignores it. minimum: `0`. |
| `signers` | array<object{`id!`: string; `verification_method`: string enum[Email, Whatsapp, DigitalCertificate, IdDocumentFaceMatchBasic]; `notification_methods`: array<string enum[Email, Whatsapp]>; `embedded`: boolean}> | yes | no | Every signer of the envelope, once, by id: up to 20. They all sign at once: an envelope takes no `step`. minItems: `1`. maxItems: `20`. |
| `documents` | array<[EnvelopeDocumentRequest](#schema-envelopedocumentrequest)> | yes | no | The documents, in the order the envelope lists them: up to 20. minItems: `1`. maxItems: `20`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [CostEstimate](#schema-costestimate)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [CostEstimate](#schema-costestimate) | no | no |  |

Documented statuses: `200` Cost estimate and balances; `400` Refused on the same fields as creation, in `errors`, and with `message` alone for a method the platform does not know or a pairing it refuses. What creation checks while it creates each document (a signer's missing contact, a page or field not found, the balance) is not checked here: `has_sufficient_resources` says what the balance allows.; `403` The workspace's plan lacks the Envelopes feature, or a signer's method or embedded signing the plan lacks (`PlanRestricted`); or an OAuth app's token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 54. Get envelope

- **Java:** `client.envelopes()` — `EnvelopeResource: public DocumentEnvelope get(String envelopeId, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/envelopes/{envelopeId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** One envelope of the workspace: its documents in order, each with its status and signers, and the counts by status.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `envelopeId` | path | string | yes | The envelope ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [DocumentEnvelope](#schema-documentenvelope)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [DocumentEnvelope](#schema-documentenvelope) | no | no |  |

Documented statuses: `200` The envelope; `403` An OAuth app's token lacks `documents:read` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 55. Delete envelope

- **Java:** `client.envelopes()` — `EnvelopeResource: public void delete(String envelopeId, String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/envelopes/{envelopeId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Delete an envelope once every document of it is closed: certificated, declined, cancelled, expired or failed. Before that it answers `409` with code `EnvelopeOpen`: cancel it first. The envelope is gone: it is no longer listed and reads `404`, and a create retried with its `Idempotency-Key` no longer answers it. Its documents stay as ordinary documents, with their certificates, downloads and webhooks; `GET /v1/documents/{documentId}?expand=envelope_id` reads null for them.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `envelopeId` | path | string | yes | The envelope ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no |  |

Documented statuses: `200` Envelope deleted; `403` An OAuth app's token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; `404` The requested resource does not exist.; `409` A document of the envelope is still open (`EnvelopeOpen`).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 56. Update envelope

- **Java:** `client.envelopes()` — `EnvelopeResource: public DocumentEnvelope update(String envelopeId, Map<String, Object> changes, String accountId)`
- **HTTP:** `PATCH /v1/accounts/{accountId}/envelopes/{envelopeId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Rename the envelope, change its message or put its documents in a new order; send only what changes. Nothing is sent to anyone: the reads and every notice sent from then on (a reminder, a cancel, a document added) use the new name, message and order. The message is also each document's assignment `message`. Works on a closed envelope too. A refused request changes nothing; `errors` names every field at fault.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `envelopeId` | path | string | yes | The envelope ID. |

Request body: **required**.

`application/json`: object{`name`: string; `message`: string or null; `document_ids`: array<string>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | string | no | no | What the invitation calls what is being signed. `<` is refused (`400`). minLength: `1`. maxLength: `255`. |
| `message` | string or null | no | yes | The message sent to every signer from now on; null for none. |
| `document_ids` | array<string> | no | no | Every document of the envelope, each once, in the new order; `position` follows it. An envelope listing no document takes no order. minItems: `1`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [DocumentEnvelope](#schema-documentenvelope)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [DocumentEnvelope](#schema-documentenvelope) | no | no |  |

Documented statuses: `200` The envelope as changed; `400` Refused as sent; nothing changed. Each field at fault is an entry of `errors`: `name` (empty, not text, over 255 characters, or holding `<`), `message` (not text), `document_ids` (empty, or not a list of every document of the envelope, each once).; `403` An OAuth app's token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 57. List envelope activities

- **Java:** `client.envelopes()` — `EnvelopeResource: public PaginatedResult<DocumentActivity> activities(String envelopeId, ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/envelopes/{envelopeId}/activities`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** The envelope's trail: the activities of every document it has had, one taken out included (not one deleted, whose own list answers 404), newest first (`sort=id` for oldest first), each naming its document (`document_id`, `document_name`). Each entry is otherwise a document activity, as `GET /v1/documents/{documentId}/activities` answers it. Paged: 20 per page by default; the `X-Pagination-Total-Count`, `X-Pagination-Page-Count`, `X-Pagination-Current-Page` and `X-Pagination-Per-Page` headers describe the full list, and a page past the end returns an empty `data`.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `envelopeId` | path | string | yes | The envelope ID. |
| `sort` | query | string | no | `-id` (default, newest first) or `id`. |
| `page` | query | integer | no | Page number. |
| `per-page` | query | integer | no | Records per page: 20 by default, at most 50; a larger value answers 50 a page. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[EnvelopeActivity](#schema-envelopeactivity)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[EnvelopeActivity](#schema-envelopeactivity)> | no | no |  |

Documented statuses: `200` The envelope's activities; `403` An OAuth app's token lacks `documents:read` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 58. Download envelope artifacts

- **Java:** `client.envelopes()` — `EnvelopeResource: public byte[] download(String envelopeId, String artifactName, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/envelopes/{envelopeId}/download/{artifactName}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** One zip, named after the envelope, of the chosen artifact of every document of it, in order. Each file is named after its document's position (`1 - contract.pdf`); `bundle` gives each document a folder of its bundle's files (`1 - contract/`). `certificated`, `certificate-page`, `pades` and `bundle` include only the documents certified so far, and `pades` only those that have one; `404` when no document has the artifact. The artifacts are those of `GET /v1/documents/{documentId}/download/{artifactName}`.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `envelopeId` | path | string | yes | The envelope ID. |
| `artifactName` | path | string enum[original, certificated, certificate-page, pades, bundle] | yes | Artifact type. |

Request body: none.

Success `200` `application/zip`: string (binary).


Documented statuses: `200` The zip; `403` An OAuth app's token lacks `documents:read` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 59. Cancel envelope

- **Java:** `client.envelopes()` — `EnvelopeResource: public List<EnvelopeCancellation> cancel(String envelopeId, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/envelopes/{envelopeId}/cancel`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Cancels every document of the envelope that can still be cancelled, the way deleting a pending document cancels it: the owner, its signers and its copy receivers are told. A document already closed, one every signer has signed (its certification queued), or one being certificated, is left as it is. A notice that fails doesn't stop the other documents. Safe to repeat: a second call cancels nothing and answers each document's status.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `envelopeId` | path | string | yes | The envelope ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<object{`id!`: string; `status!`: string; `cancelled!`: boolean}>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<object{`id!`: string; `status!`: string; `cancelled!`: boolean}> | no | no |  |

Documented statuses: `200` What happened to each document, in the envelope's order; `403` An OAuth app's token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 60. Remind envelope signer

- **Java:** `client.envelopes()` — `EnvelopeResource: public EnvelopeResendResult resendNotification(String envelopeId, String signerId, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/envelopes/{envelopeId}/signers/{signerId}/resend`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Reminds a signer of the whole envelope at once: one message, the envelope's invitation sent again from their first link, listing the documents they still have to sign and, by e-mail, those they already signed. Paid notification methods are charged once for it, for each message that goes out. Each document they still have to sign records a `signature_requested` event, as the invitation does.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `envelopeId` | path | string | yes | The envelope ID. |
| `signerId` | path | string | yes | The signer ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`is_sent!`: boolean; `envelope_id!`: string; `signer_id!`: string}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`is_sent!`: boolean; `envelope_id!`: string; `signer_id!`: string} | no | no |  |

Documented statuses: `200` Reminder result; `400` The signer has nothing left to sign in the envelope: every document of theirs is signed or closed; or the account lacks credits for the reminder.; `403` An OAuth app's token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; `404` The envelope was not found, or the signer is not one of its signers.; `409` The envelope is closed: every document of it is signed, declined, cancelled, expired or failed (`EnvelopeClosed`).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 61. Add document to envelope

- **Java:** `client.envelopes()` — `EnvelopeResource: public DocumentEnvelope addDocument(String envelopeId, EnvelopeDocumentRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/envelopes/{envelopeId}/documents`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Add a document to an open envelope for some of its signers. The document is checked as on creation: a `metadata_ready` document of the workspace with no assignment, signed on its virtual page or, with `collect`, on the fields placed in `entries`, with its own `expires_at` and `copy_receiver_ids`. An envelope holds up to 20 documents. It charges only its digital-certificate signatures, the envelope having paid for its document and its signers' notifications and identity checks when it was sent, and goes last in the envelope's order; a signer whose identity check the envelope approved is verified on it too. Each chosen signer is sent the envelope's invitation again, through the methods and with the link of their first invitation, now listing the new document, even once the document that invitation was sent from is taken out; the other signers hear nothing. The signers are the envelope's own: no one new joins once it was sent. Needs the Envelopes feature on the workspace's plan, as creating does. A refused request creates and charges nothing; `errors` names every field at fault.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `envelopeId` | path | string | yes | The envelope ID. |

Request body: **required**.

`application/json`: [EnvelopeDocumentRequest](#schema-envelopedocumentrequest) + object{`max_total_credits`: number}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | string | yes | no | A `metadata_ready` document of the workspace with no assignment. minLength: `1`. |
| `method` | string enum[virtual, collect] | no | no | As on an assignment: `virtual` (the default), signed on its virtual page, or `collect`, signed on the fields placed in `entries`. default: `"virtual"`. |
| `entries` | array<object{`page_id!`: string; `fields!`: array<object{`signer_id!`: string; `field_id!`: string; `display_settings!`: [DisplaySettings](#schema-displaysettings)}>}> | no | no | Required for `collect`, as on an assignment: the fields placed on each page of the document. Its signers are the ones on its fields, exactly its `signer_ids`. minItems: `1`. |
| `signer_ids` | array<string> | yes | no | The signers of this document, each listed once: on creation, signers listed in `signers`; when added, signers of the envelope's documents. minItems: `1`. |
| `expires_at` | string or number or null | no | no | This document's expiration, read as on an assignment, at least one hour in the future. Text is read as a date: ISO 8601 is the format to send (a UTC offset in the value is honored, a value without one is read as UTC, and a date alone means 23:59 UTC that day), and text that is a number is Unix time. A number is Unix time in seconds, its fraction dropped. Null, 0, an empty text or leaving it out means no expiration; a value that can't be read as a date answers `400`. |
| `copy_receiver_ids` | array<string> | no | no | Signer IDs that only receive a copy of this document, as on an assignment. |
| `max_total_credits` | number | no | no | Optional ceiling on what this request may spend, as on an assignment: refused with `409` and code `CreditCeilingExceeded`, nothing created or charged, when the document costs more on its own. A value that is not a number of credits, zero or more, answers `400`; numeric text counts, as a form-encoded body sends it. minimum: `0`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [DocumentEnvelope](#schema-documentenvelope)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [DocumentEnvelope](#schema-documentenvelope) | no | no |  |

Documented statuses: `200` The envelope with the document added; `400` Refused as sent; nothing was created or charged. Each field at fault is an entry of `errors`: `id` (missing, or a document that can't take the assignment: not found, not `metadata_ready`, or already assigned); `method` (other than `virtual` or `collect`); `entries` (missing for `collect`, or not a list of pages with fields naming their `signer_id`); `signer_ids` (missing, not a list of ids, listed twice, not a signer of the envelope's documents, for `collect` not exactly the signers on its fields, or a digital-certificate signer on a `collect` document or with another signer); `expires_at` (not text, a number or null); a null `field` when the envelope already has 20 documents, or would hold more than 2,000 fields (a virtual document's being one per signer) or more than 100 copy receivers with this one. A page or field the assignment doesn't find on a `collect` document, a balance too low, an `expires_at` or `copy_receiver_ids` the assignment refuses, and a document another request assigned or put back into processing meanwhile answer `400` with `message` only, as on an assignment.; `403` The workspace's plan lacks the Envelopes feature, or a signer's method or embedded signing the plan lacks (`PlanRestricted`); or an OAuth app's token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; `404` The requested resource does not exist.; `409` Every document of the envelope is closed (`EnvelopeClosed`), or the document costs more than `max_total_credits` on its own (`CreditCeilingExceeded`).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 62. Take document out of envelope

- **Java:** `client.envelopes()` — `EnvelopeResource: public DocumentEnvelope removeDocument(String envelopeId, String documentId, String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/envelopes/{envelopeId}/documents/{documentId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Take an open document out of the envelope: it is cancelled as cancelling the envelope cancels it, its owner, signers and copy receivers told in a notice naming the envelope, and it is no longer listed; the other documents carry on. Out of the envelope, `GET /v1/documents/{documentId}?expand=envelope_id` reads null for it; a signer whose invitation link was sent from it keeps reaching the envelope's other documents with that link, so it can't be deleted on its own (`409` with code `EnvelopeOpen`) until every document of the envelope is closed. A document that is not in the envelope, or was taken out already, answers `404`.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `envelopeId` | path | string | yes | The envelope ID. |
| `documentId` | path | string | yes | Document ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [DocumentEnvelope](#schema-documentenvelope)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [DocumentEnvelope](#schema-documentenvelope) | no | no |  |

Documented statuses: `200` The envelope without the document; `403` An OAuth app's token lacks `documents:write` (`InsufficientScope`). Also a member deactivated in the workspace, a workspace scheduled for deletion, or an OAuth token of another workspace.; `404` The requested resource does not exist.; `409` The document is closed, signed by every signer (its certification queued) or being certificated, and can no longer be cancelled (`DocumentClosed`).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 63. List fields

- **Java:** `client.fields()` — `FieldResource: public PaginatedResult<FieldDefinition> list()`; `client.fields()` — `FieldResource: public PaginatedResult<FieldDefinition> list(ListParams params)`; `client.fields()` — `FieldResource: public PaginatedResult<FieldDefinition> list(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/fields`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List the field definitions of a workspace.  When `include_standard` is enabled, records of type `signature`, `initial` and `signatureDate` are also returned. Every workspace has one of each, created with it; they cannot be created, and a field can't be placed on the `Editor` role with them.  To find the fields a sender can place on a document or template, list with `include_standard=true`: the result is every active field definition, signature fields included, and each one's `id` is the `field_id` to place.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `include_inactive` | query | boolean | no | Include inactive field definitions. |
| `include_standard` | query | boolean | no | Include standard field types (signature, initial, signatureDate). |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[Field](#schema-field)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[Field](#schema-field)> | no | no |  |

Documented statuses: `200` Field definitions; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 64. Create field

- **Java:** `client.fields()` — `FieldResource: public FieldDefinition create(CreateFieldRequest request)`; `client.fields()` — `FieldResource: public FieldDefinition create(CreateFieldRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/fields`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Create a field definition in the workspace.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: **required**.

`application/json`: object{`name!`: string; `type!`: string; `regex`: string or null; `is_required`: boolean}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | string | yes | no |  |
| `type` | string | yes | no |  |
| `regex` | string or null | no | yes |  |
| `is_required` | boolean | no | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Field](#schema-field)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Field](#schema-field) | no | no |  |

Documented statuses: `200` The created field; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 65. Get field

- **Java:** `client.fields()` — `FieldResource: public FieldDefinition get(String fieldId)`; `client.fields()` — `FieldResource: public FieldDefinition get(String fieldId, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/fields/{fieldId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Retrieve a single field definition.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `fieldId` | path | string | yes | The field ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Field](#schema-field)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Field](#schema-field) | no | no |  |

Documented statuses: `200` The field; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 66. Update field

- **Java:** `client.fields()` — `FieldResource: public FieldDefinition update(String fieldId, UpdateFieldRequest request)`; `client.fields()` — `FieldResource: public FieldDefinition update(String fieldId, UpdateFieldRequest request, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/fields/{fieldId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Update a field definition.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `fieldId` | path | string | yes | The field ID. |

Request body: **required**.

`application/json`: object{`name`: string; `regex`: string or null; `is_active`: boolean}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | string | no | no |  |
| `regex` | string or null | no | yes |  |
| `is_active` | boolean | no | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Field](#schema-field)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Field](#schema-field) | no | no |  |

Documented statuses: `200` The updated field; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 67. Delete field

- **Java:** `client.fields()` — `FieldResource: public void delete(String fieldId)`; `client.fields()` — `FieldResource: public void delete(String fieldId, String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/fields/{fieldId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Delete a field definition.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `fieldId` | path | string | yes | The field ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no |  |

Documented statuses: `200` Field deleted; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 68. Validate field value

- **Java:** signer: `client.fields()` — `FieldResource: public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode)`; `FieldResource: public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode, String accountId)`; workspace: `FieldResource: public FieldValidationResult validateAsWorkspace(String fieldId, Object value)`; `FieldResource: public FieldValidationResult validateAsWorkspace(String fieldId, Object value, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/fields/{fieldId}/validate`
- **Auth:** Bearer token or `X-Api-Key` or `signer-access-code` query credential
- **Contract notes:** Validate an input value against a field definition. Typically called with a signer access code during the signing flow.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `fieldId` | path | string | yes | The field ID. |

Request body: **required**.

`application/json`: object{`value!`: any}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `value` | any | yes | no | The input value to validate. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [FieldValidation](#schema-fieldvalidation)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [FieldValidation](#schema-fieldvalidation) | no | no |  |

Documented statuses: `200` Validation result; `400` The request was rejected because of its current state or invalid input.; `403` Authenticated but not allowed to perform this action.; `404` The field is not one of the workspace, or the workspace does not exist.; `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 69. Validate multiple field values

- **Java:** signer: `client.fields()` — `FieldResource: public List<FieldValidationResult> validateMultiple(List<Map<String, Object>> entries, String signerAccessCode)`; `FieldResource: public List<FieldValidationResult> validateMultiple(List<Map<String, Object>> entries, String signerAccessCode, String accountId)`; workspace: `FieldResource: public List<FieldValidationResult> validateMultipleAsWorkspace(List<Map<String, Object>> entries)`; `FieldResource: public List<FieldValidationResult> validateMultipleAsWorkspace(List<Map<String, Object>> entries, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/fields/validate-multiple`
- **Auth:** Bearer token or `X-Api-Key` or `signer-access-code` query credential
- **Contract notes:** Validate multiple input values at once. The request body is a JSON array of `{field_id, value}` objects.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: **required**.

`application/json`: array<object{`field_id!`: string; `value!`: any}>.


Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[FieldValidationResult](#schema-fieldvalidationresult)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[FieldValidationResult](#schema-fieldvalidationresult)> | no | no |  |

Documented statuses: `200` Validation results; `400` The request was rejected because of its current state or invalid input.; `403` Authenticated but not allowed to perform this action.; `404` The field is not one of the workspace, or the workspace does not exist.; `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 70. List field types

- **Java:** `client.fields()` — `FieldResource: public List<FieldType> listTypes()`
- **HTTP:** `GET /v1/field-types`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List the types a workspace can create field definitions with (`POST /v1/accounts/{accountId}/fields`). The built-in `signature`, `initial` and `signatureDate` types are not listed: they can't be created, and their definitions come from `GET /v1/accounts/{accountId}/fields?include_standard=true`. `cpf` expects 11 digits; `cnpj` accepts 14-char values (letters A-Z allowed in positions 1–12 per the CNPJ Alfanumérico rule; check digits 13–14 stay numeric). Punctuation is ignored during validation.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[FieldType](#schema-fieldtype)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[FieldType](#schema-fieldtype)> | no | no |  |

Documented statuses: `200` Field types; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 71. List two-factor methods

- **Java:** `client.users()` — `UserResource: public MfaStatus listMfaMethods()`
- **HTTP:** `GET /v1/users/self/mfa`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** The authenticated user's enrolled two-factor methods and how many recovery codes remain unused.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`methods`: array<object{`id`: string; `type`: string; `label`: string; `confirmed_at`: string (date-time); `last_used_at`: string (date-time)}>; `recovery_codes_remaining`: integer}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`methods`: array<object{`id`: string; `type`: string; `label`: string; `confirmed_at`: string (date-time); `last_used_at`: string (date-time)}>; `recovery_codes_remaining`: integer} | no | no |  |

Documented statuses: `200` Enrolled methods; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 72. Start authenticator enrollment

- **Java:** `client.users()` — `UserResource: public TotpEnrollment startTotpEnrollment(String label)`. A null or blank label sends `{}`.
- **HTTP:** `POST /v1/users/self/mfa/totp`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Creates an unconfirmed authenticator method and returns the shared secret. The secret is returned only by this call and cannot be retrieved again. Two-factor authentication is not active until the enrollment is confirmed.

Parameters: none.

Request body: **optional**.

`application/json`: object{`label`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `label` | string | no | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`id`: string; `secret`: string; `provisioning_uri`: string}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`id`: string; `secret`: string; `provisioning_uri`: string} | no | no |  |

Documented statuses: `200` Enrollment started; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 73. Confirm authenticator enrollment

- **Java:** `client.users()` — `UserResource: public List<String> confirmTotpEnrollment(String methodId, String code, String password, String reauthCode)`. Returns `data.recovery_codes`.
- **HTTP:** `PUT /v1/users/self/mfa/totp/confirm`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Activates the method by proving one live code from the NEW device (`code`). Returns the recovery codes, which are shown only once and cannot be retrieved again. From this point every login requires a second factor. If the user already has a confirmed method of the same type, confirming REPLACES it — the old one is soft-deleted and recovery codes are reissued — so this call additionally requires re-authentication via `password` or `reauth_code` (a live code from the CURRENT device, or one of the existing recovery codes), exactly like disabling a method. First-time enrollment needs neither.

Parameters: none.

Request body: **required**.

`application/json`: object{`id!`: string; `code!`: string; `password`: string (password); `reauth_code`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | string | yes | no |  |
| `code` | string | yes | no | Live code from the NEW device being confirmed. |
| `password` | string (password) | no | no | Re-authentication proof, required only when replacing an existing confirmed method. |
| `reauth_code` | string | no | no | Re-authentication proof alternative to password: a live code from the CURRENT device, or a recovery code. Required only when replacing an existing confirmed method. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`recovery_codes`: array<string>}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`recovery_codes`: array<string>} | no | no |  |

Documented statuses: `200` Two-factor enabled; `400` One or more fields failed validation.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 74. Regenerate recovery codes

- **Java:** `client.users()` — `UserResource: public List<String> regenerateRecoveryCodes(String password, String code)`. At least one of `password` or `code` is required locally. Returns `data.recovery_codes`.
- **HTTP:** `POST /v1/users/self/mfa/recovery-codes`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Issues a fresh set of ten recovery codes and invalidates the previous set. Requires the current password, a live authenticator code, or one of the existing recovery codes (which is then consumed).

Parameters: none.

Request body: **required**.

`application/json`: object{`password`: string (password); `code`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `password` | string (password) | no | no |  |
| `code` | string | no | no | A live 6-digit authenticator code, or one of the existing recovery codes. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`recovery_codes`: array<string>}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`recovery_codes`: array<string>} | no | no |  |

Documented statuses: `200` New recovery codes; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 75. Remove a two-factor method

- **Java:** `client.users()` — `UserResource: public boolean removeMfaMethod(String methodId, String password, String code)`. At least one of `password` or `code` is required locally. Returns `data.is_mfa_enabled`.
- **HTTP:** `DELETE /v1/users/self/mfa/{customId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Removes an enrolled method. Requires the current password, a live authenticator code, or one of the existing recovery codes (which is then consumed), so that a stolen session cannot silently disable two-factor authentication. Removing the last method also discards the recovery codes.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `customId` | path | string | yes |  |

Request body: **required**.

`application/json`: object{`password`: string (password); `code`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `password` | string (password) | no | no |  |
| `code` | string | no | no | A live 6-digit authenticator code, or one of the existing recovery codes. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`is_mfa_enabled`: boolean}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`is_mfa_enabled`: boolean} | no | no |  |

Documented statuses: `200` Method removed; `400` One or more fields failed validation.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 76. Get the workspace notification policy

- **Java:** `client.workspaces()` — `WorkspaceResource: public NotificationPolicy getNotificationPolicy(String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/notification-policy`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Which document alerts this workspace sends, set by its owners. Every member can read it; `editable` says whether the requesting user can change it. Not available to third-party OAuth applications (`403`), whatever their scopes. Assinafy's own apps can reach it.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [NotificationPolicy](#schema-notificationpolicy)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [NotificationPolicy](#schema-notificationpolicy) | no | no |  |

Documented statuses: `200` The policy; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 77. Update the workspace notification policy

- **Java:** `client.workspaces()` — `WorkspaceResource: public NotificationPolicy updateNotificationPolicy(String accountId, Map<String, Object> changes)`
- **HTTP:** `PUT /v1/accounts/{accountId}/notification-policy`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Owners only (`403` for other members). Merges the supplied fields: send only `events` keys you want to change, and/or `owners_receive_all`. An event set to `false` reaches nobody in this workspace, on any channel, whatever each member's own preferences; the identity review e-mail is the exception and is always sent, because the signer cannot continue without it. An unknown field or code, a non-boolean value or an empty body is rejected with `400` and nothing is written. Returns the full policy.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: **required**.

`application/json`: object{`events`: [NotificationPolicyEvents](#schema-notificationpolicyevents); `owners_receive_all`: boolean}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `events` | [NotificationPolicyEvents](#schema-notificationpolicyevents) | no | no |  |
| `owners_receive_all` | boolean | no | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [NotificationPolicy](#schema-notificationpolicy)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [NotificationPolicy](#schema-notificationpolicy) | no | no |  |

Documented statuses: `200` The updated policy; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 78. Get my notification preferences

- **Java:** `client.users()` — `UserResource: public NotificationPreferences getNotificationPreferences()`
- **HTTP:** `GET /v1/users/self/notification-preferences`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Which owner-facing document notifications the authenticated user receives by e-mail. All thirteen keys are always returned; everything defaults to `true`. Account and security e-mail (welcome, password reset, invitations, account deletion) is not configurable and never appears here. Not available to third-party OAuth applications (`403`), whatever their scopes: these are the user's personal settings. Assinafy's own apps can reach it.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [NotificationPreferences](#schema-notificationpreferences)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [NotificationPreferences](#schema-notificationpreferences) | no | no |  |

Documented statuses: `200` The current preferences; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 79. Update my notification preferences

- **Java:** `client.users()` — `UserResource: public NotificationPreferences updateNotificationPreferences(Map<String, Boolean> changes)`
- **HTTP:** `PUT /v1/users/self/notification-preferences`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Merges the supplied map into the authenticated user's preferences. Send only the keys you want to change — omitted keys keep their current value. Setting a key to `false` stops that e-mail for this user in every account they belong to. Returns the full map. An unknown code, a non-boolean value, or an empty body is rejected with 400 and nothing is written. Not available to third-party OAuth applications (`403`), whatever their scopes: these are the user's personal settings. Assinafy's own apps can reach it.

Parameters: none.

Request body: **required**.

`application/json`: [NotificationPreferences](#schema-notificationpreferences).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `DocumentCompleted` | boolean | no | no | Every signer has signed and the document is certified. |
| `SignerDeclined` | boolean | no | no | A signer declined to sign. |
| `DocumentCancelled` | boolean | no | no | The document was cancelled. |
| `DocumentAboutToExpire` | boolean | no | no | The signature deadline is approaching. |
| `DocumentExpired` | boolean | no | no | The signature deadline passed. |
| `DocumentExpirationReset` | boolean | no | no | The signature deadline was extended. |
| `DocumentProcessingFailed` | boolean | no | no | An uploaded document could not be processed. |
| `TemplateProcessingFailed` | boolean | no | no | A template could not be processed. |
| `SignerWhatsappFailed` | boolean | no | no | A WhatsApp notification to a signer could not be delivered. |
| `ContractRenewalNotice` | boolean | no | no | A tracked contract entered its notice period: it is about to end or to renew. |
| `ContractExpiring` | boolean | no | no | A tracked contract ends in 45 or in 30 days. |
| `ContractExpired` | boolean | no | no | A tracked contract ended without renewal. |
| `ContractRenewed` | boolean | no | no | A tracked contract was auto-renewed into its next term. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [NotificationPreferences](#schema-notificationpreferences)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [NotificationPreferences](#schema-notificationpreferences) | no | no |  |

Documented statuses: `200` The updated preferences; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 80. Exchange a code, refresh token, or subject token for an access token

- **Java:** `client.oauth()` — `OAuthResource: public OAuthTokens exchangeCode(OAuthClient client, String code, String codeVerifier, String redirectUri)`; `client.oauth()` — `OAuthResource: public OAuthTokens refreshToken(OAuthClient client, String refreshToken)`
- **HTTP:** `POST /v1/oauth/token`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** Implements the RFC 6749 §5.1/§5.2 token-endpoint body contract in      *         both directions: a successful exchange returns a flat JSON object with      *         `access_token` at the top level, and a failure returns a flat      *         `{error, error_description}` object — neither is wrapped in this API's      *         usual response envelope, since no standard OAuth client library (or MCP      *         connector) would find `access_token` or `error` inside a `data` key.      *      *         The third grant, `urn:ietf:params:oauth:grant-type:token-exchange` (RFC 8693),      *         is restricted to a confidential internal-service client (see `/oauth/introspect`)      *         and trades a front-end resource's access token (the `subject_token`) for one      *         minted for THIS API — the crossing an MCP server makes after a user consents,      *         since its own token can never be forwarded here directly. The issued token is      *         never wider than the subject: `scope`, when sent, must be a subset of the      *         subject's own scopes, and `resource` must equal this API's own resource      *         identifier exactly. No refresh token is issued; the caller re-exchanges from      *         the user's own token instead of holding unattended API access.  Replaying a refresh token already rotated ends the whole authorization. For Assinafy's own apps it ends that device's session.

Parameters: none.

Request body: **required**.

`application/x-www-form-urlencoded`: [OAuthTokenRequest](#schema-oauthtokenrequest).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `grant_type` | string enum[authorization_code, refresh_token, urn:ietf:params:oauth:grant-type:token-exchange] | yes | no | `urn:ietf:params:oauth:grant-type:token-exchange` is for internal service clients only (Assinafy's own MCP server) — an ordinary confidential or public client authenticates with it and always gets `invalid_client`, exactly as an unrecognized client would. Everyday integrators use `authorization_code` and `refresh_token`. |
| `code` | string | no | no |  |
| `redirect_uri` | string (uri) | no | no |  |
| `code_verifier` | string | no | no | RFC 7636: 43-128 characters from [A-Za-z0-9-._~]. Shorter values are rejected with `invalid_grant`. |
| `refresh_token` | string | no | no |  |
| `client_id` | string | yes | no |  |
| `client_secret` | string | no | no | Confidential clients only. Public clients authenticate with PKCE and are never issued a secret; the token-exchange grant requires a confidential, internal-service client and therefore always requires this. |
| `resource` | string (uri) | no | no | RFC 8707 resource indicator. For `authorization_code`/`refresh_token`, optional; when present it must be the `resource` value published by /.well-known/oauth-protected-resource and must match the one sent to /authorize, otherwise `invalid_target`. For the token-exchange grant it is REQUIRED and must equal this API's own resource identifier exactly (never a front-end resource such as the MCP server), otherwise `invalid_target`. |
| `subject_token` | string | no | no | Token-exchange grant only. The front-end resource's access token being traded in. Must be a live, original (never itself exchanged) token minted for a resource this server issues tokens for, other than this API's own audience. |
| `subject_token_type` | string enum[urn:ietf:params:oauth:token-type:access_token] | no | no | Token-exchange grant only. Required; only `urn:ietf:params:oauth:token-type:access_token` is supported. |
| `requested_token_type` | string enum[urn:ietf:params:oauth:token-type:access_token] | no | no | Token-exchange grant only. Optional; when present it must agree with the only type this server issues. |

`application/json`: [OAuthTokenRequest](#schema-oauthtokenrequest).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `grant_type` | string enum[authorization_code, refresh_token, urn:ietf:params:oauth:grant-type:token-exchange] | yes | no | `urn:ietf:params:oauth:grant-type:token-exchange` is for internal service clients only (Assinafy's own MCP server) — an ordinary confidential or public client authenticates with it and always gets `invalid_client`, exactly as an unrecognized client would. Everyday integrators use `authorization_code` and `refresh_token`. |
| `code` | string | no | no |  |
| `redirect_uri` | string (uri) | no | no |  |
| `code_verifier` | string | no | no | RFC 7636: 43-128 characters from [A-Za-z0-9-._~]. Shorter values are rejected with `invalid_grant`. |
| `refresh_token` | string | no | no |  |
| `client_id` | string | yes | no |  |
| `client_secret` | string | no | no | Confidential clients only. Public clients authenticate with PKCE and are never issued a secret; the token-exchange grant requires a confidential, internal-service client and therefore always requires this. |
| `resource` | string (uri) | no | no | RFC 8707 resource indicator. For `authorization_code`/`refresh_token`, optional; when present it must be the `resource` value published by /.well-known/oauth-protected-resource and must match the one sent to /authorize, otherwise `invalid_target`. For the token-exchange grant it is REQUIRED and must equal this API's own resource identifier exactly (never a front-end resource such as the MCP server), otherwise `invalid_target`. |
| `subject_token` | string | no | no | Token-exchange grant only. The front-end resource's access token being traded in. Must be a live, original (never itself exchanged) token minted for a resource this server issues tokens for, other than this API's own audience. |
| `subject_token_type` | string enum[urn:ietf:params:oauth:token-type:access_token] | no | no | Token-exchange grant only. Required; only `urn:ietf:params:oauth:token-type:access_token` is supported. |
| `requested_token_type` | string enum[urn:ietf:params:oauth:token-type:access_token] | no | no | Token-exchange grant only. Optional; when present it must agree with the only type this server issues. |

Success `200` `application/json`: object{`access_token`: string; `issued_token_type`: string; `token_type`: string; `expires_in`: integer; `refresh_token`: string or null; `scope`: string; `id_token`: string or null}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `access_token` | string | no | no |  |
| `issued_token_type` | string | no | no | Present only for the token-exchange grant, per RFC 8693 §2.2.1. |
| `token_type` | string | no | no |  |
| `expires_in` | integer | no | no | For the token-exchange grant, clamped to the subject token's own remaining lifetime as well as the exchanged-token TTL — never longer than either. |
| `refresh_token` | string or null | no | yes | Present only when the `offline_access` scope was requested AND consented on the authorization_code/refresh_token grants. Never present for the token-exchange grant. Without it a client must send the user through the authorization flow again once the access token expires. |
| `scope` | string | no | no | The scope of the ACCESS token. `offline_access` is a request-time signal rather than a permission, so it never appears here even when it was requested. |
| `id_token` | string or null | no | yes | A signed OIDC id_token (RS256). Present only when the openid scope was granted. |

Documented statuses: `200` The token response; `400` `invalid_grant` (bad, expired, replayed, or wrong-client authorization code; a `code_verifier` outside the RFC 7636 grammar of 43-128 unreserved characters; redirect_uri mismatch; a refresh token whose authorization no longer includes `offline_access`; for token-exchange, a `subject_token` that is unusable, itself an exchanged token, not visible to service clients, or expired), `invalid_target` (a `resource` this server does not issue tokens for, one disagreeing with the authorized value, or — for token-exchange — one other than this API's own resource identifier), `invalid_scope` (token-exchange only: the requested `scope` is not a subset of the subject token's own scopes), `invalid_request` (token-exchange only: an unsupported `subject_token_type`/`requested_token_type`), or `unsupported_grant_type`; `401` `invalid_client` — unknown/disabled client or failed client authentication. The description never reveals whether client_id exists.; `429` Rate limit exceeded; wait the `Retry-After` seconds. Limited per caller IP.; `500` Unexpected server error..

### 81. Revoke a token

- **Java:** `client.oauth()` — `OAuthResource: public void revokeToken(OAuthClient client, String token, String tokenTypeHint)`
- **HTTP:** `POST /v1/oauth/revoke`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** Revokes an access or refresh token. A refresh token ends the whole authorization; for Assinafy's own apps, it ends that device's session, and the authorization stays. Every token outcome always returns 200 — including a token that does not exist, is already revoked, or is malformed — so the endpoint can never be used to probe whether a token exists. The one exception is failed client authentication, which returns 401.

Parameters: none.

Request body: **required**.

`application/x-www-form-urlencoded`: [OAuthRevokeRequest](#schema-oauthrevokerequest).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `token` | string | yes | no |  |
| `token_type_hint` | string enum[access_token, refresh_token] | no | no |  |
| `client_id` | string | yes | no |  |
| `client_secret` | string | no | no |  |

`application/json`: [OAuthRevokeRequest](#schema-oauthrevokerequest).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `token` | string | yes | no |  |
| `token_type_hint` | string enum[access_token, refresh_token] | no | no |  |
| `client_id` | string | yes | no |  |
| `client_secret` | string | no | no |  |

Documented statuses: `200` Revoked; `401` `invalid_client` — the only case that is not reported as success. Every token outcome (revoked, already revoked, unknown, malformed) always returns 200; only failed client authentication returns 401.; `500` Unexpected server error..

### 82. OpenID Connect userinfo

- **Java:** `client.oauth()` — `OAuthResource: public OAuthUserInfo userInfo()`
- **HTTP:** `GET /v1/oauth/userinfo`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Claims about the user who authorized this token. Requires the openid scope; name requires profile and email requires email. Per OIDC Core §5.3.2, the response is a flat JSON object of claims — never this API's usual `{status, data, message}` envelope.

Parameters: none.

Request body: none.

Success `200` `application/json`: object{`sub`: string; `name`: string or null; `email`: string or null (email); `email_verified`: boolean or null}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `sub` | string | no | no |  |
| `name` | string or null | no | yes |  |
| `email` | string or null (email) | no | yes |  |
| `email_verified` | boolean or null | no | yes |  |

Documented statuses: `200` The user's claims; `403` Authenticated but not allowed to perform this action.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 83. Download document thumbnail

- **Java:** `client.documents()` — `DocumentResource: public byte[] thumbnail(String documentId)`; `client.documents()` — `DocumentResource: public byte[] thumbnail(String documentId, boolean certificated)`
- **HTTP:** `GET /v1/documents/{documentId}/thumbnail`
- **Auth:** Bearer token or `X-Api-Key` or `signer-access-code` query credential
- **Contract notes:** Download the thumbnail image of a document's first page.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `artifact` | query | string | no | `certificated`: the page, or the thumbnail, as it is in the signed document, with its signatures and stamps. Only for a certificated document, and only for the workspace's members and apps: `404` when the document isn't certificated or the caller uses a signer's access code. The first request for a page renders it with the pages around it, ten at a time, so it takes longer than the next ones; a request that finds the document's pages being rendered waits up to 30 seconds, then answers `503` with `Retry-After` if they still aren't ready; it answers `503` at once when the API is already rendering as many pages as it allows. Sent with `Cache-Control: no-store`. The certificate pages at the end of the signed file have no page ID and no render, and the visible seal of a document signed with a digital certificate is only in its `pades` file. Any other value is ignored. |

Request body: none.

Success `200` `image/*`: string (binary).


Documented statuses: `200` The thumbnail image; `404` The requested resource does not exist.; `503` With `artifact=certificated`: another request was still rendering the page after 30 seconds, or the API was already rendering as many pages as it allows at once. Try again after the `Retry-After` seconds.; `304` Not modified: the `If-None-Match` or `If-Modified-Since` sent matches the page. No body.; `403` Authenticated but not allowed to perform this action.; `400` With a signer access code: the signer's step has not started yet (sequential signing).; `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 84. Download document page

- **Java:** `client.documents()` — `DocumentResource: public byte[] downloadPage(String documentId, String pageId)`; `client.documents()` — `DocumentResource: public byte[] downloadPage(String documentId, String pageId, boolean certificated)`
- **HTTP:** `GET /v1/documents/{documentId}/pages/{pageId}/download`
- **Auth:** Bearer token or `X-Api-Key` or `signer-access-code` query credential
- **Contract notes:** Download the rendered image of a specific document page.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `pageId` | path | string | yes | The page ID. |
| `artifact` | query | string | no | `certificated`: the page, or the thumbnail, as it is in the signed document, with its signatures and stamps. Only for a certificated document, and only for the workspace's members and apps: `404` when the document isn't certificated or the caller uses a signer's access code. The first request for a page renders it with the pages around it, ten at a time, so it takes longer than the next ones; a request that finds the document's pages being rendered waits up to 30 seconds, then answers `503` with `Retry-After` if they still aren't ready; it answers `503` at once when the API is already rendering as many pages as it allows. Sent with `Cache-Control: no-store`. The certificate pages at the end of the signed file have no page ID and no render, and the visible seal of a document signed with a digital certificate is only in its `pades` file. Any other value is ignored. |

Request body: none.

Success `200` `image/*`: string (binary).


Documented statuses: `200` The page image; `404` The requested resource does not exist.; `503` With `artifact=certificated`: another request was still rendering the page after 30 seconds, or the API was already rendering as many pages as it allows at once. Try again after the `Retry-After` seconds.; `304` Not modified: the `If-None-Match` or `If-Modified-Since` sent matches the page. No body.; `403` Authenticated but not allowed to perform this action.; `400` With a signer access code: the signer's step has not started yet (sequential signing).; `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 85. View public document

- **Java:** `client.publicDocuments()` — `PublicDocumentResource: public Document getBasicInfo(String documentId)`
- **HTTP:** `GET /v1/public/documents/{documentId}`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** Retrieve a publicly shared document by ID. Public endpoint.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | The document ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Document](#schema-document)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Document](#schema-document) | no | no |  |

Documented statuses: `200` The public document; `404` The requested resource does not exist.; `500` Unexpected server error..

### 86. Send access token for public document

- **Java:** `client.publicDocuments()` — `PublicDocumentResource: public Map<String, Object> sendToken(String documentId, String email)`; `PublicDocumentResource: public TokenDelivery sendTokenTyped(String documentId, String email)`; `PublicDocumentResource: public Map<String, Object> sendToken(String documentId, String recipient, String channel)`
- **HTTP:** `PUT /v1/public/documents/{documentId}/send-token`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** Send a one-time access token to a signer of a public document, so they can view it. The document must be pending signature, and `recipient` must belong to a signer of the document who has not signed yet. Only the `email` channel is supported. Public endpoint.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | The document ID. |

Request body: **required**.

`application/json`: object{`channel!`: string enum[email]; `recipient!`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `channel` | string enum[email] | yes | no | Where to send the token. Only `email` is supported. |
| `recipient` | string | yes | no | The signer's address on that channel — for `email`, the signer's email address. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`document`: object{`resource`: string; `id`: string; `name`: string; `page_count`: integer; `created_by`: string}; `channel`: string; `recipient`: string}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`document`: object{`resource`: string; `id`: string; `name`: string; `page_count`: integer; `created_by`: string}; `channel`: string; `recipient`: string} | no | no |  |

Documented statuses: `200` Token sent; `400` One or more fields failed validation.; `404` The requested resource does not exist.; `500` Unexpected server error..

### 87. Get my push notification preferences

- **Java:** `client.users()` — `UserResource: public PushPreferences getPushPreferences()`
- **HTTP:** `GET /v1/users/self/push-preferences`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Which document alerts the authenticated user receives as push notifications on their phones, separate from the e-mail preferences. Every key is always returned; everything defaults to `true`. New keys may be added. Not available to third-party OAuth applications (`403`), whatever their scopes: these are the user's personal settings. Assinafy's own apps can reach it.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [PushPreferences](#schema-pushpreferences)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [PushPreferences](#schema-pushpreferences) | no | no |  |

Documented statuses: `200` The current preferences; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 88. Update my push notification preferences

- **Java:** `client.users()` — `UserResource: public PushPreferences updatePushPreferences(Map<String, Boolean> changes)`
- **HTTP:** `PUT /v1/users/self/push-preferences`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Merges the supplied map into the authenticated user's push preferences. Send only the keys you want to change — omitted keys keep their current value. Setting a key to `false` stops that push for this user in every account they belong to; e-mail is not affected. Returns the full map. An unknown code, a non-boolean value, or an empty body is rejected with 400 and nothing is written. Not available to third-party OAuth applications (`403`), whatever their scopes: these are the user's personal settings. Assinafy's own apps can reach it.

Parameters: none.

Request body: **required**.

`application/json`: [PushPreferences](#schema-pushpreferences).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `SignerViewed` | boolean | no | no | A signer opened the document for the first time. |
| `SignerSigned` | boolean | no | no | A signer signed the document. |
| `DocumentCompleted` | boolean | no | no | Every signer has signed and the document is certified. |
| `SignerDeclined` | boolean | no | no | A signer declined to sign. |
| `DocumentAboutToExpire` | boolean | no | no | The signature deadline is approaching. |
| `DocumentExpired` | boolean | no | no | The signature deadline passed. |
| `SignerIdentityReviewRequired` | boolean | no | no | A signer's identity awaits the sender's review. The e-mail for this is always sent. |
| `SignerWhatsappFailed` | boolean | no | no | A WhatsApp notification to a signer could not be delivered. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [PushPreferences](#schema-pushpreferences)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [PushPreferences](#schema-pushpreferences) | no | no |  |

Documented statuses: `200` The updated preferences; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 89. List signers

- **Java:** `client.signers()` — `SignerResource: public PaginatedResult<Signer> list()`; `client.signers()` — `SignerResource: public PaginatedResult<Signer> list(ListParams params)`; `client.signers()` — `SignerResource: public PaginatedResult<Signer> list(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/signers`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List the signers of a workspace.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `search` | query | string | no | Filter by full_name or email. |
| `page` | query | integer | no | Page number. |
| `per-page` | query | integer | no | Records per page: 20 by default, at most 50; a larger value answers 50 a page. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[Signer](#schema-signer)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[Signer](#schema-signer)> | no | no |  |

Documented statuses: `200` A page of signers; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 90. Create signer

- **Java:** `client.signers()` — `SignerResource: public Signer create(CreateSignerRequest request)`; `client.signers()` — `SignerResource: public Signer create(CreateSignerRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/signers`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Create a signer in the workspace.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: **required**.

`application/json`: object{`full_name!`: string; `email`: string (email); `whatsapp_phone_number`: string; `government_id`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `full_name` | string | yes | no |  |
| `email` | string (email) | no | no |  |
| `whatsapp_phone_number` | string | no | no | E.164; normalized on save. |
| `government_id` | string | no | no | Signer's CPF (11 digits) or CNPJ (14 characters; may be alphanumeric). Formatting is accepted and the value is normalized on save. Required before the signer can use a verification method that identifies them by CPF. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Signer](#schema-signer)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Signer](#schema-signer) | no | no |  |

Documented statuses: `200` The created signer; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 91. Get signer

- **Java:** `client.signers()` — `SignerResource: public Signer get(String signerId)`; `client.signers()` — `SignerResource: public Signer get(String signerId, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/signers/{signerId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Retrieve a signer's information.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `signerId` | path | string | yes | The signer ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Signer](#schema-signer)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Signer](#schema-signer) | no | no |  |

Documented statuses: `200` The signer; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 92. Update signer

- **Java:** `client.signers()` — `SignerResource: public Signer update(String signerId, UpdateSignerRequest request)`; `client.signers()` — `SignerResource: public Signer update(String signerId, UpdateSignerRequest request, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/signers/{signerId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Update a signer's information. Only the fields sent change; send just the ones you are changing.  **Clearing a field:** `null` or `"` clears `email`, `whatsapp_phone_number` and `government_id` (`whatsapp_phone_number` is stored as `null` either way; the other two keep the value sent). `full_name` cannot be cleared.  **Verification integrity:** `email` / `whatsapp_phone_number` cannot be changed while the signer has verified that channel on an in-flight (not yet certificated) document — the response is `400` naming the offending document(s). Already-certificated documents do not block updates, except a document of an envelope while another of the signer's documents of it is in flight. Changing a channel that has *unverified* in-flight requests, or unverified requests on a document of an envelope while another of the signer's documents of it is in flight, rotates their access/verification codes (invalidating previously sent links/OTPs); use the resend endpoint to redeliver. `full_name` can always be updated.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `signerId` | path | string | yes | The signer ID. |

Request body: **required**.

`application/json`: object{`full_name`: string; `email`: string (email); `whatsapp_phone_number`: string; `government_id`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `full_name` | string | no | no |  |
| `email` | string (email) | no | no | Can't be changed or cleared while the signer has verified it on an in-flight document, or on a document of an envelope while another of their documents of it is in flight (`400`). |
| `whatsapp_phone_number` | string | no | no | E.164; normalized on save, and compared after normalizing, so the same number in another format is not a change. Can't be changed or cleared while the signer has verified it on an in-flight document, or on a document of an envelope while another of their documents of it is in flight (`400`). |
| `government_id` | string | no | no | Signer's CPF (11 digits) or CNPJ (14 characters; may be alphanumeric). Formatting is accepted and the value is normalized on save. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Signer](#schema-signer)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Signer](#schema-signer) | no | no |  |

Documented statuses: `200` The updated signer; `400` The request was rejected because of its current state or invalid input.; `404` The requested resource does not exist.; `403` Authenticated but not allowed to perform this action.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 93. Delete signer

- **Java:** `client.signers()` — `SignerResource: public void delete(String signerId)`; `client.signers()` — `SignerResource: public void delete(String signerId, String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/signers/{signerId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Delete a signer.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `signerId` | path | string | yes | The signer ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no |  |

Documented statuses: `200` Signer deleted; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 94. Get current signer

- **Java:** `client.signers()` — `SignerResource: public Signer getSelf(String signerAccessCode)`
- **HTTP:** `GET /v1/signers/self`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** Return the signer identified by the signer access code, including the `has_signature`/`has_initial`/`is_signature_reusable` flags and the `verification_method` of this signature request. Available before verification, so the signing page can pick the right first step.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [SignerSelf](#schema-signerself)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [SignerSelf](#schema-signerself) | no | no |  |

Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `200` The signer; `400` With a signer access code: the signer's step has not started yet (sequential signing).; `403` Called with an OAuth application token instead of the access code.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 95. Workspace brand for the signing page

- **Java:** `client.signers()` — `SignerResource: public AccountTheme getTheme(String signerAccessCode)`
- **HTTP:** `GET /v1/sign/theme`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** The brand of the workspace that sent the document: its name, its colors (`primary_color` is the button background, `secondary_color` the button text, 6-digit hex without `#`) and the address of its logo, or `null` when it has none. The logo address carries no access code: append `signer-access-code` to it, as to every signer call. Available before verification and after the document is deleted, so every screen of the signing page can carry the brand. Uses the signer access code.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [AccountTheme](#schema-accounttheme)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [AccountTheme](#schema-accounttheme) | no | no |  |

Documented statuses: `200` The brand; `400` With a signer access code: the signer's step has not started yet (sequential signing).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 96. Workspace logo for the signing page

- **Java:** `client.signers()` — `SignerResource: public byte[] downloadLogo(String signerAccessCode)`
- **HTTP:** `GET /v1/sign/logo`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** The logo image of the workspace that sent the document. Available before verification and after the document is deleted. Uses the signer access code.

Parameters: none.

Request body: none.

Success `200` `image/*`: string (binary).


Documented statuses: `200` The logo image; `400` With a signer access code: the signer's step has not started yet (sequential signing).; `401` Missing or invalid credentials.; `404` The workspace has no logo.; `500` Unexpected server error..

### 97. Identity document verification status

- **Java:** `client.signers().identityVerification(signerAccessCode)` → `SignerIdentityVerification`.; `client.signers()` — `SignerResource: public SignerIdentityVerification identityVerification(String signerAccessCode)`
- **HTTP:** `GET /v1/signers/identity-verification`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** Current state of the signer's identity document verification. Backfills a pending decision from the provider. Uses the signer access code.  **One live session per signer:** `can_start` is true before a provider session exists, and again once that session is Expired -- the capture link lapsed before anything was submitted, so the signer may open a new one. Once a session is Pending, In Review, Approved, Declined or Abandoned it is false. A signer who reaches Declined or Abandoned cannot restart here; the account must start a whole new process (new document, new assignment). With the code of a document of an envelope, this is the signer's one check for the whole envelope, whichever of their codes of it asks.  **In review:** the signer continues -- views, confirms their data and signs -- while the account reviews the result. The document is certificated only after the account approves every signer under review; if it rejects one, the document is cancelled and every signer receives the cancellation notice.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`can_start`: boolean; `status`: string or null; `url`: string or null; `expires_at`: string or null (date-time); `completed_at`: string or null (date-time)}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`can_start`: boolean; `status`: string or null; `url`: string or null; `expires_at`: string or null (date-time); `completed_at`: string or null (date-time)} | no | no |  |

Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `200` Verification state; `400` With a signer access code: the signer's step has not started yet (sequential signing).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 98. Start identity document verification

- **Java:** `client.signers().startIdentityVerification(signerAccessCode, consent)` → `SignerIdentityVerification`.; `client.signers()` — `SignerResource: public SignerIdentityVerification startIdentityVerification(String signerAccessCode, boolean consent)`
- **HTTP:** `POST /v1/signers/identity-verification`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** Records the signer's biometric-processing consent and opens a provider session. Returns the hosted verification URL to redirect to. Uses the signer access code.  **One live session per signer:** this succeeds when no session exists yet, or when the last one Expired (the capture link lapsed before anything was submitted); the expired session is replaced by a new one. Otherwise -- Pending, In Review, Approved, Declined or Abandoned -- it returns `400`. A resend never destroys or replaces a live provider session, and the account is billed once per signer at assignment creation, never again on a restart. Recovering from a Declined or Abandoned verification means the account must start a whole new process (new document, new assignment). With the code of a document of an envelope, it starts the signer's one check for the whole envelope: one session, whichever of their codes of it is used, billed once with the envelope.

Parameters: none.

Request body: **required**.

`application/json`: object{`consent!`: boolean}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `consent` | boolean | yes | no | Must be true. Explicit consent to biometric processing. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`id`: string; `url`: string; `expires_at`: string or null (date-time)}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`id`: string; `url`: string; `expires_at`: string or null (date-time)} | no | no |  |

Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `200` Session created; `400` Consent missing, this signer already has a verification session that is not Expired (with the code of a document of an envelope, on any of their documents of it) or can no longer verify (with the code of a document of an envelope, also once no document of it, not taken out, awaits their signature), another start of the signer's envelope check is still running (`Identity verification is already being started for this signer. Try again shortly.`), the signer has no full name, or the signer's step has not started yet (sequential signing).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 99. Get signer's document

- **Java:** `client.signers()` — `SignerResource: public Map<String, Object> getCurrentDocument(String signerId, String signerAccessCode)`; `client.signers()` — `SignerResource: public Document getCurrentDocumentTyped(String signerId, String signerAccessCode)`; `client.signers()` — `SignerResource: public Document getCurrentDocumentTyped(String signerId, String signerAccessCode, boolean includeEnvelope)`
- **HTTP:** `GET /v1/signers/{signerId}/document`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** Return the document and the signer's assignment items, scoped to the signer access code. The response adds `current_signer`: this signer, with their `verification_method`, `notification_methods` and `completed` (whether they have already signed). Once the signer is done (signed, or the document was declined, cancelled, expired or failed; for a document of an envelope, with every document of the envelope they are on) for more than 30 minutes, or the workspace was deleted, the code is closed: this returns only `id`, `name`, `status`, `current_signer` and `certified_copy`, with `is_signer_access_closed: true` (and `envelope`, when asked for), and the signer's other endpoints answer `410`. `certified_copy` says whether the signer can ask for the certified copy again (`POST /v1/sign/certified-copy`): `available`, the `notification_method` it goes on and, for WhatsApp, `resends_left`. Unlike `GET /v1/sign`, this answers before a certificate or identity-check signer confirms their data, so their link learns its envelope here.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `signerId` | path | string | yes | The signer ID. |
| `expand` | query | array<string enum[envelope]> | no | `envelope` adds the envelope the code belongs to, its id and name, also once the code is closed and when the code's own document was taken out (the code still reaches the rest); `null` for a link outside an envelope or whose envelope was deleted. Without it the document reads as it always has. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [SigningDocument](#schema-signingdocument)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [SigningDocument](#schema-signingdocument) | no | no |  |

Documented statuses: `200` The document with the signer's items; `404` The requested resource does not exist.; `400` With a signer access code: the signer's step has not started yet (sequential signing).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 100. View document to sign

- **Java:** `client.assignments()` — `AssignmentResource: public Map<String, Object> getForSigner(String signerAccessCode)`; `client.assignments()` — `AssignmentResource: public Map<String, Object> getForSigner(String signerAccessCode, Boolean hasAcceptedTerms)`; `client.assignments()` — `AssignmentResource: public Document getForSignerTyped(String signerAccessCode)`; `client.assignments()` — `AssignmentResource: public Document getForSignerTyped(String signerAccessCode, Boolean hasAcceptedTerms)`; `client.assignments()` — `AssignmentResource: public Document getForSignerTyped(String signerAccessCode, Boolean hasAcceptedTerms, String documentId, boolean includeEnvelope)`; `public Map<String, Object> getForSigner(String signerAccessCode, Boolean hasAcceptedTerms, String documentId, boolean includeEnvelope)`
- **HTTP:** `GET /v1/sign`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** Retrieve the document a signer has been invited to sign, using the signer access code. Marks the document as viewed. The assignment carries only this signer's items, and leaves out the other signers, their signing links (`signing_urls`), the copy receivers and the signing `summary`. The response adds `current_signer`: this signer, with their `verification_method`, `notification_methods` and `completed` (whether they have already signed). Returns 409 while the document is still being prepared (retry with backoff). Once the signer is done (signed, or the document was declined, cancelled, expired or failed; for a document of an envelope, with every document of the envelope they are on) for more than 30 minutes, or the workspace was deleted, the code is closed: this returns only `id`, `name`, `status`, `current_signer` and `certified_copy`, with `is_signer_access_closed: true` (and `envelope`, when asked for), and the signer's other endpoints answer `410`. `certified_copy` says whether the signer can ask for the certified copy again (`POST /v1/sign/certified-copy`): `available`, the `notification_method` it goes on and, for WhatsApp, `resends_left`.  **Signers whose verification method is `DigitalCertificate`** must have confirmed their data *and* accepted the terms before this returns the document; otherwise it is `400`. Both are satisfied in one call to `PUT /v1/documents/{documentId}/signers/confirm-data` with `has_accepted_terms: true`, so send that before this endpoint — the `has_accepted_terms` query parameter here is too late to open the gate. `PUT /v1/signers/accept-terms` also works and is never gated.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `has_accepted_terms` | query | boolean | no | Set true to record terms acceptance. |
| `expand` | query | array<string enum[envelope]> | no | `envelope` adds the envelope the code belongs to, its id and name, also once the code is closed and when the code's own document was taken out (the code still reaches the rest); `null` for a link outside an envelope or whose envelope was deleted. Without it the document reads as it always has. |
| `document_id` | query | string | no | With the code of a document of an envelope: another document of that envelope the signer is on, to open in place of the code's own; the code's e-mail or WhatsApp verification is carried there, once. A value that is no document id (a list, or empty) answers `400`. Ignored with any other code. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [SigningDocument](#schema-signingdocument)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [SigningDocument](#schema-signingdocument) | no | no |  |

Documented statuses: `200` The document with the signer's assignment; `400` A digital-certificate signer has not yet confirmed their data or accepted the terms, the signer's step has not started yet (sequential signing), or, with the code of a document of an envelope, `document_id` is no document id.; `404` `document_id` names a document outside the code's envelope, or one the signer is not on; or the workspace deleted the document while the code is still open.; `409` The document is not ready to be viewed yet.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 101. Sign assignment items

- **Java:** `client.assignments()` — `AssignmentResource: public Map<String, Object> sign(String documentId, String assignmentId, String signerAccessCode, List<Map<String, Object>> items)`
- **HTTP:** `POST /v1/documents/{documentId}/assignments/{assignmentId}`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** Sign a document with input fields (collect method): submit the signer's item values, completing their items. Signing again after the signer has signed changes nothing and returns the current assignment. For **virtual** assignments the signer must first confirm their data via `PUT /v1/documents/{documentId}/signers/confirm-data`, otherwise this returns `400` (Signer data must be confirmed before signing). Signers whose verification method is `DigitalCertificate` cannot use this endpoint — their signature must be produced through `POST /v1/signers/certificate/start` + `/complete`, and this returns `400`. The request body is a JSON array of item entries. Uses the signer access code. With the code of a document of an envelope, it acts on the signer's request on the document the route names, if that document is in the same envelope, carrying the code's e-mail or WhatsApp verification there.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `assignmentId` | path | string | yes | The assignment ID. |

Request body: **required**.

`application/json`: array<object{`itemId!`: string; `fieldId!`: string; `pageId!`: string; `value!`: string}>.


Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object | no | no |  |

Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `200` Signing result; `400` Signer data must be confirmed before signing (virtual assignments), the signer must sign with a digital certificate through the digital certificate endpoints, an entry names a page, field or item that is not one of this signer's, or the signer's step has not started yet (sequential signing). Nothing is signed when an entry is refused.; `404` The assignment is not the one of the signer's request: the access code's own or, with the code of a document of an envelope, the one on the document the route names; or the route names a document outside the code's envelope, or one the signer is not on.; `409` The document is not ready to be signed yet.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 102. Reject (decline) assignment

- **Java:** `client.assignments()` — `AssignmentResource: public Map<String, Object> decline(String documentId, String assignmentId, String signerAccessCode, String declineReason)`
- **HTTP:** `PUT /v1/documents/{documentId}/assignments/{assignmentId}/reject`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** The signer declines to sign the document, giving a reason. Uses the signer access code. With the code of a document of an envelope, it acts on the signer's request on the document the route names, if that document is in the same envelope, carrying the code's e-mail or WhatsApp verification there.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `assignmentId` | path | string | yes | The assignment ID. |

Request body: **required**.

`application/json`: object{`decline_reason!`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `decline_reason` | string | yes | no | Descriptive reason for declining. Up to 2000 characters; longer values return 400. maxLength: `2000`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no | Empty array. |

Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `200` Assignment declined; `400` `decline_reason` is missing or longer than 2000 characters, the assignment or the document is no longer pending, or the signer's step has not started yet (sequential signing).; `404` The assignment is not the one of the signer's request: the access code's own or, with the code of a document of an envelope, the one on the document the route names; or the route names a document outside the code's envelope, or one the signer is not on.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 103. Sign multiple documents

- **Java:** `client.signers()` — `SignerResource: public Map<String, Object> signMultiple(String signerAccessCode, List<String> documentIds)`
- **HTTP:** `PUT /v1/signers/documents/sign-multiple`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** Sign several documents in one request, for a signer with multiple pending documents. Each document must be prepared for the **virtual** signature method. A listed document the signer already signed is left as it is. Uses the signer access code.

Parameters: none.

Request body: **required**.

`application/json`: object{`document_ids!`: array<string>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `document_ids` | array<string> | yes | no | IDs of the documents to sign. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no | Empty array. |

Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `200` Signing result; `400` A listed document is not a virtual document of this signer, is not pending signature, waits for an earlier signing step, or, for the code of a document of an envelope, is not part of that envelope.; `404` A listed document ID does not exist in the workspace. The message names the unknown IDs.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 104. Decline multiple documents

- **Java:** `client.signers()` — `SignerResource: public Map<String, Object> declineMultiple(String signerAccessCode, List<String> documentIds, String declineReason)`
- **HTTP:** `PUT /v1/signers/documents/decline-multiple`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** Decline several documents in one request. Uses the signer access code.

Parameters: none.

Request body: **required**.

`application/json`: object{`document_ids!`: array<string>; `decline_reason!`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `document_ids` | array<string> | yes | no | IDs of the documents to decline. |
| `decline_reason` | string | yes | no | Reason for declining. Up to 2000 characters; longer values return 400. maxLength: `2000`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no | Empty array. |

Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `200` Decline result; `400` A listed document is not this signer's, is not pending signature, waits for an earlier signing step, or, for the code of a document of an envelope, is not part of that envelope. Nothing is declined.; `404` A listed document ID does not exist in the workspace. The message names the unknown IDs.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 105. Verify signer code (OTP)

- **Java:** `client.signers()` — `SignerResource: public Map<String, Object> verifyEmail(String signerAccessCode, String verificationCode)`
- **HTTP:** `POST /v1/verify`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** Submit the verification code (OTP) sent to the signer to unlock the signing flow. Uses the signer access code. 5 wrong codes within 15 minutes of the first one lock the request until those 15 minutes end: every attempt, the right code included, returns `429`. A right code clears the count. With the code of a document of an envelope, the code of the signer's envelope invitation is accepted too, and wrong codes on any of their documents of the envelope count together.

Parameters: none.

Request body: **required**.

`application/json`: object{`verification-code!`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `verification-code` | string | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no |  |

Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `200` Code verified. `data` is an empty list.; `400` The code is wrong, or the signer's step has not started yet (sequential signing).; `429` Too many wrong codes. Try again once 15 minutes have passed since the first one.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 106. Confirm signer data

- **Java:** `client.signers()` — `SignerResource: public Signer confirmSignerData(String documentId, String signerAccessCode, Map<String, Object> data)`
- **HTTP:** `PUT /v1/documents/{documentId}/signers/confirm-data`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** The signer confirms or updates their data before signing. Uses the signer access code. With the code of a document of an envelope, it acts on the signer's request on the document the route names, if that document is in the same envelope, carrying the code's e-mail or WhatsApp verification there. The code's own document, taken out of its envelope, takes the confirmation too while a document of the envelope still awaits signatures: it signs nothing (that document stays cancelled) and opens the reads a certificate or identity-check signer's method gates, so the signer reaches the documents left.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |

Request body: **required**.

`application/json`: object{`email`: string (email); `whatsapp_phone_number`: string; `has_accepted_terms`: boolean}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `email` | string (email) | no | no | Required when the signer is verified or notified by e-mail. Saved when the signer has none; otherwise it must be theirs. |
| `whatsapp_phone_number` | string | no | no | Required when the signer is verified or notified by WhatsApp. Saved when the signer has none; otherwise it must be theirs. |
| `has_accepted_terms` | boolean | no | no | `true` also records that the signer accepted the terms of use. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no |  |

Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `404` The route names a document the code doesn't reach: outside the code's envelope, or one the signer is not on.; `200` Data confirmed. `data` is an empty list.; `400` An e-mail or WhatsApp number the signer's methods need is missing, invalid or not theirs, or the signer's step has not started yet (sequential signing).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 107. Accept terms (signer)

- **Java:** `client.signers()` — `SignerResource: public void acceptTerms(String signerAccessCode)`
- **HTTP:** `PUT /v1/signers/accept-terms`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** Record that the signer accepted the terms of use. Uses the signer access code.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`full_name!`: string; `email!`: string or null (email); `has_accepted_terms!`: boolean}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`full_name!`: string; `email!`: string or null (email); `has_accepted_terms!`: boolean} | no | no |  |

Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `200` Terms accepted; `400` The signer's verification is not done yet (e.g. the e-mail code), or the signer's step has not started yet (sequential signing).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 108. Upload signature image

- **Java:** `client.signers()` — `SignerResource: public void uploadSignature(String signerAccessCode, String type, byte[] imageData)`; `client.signers()` — `SignerResource: public void uploadSignature(String signerAccessCode, String type, byte[] imageData, Boolean reuse)`
- **HTTP:** `POST /v1/signature`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** Upload the signer's signature (or initials) image as the raw request body. Uses the signer access code.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `type` | query | string | no | Image type, e.g. `signature` or `initial`. |
| `reuse` | query | boolean | no | Whether the signer opted to reuse this signature in future processes. When set, updates the signer's `is_signature_reusable` flag; when omitted, the flag is left unchanged. |

Request body: **required**.

`image/png`: string (binary).


Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no |  |

Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `200` Signature stored. `data` is an empty list.; `400` The image is not a PNG or JPEG file, or it is blank; or the signer's step has not started yet (sequential signing).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 109. Download signature image

- **Java:** `client.signers()` — `SignerResource: public byte[] downloadSignature(String signerAccessCode, String type)`
- **HTTP:** `GET /v1/signature/{signatureType}`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** Download the signer's stored signature/initials image. Uses the signer access code.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `signatureType` | path | string | yes | Image type (e.g. `signature`, `initial`). |

Request body: none.

Success `200` `image/*`: string (binary).


Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `200` The signature image; `404` The requested resource does not exist.; `400` With a signer access code: the signer's step has not started yet (sequential signing).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 110. List signer's documents

- **Java:** `client.signers()` — `SignerResource: public PaginatedResult<Document> listDocuments(String signerId, String signerAccessCode)`; `client.signers()` — `SignerResource: public PaginatedResult<Document> listDocuments(String signerId, String signerAccessCode, ListParams params)`; workspace: `SignerResource: public PaginatedResult<Document> listDocumentsAsWorkspace(String signerId)`; `SignerResource: public PaginatedResult<Document> listDocumentsAsWorkspace(String signerId, ListParams params)`
- **HTTP:** `GET /v1/signers/{signerId}/documents`
- **Auth:** `signer-access-code` query credential or Bearer token or `X-Api-Key`
- **Contract notes:** List the documents a signer is party to. Accepts either the signer access code or the account's own credentials (API key or bearer token) — with account credentials, the signer must belong to that account's workspace. With the signer access code, `signerId` must be the code's own signer (otherwise `403`), only documents whose signing step has been reached are listed (for the code of a document of an envelope, only that envelope's documents), and each assignment leaves out `signing_urls`.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `signerId` | path | string | yes | The signer ID. |
| `envelope_id` | query | string | no | Only the members of this envelope. Anything but text answers `400`. |
| `page` | query | integer | no | Page number. |
| `per-page` | query | integer | no | Records per page: 20 by default, at most 50; a larger value answers 50 a page. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[Document](#schema-document)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[Document](#schema-document)> | no | no |  |

Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `200` The signer's documents; `403` The access code belongs to another signer; or, with a user's token or key, a role, a membership or an OAuth scope refuses it.; `404` The signer is not in the caller's workspace.; `400` With a signer access code, the signer's step has not started yet (sequential signing); or `envelope_id` is not text, an entry of `errors`.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 111. Search signer's documents

- **Java:** `client.signers()` — `SignerResource: public PaginatedResult<Document> searchDocuments(String signerId, String signerAccessCode, String search)`; workspace: `SignerResource: public PaginatedResult<Document> searchDocumentsAsWorkspace(String signerId, String search)`; `client.signers()` — `SignerResource: public PaginatedResult<Document> searchDocuments(String signerId, String signerAccessCode, String search, String envelopeId)`; `client.signers()` — `SignerResource: public PaginatedResult<Document> searchDocumentsAsWorkspace(String signerId, String search, String envelopeId)`
- **HTTP:** `GET /v1/signers/{signerId}/documents/search`
- **Auth:** `signer-access-code` query credential or Bearer token or `X-Api-Key`
- **Contract notes:** Search the documents a signer is party to (compact representation). Accepts either the signer access code or the account's own credentials (API key or bearer token) — with account credentials, the signer must belong to that account's workspace. With the signer access code, `signerId` must be the code's own signer (otherwise `403`), only documents whose signing step has been reached are listed (for the code of a document of an envelope, only that envelope's documents), and each assignment leaves out `signing_urls`.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `signerId` | path | string | yes | The signer ID. |
| `envelope_id` | query | string | no | Only the members of this envelope. Anything but text answers `400`. |
| `search` | query | string | no | Search term. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[Document](#schema-document)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[Document](#schema-document)> | no | no |  |

Documented statuses: `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `200` Matching documents; `403` The access code belongs to another signer; or, with a user's token or key, a role, a membership or an OAuth scope refuses it.; `404` The signer is not in the caller's workspace.; `400` With a signer access code, the signer's step has not started yet (sequential signing); or `envelope_id` is not text, an entry of `errors`.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 112. Download the document to sign

- **Java:** `client.signers()` — `SignerResource: public byte[] downloadDocument(String signerId, String documentId, String artifactName, String signerAccessCode)`; `client.signers()` — `SignerResource: public byte[] downloadDocument(String signerId, String documentId, String artifactName)`
- **HTTP:** `GET /v1/signers/{signerId}/documents/{documentId}/download/{artifactName}`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** The original of a document the signer still has to sign, with their access code (`signer-access-code`). Only `original` is available (`403` otherwise): the certified copy is sent to the signer by e-mail or WhatsApp. A request without an access code, such as a link from an older completion e-mail, is redirected (`302`) to the expired-link page, with a `?token=` the page can ask for the certified copy with when the signer and document match a completed signature request.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `signerId` | path | string | yes | The signer ID. |
| `documentId` | path | string | yes | Document ID. |
| `artifactName` | path | string enum[original] | yes | Artifact type. Only `original`. |

Request body: none.

Success `200` `application/pdf`: string (binary).


Documented statuses: `200` The artifact binary; `302` No access code: redirected to the expired-link page.; `403` An artifact other than `original`.; `404` The signer or the document is not the code's, or the code doesn't reach the document.; `410` With a signer access code: the code is closed, because its signer finished (for a document of an envelope, every document of it they are on) more than 30 minutes ago. It still shows the signer's status and asks for the certified copy (`GET /v1/sign`).; `400` With a signer access code: the signer's step has not started yet (sequential signing).; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 113. Send the certified copy again

- **Java:** `client.signers()` — `SignerResource: public CertifiedCopy resendCertifiedCopy(String signerAccessCode, String documentId)`
- **HTTP:** `POST /v1/sign/certified-copy`
- **Auth:** `signer-access-code` query credential
- **Contract notes:** Sends the signer their certified document again, with a download link valid for 30 minutes. It goes on the channel of the signature request (e-mail when the request used e-mail, otherwise WhatsApp) and only to the e-mail or number the signing messages went to, never to a changed signer record. Works with the signer's access code after they signed, including once the code is closed and after the workspace is deleted. E-mail: once every 2 minutes and 10 times in 24 hours. WhatsApp: once per signature request; after that the signer must ask the sender. `certified_copy` on `GET /v1/sign` says whether it is available.

Parameters: none.

Request body: **optional**.

`application/json`: object{`document_id`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `document_id` | string | no | no | With the code of a document of an envelope: another document of that envelope the signer is on, whose certified copy to send, in place of the code's own. A value that is no document id (a list, or empty) answers `400`. Ignored with any other code. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`available`: boolean; `notification_method`: string enum[Email, Whatsapp]; `resends_left`: integer or null; `sent_to`: string}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`available`: boolean; `notification_method`: string enum[Email, Whatsapp]; `resends_left`: integer or null; `sent_to`: string} | no | no |  |

Documented statuses: `200` Sent; `404` `document_id` names a document outside the code's envelope, or one the signer is not on.; `409` The signer has not signed, the document is not certified yet, or the request has no e-mail or WhatsApp to send to (ask the sender).; `429` E-mail: asked again within 2 minutes or more than 10 times in 24 hours. WhatsApp: already re-sent once (ask the sender).; `500` The message could not be sent; nothing was used up, so it can be asked again.; `400` The signer's step has not started yet (sequential signing), or, with the code of a document of an envelope, `document_id` is no document id.; `401` Missing or invalid credentials..

### 114. Download the certified copy from a sent link

- **Java:** `client.signers()` — `SignerResource: public byte[] downloadCertifiedCopy(String token, boolean pades)`
- **HTTP:** `GET /v1/signers/downloads/{token}`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** The link in the completion and re-send messages. Valid 7 days in the completion message and 30 minutes when re-sent. Add `/pades` for the PAdES version on documents that have one. An expired or unknown link is redirected (`302`) to the expired-link page; for a known link the redirect carries `?token=`, with which the page can ask for a new one (`POST /v1/signers/downloads/{token}/resend`).

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `token` | path | string | yes | The link token. |

Request body: none.

Success `200` `application/pdf`: string (binary).


Documented statuses: `200` The certified PDF; `302` Expired or unknown link: redirected to the expired-link page.; `404` `/pades` on a document that has no PAdES version.; `500` Unexpected server error..

### 115. Send the certified copy again from an expired link

- **Java:** `client.signers()` — `SignerResource: public CertifiedCopy resendCertifiedCopyFromToken(String token)`
- **HTTP:** `POST /v1/signers/downloads/{token}/resend`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** Used by the page an expired download link leads to. Works with the link's token even after it expired, and on the same terms as `POST /v1/sign/certified-copy`: the request's channel, only the e-mail or number of the signing, the same limits. Copy receivers' links have no signature request (`409`).

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `token` | path | string | yes | The link token. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`available`: boolean; `notification_method`: string enum[Email, Whatsapp]; `resends_left`: integer or null; `sent_to`: string}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`available`: boolean; `notification_method`: string enum[Email, Whatsapp]; `resends_left`: integer or null; `sent_to`: string} | no | no |  |

Documented statuses: `200` Sent; `404` Unknown link.; `409` Nothing can be sent from here (copy receiver, Bypass signer, no recorded contact, or the document is not certified): ask the sender.; `429` E-mail: asked again within 2 minutes or more than 10 times in 24 hours. WhatsApp: already re-sent once.; `500` Unexpected server error..

### 116. Social login

- **Java:** `client.authentication()` — `AuthenticationResource: public AuthSession socialLogin(String provider, String token, boolean hasAcceptedTerms)`
- **HTTP:** `POST /v1/authentication/social-login`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** Exchange a token from a social login provider (currently only `google`) for an Assinafy access token.

Parameters: none.

Request body: **required**.

`application/json`: object{`provider!`: string enum[google]; `token!`: string; `has_accepted_terms!`: boolean}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `provider` | string enum[google] | yes | no |  |
| `token` | string | yes | no | Access/ID token from the provider. |
| `has_accepted_terms` | boolean | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [AuthSession](#schema-authsession)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [AuthSession](#schema-authsession) | no | no |  |

Documented statuses: `200` Access token, user and accounts; `400` One or more fields failed validation.; `500` Unexpected server error..

### 117. Link social login

- **Java:** `client.authentication()` — `AuthenticationResource: public void linkSocialLogin(String provider, String token)`
- **HTTP:** `POST /v1/auth/link-social-login`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Link a social-login provider account to the authenticated user.

Parameters: none.

Request body: **required**.

`application/json`: object{`provider!`: string enum[google]; `token!`: string}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `provider` | string enum[google] | yes | no |  |
| `token` | string | yes | no | Token from the provider. |

Success `200` `application/json`: [Envelope](#schema-envelope).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |

Documented statuses: `200` Provider linked; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 118. Account document KPIs

- **Java:** `client.workspaces()` — `WorkspaceResource: public List<DocumentStatsRow> stats(String accountId)`; `client.workspaces()` — `WorkspaceResource: public List<DocumentStatsRow> stats(String accountId, String granularity, String month)`
- **HTTP:** `GET /v1/accounts/{accountId}/stats`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Precomputed per-account document-funnel KPIs. `granularity=monthly` (default) returns the last 12 months, most recent first; `granularity=daily` with `month=YYYY-MM` returns that month's days. Series are zero-filled. Requires the `account:read` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `granularity` | query | string enum[monthly, daily] | no | `monthly` (default) or `daily`. |
| `month` | query | string | no | Target month `YYYY-MM` (required when `granularity=daily`). |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[DocumentStatsRow](#schema-documentstatsrow)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[DocumentStatsRow](#schema-documentstatsrow)> | no | no |  |

Documented statuses: `200` KPI series; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 119. My cross-account document KPIs

- **Java:** `client.users()` — `UserResource: public List<DocumentStatsRow> stats()`; `client.users()` — `UserResource: public List<DocumentStatsRow> stats(String granularity, String month)`
- **HTTP:** `GET /v1/users/self/stats`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** The authenticated user's document-funnel KPIs summed across all accounts they currently belong to. `granularity=monthly` (default) returns the last 12 months, most recent first; `granularity=daily` with `month=YYYY-MM` returns that month's days. Series are zero-filled. Not available to OAuth access tokens (`403`): a token is bound to one workspace, so an OAuth app reads that workspace's figures from **Account document KPIs** instead.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `granularity` | query | string enum[monthly, daily] | no | `monthly` (default) or `daily`. |
| `month` | query | string | no | Target month `YYYY-MM` (required when `granularity=daily`). |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[DocumentStatsRow](#schema-documentstatsrow)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[DocumentStatsRow](#schema-documentstatsrow)> | no | no |  |

Documented statuses: `200` KPI series; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 120. List tags

- **Java:** `client.tags()` — `TagResource: public PaginatedResult<Tag> list()`; `client.tags()` — `TagResource: public PaginatedResult<Tag> list(ListParams params)`; `client.tags()` — `TagResource: public PaginatedResult<Tag> list(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/tags`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List the tags of a workspace.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `search` | query | string | no | Search term. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[Tag](#schema-tag)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[Tag](#schema-tag)> | no | no |  |

Documented statuses: `200` The workspace tags; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 121. Create tag

- **Java:** `client.tags()` — `TagResource: public Tag create(CreateTagRequest request)`; `client.tags()` — `TagResource: public Tag create(CreateTagRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/tags`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Create a tag in the workspace. Names are unique per workspace (case-insensitive); a collision returns 409.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: **required**.

`application/json`: object{`name!`: string; `color`: string or null}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | string | yes | no | Trimmed; whitespace collapsed; max 64 chars. |
| `color` | string or null | no | yes | 6-char hex (with or without leading #). |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Tag](#schema-tag)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Tag](#schema-tag) | no | no |  |

Documented statuses: `200` The created tag; `400` One or more fields failed validation.; `409` A tag with the same name already exists.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 122. Update tag

- **Java:** `client.tags()` — `TagResource: public Tag rename(String tagId, RenameTagRequest request)`; `client.tags()` — `TagResource: public Tag rename(String tagId, RenameTagRequest request, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/tags/{tagId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Update a tag's name or color.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `tagId` | path | string | yes | The tag ID. |

Request body: **required**.

`application/json`: object{`name`: string; `color`: string or null}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | string | no | no |  |
| `color` | string or null | no | yes |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Tag](#schema-tag)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Tag](#schema-tag) | no | no |  |

Documented statuses: `200` The updated tag; `400` One or more fields failed validation.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 123. Delete tag

- **Java:** `client.tags()` — `TagResource: public void delete(String tagId)`; `client.tags()` — `TagResource: public void delete(String tagId, boolean force)`; `client.tags()` — `TagResource: public void delete(String tagId, boolean force, String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/tags/{tagId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Delete a tag. Pass `?force=true` to detach it from any documents/templates it is attached to.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `tagId` | path | string | yes | The tag ID. |
| `force` | query | boolean | no | Detach from resources before deleting. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: object{`deleted`: boolean}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | object{`deleted`: boolean} | no | no |  |

Documented statuses: `200` Tag deleted; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 124. List templates

- **Java:** `client.templates()` — `TemplateResource: public PaginatedResult<Template> list()`; `client.templates()` — `TemplateResource: public PaginatedResult<Template> list(ListParams params)`; `client.templates()` — `TemplateResource: public PaginatedResult<Template> list(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/templates`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List the templates of a workspace.  The `status` field of a template is one of:  &#124; Status &#124; Description &#124; &#124;--------&#124;-------------&#124; &#124; `uploading` &#124; The template is being uploaded. &#124; &#124; `uploaded` &#124; The template has been uploaded. &#124; &#124; `processing` &#124; The template is being processed. &#124; &#124; `ready` &#124; The template is ready to use. &#124; &#124; `failed` &#124; The template processing has failed. &#124;

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `search` | query | string | no | Search term. |
| `page` | query | integer | no | Page number. |
| `per-page` | query | integer | no | Records per page: 20 by default, at most 50; a larger value answers 50 a page. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[Template](#schema-template)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[Template](#schema-template)> | no | no |  |

Documented statuses: `200` A page of templates (default_document_tags omitted in the list); `401` Missing or invalid credentials.; `500` Unexpected server error..

### 125. Create document from template

- **Java:** `client.documents()` — `DocumentResource: public Document createFromTemplate(String templateId, CreateDocumentFromTemplateRequest request)`; `client.documents()` — `DocumentResource: public Document createFromTemplate(String templateId, CreateDocumentFromTemplateRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/templates/{templateId}/documents`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Generate a new document from a template. The request is checked (roles, fields, balance) before it returns, but the assignment is created in the background, so the response has `status: uploaded` and `assignment: null`. Follow the outcome on `GET /v1/documents/{documentId}` or through webhooks: when the send succeeds the status moves to `pending_signature` and `assignment_created` is sent; when it fails the status becomes `failed`, `document_processing_failed` is sent with the reason, and nothing is charged. When the template has fields, every signing role used must have at least one field; otherwise `400`. A template with placed signer fields sends a `collect` assignment, so it cannot take a `DigitalCertificate` signer (`400`); certificate signing needs a template without placed signer fields. Provide one signer entry per template role. An entry with `id` uses that existing signer; an entry with `email` reuses the account's signer with that email or creates one.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `templateId` | path | string | yes | The template ID. |
| `Idempotency-Key` | header | string | no | Any text, case-sensitive and new for each document you mean to create (a UUID, say). Retrying with the same key after a lost response answers the document the first request created, as the first response would, and creates and charges nothing, whatever the body says. A key belongs to its template and doesn't expire, so a `failed` document keeps answering it. `404` if that document was deleted; `409` with code `IdempotencyKeyInUse` if the first request is still running: try again. A request refused before its job is queued leaves its key free. |

Request body: **required**.

`application/json`: object{`signers!`: array<object{`role_id!`: string; `id`: string; `email`: string; `name`: string; `whatsapp_phone_number`: string; `verification_method`: string enum[Email, Whatsapp, DigitalCertificate, IdDocumentFaceMatchBasic]; `notification_methods`: array<string>; `embedded`: boolean; `step`: integer}>; `editor_fields`: array<object{`field_id!`: string; `value!`: string}>; `name`: string; `message`: string; `expires_at`: string (date-time); `max_total_credits`: number}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `signers` | array<object{`role_id!`: string; `id`: string; `email`: string; `name`: string; `whatsapp_phone_number`: string; `verification_method`: string enum[Email, Whatsapp, DigitalCertificate, IdDocumentFaceMatchBasic]; `notification_methods`: array<string>; `embedded`: boolean; `step`: integer}> | yes | no | One entry per template role. |
| `editor_fields` | array<object{`field_id!`: string; `value!`: string}> | no | no | Values for the fields placed on the template's `Editor` role, baked into the generated document. The `Editor` role takes no `signers` entry: it is filled only through this list. Every field placed on it needs an entry with a non-empty `value` (`400` otherwise); when it has no fields, send nothing. In `signers`, every `Signer` role needs an entry (`400` otherwise), and a `CopyReceiver` role takes an entry only for someone who should receive the certified document; a copy receiver's `verification_method`, `notification_methods` and `step` are ignored. |
| `name` | string | no | no | Title for the document. Defaults to the template name. |
| `message` | string | no | no | Optional message sent to signers. |
| `expires_at` | string (date-time) | no | no | Assignment expiration date (ISO 8601). No expiration by default. Must be at least one hour in the future. A UTC offset in the value is honored (`2026-12-31T23:59:59-03:00` is `2027-01-01T02:59:59Z`); a value without one is read as UTC, and a date alone (`2026-12-31`) means 23:59 UTC that day. |
| `max_total_credits` | number | no | no | Optional ceiling on what this request may spend: refused with `409` and code `CreditCeilingExceeded`, nothing charged, when its `total_credits` is above this. Send the `total_credits` of the last estimate. A value that is not a number of credits, zero or more, answers `400`; numeric text counts, as a form-encoded body sends it. If the cost rises after the request is accepted, as when the plan's documents run out, nothing is charged, the document ends `failed` and `document_processing_failed` carries the reason in `error_message`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [Document](#schema-document)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [Document](#schema-document) | no | no |  |

Documented statuses: `200` The created document; `400` The request was rejected because of its current state or invalid input.; `404` The requested resource does not exist.; `409` The request costs more than `max_total_credits` (`CreditCeilingExceeded`), or another request with this `Idempotency-Key` is still running (`IdempotencyKeyInUse`).; `403` The workspace's plan lacks a verification or notification method sent (`PlanRestricted`), or an OAuth app's token lacks `documents:write` (`InsufficientScope`); also a deactivated membership or a role.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 126. Estimate document-from-template cost

- **Java:** `client.documents()` — `DocumentResource: public Map<String, Object> estimateCostFromTemplate(String templateId, CreateDocumentFromTemplateRequest request)`; `client.documents()` — `DocumentResource: public Map<String, Object> estimateCostFromTemplate(String templateId, CreateDocumentFromTemplateRequest request, String accountId)`; `client.documents()` — `DocumentResource: public CostEstimate estimateCostFromTemplateTyped(String templateId, CreateDocumentFromTemplateRequest request)`; `client.documents()` — `DocumentResource: public CostEstimate estimateCostFromTemplateTyped(String templateId, CreateDocumentFromTemplateRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/templates/{templateId}/documents/estimate-cost`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Estimate the cost of creating a document from a template without creating it. Contact information is not required — only the role_id and optionally a verification or notification method are needed. Each document always consumes 1 document from the plan's monthly allowance; if exhausted, the ExtraDocument cost is charged from credits (needs_extra_document = true). blocking_reason may be PendingPayment, InsufficientDocuments, or InsufficientCredits.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `templateId` | path | string | yes | The template ID. |

Request body: **required**.

`application/json`: object{`signers!`: array<object{`role_id!`: string; `verification_method`: string enum[Email, Whatsapp, DigitalCertificate, IdDocumentFaceMatchBasic]; `notification_methods`: array<string>}>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `signers` | array<object{`role_id!`: string; `verification_method`: string enum[Email, Whatsapp, DigitalCertificate, IdDocumentFaceMatchBasic]; `notification_methods`: array<string>}> | yes | no | One entry per template role (editor roles are ignored for cost calculation). |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [CostEstimate](#schema-costestimate)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [CostEstimate](#schema-costestimate) | no | no |  |

Documented statuses: `200` Cost estimate; `400` The roles, signers or methods sent cannot make a document (the same rules as creating it).; `403` The workspace's plan lacks a verification or notification method sent (`PlanRestricted`), or an OAuth app's token lacks `documents:write` (`InsufficientScope`); also a deactivated membership or a role.; `404` The template is not in the workspace, or the workspace does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 127. Get the authenticated user

- **Java:** `client.users()` — `UserResource: public AuthUser get()`
- **HTTP:** `GET /v1/users/self`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Returns the profile of the user owning the access token. Not available to OAuth access tokens (`403`); an OAuth app reads the user's identity from `GET /v1/oauth/userinfo`.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [AuthUser](#schema-authuser)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [AuthUser](#schema-authuser) | no | no |  |

Documented statuses: `200` The current user; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 128. Get API key

- **Java:** `client.apiKeys()` — `ApiKeyResource: public ApiKey get()`
- **HTTP:** `GET /v1/users/api-keys`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Retrieve a masked version of the existing API key. The full key cannot be retrieved. Returns `null` when no key has been generated yet.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [ApiKey](#schema-apikey)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [ApiKey](#schema-apikey) | no | no |  |

Documented statuses: `200` The masked API key; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 129. Create API key

- **Java:** `client.apiKeys()` — `ApiKeyResource: public ApiKey create(String password)`
- **HTTP:** `POST /v1/users/api-keys`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Generate an API key for the user, used via the `X-Api-Key` header. Generating a new key deletes the previous one. Never use an API key from a front-end application.

Parameters: none.

Request body: **required**.

`application/json`: object{`password!`: string (password)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `password` | string (password) | yes | no | The user's password. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [ApiKey](#schema-apikey)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [ApiKey](#schema-apikey) | no | no |  |

Documented statuses: `200` The generated API key (shown in full only once); `401` Missing or invalid credentials.; `500` Unexpected server error..

### 130. Delete API key

- **Java:** `client.apiKeys()` — `ApiKeyResource: public void delete()`
- **HTTP:** `DELETE /v1/users/api-keys`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Delete the existing API key.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no |  |

Documented statuses: `200` API key deleted; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 131. Get webhook subscription

- **Java:** `client.webhooks()` — `WebhookResource: public WebhookSubscription get()`; `client.webhooks()` — `WebhookResource: public WebhookSubscription get(String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/webhooks/subscriptions`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Retrieve the account's oldest webhook endpoint — which events it is subscribed to and the delivery configuration. Accounts with several endpoints should use **List webhook endpoints**. Requires the `account:read` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [WebhookSubscription](#schema-webhooksubscription)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [WebhookSubscription](#schema-webhooksubscription) | no | no |  |

Documented statuses: `200` The subscription; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 132. Update webhook subscription

- **Java:** `client.webhooks()` — `WebhookResource: public WebhookSubscription register(RegisterWebhookRequest request)`; `client.webhooks()` — `WebhookResource: public WebhookSubscription register(RegisterWebhookRequest request, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/webhooks/subscriptions`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Update the account's oldest webhook endpoint (creating it if the account has none) — which events are monitored, whether delivery is enabled, and the delivery/contact details. Accounts with several endpoints should use **Update webhook endpoint**. Requires the `webhooks:write` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: **required**.

`application/json`: object{`events!`: array<string>; `is_active!`: boolean; `url!`: string (uri); `email!`: string (email)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `events` | array<string> | yes | no | Event type codes to subscribe to (see `GET /v1/webhooks/event-types`). |
| `is_active` | boolean | yes | no | Whether events should be delivered to the webhook. |
| `url` | string (uri) | yes | no | The URL that will receive events: `https` for a new or changed URL (a URL stored before keeps working). Its host must resolve to a public address (`400` otherwise), and redirects are not followed. |
| `email` | string (email) | yes | no | Email that receives important webhook-communication notices. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [WebhookSubscription](#schema-webhooksubscription)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [WebhookSubscription](#schema-webhooksubscription) | no | no |  |

Documented statuses: `200` The updated subscription; `400` One or more fields failed validation.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 133. Inactivate webhook subscription

- **Java:** `client.webhooks()` — `WebhookResource: public WebhookSubscription inactivate()`; `client.webhooks()` — `WebhookResource: public WebhookSubscription inactivate(String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/webhooks/inactivate`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Deactivate the account's oldest webhook endpoint. While inactive, no events are sent to it; other endpoints are unaffected. Requires the `webhooks:write` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [WebhookSubscription](#schema-webhooksubscription)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [WebhookSubscription](#schema-webhooksubscription) | no | no |  |

Documented statuses: `200` The inactivated subscription; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 134. List webhook event types

- **Java:** `client.webhooks()` — `WebhookResource: public List<WebhookEventTypeInfo> listEventTypes()`
- **HTTP:** `GET /v1/webhooks/event-types`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List all available event types that can be subscribed to via webhooks. Requires the `documents:read` OAuth scope.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[WebhookEventType](#schema-webhookeventtype)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[WebhookEventType](#schema-webhookeventtype)> | no | no |  |

Documented statuses: `200` Event types; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 135. List webhook deliveries

- **Java:** `client.webhooks()` — `WebhookResource: public PaginatedResult<WebhookDispatch> listDispatches()`; `client.webhooks()` — `WebhookResource: public PaginatedResult<WebhookDispatch> listDispatches(ListParams params)`; `client.webhooks()` — `WebhookResource: public PaginatedResult<WebhookDispatch> listDispatches(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/webhooks`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Retrieve the delivery history for webhooks sent to the account's endpoints — use it to monitor status, debug failures, and verify payloads. Pagination is returned in the `X-Pagination-*` response headers. Requires the `documents:read` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `endpoint_id` | query | string | no | Only deliveries to this webhook endpoint. |
| `event` | query | string | no | Filter by event type (e.g. `document_ready`). |
| `delivered` | query | string enum[true, false] | no | Filter by delivery status: `true` or `false`. |
| `from` | query | integer | no | Unix timestamp — only entries after this time. |
| `to` | query | integer | no | Unix timestamp — only entries before this time. |
| `page` | query | integer | no | Page number. |
| `per-page` | query | integer | no | Records per page: 20 by default, at most 50; a larger value answers 50 a page. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[WebhookDispatch](#schema-webhookdispatch)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[WebhookDispatch](#schema-webhookdispatch)> | no | no |  |

Documented statuses: `200` Delivery history; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 136. Retry webhook delivery

- **Java:** `client.webhooks()` — `WebhookResource: public WebhookDispatch retryDispatch(String dispatchId)`; `client.webhooks()` — `WebhookResource: public WebhookDispatch retryDispatch(String dispatchId, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/webhooks/{historyId}/retry`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Sends the entry's event to the entry's endpoint once, now, even while the endpoint is paused, and returns the dispatch entry of that attempt; a delivery that gets through lifts the pause. Requires the `webhooks:write` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `historyId` | path | string | yes | The webhook dispatch entry ID to retry. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [WebhookDispatch](#schema-webhookdispatch)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [WebhookDispatch](#schema-webhookdispatch) | no | no |  |

Documented statuses: `200` The new dispatch entry; `400` One or more fields failed validation.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 137. List webhook endpoints

- **Java:** `client.webhooks()` — `WebhookResource: public List<WebhookEndpoint> listEndpoints()`;
- **HTTP:** `GET /v1/accounts/{accountId}/webhooks/endpoints`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List the account's webhook endpoints, oldest first. Requires the `account:read` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `url` | query | string (uri) | no | Only the endpoint registered with exactly this URL. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[WebhookEndpoint](#schema-webhookendpoint)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[WebhookEndpoint](#schema-webhookendpoint)> | no | no |  |

Documented statuses: `200` The endpoints; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 138. Create webhook endpoint

- **Java:** `client.webhooks()` — `WebhookResource: public WebhookEndpoint createEndpoint(WebhookEndpointRequest request)`; `client.webhooks()` — `WebhookResource: public WebhookEndpoint createEndpoint(WebhookEndpointRequest request, String accountId)`. When `events` is null the SDK sends the same default set as `register`; a `403` past the plan limit raises `AuthenticationException`.
- **HTTP:** `POST /v1/accounts/{accountId}/webhooks/endpoints`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Register a URL to receive the account's webhook events. An account can have 1 endpoint, or up to 3 on paid plans; creating one past the limit returns `403`. Each endpoint of a workspace must have a different `url` (`400` otherwise). When `signing_enabled` is `true`, a signing secret is generated and returned once, as `secret` in this response — store it, since it is never shown in list or view responses. It can be read again later with **Get webhook endpoint signing secret**, except by OAuth applications: an OAuth application that loses the secret gets a new one with **Rotate webhook endpoint signing secret**. Requires the `webhooks:write` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |

Request body: **required**.

`application/json`: object{`url!`: string (uri); `email!`: string (email); `events!`: array<string>; `name`: string; `is_active`: boolean; `signing_enabled`: boolean}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `url` | string (uri) | yes | no | URL that receives the events: `https` for a new or changed URL (a URL stored before keeps working). Its host must resolve to a public address; private, loopback, link-local and other reserved addresses are refused with `400`. Deliveries don't follow redirects: a `3xx` answer counts as a failed delivery. |
| `email` | string (email) | yes | no | Contact email for delivery-failure notices. |
| `events` | array<string> | yes | no | Event types to deliver (see `GET /v1/webhooks/event-types`). |
| `name` | string | no | no | Label to tell endpoints apart. |
| `is_active` | boolean | no | no | Whether events are delivered. Defaults to `true`. |
| `signing_enabled` | boolean | no | no | Sign deliveries with a Standard Webhooks signature. Defaults to `false`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [WebhookEndpoint](#schema-webhookendpoint) + object{`secret`: string}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [WebhookEndpoint](#schema-webhookendpoint) + object{`secret`: string} | no | no |  |

Documented statuses: `200` The created endpoint; `400` One or more fields failed validation.; `403` Authenticated but not allowed to perform this action.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 139. Delete webhook endpoint by URL

- **Java:** `client.webhooks().deleteEndpointByUrl(url, accountId)` → `void`.; `client.webhooks()` — `WebhookResource: public void deleteEndpointByUrl(String url, String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/webhooks/endpoints`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Stop delivering events to the endpoint registered with `url` and free its slot, for integrations that keep the URL they registered rather than the endpoint ID. The URL must match exactly as it was registered. Returns `404` when no endpoint of the workspace uses it. Requires the `webhooks:write` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `url` | query | string (uri) | yes | The endpoint's URL. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no |  |

Documented statuses: `200` Endpoint deleted; `400` One or more fields failed validation.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 140. Get webhook endpoint

- **Java:** `client.webhooks()` — `WebhookResource: public WebhookEndpoint getEndpoint(String endpointId)`; `client.webhooks()` — `WebhookResource: public WebhookEndpoint getEndpoint(String endpointId, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/webhooks/endpoints/{endpointId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Retrieve one webhook endpoint. Requires the `account:read` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `endpointId` | path | string | yes | The webhook endpoint ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [WebhookEndpoint](#schema-webhookendpoint)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [WebhookEndpoint](#schema-webhookendpoint) | no | no |  |

Documented statuses: `200` The endpoint; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 141. Update webhook endpoint

- **Java:** `client.webhooks()` — `WebhookResource: public WebhookEndpoint updateEndpoint(String endpointId, WebhookEndpointRequest request)`; `client.webhooks()` — `WebhookResource: public WebhookEndpoint updateEndpoint(String endpointId, WebhookEndpointRequest request, String accountId)`. Only non-null request fields are sent; an empty request raises `ValidationException`.
- **HTTP:** `PUT /v1/accounts/{accountId}/webhooks/endpoints/{endpointId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Change a webhook endpoint. Only the fields sent are updated; `url` cannot be one another endpoint of the workspace already uses, nor point to a private or reserved address (`400`). Setting `signing_enabled` to `true` generates a secret if the endpoint has none and keeps the current one otherwise; setting it to `false` discards the secret. When this request turns signing on, the new secret is returned once, as `secret` in this response, as on create; otherwise `secret` is absent. Requires the `webhooks:write` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `endpointId` | path | string | yes | The webhook endpoint ID. |

Request body: **required**.

`application/json`: object{`url`: string (uri); `email`: string (email); `events`: array<string>; `name`: string; `is_active`: boolean; `signing_enabled`: boolean}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `url` | string (uri) | no | no |  |
| `email` | string (email) | no | no |  |
| `events` | array<string> | no | no |  |
| `name` | string | no | no |  |
| `is_active` | boolean | no | no |  |
| `signing_enabled` | boolean | no | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [WebhookEndpoint](#schema-webhookendpoint) + object{`secret`: string}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [WebhookEndpoint](#schema-webhookendpoint) + object{`secret`: string} | no | no |  |

Documented statuses: `200` The updated endpoint; `400` One or more fields failed validation.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 142. Delete webhook endpoint

- **Java:** `client.webhooks()` — `WebhookResource: public void deleteEndpoint(String endpointId)`; `client.webhooks()` — `WebhookResource: public void deleteEndpoint(String endpointId, String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/webhooks/endpoints/{endpointId}`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Stop delivering events to an endpoint and free its slot. Requires the `webhooks:write` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `endpointId` | path | string | yes | The webhook endpoint ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<any>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<any> | no | no |  |

Documented statuses: `200` Endpoint deleted; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 143. Get webhook endpoint signing secret

- **Java:** `client.webhooks()` — `WebhookResource: public String getEndpointSecret(String endpointId)`; `client.webhooks()` — `WebhookResource: public String getEndpointSecret(String endpointId, String accountId)`. Returns `data.secret`; verify deliveries with `WebhookSignature.verify(...)`.
- **HTTP:** `GET /v1/accounts/{accountId}/webhooks/endpoints/{endpointId}/secret`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Return the secret used to sign deliveries to this endpoint (see **Webhook Payloads → Verifying signatures**). Returns `400` when signing is disabled. Not available to OAuth applications: they receive a secret only when one is generated, in the **Create webhook endpoint** or **Update webhook endpoint** response (when that request turns signing on) or from **Rotate webhook endpoint signing secret**.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `endpointId` | path | string | yes | The webhook endpoint ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [WebhookEndpointSecret](#schema-webhookendpointsecret)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [WebhookEndpointSecret](#schema-webhookendpointsecret) | no | no |  |

Documented statuses: `200` The secret; `400` One or more fields failed validation.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 144. Rotate webhook endpoint signing secret

- **Java:** `client.webhooks()` — `WebhookResource: public String rotateEndpointSecret(String endpointId)`; `client.webhooks()` — `WebhookResource: public String rotateEndpointSecret(String endpointId, String accountId)`. Returns the new `data.secret`.; `client.webhooks()` — `WebhookResource: public WebhookEndpointSecret rotateEndpointSecret(String endpointId, boolean keepPrevious, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/webhooks/endpoints/{endpointId}/secret/rotate`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Replace the endpoint's signing secret and return the new one. The old secret stops working immediately, so update your receiver right away; or send `keep_previous: true` to keep it signing deliveries beside the new one for 24 hours (`previous_secret_expires_at`): during that time `webhook-signature` carries both signatures, so your receiver can switch to the new secret without failing verification. Only the secret just replaced is kept: rotating again within those 24 hours stops the one kept before. Two rotations sent at once take effect one after the other, each answering the secret it set. Returns `400` when signing is disabled or `keep_previous` is not a boolean. Requires the `webhooks:write` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `endpointId` | path | string | yes | The webhook endpoint ID. |

Request body: **optional**.

`application/json`: object{`keep_previous`: boolean}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `keep_previous` | boolean | no | no | `true`: the replaced secret keeps signing for 24 hours. Default `false`: it stops at once. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [WebhookEndpointSecret](#schema-webhookendpointsecret) + object{`previous_secret_expires_at`: string (date-time)}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [WebhookEndpointSecret](#schema-webhookendpointsecret) + object{`previous_secret_expires_at`: string (date-time)} | no | no |  |

Documented statuses: `200` The new secret; `400` One or more fields failed validation.; `404` The requested resource does not exist.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 145. Send a test delivery

- **Java:** `client.webhooks()` — `WebhookResource: public WebhookDispatch testEndpoint(String endpointId, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/webhooks/endpoints/{endpointId}/test`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** Sends the endpoint one `webhook_test` event now, signed and sent like any delivery, and returns the entry of that attempt (`kind: manual`, also listed in the delivery history). It goes to this endpoint only, whatever events it subscribes to, and also while the endpoint is paused or inactive; it neither lifts a pause nor counts toward one. The endpoint has 10 seconds to answer. Its `payload` and `origin` are `null`; the endpoint object carries its id, name, url, events and status, never its secret. No endpoint can subscribe to `webhook_test`, and it is not listed in `GET /webhooks/event-types`. Its `payload` and `origin` are `null`; the endpoint object carries its id, name, url, events and status, never its secret. No endpoint can subscribe to `webhook_test`, and it is not listed in `GET /webhooks/event-types`. The event names the endpoint as its `subject` and `object` and carries nothing about the workspace beyond `account_id`: no billing and no members. Up to 10 tests per user every 10 minutes. Requires the `webhooks:write` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `accountId` | path | string | yes | Workspace account ID. |
| `endpointId` | path | string | yes | The webhook endpoint ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: [WebhookDispatch](#schema-webhookdispatch)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | [WebhookDispatch](#schema-webhookdispatch) | no | no |  |

Documented statuses: `200` The entry of the test delivery; `404` The requested resource does not exist.; `429` Rate limit exceeded.; `401` Missing or invalid credentials.; `500` Unexpected server error..

### 146. OAuth 2.0 protected resource metadata

- **Java:** `client.oauth()` — `OAuthResource: public OAuthProtectedResourceMetadata protectedResourceMetadata()`
- **HTTP:** `GET /.well-known/oauth-protected-resource`
- **Auth:** Public; any path/body token is still required
- **Contract notes:** RFC 9728. Identifies this API as a protected resource, the      *         authorization server(s) that can issue tokens for it, and the scopes it      *         accepts. `scopes_supported` deliberately excludes `offline_access` — per the      *         MCP specification, requesting a refresh token is a client concern, not      *         something a resource is protected by. Also referenced from the      *         `WWW-Authenticate: Bearer resource_metadata="..."` challenge on a 401/403.      *         Per RFC 8615, the response is the bare metadata object itself — never this      *         API's usual `{status, data, message}` envelope. The `authorization_servers` entry names the host that owns the browser-facing flow — start every integration by fetching `{authorization_servers[0]}/.well-known/oauth-authorization-server` (RFC 8414) from there, not from this API.

Parameters: none.

Request body: none.

Success `200` `application/json`: object{`resource`: string (uri); `authorization_servers`: array<string (uri)>; `scopes_supported`: array<string>; `bearer_methods_supported`: array<string>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string (uri) | no | no |  |
| `authorization_servers` | array<string (uri)> | no | no |  |
| `scopes_supported` | array<string> | no | no |  |
| `bearer_methods_supported` | array<string> | no | no |  |

Documented statuses: `200` The protected resource metadata; `500` Unexpected server error..

### 147. List WhatsApp notifications

- **Java:** `client.assignments()` — `AssignmentResource: public List<Map<String, Object>> getWhatsappNotifications(String documentId, String assignmentId)`; `client.assignments()` — `AssignmentResource: public List<WhatsappNotification> getWhatsappNotificationsTyped(String documentId, String assignmentId)`
- **HTTP:** `GET /v1/documents/{documentId}/assignments/{assignmentId}/whatsapp-notifications`
- **Auth:** Bearer token or `X-Api-Key`
- **Contract notes:** List all WhatsApp notification messages sent for an assignment. The response includes the rendered template text split into `header`, `body` and `buttons` — exactly what the signer would see. In sandbox/stage, WhatsApp messages are simulated (no real delivery) and button URLs include access/verification codes you can use to simulate the signing flow; in production the button URLs are stripped. Requires the `documents:read` OAuth scope.

| Parameter | In | Type | Required | Notes |
|---|---|---|---:|---|
| `documentId` | path | string | yes | Document ID. |
| `assignmentId` | path | string | yes | The assignment ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{`data`: array<[WhatsappNotification](#schema-whatsappnotification)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |
| `data` | array<[WhatsappNotification](#schema-whatsappnotification)> | no | no |  |

Documented statuses: `200` WhatsApp notifications; `401` Missing or invalid credentials.; `500` Unexpected server error..

## Component payload schemas

Each linked schema lists its complete fields; inline objects enumerate their nested fields. Required fields carry `!` in inline types.

### Schema: Envelope

Standard success wrapper. Operations add their own `data`.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no | HTTP status code, mirrored in the body. |
| `message` | string | no | no | Human-readable message; empty on success. |

### Schema: ErrorEnvelope

Standard error wrapper. `status` mirrors the HTTP status code.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | integer | no | no |  |
| `message` | string | no | no | Human-readable error message. |
| `data` | object or null | no | yes |  |
| `code` | string | no | no | On the refusals a client can act on: see Reading a refusal. Other refusals have only `message`. |
| `errors` | array<object{`field!`: string or null; `code!`: string; `message!`: string}> | no | no | On a `400` about a field (a model's validation, a missing or an unknown field): one entry per field in error; `message` stays the first entry's. Other `400`s have only `message`. See Reading a refusal. |

### Schema: ApiKey



| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `api_key` | string or null | no | yes |  |

### Schema: AuthUser



| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | string | no | no |  |
| `name` | string | no | no |  |
| `email` | string (email) | no | no |  |
| `telephone` | string or null | no | yes |  |
| `government_id` | string or null | no | yes |  |
| `is_email_verified` | boolean | no | no |  |
| `has_accepted_terms` | boolean | no | no |  |
| `created_at` | string (date-time) | no | no |  |
| `to_be_deleted_at` | string or null (date-time) | no | yes |  |

### Schema: AuthAccount



| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | string | no | no |  |
| `name` | string | no | no |  |
| `roles` | array<string> | no | no |  |
| `is_delete_allowed` | boolean | no | no |  |
| `created_at` | string (date-time) | no | no |  |

### Schema: Signer

A signing party belonging to a workspace account.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no | Present in single-resource responses. |
| `id` | string | no | no |  |
| `full_name` | string | no | no |  |
| `email` | string or null (email) | no | yes |  |
| `whatsapp_phone_number` | string or null | no | yes | E.164 format; normalized on save. |
| `government_id` | string or null | no | yes | Signer's normalized CPF (11 digits) or CNPJ (14 characters; may be alphanumeric). |
| `has_accepted_terms` | boolean | no | no |  |

### Schema: SignerSelf

The current signer, as returned by `GET /v1/signers/self`. Extends Signer with the signature-state flags that are only computed for the authenticated signer.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no | Present in single-resource responses. |
| `id` | string | no | no |  |
| `full_name` | string | no | no |  |
| `email` | string or null (email) | no | yes |  |
| `whatsapp_phone_number` | string or null | no | yes | E.164 format; normalized on save. |
| `government_id` | string or null | no | yes | Signer's normalized CPF (11 digits) or CNPJ (14 characters; may be alphanumeric). |
| `has_accepted_terms` | boolean | no | no |  |
| `has_signature` | boolean | no | no | Whether the signer has a saved signature image stored. |
| `has_initial` | boolean | no | no | Whether the signer has a saved initials image stored. |
| `is_signature_reusable` | boolean | no | no | Whether the signer opted to reuse their saved signature/initials in future processes. When false, clients should not pre-render the saved image even if `has_signature`/`has_initial` is true. |
| `verification_method` | string enum[Email, Whatsapp, DigitalCertificate, IdDocumentFaceMatchBasic] | no | no | How this signature request verifies the signer. `Email`/`Whatsapp` ask for the 6-digit code; `IdDocumentFaceMatchBasic` runs the identity document check instead (see **Identity document verification status**). |

### Schema: DocumentPage



| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | string | no | no |  |
| `number` | integer | no | no |  |
| `height` | integer | no | no |  |
| `width` | integer | no | no |  |
| `download_url` | string | no | no |  |

### Schema: DisplaySettings

A field placement rectangle on a document page. Geometry values are pixels in Assinafy's 150-DPI page image, measured from the upper-left corner. Clients must keep the rectangle within the selected page's width and height; the API does not clamp out-of-bounds values.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `left` | number (float) | yes | no | Horizontal distance from the page's left edge, in page-image pixels. minimum: `0`. |
| `top` | number (float) | yes | no | Vertical distance from the page's top edge, in page-image pixels. minimum: `0`. |
| `width` | number (float) | yes | no | Width of the placement rectangle, in page-image pixels. |
| `height` | number (float) | yes | no | Height of the placement rectangle, in page-image pixels. |
| `fontFamily` | string | no | no | Font-family presentation metadata. |
| `fontSize` | number (float) | yes | no | Font size in the 150-DPI page-image coordinate system. |
| `backgroundColor` | string | no | no | CSS-compatible background-color presentation metadata. |

### Schema: DocumentStatus



| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `code` | string | no | no |  |
| `deletable` | boolean | no | no |  |

### Schema: Document

A document and its current lifecycle state.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no | Present in single-resource responses. |
| `id` | string | no | no |  |
| `account_id` | string | no | no |  |
| `template_id` | string or null | no | yes |  |
| `name` | string | no | no |  |
| `status` | string | no | no | Status code — see GET /v1/documents/statuses. |
| `artifacts` | object | no | no | Artifact download URLs keyed by name. Always `original`, plus `thumbnail` once one exists. A certificated document also carries `certificated`, `certificate-page` and `bundle`, and `pades` when it was signed with a digital certificate — the PAdES version holds the signers' ICP-Brasil signatures, which certification flattens out of the certificated PDF. |
| `is_closed` | boolean | no | no |  |
| `signing_url` | string | no | no |  |
| `verification_code` | string or null | no | yes | The code printed on the certificate page. Anyone can check it with `GET /v1/documents/{verification_code}/verify` or on `verification_url`. `null` until the document is `certificated`, and on documents certificated before verification codes existed. |
| `verification_url` | string or null (uri) | no | yes | The public verification page for this document. `null` until the document is `certificated`. |
| `decline_reason` | string or null | no | yes |  |
| `declined_by` | [Signer](#schema-signer) or null | no | no |  |
| `assignment` | [Assignment](#schema-assignment) or null | no | no | The document's assignment, or `null` before one is created. The server decides when it is included, whatever `expand` the request sends: the document list, `GET /v1/documents/{documentId}`, the signer's document list and document creation from a template always include it; `/documents/search` and the upload response never do. Only the rename response follows `?expand=assignment`. |
| `pages` | array<[DocumentPage](#schema-documentpage)> | no | no | The document's pages. Included on the same responses as `assignment`, and also on the upload response. |
| `envelope_id` | string or null | no | yes | Only on `GET /v1/documents/{documentId}?expand=envelope_id` read by the workspace, not in document lists or webhooks, so their payloads keep their shape: the envelope this document is in, or `null` for a document sent on its own, taken out of its envelope, or whose envelope was deleted. |
| `envelope` | object{`id!`: string; `name!`: string} | no | yes | Only on `GET /v1/documents/{documentId}?expand=envelope` read by the workspace and `GET /v1/sign?expand=envelope` or `GET /v1/signers/{signerId}/document?expand=envelope` read with the signer's code, not in document lists or webhooks: the envelope this document is in, or `null` for a document sent on its own, taken out of its envelope, or whose envelope was deleted. The signer's reads name the envelope their code belongs to even when the code's own document was taken out, since the code still reaches the rest. |
| `has_contract` | boolean | no | no | Only on `GET /v1/documents/{documentId}` read by the workspace, while the environment has contract tracking on: not with a signer's access code, not in document lists or webhooks, and left out when the lookup fails, so a missing key means unknown. Whether `GET /v1/accounts/{accountId}/documents/{documentId}/contract` answers with contract terms, the document's own or those of a contract it amends whose document still exists, so a page can lay out the contract section before reading it. |
| `created_at` | string (date-time) | no | no |  |
| `updated_at` | string (date-time) | no | no |  |

### Schema: SigningDocument

The document as its signer reads it with the access code: the document with `current_signer`, `is_signer_access_closed` and `certified_copy`. Once the code is closed, only `id`, `name`, `status` and these three, and `envelope` when asked for.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no | Present in single-resource responses. |
| `id` | string | yes | no |  |
| `account_id` | string | no | no |  |
| `template_id` | string or null | no | yes |  |
| `name` | string | yes | no |  |
| `status` | string | yes | no | Status code — see GET /v1/documents/statuses. |
| `artifacts` | object | no | no | Artifact download URLs keyed by name. Always `original`, plus `thumbnail` once one exists. A certificated document also carries `certificated`, `certificate-page` and `bundle`, and `pades` when it was signed with a digital certificate — the PAdES version holds the signers' ICP-Brasil signatures, which certification flattens out of the certificated PDF. |
| `is_closed` | boolean | no | no |  |
| `signing_url` | string | no | no |  |
| `verification_code` | string or null | no | yes | The code printed on the certificate page. Anyone can check it with `GET /v1/documents/{verification_code}/verify` or on `verification_url`. `null` until the document is `certificated`, and on documents certificated before verification codes existed. |
| `verification_url` | string or null (uri) | no | yes | The public verification page for this document. `null` until the document is `certificated`. |
| `decline_reason` | string or null | no | yes |  |
| `declined_by` | [Signer](#schema-signer) or null | no | no |  |
| `assignment` | [Assignment](#schema-assignment) or null | no | no | The document's assignment, or `null` before one is created. The server decides when it is included, whatever `expand` the request sends: the document list, `GET /v1/documents/{documentId}`, the signer's document list and document creation from a template always include it; `/documents/search` and the upload response never do. Only the rename response follows `?expand=assignment`. |
| `pages` | array<[DocumentPage](#schema-documentpage)> | no | no | The document's pages. Included on the same responses as `assignment`, and also on the upload response. |
| `envelope_id` | string or null | no | yes | Only on `GET /v1/documents/{documentId}?expand=envelope_id` read by the workspace, not in document lists or webhooks, so their payloads keep their shape: the envelope this document is in, or `null` for a document sent on its own, taken out of its envelope, or whose envelope was deleted. |
| `envelope` | object{`id!`: string; `name!`: string} | no | yes | Only on `GET /v1/documents/{documentId}?expand=envelope` read by the workspace and `GET /v1/sign?expand=envelope` or `GET /v1/signers/{signerId}/document?expand=envelope` read with the signer's code, not in document lists or webhooks: the envelope this document is in, or `null` for a document sent on its own, taken out of its envelope, or whose envelope was deleted. The signer's reads name the envelope their code belongs to even when the code's own document was taken out, since the code still reaches the rest. |
| `has_contract` | boolean | no | no | Only on `GET /v1/documents/{documentId}` read by the workspace, while the environment has contract tracking on: not with a signer's access code, not in document lists or webhooks, and left out when the lookup fails, so a missing key means unknown. Whether `GET /v1/accounts/{accountId}/documents/{documentId}/contract` answers with contract terms, the document's own or those of a contract it amends whose document still exists, so a page can lay out the contract section before reading it. |
| `created_at` | string (date-time) | no | no |  |
| `updated_at` | string (date-time) | no | no |  |
| `current_signer` | [Signer](#schema-signer) + object{`verification_method`: string; `notification_methods`: array or null; `completed`: boolean} | yes | no | This signer. Once the code is closed, only `id` and the last three. |
| `is_signer_access_closed` | boolean | yes | no | Whether the code is closed. |
| `certified_copy` | object{`available!`: boolean; `notification_method!`: string or null enum[Email, Whatsapp, None]; `resends_left!`: integer or null} | yes | no | Whether the signer can ask for the certified copy again (`POST /v1/sign/certified-copy`). |

### Schema: Account

A workspace account (organization).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no |  |
| `id` | string | no | no |  |
| `name` | string | no | no |  |
| `primary_color` | string or null | no | yes |  |
| `secondary_color` | string or null | no | yes |  |
| `notification_sender_type` | string enum[User, Account] | no | no |  |
| `roles` | array<string> | no | no |  |
| `is_delete_allowed` | boolean | no | no |  |
| `limits` | object{`webhook_endpoints`: integer} | no | no | Limits of the workspace's current plan. Returned only by `GET /v1/accounts/{accountId}`, not in account lists. More keys may be added. |
| `signing_features` | object{`DigitalCertificate!`: boolean; `VerificationIdDocumentFaceMatchBasic!`: boolean; `WhatsappNotification!`: boolean; `VerificationBypass!`: boolean; `Embed!`: boolean} | no | no | Only with `?expand=signing_features` on `GET /v1/accounts/{accountId}`: which signing features the workspace's subscription includes now (its plan's and any granted to it), so a client offers only the identity checks and notices it can use. `DigitalCertificate` and `VerificationIdDocumentFaceMatchBasic` gate the `DigitalCertificate` and `IdDocumentFaceMatchBasic` verification methods, `WhatsappNotification` the `Whatsapp` notification method (and so the `Whatsapp` verification method), and `VerificationBypass` the `Bypass` verification method. `false` means creating an assignment, or a document from a template, with that method (or estimating its cost) answers `403`. `Embed` gates embedded signing: with `false`, the embed origins endpoints, `"embedded": true` on a signer and minting an embed session answer `403` with code `PlanRestricted`. `true` is not a promise: credits, the assignment's own rules (a `DigitalCertificate` signer needs a `virtual` assignment) and a change of plan still apply. Requires the `account:read` OAuth scope. |
| `created_at` | string (date-time) | no | no |  |

### Schema: Field

A reusable field definition.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no |  |
| `id` | string | no | no |  |
| `name` | string | no | no |  |
| `type` | string | no | no |  |
| `regex` | string or null | no | yes |  |
| `is_pre_defined` | boolean | no | no |  |
| `is_active` | boolean | no | no |  |
| `is_required` | boolean | no | no |  |
| `is_standard` | boolean | no | no |  |
| `is_read_only` | boolean | no | no |  |
| `is_visible` | boolean | no | no |  |

### Schema: Tag

A workspace-scoped label. Names are unique per workspace (case-insensitive).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no |  |
| `id` | string | no | no |  |
| `name` | string | no | no |  |
| `color` | string or null | no | yes | 6-char hex without leading #. |
| `created_at` | string (date-time) | no | no |  |
| `updated_at` | string (date-time) | no | no |  |

### Schema: DocumentContract

The contract terms tracked on a document. Every key below is always present, `null` when it holds no value, except `resource`, `document` and `amendments`, which depend on the response.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no | On a response that is this contract alone; list items carry none. |
| `document_id` | string | yes | no |  |
| `status` | string enum[draft, active, expired, terminated] | yes | no |  |
| `counterparty_name` | string or null | yes | yes |  |
| `counterparty_taxpayer_id` | string or null | yes | yes | CPF or CNPJ, digits only. |
| `value` | number or null | yes | yes | Two decimal places at most. |
| `currency` | string or null | yes | yes | ISO 4217 code; `BRL` when a value is sent without one. |
| `start_date` | string or null (date) | yes | yes |  |
| `end_date` | string or null (date) | yes | yes |  |
| `auto_renew` | boolean | yes | no |  |
| `renewal_term_months` | integer or null | yes | yes |  |
| `notice_period_days` | integer or null | yes | yes |  |
| `term` | integer | yes | no | Steps on every renewal, starting at 1. |
| `owner_id` | string or null | yes | yes | ID of the workspace member who owns the contract. When the owner is no longer a member of the workspace (removed, left, or deleted with their account), the hourly run hands the contract to the first owner who is still an active member, or leaves it without one; an inactive member stays the owner. The contract's e-mails (renewal notice, expiring, expired, renewed) go to the first of the owner and the document's creator who is an active member, as that member's notification preferences allow. |
| `metadata` | object or null; values string | yes | yes | Up to 50 keys of your own, each holding text of at most 500 characters; `null` when there are none. Values come back exactly as sent. A key ending in `_at`, or `stuck_since`, cannot hold a number. |
| `external_references` | array<object{`system!`: string; `id!`: string}> | yes | no | IDs outside systems use for this contract, ordered by system and id; `[]` when there are none. |
| `document` | [Document](#schema-document) | no | no | Only with `expand=document`: the document the terms are tracked on. |
| `amendments` | array<[DocumentContractAmendment](#schema-documentcontractamendment)> | no | no | Only on the contract read and the related read (`GET …/contract`, `GET …/contract/related`), never on the list or on the answer to a write, renewal or termination: the documents that amend, renew or terminate this contract, the applied ones first by `number`, then the pending ones. |
| `created_at` | string (date-time) | yes | no |  |
| `updated_at` | string (date-time) | yes | no |  |

### Schema: DocumentContractInput

Every attribute is optional and only the ones sent change. `null` clears an attribute, except `auto_renew` (back to `false`) and `currency` (back to `BRL` while a `value` is set); `status` does not accept `null`. A changed `end_date` starts a new `term`.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | string enum[draft, active, expired, terminated] | no | no | On creation any status, so a contract that ended before it was tracked is registered as it ended: `expired` needs an `end_date` in the past, `terminated` its `end_date`; neither is walked by the lifecycle. Afterwards only `draft` or `active` (a contract in force never goes back to `draft`): `expired` and `terminated` are set by the lifecycle and the terminate endpoint; a closed contract is reopened by renewing it. |
| `counterparty_name` | string or null | no | yes | Emoji are kept; other characters outside the 3-byte range are dropped. maxLength: `255`. |
| `counterparty_taxpayer_id` | string or null | no | yes | A valid CPF or CNPJ, formatted or not. |
| `value` | number or null | no | yes | Two decimal places at most. minimum: `0`. maximum: `9999999999999.99`. |
| `currency` | string or null | no | yes | Three letters, such as `BRL` or `USD`, stored in capitals; `BRL` when a `value` is set without one. |
| `start_date` | string or null (date) | no | yes | `YYYY-MM-DD`, from the year 1000. |
| `end_date` | string or null (date) | no | yes | `YYYY-MM-DD`, on or after `start_date`. |
| `auto_renew` | boolean | no | no | Whether the hourly run starts the next term the day after `end_date`, by `renewal_term_months`, instead of expiring the contract. |
| `renewal_term_months` | integer or null | no | yes | How far a renewal by the term moves `end_date`; 12 when unset. minimum: `1`. maximum: `600`. |
| `notice_period_days` | integer or null | no | yes | Days before `end_date` the notice period starts (`document_contract_renewal_notice`); none when unset. minimum: `0`. maximum: `3650`. |
| `owner_id` | string or null | no | yes | An active member of this workspace. |
| `metadata` | object or null; values string | no | yes | Your own fields: up to 50 keys of 1 to 40 letters, digits, `_`, `-` or `.`, each holding plain text of at most 500 characters; no number under a key ending in `_at`, or `stuck_since`. |
| `external_references` | array or null | no | yes | Replaces the whole list; `[]` or `null` clears it. Two items whose `system` and `id` compare equal case aside are one reference (the first sent is kept); a pair the database takes for one beyond that (accents, `ß` and `ss`) answers `400`. maxItems: `20`. |
| `parent_document_id` | string or null | no | yes | Links the document as an amendment of this contract document, or of any document in its family (resolved to the root); `null` unlinks a pending amendment. With it, the other attributes are the terms the instrument changes, not terms of its own; on a pending amendment a `null` value drops that term from the delta rather than clearing it on the parent. |
| `relation_type` | string enum[amendment, renewal, termination] | no | no | Required on the first link. `amendment` may change `value`, `currency`, `start_date` (effective from, display only), `end_date`, `auto_renew`, `renewal_term_months`, `notice_period_days`, `counterparty_name`, `counterparty_taxpayer_id` and `metadata`; `renewal` only `end_date` and `value`; `termination` only `end_date` (the effective date). |
| `updated_at` | string (date-time) | no | no | Optional precondition: the `updated_at` the client read the contract (or this document's amendment link) with, as the API returned it. If the contract or link changed since, nothing is written and the answer is `409` with `code: ContractChanged`. Not sent, or `null`: the write applies whatever changed meanwhile (last write wins). Compared to the second; a fraction of a second, as a JavaScript date prints it, is read and ignored. New terms and a first link have nothing to have read, so sending one answers `409`; on a pending amendment, send the link's own `updated_at` (the GET on that document). |

### Schema: DocumentContractAmendment

A document that amends, renews or terminates a contract: its link and the terms it changes. The parent's terms take the delta once this document is certificated; until then the parent is untouched. Every key below is always present, `null` when it holds no value, except `resource`.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no | On a response that is this amendment alone; the entries of a contract's `amendments` carry none. |
| `document_id` | string | yes | no | This document, the instrument. |
| `document_name` | string | yes | no | This document's name. |
| `parent_document_id` | string | yes | no | The contract document whose terms change. |
| `relation_type` | string enum[amendment, renewal, termination] | yes | no |  |
| `number` | integer or null | yes | yes | Position among the parent's applied amendments by signing date; `null` while pending. |
| `status` | string enum[pending, applied] | yes | no | `applied` once the delta moved the parent's terms. |
| `document_status` | string | yes | no | This document's own status, so a declined, cancelled or expired instrument shows as such. An instrument applies only while its document is `certificated`: one whose document reads `failed` after it was signed waits, with no `apply_error`, until it is certificated again, and then applies in its place in the signing order. |
| `apply_error` | string or null | yes | yes | Why a signed instrument has not applied yet, retried every hour: the workspace's plan does not include contract tracking (it applies on the first run after the plan returns), the parent would not take the delta, an instrument signed before this one has not applied yet, one signed after it has already applied (this one then stays blocked: unlink it, and record what it still changes on a new instrument), or an unexpected failure. |
| `currency` | string or null | yes | yes | The currency of the amounts in `terms` and `changes`. Once applied, the contract's currency at that moment, kept whatever the contract moves to later. While pending (and for an instrument applied before this was kept), the one this instrument moves the contract to, otherwise the contract's. `null` when neither has one. |
| `terms` | object or null | yes | yes | The delta: only the terms this instrument changes; `null` when it changes none (a termination on the day it is signed). |
| `changes` | object or null | yes | yes | Every parent field the roll-up moves, as `{from, to}`: a preview while pending, the record once applied; `null` when it moves none. |
| `applied_at` | string or null (date-time) | yes | yes |  |
| `created_at` | string (date-time) | yes | no |  |
| `updated_at` | string (date-time) | yes | no |  |

### Schema: SigningUrl

One entry per signer whose turn has come. The link depends on how the signer gets the invitation: with `Email` it is a page that does not open the document by itself, where the signer asks for their invitation again by e-mail; with `Whatsapp` only it is `null` (the signer has their link in the WhatsApp message). An embedded signer (`embedded: true`) never gets a link: `url` is `null` and the integrator mints an embed session for them instead.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `signer_id` | string | no | no |  |
| `url` | string or null | no | yes | See the schema description. `null` for a signer notified only by WhatsApp and for an embedded signer. |
| `embedded` | boolean | no | no | Whether the signer was marked `embedded` when the assignment was created. |

### Schema: AssignmentSigner

A signer within an assignment: the base Signer plus per-assignment verification/notification details.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no | Present in single-resource responses. |
| `id` | string | no | no |  |
| `full_name` | string | no | no |  |
| `email` | string or null (email) | no | yes |  |
| `whatsapp_phone_number` | string or null | no | yes | E.164 format; normalized on save. |
| `government_id` | string or null | no | yes | Signer's normalized CPF (11 digits) or CNPJ (14 characters; may be alphanumeric). |
| `has_accepted_terms` | boolean | no | no |  |
| `verification_method` | string or null | no | yes |  |
| `notification_methods` | array or null | no | yes |  |
| `step` | integer or null | no | yes | Sequential signing step (defaults to 1). |
| `notified` | boolean or null | no | yes |  |
| `identity_verification_status` | string or null enum[Pending, InReview, Approved, Declined, Expired, Abandoned] | no | yes | Latest identity-document verification status; `InReview` means the account must decide it before the document can be certificated. |
| `completed` | boolean or null | no | yes | Only present in account-owner contexts. |
| `notification_history` | array or null | no | yes | Per-channel delivery history for this signer (email + WhatsApp), most-recent send order. |

### Schema: NotificationHistoryEntry

A single notification delivery record for a signer channel.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `event` | string | no | no |  |
| `status` | string enum[sent, delivered, opened, failed] | no | no | Furthest delivery stage reached. `opened` means the email was opened/clicked or the WhatsApp message was read. |
| `error_code` | string or null enum[MailboxFull, AddressNotFound, AddressSuppressed, ContentRejected, MarkedAsSpam, NotOnWhatsapp, RecipientRestricted, NotSent, Unknown] | no | yes | Why the notification failed. Only set when `status` is `failed`. |
| `error_message` | string or null | no | yes | Human-readable, localized explanation of `error_code`. Only set when `status` is `failed`. |
| `sent_at` | string or null (date-time) | no | yes |  |
| `failed_at` | string or null (date-time) | no | yes |  |

### Schema: AssignmentItem



| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | string | no | no |  |
| `page` | [DocumentPage](#schema-documentpage) or null | no | no |  |
| `signer` | object | no | no | Signer responsible for this item. |
| `field` | object or null | no | yes | Field definition associated with the item. |
| `display_settings` | any | no | no | Rendering metadata for the item. Collect items use the DisplaySettings schema; virtual and legacy items may return an empty or non-object value. |
| `value` | any | no | no | Captured value when completed; `null` until then. |
| `completed` | boolean | no | no |  |

### Schema: AssignmentSummary



| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `signer_count` | integer | no | no |  |
| `completed_count` | integer | no | no |  |
| `signers` | array<object> | no | no |  |

### Schema: Assignment

A request for signers to sign a document.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no |  |
| `id` | string | no | no |  |
| `document_id` | string or null | no | yes | The document this assignment belongs to. `null` only if the document was deleted. |
| `created_at` | string (date-time) | no | no |  |
| `sender_email` | string or null (email) | no | yes | Email of the user who sent the document; null once that user has deleted their account. |
| `method` | string enum[virtual, collect] | no | no |  |
| `expires_at` | string or null (date-time) | no | yes |  |
| `message` | string or null | no | yes |  |
| `signers` | array<[AssignmentSigner](#schema-assignmentsigner)> | no | no |  |
| `copy_receivers` | array<object> | no | no |  |
| `items` | array<[AssignmentItem](#schema-assignmentitem)> | no | no |  |
| `summary` | [AssignmentSummary](#schema-assignmentsummary) | no | no |  |
| `signing_urls` | array<[SigningUrl](#schema-signingurl)> | no | no |  |

### Schema: EnvelopeCreateRequest

An envelope as sent: on creation, and to estimate what creating it would cost.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | string | yes | no | What the invitation calls what is being signed, in place of a document's name. `<` is refused (`400`). minLength: `1`. maxLength: `255`. |
| `message` | string or null | no | yes | Optional message sent to every signer; their invitation adds the names of the documents they are on. |
| `max_total_credits` | number | no | no | Optional ceiling on what this request may spend, as on an assignment: refused with `409` and code `CreditCeilingExceeded`, nothing created or charged, when the envelope's total credits are above it. A value that is not a number of credits, zero or more, answers `400`; numeric text counts, as a form-encoded body sends it. The estimate ignores it. minimum: `0`. |
| `signers` | array<object{`id!`: string; `verification_method`: string enum[Email, Whatsapp, DigitalCertificate, IdDocumentFaceMatchBasic]; `notification_methods`: array<string enum[Email, Whatsapp]>; `embedded`: boolean}> | yes | no | Every signer of the envelope, once, by id: up to 20. They all sign at once: an envelope takes no `step`. minItems: `1`. maxItems: `20`. |
| `documents` | array<[EnvelopeDocumentRequest](#schema-envelopedocumentrequest)> | yes | no | The documents, in the order the envelope lists them: up to 20. minItems: `1`. maxItems: `20`. |

### Schema: EnvelopeDocumentRequest

A document of an envelope, as sent on creation (`documents[]`) and when added.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | string | yes | no | A `metadata_ready` document of the workspace with no assignment. minLength: `1`. |
| `method` | string enum[virtual, collect] | no | no | As on an assignment: `virtual` (the default), signed on its virtual page, or `collect`, signed on the fields placed in `entries`. default: `"virtual"`. |
| `entries` | array<object{`page_id!`: string; `fields!`: array<object{`signer_id!`: string; `field_id!`: string; `display_settings!`: [DisplaySettings](#schema-displaysettings)}>}> | no | no | Required for `collect`, as on an assignment: the fields placed on each page of the document. Its signers are the ones on its fields, exactly its `signer_ids`. minItems: `1`. |
| `signer_ids` | array<string> | yes | no | The signers of this document, each listed once: on creation, signers listed in `signers`; when added, signers of the envelope's documents. minItems: `1`. |
| `expires_at` | string or number or null | no | no | This document's expiration, read as on an assignment, at least one hour in the future. Text is read as a date: ISO 8601 is the format to send (a UTC offset in the value is honored, a value without one is read as UTC, and a date alone means 23:59 UTC that day), and text that is a number is Unix time. A number is Unix time in seconds, its fraction dropped. Null, 0, an empty text or leaving it out means no expiration; a value that can't be read as a date answers `400`. |
| `copy_receiver_ids` | array<string> | no | no | Signer IDs that only receive a copy of this document, as on an assignment. |

### Schema: DocumentEnvelope

Documents sent together to the same signers. Each document keeps its own assignment, status, certificate and downloads.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no | Present in single-resource responses. |
| `id` | string | yes | no |  |
| `name` | string | yes | no |  |
| `message` | string or null | yes | yes |  |
| `documents` | array<object{`id!`: string; `position!`: integer; `name!`: string; `status!`: string; `artifacts!`: object; values string; `signers!`: array<object{`id!`: string; `full_name!`: string; `email!`: string or null; `whatsapp_phone_number!`: string or null; `completed!`: boolean; `signed_at!`: string or null (date-time)}>}> | yes | no | The envelope's documents, in the order it lists them. |
| `counts` | object or null; values integer | yes | yes | How many of its documents are in each status (status code => count); only the statuses present. An envelope has no status of its own. `null` once it lists no document: every one deleted or taken out. |
| `signing_urls` | array<[SigningUrl](#schema-signingurl)> | yes | no | One entry per signer, as on an assignment (`SigningUrl`): the link of the document their invitation is sent from while it waits for them, else of their first document that still does (the e-mail page asks that document for the invitation again; any of their links opens the whole envelope). |
| `created_at` | string (date-time) | yes | no |  |

### Schema: CostEstimateBreakdownItem



| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `code` | string | no | no |  |
| `name` | string | no | no |  |
| `cost` | number | no | no |  |
| `quantity` | integer | no | no |  |
| `unit_cost` | number | no | no |  |

### Schema: CostEstimate

Cost breakdown for an assignment plus current account balances. `credits` is the sum of `breakdown` (notifications, digital-certificate signatures and identity checks). `total_credits` is what the send will take from the credit balance: `credits`, plus `extra_document_cost` when `needs_extra_document` is true. The extra document is not a `breakdown` line.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `documents` | integer | yes | no | Documents consumed: 1, for an assignment as for an envelope, whatever its documents. |
| `credits` | number | yes | no | Sum of the `breakdown` costs: notifications, digital-certificate signatures and identity checks. Does not include the extra document. |
| `needs_extra_document` | boolean | yes | no | True when the plan's document allowance is exhausted and an extra document will be charged from credits. |
| `extra_document_cost` | number | yes | no | Credits for one extra document. Charged only when `needs_extra_document` is true. |
| `total_credits` | number | yes | no | Credits the send will consume: `credits`, plus `extra_document_cost` when `needs_extra_document` is true. |
| `breakdown` | array<[CostEstimateBreakdownItem](#schema-costestimatebreakdownitem)> | yes | no |  |
| `document_balance` | number or null | yes | yes | Documents left in the plan. `null` when the account's documents are not limited. |
| `credit_balance` | number or null | yes | yes | Credits available. `null` when the account's credits are not limited. |
| `has_sufficient_resources` | boolean | yes | no | Whether creating it would pass its balance checks now: the checks creation itself makes, so a contract that allows overage passes with a low `document_balance` or `credit_balance`. |
| `blocking_reason` | string or null enum[PendingPayment, InsufficientDocuments, InsufficientCredits] | yes | yes |  |
| `message` | string or null | yes | yes |  |

### Schema: ResendCostEstimate

Credits a reminder to one signer will take, and whether the account can pay for it. A reminder uses no document.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `total` | number | no | no | Sum of the `breakdown` costs. |
| `breakdown` | array<object{`code`: string; `name`: string; `cost`: number}> | no | no |  |
| `credit_balance` | number or null | no | yes | Credits available. `null` when the account's credits are not limited. |
| `has_sufficient_credits` | boolean | no | no | Whether the reminder would pass its credit check now: the check the reminder itself makes, so a contract that allows overage passes with a low `credit_balance`, and a plan whose payment is pending doesn't, whatever the balance. |

### Schema: TemplateFieldPlacement



| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | string | no | no |  |
| `field_id` | string | no | no |  |
| `role_id` | string | no | no |  |
| `label` | string | no | no |  |
| `display_settings` | any | no | no | Rendering metadata for the placement. |
| `created_at` | string (date-time) | no | no |  |
| `updated_at` | string (date-time) | no | no |  |

### Schema: TemplatePage



| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | string | no | no |  |
| `number` | integer | no | no |  |
| `height` | integer | no | no |  |
| `width` | integer | no | no |  |
| `download_url` | string | no | no |  |
| `fields` | array<[TemplateFieldPlacement](#schema-templatefieldplacement)> | no | no |  |

### Schema: TemplateRole



| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | string | no | no |  |
| `name` | string | no | no |  |
| `assignment_type` | string | no | no |  |
| `created_at` | string (date-time) | no | no |  |
| `updated_at` | string (date-time) | no | no |  |

### Schema: Template

A reusable document template.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no |  |
| `id` | string | no | no |  |
| `name` | string | no | no |  |
| `document_name` | string or null | no | yes | Default name for documents created from this template. |
| `message` | string or null | no | yes | Default invitation message. |
| `status` | string | no | no | One of uploading, uploaded, processing, ready, failed. |
| `pages` | array<[TemplatePage](#schema-templatepage)> | no | no |  |
| `roles` | array<[TemplateRole](#schema-templaterole)> | no | no |  |
| `default_document_tags` | array<object{`id`: string; `name`: string}> | no | no | Applied to documents created from this template; only returned by the single-template endpoint. |
| `created_at` | string (date-time) | no | no |  |
| `updated_at` | string (date-time) | no | no |  |

### Schema: Balance

A document or credit balance for the current billing cycle.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `balance` | number or null | no | yes | Remaining quantity; null for unlimited. |
| `limit` | number or null | no | yes | Allowance per cycle/pool; null when not cycle-limited or unlimited. |
| `spent` | number | no | no | Quantity consumed in the current cycle. |

### Schema: AccountUsage

The workspace's plan and what is left of it.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `plan` | string or null | no | yes | The plan's name. `null` when the subscription has no plan or its plan was retired. |
| `documents_renewal_date` | string or null (date) | no | yes | When the document allowance next renews. On a custom contract, the next boundary of the contract's billing day. `null` while the subscription awaits its first payment, or on a contract with no billing day. |
| `balances` | object{`documents`: [Balance](#schema-balance); `credits`: [Balance](#schema-balance)} | no | no | Document and credit balances, as in **Get current subscription**. |

### Schema: WebhookSubscription

An account's webhook subscription configuration.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `events` | array<string> | no | no | Event types subscribed for delivery. |
| `is_active` | boolean | no | no | Whether webhook delivery is active. |
| `is_held` | boolean | no | no | Whether deliveries are paused after 10 failures in a row (see **Delivery contract**). |
| `held_since` | integer or null | no | yes | When the pause began (Unix timestamp, seconds); `null` while deliveries run. |
| `url` | string or null | no | yes | Webhook endpoint URL; `https` is required for a new or changed URL. |
| `email` | string or null | no | yes | Contact email for delivery notices. |
| `updated_at` | string or null (date-time) | no | yes |  |

### Schema: WebhookEndpoint

A URL that receives the account's webhook events. Every active endpoint subscribed to an event receives it.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no | Always `activity_event_subscription` in single-resource responses. |
| `id` | string | no | no | Endpoint ID. |
| `name` | string or null | no | yes | Label to tell endpoints apart. |
| `url` | string (uri) | no | no | URL that receives the events; `https` is required for a new or changed URL. |
| `email` | string (email) | no | no | Contact email for delivery-failure notices. |
| `events` | array<string> | no | no | Event types delivered to this endpoint (see **List webhook event types**). |
| `is_active` | boolean | no | no | Whether events are delivered to this endpoint. |
| `is_held` | boolean | no | no | Whether deliveries are paused after 10 failures in a row: we probe the endpoint on a growing schedule and deliver what it missed when it answers (see **Delivery contract**). Editing the URL, or sending `is_active: true`, lifts the pause. |
| `held_since` | integer or null | no | yes | When the pause began (Unix timestamp, seconds); `null` while deliveries run. |
| `signing_enabled` | boolean | no | no | Whether deliveries carry a `webhook-signature` header (see **Webhook Payloads → Verifying signatures**). |
| `created_at` | string (date-time) | no | no |  |
| `updated_at` | string (date-time) | no | no |  |

### Schema: EmbedOrigin

An origin allowed to embed the workspace's signing pages.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | string | no | no | Embed origin ID. |
| `origin` | string | no | no | Exact `scheme://host[:port]`. |
| `created_at` | string (date-time) | no | no |  |

### Schema: EmbedSession

A single-use embed session for an embedded signer.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `url` | string | no | no | Load this URL in an iframe within 5 minutes. Opening it spends the token. |
| `expires_at` | string (date-time) | no | no |  |

### Schema: EmbedSessionExchange

What the embed page gets for a session token.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `signer_access_code` | string | no | no | The signer's access code, used by the embed page only. |
| `signer_id` | string | no | no |  |
| `document_id` | string | no | no |  |
| `verification_method` | string | no | no |  |
| `embed_origins` | array<string> | no | no |  |

### Schema: WebhookEndpointSecret

An endpoint's signing secret.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `secret` | string | no | no | Standard Webhooks secret: `whsec_` followed by the base64-encoded key. |

### Schema: WebhookDispatch

A single webhook delivery-history entry.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | string | no | no | Always `activity_dispatching_history` in single-resource responses. |
| `id` | string | no | no | Dispatch entry ID. |
| `event` | string | no | no | Event type that triggered the dispatch. |
| `activity_id` | integer | no | no | Internal activity ID associated with the dispatch. |
| `endpoint_id` | string or null | no | yes | ID of the webhook endpoint the delivery was sent to (`null` once that endpoint is deleted). |
| `endpoint` | string or null | no | yes | URL that received the request. |
| `payload` | object or null | no | yes | JSON payload sent to the endpoint. |
| `delivered` | boolean | no | no | Whether delivery succeeded. |
| `webhook_id` | string or null | no | yes | The `webhook-id` header of the request: the same on every attempt of this event to this endpoint. |
| `attempt` | integer or null | no | yes | Which attempt of this event to this endpoint the entry records, counted from 1. |
| `kind` | string enum[scheduled, probe, manual] | no | no | `scheduled`: the delivery of the event as it happened; `probe`: the one delivery we send to a paused endpoint to see whether it answers; `manual`: asked for through **Retry webhook delivery** or **Send a test delivery**. |
| `will_retry` | boolean | no | no | Whether another attempt follows this one. `false` on a delivered entry and on the last attempt of a failed one. |
| `duration_ms` | integer or null | no | yes | How long the endpoint took to answer or fail, in milliseconds. `null` when no request was sent (a host that does not resolve, an address that is not public) and on entries recorded before it was measured. |
| `http_status` | integer or null | no | yes | HTTP status returned (null if connection failed). |
| `response_body` | string or null | no | yes | Endpoint response body, truncated to 2000 chars. |
| `error` | string or null | no | yes | Delivery error message, if any. |
| `created_at` | string (date-time) | no | no |  |
| `updated_at` | string (date-time) | no | no |  |

### Schema: WebhookEvent

Body of every webhook delivery. Timestamps in the body (`created_at`, and the `*_at` fields of `subject`/`object`) are Unix timestamps in seconds, not the ISO strings the REST responses use. `subject` and `object` are serialized from their current state when the delivery is sent.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | integer | yes | no | ID of the activity that produced the event. |
| `event` | string | yes | no | Event type. See **List webhook event types**; `webhook_test` is sent only by **Send a test delivery**. |
| `message` | string or null | no | yes | Reserved; currently always `null`. |
| `payload` | object or null | no | yes | Event-specific parameters; keys vary per event. |
| `origin` | object{`ip`: string; `user-agent`: string} | no | yes | Where the action came from, when it was triggered by a request. |
| `created_at` | integer | yes | no | When the event was recorded (Unix timestamp, seconds). |
| `subject` | object | yes | no | Who performed the action: a `User`, `Signer` or `Account` (the endpoint itself in a `webhook_test`), plus a `type` property naming it. |
| `object` | object | yes | no | What the action was performed on: a `Document`, `Signer` or `Template` with its relations expanded (the endpoint itself in a `webhook_test`), plus a `type` property naming it. |
| `account_id` | string | yes | no | ID of the account that owns the event. |

### Schema: WebhookEventType

A subscribable webhook event type.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | string | no | no | Event type code. |
| `description` | string | no | no | When the event is triggered. |

### Schema: AccountTheme

An account's branding theme.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `account_name` | string | no | no |  |
| `primary_color` | string | no | no | Button background: hex color without leading `#`. The platform default when the account set none. |
| `secondary_color` | string | no | no | Button text: hex color without leading `#`. The platform default when the account set none. |
| `logo` | string or null | no | yes | Absolute URL of the logo for the caller: `/v1/accounts/{accountId}/logo` for workspace members, `/v1/sign/logo` (append `signer-access-code`) for signers. `null` when the account has no logo. |

### Schema: FieldType

A supported field/validation type.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `type` | string | no | no |  |
| `name` | string | no | no |  |

### Schema: FieldValidation

The result of validating a value against a field definition.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `type` | string | no | no | The field's validation type. |
| `success` | boolean | no | no |  |
| `error_message` | string | no | no | Empty when valid. |

### Schema: FieldValidationResult

A per-field result from a multi-field validation.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `field_id` | string | no | no |  |
| `type` | string | no | no |  |
| `success` | boolean | no | no |  |
| `error_message` | string | no | no |  |

### Schema: DocumentVerification

The verification result for a document looked up by signature hash. When not verified, most fields are null and `is_valid` is false.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `hash` | string | no | no |  |
| `id` | string or null | no | yes |  |
| `agreement_code` | string or null | no | yes | Agreement code printed on the document certificate. |
| `status` | string or null | no | yes |  |
| `page_count` | string or null | no | yes |  |
| `signer_count` | string or null | no | yes |  |
| `completed_count` | integer or null | no | yes |  |
| `completed_at` | string or null (date-time) | no | yes | When the last signature was made. |
| `certificated_at` | string or null (date-time) | no | yes | When certification was first recorded by the document_ready event. Null when no certification event is available, including some historical documents. |
| `verified_at` | string (date-time) | no | no | When this check ran (the time of the request). It says nothing about when the document was signed or certified. |
| `is_valid` | boolean | no | no |  |
| `message` | string | no | no | Reason when not valid. |

### Schema: DocumentActivity

A document activity event.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | integer | no | no |  |
| `event` | string | no | no | Event type code. |
| `message` | string | no | no |  |
| `payload` | object or array or null | no | yes | `[]` for the events that record nothing (`document_uploaded`, `document_metadata_ready`, `document_prepared`). Otherwise an event-specific snapshot taken when the event happened, with the same keys as the webhook of that event (see **Webhook Payloads**). The common ones: `user_name` (`assignment_created`, `user_rejected_document`); `signer_full_name` (every `signer_*` event and `signature_requested`); `signer_email`, `signer_whatsapp_phone_number` and `notification_method` (`signature_requested`); `reviewer_name` (identity review decisions); `error_message` (`document_processing_failed`). `message` already carries the actor's name, translated, so clients can show it as is. A `*_at` key inside the payload is returned as an ISO 8601 string here, as in every API response, but as a Unix integer in webhook bodies. |
| `origin` | object{`ip`: string or null; `user-agent`: string} | no | yes | Request origin when available. |
| `created_at` | string (date-time) | no | no |  |

### Schema: EnvelopeActivity

A document activity of an envelope's trail, naming its document.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | integer | no | no |  |
| `event` | string | no | no | Event type code. |
| `message` | string | no | no |  |
| `payload` | object or array or null | no | yes | `[]` for the events that record nothing (`document_uploaded`, `document_metadata_ready`, `document_prepared`). Otherwise an event-specific snapshot taken when the event happened, with the same keys as the webhook of that event (see **Webhook Payloads**). The common ones: `user_name` (`assignment_created`, `user_rejected_document`); `signer_full_name` (every `signer_*` event and `signature_requested`); `signer_email`, `signer_whatsapp_phone_number` and `notification_method` (`signature_requested`); `reviewer_name` (identity review decisions); `error_message` (`document_processing_failed`). `message` already carries the actor's name, translated, so clients can show it as is. A `*_at` key inside the payload is returned as an ISO 8601 string here, as in every API response, but as a Unix integer in webhook bodies. |
| `origin` | object{`ip`: string or null; `user-agent`: string} | no | yes | Request origin when available. |
| `created_at` | string (date-time) | no | no |  |
| `document_id` | string | yes | no | The document the activity is about. |
| `document_name` | string | yes | no |  |

### Schema: WhatsappNotification

A rendered WhatsApp notification sent for an assignment, split into header/body/buttons as the signer would see them.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `sent_at` | string (date-time) | no | no | When the message was sent. |
| `header` | string | no | no |  |
| `body` | string | no | no |  |
| `buttons` | array<object{`text`: string}> | no | no |  |
| `phone_number` | string | no | no | Recipient phone (E.164). |
| `signer_id` | string | no | no |  |

### Schema: AuthSession

A JWT access token plus the authenticated user and the accounts they belong to.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `access_token` | string | no | no |  |
| `user` | [AuthUser](#schema-authuser) | no | no |  |
| `accounts` | array<[AuthAccount](#schema-authaccount)> | no | no |  |

### Schema: SignerIdentityReview



| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | string | no | no |  |
| `status` | string enum[Pending, InReview, Approved, Declined, Expired, Abandoned] | no | no |  |
| `provider` | string enum[AssinafyVerify] | no | no |  |
| `verification_method` | string enum[IdDocumentFaceMatchBasic] | no | no |  |
| `reason_codes` | array<object{`code`: string; `description`: string}> | no | no | Why the provider sent it to review, in its order. Unknown codes have the code as description. |
| `evidence_available` | boolean | no | no |  |
| `expected` | object{`name`: string or null; `cpf`: string or null} | no | yes | What the signer was registered with. |
| `document` | object{`type`: string or null; `name`: string or null; `cpf`: string or null} | no | yes | What was read from the document. |
| `matches` | object{`cpf`: boolean or null; `name`: number or null; `birth_date`: boolean or null} | no | yes |  |
| `checks` | array<object{`name`: string; `result`: string; `score`: number or null; `reasons`: array<string>}> | no | no |  |
| `images` | array<string enum[document_front, document_back, document_pdf, face]> | no | no | The evidence images the reviewer can open with **Signer identity review image**, in display order: `document_front`, `document_back`, `document_pdf`, `face`. Only while `status` is `InReview`; empty once decided. |
| `ruleset_version` | string or null | no | yes |  |
| `evidence_sha256` | string or null | no | yes |  |
| `reviewed_by` | object{`id`: string; `name`: string} | no | yes |  |
| `reviewed_at` | string or null (date-time) | no | yes |  |
| `review_note` | string or null | no | yes |  |
| `completed_at` | string or null (date-time) | no | yes |  |

### Schema: DocumentStatsRow

One period of the document-funnel KPI series. `period` is `YYYY-MM` (monthly) or `YYYY-MM-DD` (daily); series are zero-filled, no gaps. Signature requests come with two independent breakdowns: the `signature_requests_notification_*` counters split them by the channels the signer was notified on — a signer reached on more than one channel counts once per channel, so these add up to at least `signature_requests` — while the `signature_requests_verification_*` counters split them by how the signer's identity is verified, and since each request has exactly one verification method those four always add up to `signature_requests`.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `period` | string | no | no | `YYYY-MM` (monthly) or `YYYY-MM-DD` (daily). |
| `documents_uploaded` | integer | no | no |  |
| `documents_sent` | integer | no | no |  |
| `signature_requests` | integer | no | no |  |
| `signature_requests_notification_email` | integer | no | no | Requests notified by e-mail. |
| `signature_requests_notification_whatsapp` | integer | no | no | Requests notified by WhatsApp. |
| `signature_requests_notification_bypass` | integer | no | no | Requests with no notification sent (`Bypass`). |
| `signature_requests_verification_email` | integer | no | no | Requests verified by an e-mail token. |
| `signature_requests_verification_whatsapp` | integer | no | no | Requests verified by a WhatsApp token. |
| `signature_requests_verification_bypass` | integer | no | no | Requests signed without token verification (`Bypass`). |
| `signature_requests_verification_digital_certificate` | integer | no | no | Requests signed with the signer's own ICP-Brasil digital certificate. |
| `signature_requests_viewed` | integer | no | no | Signature requests whose document was first viewed during the period. |
| `signature_requests_completed` | integer | no | no | Signature requests completed by individual signers during the period. |
| `documents_certified` | integer | no | no |  |

### Schema: NotificationPreferences

Owner-facing document notifications, keyed by notification type. `true` means the e-mail is sent.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `DocumentCompleted` | boolean | no | no | Every signer has signed and the document is certified. |
| `SignerDeclined` | boolean | no | no | A signer declined to sign. |
| `DocumentCancelled` | boolean | no | no | The document was cancelled. |
| `DocumentAboutToExpire` | boolean | no | no | The signature deadline is approaching. |
| `DocumentExpired` | boolean | no | no | The signature deadline passed. |
| `DocumentExpirationReset` | boolean | no | no | The signature deadline was extended. |
| `DocumentProcessingFailed` | boolean | no | no | An uploaded document could not be processed. |
| `TemplateProcessingFailed` | boolean | no | no | A template could not be processed. |
| `SignerWhatsappFailed` | boolean | no | no | A WhatsApp notification to a signer could not be delivered. |
| `ContractRenewalNotice` | boolean | no | no | A tracked contract entered its notice period: it is about to end or to renew. |
| `ContractExpiring` | boolean | no | no | A tracked contract ends in 45 or in 30 days. |
| `ContractExpired` | boolean | no | no | A tracked contract ended without renewal. |
| `ContractRenewed` | boolean | no | no | A tracked contract was auto-renewed into its next term. |

### Schema: NotificationPolicyEvents

Every e-mail and push alert, keyed by notification type. `false` means the workspace never sends it. New keys may be added.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `DocumentCompleted` | boolean | no | no |  |
| `SignerDeclined` | boolean | no | no |  |
| `DocumentCancelled` | boolean | no | no |  |
| `DocumentAboutToExpire` | boolean | no | no |  |
| `DocumentExpired` | boolean | no | no |  |
| `DocumentExpirationReset` | boolean | no | no |  |
| `DocumentProcessingFailed` | boolean | no | no |  |
| `TemplateProcessingFailed` | boolean | no | no |  |
| `SignerWhatsappFailed` | boolean | no | no |  |
| `ContractRenewalNotice` | boolean | no | no |  |
| `ContractExpiring` | boolean | no | no |  |
| `ContractExpired` | boolean | no | no |  |
| `ContractRenewed` | boolean | no | no |  |
| `SignerViewed` | boolean | no | no |  |
| `SignerSigned` | boolean | no | no |  |
| `SignerIdentityReviewRequired` | boolean | no | no | Turns off the push only; the identity review e-mail is always sent. |

### Schema: NotificationPolicy

The workspace's notification rules, set by its owners.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `editable` | boolean | no | no | Whether the requesting user is an owner and can change the policy. |
| `owners_receive_all` | boolean | no | no | Owners also get the push alerts for every member's documents. Each owner's own preferences still apply. |
| `events` | [NotificationPolicyEvents](#schema-notificationpolicyevents) | no | no |  |

### Schema: PushPreferences

Document alerts sent as push notifications to the user's phones, keyed by notification type. `true` means the push is sent. Separate from the e-mail preferences. New keys may be added.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `SignerViewed` | boolean | no | no | A signer opened the document for the first time. |
| `SignerSigned` | boolean | no | no | A signer signed the document. |
| `DocumentCompleted` | boolean | no | no | Every signer has signed and the document is certified. |
| `SignerDeclined` | boolean | no | no | A signer declined to sign. |
| `DocumentAboutToExpire` | boolean | no | no | The signature deadline is approaching. |
| `DocumentExpired` | boolean | no | no | The signature deadline passed. |
| `SignerIdentityReviewRequired` | boolean | no | no | A signer's identity awaits the sender's review. The e-mail for this is always sent. |
| `SignerWhatsappFailed` | boolean | no | no | A WhatsApp notification to a signer could not be delivered. |

### Schema: OAuthTokenRequest

Body of `POST /v1/oauth/token`. Sent form-encoded per RFC 6749, or as JSON.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `grant_type` | string enum[authorization_code, refresh_token, urn:ietf:params:oauth:grant-type:token-exchange] | yes | no | `urn:ietf:params:oauth:grant-type:token-exchange` is for internal service clients only (Assinafy's own MCP server) — an ordinary confidential or public client authenticates with it and always gets `invalid_client`, exactly as an unrecognized client would. Everyday integrators use `authorization_code` and `refresh_token`. |
| `code` | string | no | no |  |
| `redirect_uri` | string (uri) | no | no |  |
| `code_verifier` | string | no | no | RFC 7636: 43-128 characters from [A-Za-z0-9-._~]. Shorter values are rejected with `invalid_grant`. |
| `refresh_token` | string | no | no |  |
| `client_id` | string | yes | no |  |
| `client_secret` | string | no | no | Confidential clients only. Public clients authenticate with PKCE and are never issued a secret; the token-exchange grant requires a confidential, internal-service client and therefore always requires this. |
| `resource` | string (uri) | no | no | RFC 8707 resource indicator. For `authorization_code`/`refresh_token`, optional; when present it must be the `resource` value published by /.well-known/oauth-protected-resource and must match the one sent to /authorize, otherwise `invalid_target`. For the token-exchange grant it is REQUIRED and must equal this API's own resource identifier exactly (never a front-end resource such as the MCP server), otherwise `invalid_target`. |
| `subject_token` | string | no | no | Token-exchange grant only. The front-end resource's access token being traded in. Must be a live, original (never itself exchanged) token minted for a resource this server issues tokens for, other than this API's own audience. |
| `subject_token_type` | string enum[urn:ietf:params:oauth:token-type:access_token] | no | no | Token-exchange grant only. Required; only `urn:ietf:params:oauth:token-type:access_token` is supported. |
| `requested_token_type` | string enum[urn:ietf:params:oauth:token-type:access_token] | no | no | Token-exchange grant only. Optional; when present it must agree with the only type this server issues. |

### Schema: OAuthRevokeRequest

Body of `POST /v1/oauth/revoke`. Sent form-encoded per RFC 7009, or as JSON.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `token` | string | yes | no |  |
| `token_type_hint` | string enum[access_token, refresh_token] | no | no |  |
| `client_id` | string | yes | no |  |
| `client_secret` | string | no | no |  |

## Deployed extensions

Routes the SDK calls that are live on the API but absent from the published OpenAPI document. They
carry no compatibility promise from the reference, so each one names what the SDK does with it.

### `POST /v1/signers/certificate/start`

- **Java:** `client.signers()` — `SignerResource: public String startCertificateSignature(String signerAccessCode)`
- **Auth:** signer access code. Configured bearer and API-key credentials are not used by this route.
- **Availability:** production and sandbox; the workspace must have Digital Certificate enabled.
- **Contract notes:** Begins an ICP-Brasil digital-certificate signature for a signer whose `verification_method` is `DigitalCertificate`. Those signers cannot use [Sign assignment items](#api-operations) — that endpoint returns `400` for them. The signer must have confirmed their data and accepted the terms first. The returned token is signed in the browser by the signer's own A1 or A3 certificate through the Web PKI extension.

Request body: **required**. The access code travels in both the query and the body.

`application/json`: object{signer-access-code!: `string`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `signer-access-code` | `string` | yes | no | The same value as the query parameter. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: object{token: `string`}}.

```json
{ "status": 200, "message": "", "data": { "token": "web-pki-token" } }
```

The Java method returns `data.token` and raises `ValidationException` when it is absent.

Statuses: `200` Operation started; `400` Invalid signing state or request; `401` Invalid or expired signer access code; `500` Unexpected server error.

### `POST /v1/signers/certificate/complete`

- **Java:** `client.signers()` — `SignerResource: public String completeCertificateSignature(String signerAccessCode, String token)`
- **Auth:** signer access code. Configured bearer and API-key credentials are not used by this route.
- **Availability:** production and sandbox; the workspace must have Digital Certificate enabled.
- **Contract notes:** Completes the signature with the browser-signed Web PKI token. Afterwards the document's `pades` artifact carries the qualified PAdES signature.

Request body: **required**.

`application/json`: object{signer-access-code!: `string`; token!: `string`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `signer-access-code` | `string` | yes | no | The same value as the query parameter. |
| `token` | `string` | yes | no | The Web PKI token after the browser signed it with the signer's certificate. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: object{signerName: `string`}}.

```json
{ "status": 200, "message": "", "data": { "signerName": "Maria Silva" } }
```

The Java method returns `data.signerName` and raises `ValidationException` when it is absent.

Statuses: `200` Signature completed; `400` Invalid signed token or signing state; `401` Invalid or expired signer access code; `500` Unexpected server error.

### `GET /v1/accounts/{accountId}/templates/{templateId}`

- **Java:** `client.templates()` — `TemplateResource: public Template get(String templateId)`; `public Template get(String templateId, String accountId)`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Availability:** live on production and sandbox.
- **Contract notes:** Returns one [Template](#schema-template), the same schema [List templates](#api-operations) returns per element. Use the list operation when you need a contract the published reference guarantees.

## Java convenience APIs

- `DocumentResource: public Document get(String documentId)` aliases `details`. `public Document waitUntilReady(String documentId)`, `public Document waitUntilReady(String documentId, long maxWaitMs, long pollIntervalMs)`, `public boolean isFullySigned(String documentId)`, and `public SigningProgress getSigningProgress(String documentId)` compose documented document reads locally.
- `DocumentResource.download(String)` and `SignerResource.downloadDocument(...)` supply the `certificated` artifact default. The documented signer download is public: `downloadDocument(signerId, documentId, artifactName)` sends no credential, and the overload taking an access code sends only that code.
- `DocumentResource.upload(byte[], String, Map<String,Object>, String)` sends multipart `file`, `name`, and optional `metadata` parts.
- `DocumentResource.appendTagIds(...)` and `replaceTagIds(...)` resolve workspace tag IDs before
  changing a document. The existing `appendTags(...)` and `replaceTags(...)` methods accept tag
  names. Returned `Tag` records carry the attached-tag IDs accepted by `detachTag(...)`.
- `AssinafyClient: public UploadAndRequestSignaturesResult uploadAndRequestSignatures(UploadAndRequestSignaturesRequest request)` composes upload, polling, signer resolution, and assignment creation. On an ordinary later-step failure, it attempts to delete the uploaded document and signer records whose create responses returned valid IDs; cleanup failures are suppressed on the original exception. Signers recovered after an indeterminate create response are not updated or deleted. A recovered entry containing CPF/CNPJ fails before assignment creation. If assignment creation has an indeterminate result and reconciliation cannot find it, the resources are retained to avoid deleting a potentially active request.
- `SignerResource.create(...)` always sends the create POST, including `government_id` when `CreateSignerRequest.governmentId` (CPF or CNPJ) is set. `findByEmail(...)` is a list/search convenience. `findOrCreate(...)` reuses an exact case-insensitive email match unchanged and handles a concurrent duplicate-create response.
- `TemplateResource: public Template get(String templateId)` and `public Template get(String templateId, String accountId)` call `GET /accounts/{accountId}/templates/{templateId}`, a live route that the published OpenAPI document does not list — see [Deployed extensions](#deployed-extensions).
- Assignment/template creation and expiration reset validate ISO 8601 dates and date-times at least
  one hour ahead. A date ends at 23:59 UTC, a date-time without an offset is UTC, and an explicit
  offset is honored. Estimates ignore expiration. Rename and decline enforce 255 and 2,000 Unicode
  character limits.
- A password-reset request for an unknown user returns `404`, with no user found; a signer contact does not establish a registered user identity.
- `AssignmentResource.resetExpiration(..., null)` sends `{"expires_at": null}`, which removes the expiration.
- `AssignmentResource.list(..., accountId)` adds an `accountId` query parameter that the published document does not list. The sandbox answers `400` ("account context is required") to an API-key request without it, so the SDK sends the explicit or default account.
- `CreateFieldRequest` exposes optional deployment fields `is_active`, `is_read_only`, and `is_visible`. `UpdateFieldRequest` also exposes `type`, `is_required`, `is_read_only`, and `is_visible` for deployments that accept them.
- Field validation: `validate`/`validateMultiple` take a required signer access code and send only it; `validateAsWorkspace`/`validateMultipleAsWorkspace` use the workspace credential.
- `SignerResource.uploadSignature` accepts PNG or JPEG bytes. Use PNG unless the target tenant accepts JPEG uploads.
- `WebhookResource.get` returns `null` when no subscription exists (HTTP 404).
- `WebhookSignature.verify(secret, headers, rawBody)` checks a signed delivery locally (Standard Webhooks HMAC-SHA256 over `{webhook-id}.{webhook-timestamp}.{body}`, constant-time equality check, 5-minute replay window); `verify(secret, id, timestamp, signature, rawBody, tolerance, now)` takes the header values, tolerance and clock explicitly. A secret that is not `whsec_` base64 raises `ValidationException`; a missing header returns `false`.
- `CreateWorkspaceRequest` and `UpdateWorkspaceRequest` expose optional deployment theme fields through `primaryColor` and `secondaryColor`; use `WorkspaceResource.getTheme` and the logo methods for the remaining branding operations.

## SDK convenience payloads

These Java types support composed or locally calculated helpers and are not standalone HTTP
operation bodies unless stated otherwise.

### `UploadAndRequestSignaturesRequest`

Input to `AssinafyClient.uploadAndRequestSignatures(...)`. The client converts it into document,
signer, and assignment requests.

| Java property | Type | Required/default | Behavior |
|---|---|---|---|
| `fileData` | `byte[]` | required | PDF bytes passed to `documents().upload(...)`. |
| `fileName` | `String` | required | Uploaded PDF file name. |
| `signers` | `List<SignerEntry>` | at least one | Signers resolved in list order. |
| `message` | `String` | optional | Assignment invitation message. |
| `metadata` | `Map<String,Object>` | optional | Upload metadata multipart field. |
| `waitForReady` | `boolean` | `true` | Poll before signer and assignment creation. |
| `expiresAt` | `String` | optional | ISO 8601 date or date-time at least one hour ahead; date-only ends at 23:59 UTC and a date-time without an offset is UTC. |
| `copyReceivers` | `List<String>` | optional | Assignment copy recipients. |
| `accountId` | `String` | client default | Explicit workspace account ID. |

`SignerEntry` fields are `name` (required), `email`, `whatsappPhoneNumber` (E.164), and `cpf`. Each
entry requires an email or WhatsApp number. Email entries reuse an exact
case-insensitive match without replacing its name, phone, or CPF/CNPJ. When a signer create response
returns a valid ID, a supplied CPF/CNPJ is persisted through the signer's `government_id` update.
A signer recovered after an indeterminate create response is not updated or deleted; if the entry
contains CPF/CNPJ, the workflow fails before assignment creation. Email-bearing entries
must have unique addresses (case-insensitive); WhatsApp-only entries must have unique phone numbers
(exact match). WhatsApp-only entries use `Whatsapp` verification and notification.

### `UploadAndRequestSignaturesResult`

| Getter | Type | Meaning |
|---|---|---|
| `getDocument()` | `Document` | Uploaded document. |
| `getAssignment()` | `Assignment` | Created virtual assignment. |
| `getSignerIds()` | `List<String>` | Resolved signer IDs in request order, whether created, reused, or recovered after an indeterminate response. |

### `SigningProgress`

Returned by `documents().getSigningProgress(documentId)` and calculated from the expanded
assignment summary.

| Getter | Type | Meaning |
|---|---|---|
| `getSigned()` | `int` | Completed signer count. |
| `getTotal()` | `int` | Total signer count. |
| `getPending()` | `int` | `max(total - signed, 0)`. |
| `getPercentage()` | `double` | Completion percentage rounded to two decimal places; zero when total is zero. |

### `PaginationMeta`

| Response header | Getter | Meaning |
|---|---|---|
| `X-Pagination-Current-Page` | `getCurrentPage()` | Current page number. |
| `X-Pagination-Page-Count` | `getLastPage()` | Total page count. |
| `X-Pagination-Per-Page` | `getPerPage()` | Requested page size. |
| `X-Pagination-Total-Count` | `getTotal()` | Total matching item count across every page. |

`PaginatedResult.getMeta()` is `null` when a response supplies no pagination headers.

### `WebhookPayload`

The delivery envelope posted to your webhook endpoint. A webhook does not arrive through the SDK's
transport, so deserialize the raw request body into this model with your own Jackson mapper
(configure `FAIL_ON_UNKNOWN_PROPERTIES=false`). Unknown top-level fields remain available from
`getAdditionalProperties()`.

Enable `signing_enabled` on each endpoint and verify the raw body with `WebhookSignature` before
deserializing it. Store `WebhookEndpoint.getSecret()` whenever creation or an update returns it in
server-side secret storage. OAuth applications cannot read it later but can rotate it with
`webhooks:write`. Deduplicate on the `webhook-id` header, check the
workspace `account_id`, and process accepted events asynchronously.

Return a `2xx` response quickly. A transient connection error, timeout, `408`, `429` or `5xx` is
retried up to six times, about ten minutes apart. Ten failed deliveries in a row hold the endpoint;
the API probes the oldest missed event after 3, 15 and 60 minutes, then every 6 hours, and resumes
oldest-first delivery after success. `retryDispatch(...)`, a URL change or `is_active: true` can also
release the hold. The API retains delivery entries for seven days and at most the first 2,000
characters of a response body.

| JSON field | Java type | Meaning |
|---|---|---|
| `id` | `Long` | Activity/event ID. |
| `event` | `String` | Event type code. |
| `message` | `String` | Human-readable event message. |
| `payload` | `Object` | Event-specific object, array, or null; `getPayload()` returns only object-shaped values. |
| `origin` | `Map<String,Object>` | Origin IP and user-agent details when supplied. |
| `created_at` | `Long` | Unix timestamp in seconds. |
| `subject` | `Map<String,Object>` | Actor, including its `type` discriminator. |
| `object` | `Map<String,Object>` | Entity affected by the event, including its `type` discriminator. |
| `account_id` | `String` | Workspace account ID. |

### `DocumentArtifacts`

Document payloads expose available artifact URLs through `Document.getArtifacts()`.

| JSON field | Getter | Availability |
|---|---|---|
| `original` | `getOriginal()` | Original uploaded PDF. |
| `thumbnail` | `getThumbnail()` | First-page thumbnail after metadata processing. |
| `certificated` | `getCertificated()` | Final certificated PDF. |
| `certificate-page` | `getCertificatePage()` | Certificate page artifact. |
| `pades` | `getPades()` | PAdES PDF for digital-certificate signing processes. |
| `bundle` | `getBundle()` | ZIP bundle of available final artifacts. |
