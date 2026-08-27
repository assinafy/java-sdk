# Assinafy Java SDK API reference

This is the Java mapping of the official production OpenAPI document published at <https://api.assinafy.com.br/v1/docs/openapi.json>. It covers all **89** documented operations.

## Conventions

- Build a client with `new AssinafyClient(AssinafyClientOptions.builder()...build())`; each operation names its client accessor and exact current public signature.
- Custom API base URLs must use HTTPS. Plain HTTP is accepted only for loopback hosts used in local tests, so credentials are not sent over cleartext networks.
- Authenticated operations accept either `X-Api-Key` (recommended for server integrations) or `Authorization: Bearer <JWT>`. Signer-facing operations use the `signer-access-code` query credential. Public operations should use a credential-free client.
- JSON success bodies use `{ "status": integer, "message": string, "data": ... }`. The SDK returns `data`. A Java `void` method discards the success envelope and also accepts an empty 2xx body. Binary methods return raw `byte[]`, not JSON.
- A `!` after an inline JSON field name means required. `?` after a type means explicitly nullable. “Required: no” means the OpenAPI schema does not require the field; it does not imply the server always omits it.
- Linked component schemas are part of the operation payload; follow the link for every nested field. List methods return `PaginatedResult<T>` when pagination headers are exposed.
- Raw `Map<String,Object>` methods expose object-valued `data` fields directly. A scalar or array `data` value is returned as `{ "data": value }`; missing data or an empty success body becomes an empty map. Typed alternatives are listed beside their map-returning methods.
- Operation request tables describe the production OpenAPI wire contract. Additional Java helpers are identified at the end of this reference.
- Non-2xx responses and non-2xx numeric envelope statuses throw `ApiException`; 401/403 use `AuthenticationException`, 429 uses `RateLimitException`, I/O failures use `NetworkException`, and local argument failures use `ValidationException`.

## Authentication

### 1. Link social login

- **Java:** `client.authentication()` — `AuthenticationResource: public void linkSocialLogin(String provider, String token)`
- **HTTP:** `POST /v1/auth/link-social-login`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Issues or links authentication credentials/session state.
- **Contract notes:** Link a social-login provider account to the authenticated user.

Parameters: none.

Request body: **required**.

`application/json`: object{provider!: `string enum[google]`; token!: `string`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `provider` | `string enum[google]` | yes | no |  |
| `token` | `string` | yes | no | Token from the provider. |

Success `200` `application/json`: [Envelope](#schema-envelope).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |

Documented statuses: `200` Provider linked; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

### 2. Change password

- **Java:** `client.authentication()` — `AuthenticationResource: public void changePassword(String email, String password, String newPassword)`; `client.authentication()` — `AuthenticationResource: public Map<String, Object> changePasswordResult(String email, String password, String newPassword)`
- **HTTP:** `PUT /v1/authentication/change-password`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: change password.
- **Contract notes:** Change the authenticated user's password.

Parameters: none.

Request body: **required**.

`application/json`: object{email!: `string(email)`; password!: `string(password)`; new_password!: `string(password)`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `email` | `string(email)` | yes | no |  |
| `password` | `string(password)` | yes | no | The current password. |
| `new_password` | `string(password)` | yes | no | The new password. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: object{email: `string(email)`}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | object{email: `string(email)`} | no | no |  |

Documented statuses: `200` Password changed; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

### 3. Request password reset

- **Java:** `client.authentication()` — `AuthenticationResource: public void requestPasswordReset(String email)`; `client.authentication()` — `AuthenticationResource: public Map<String, Object> requestPasswordResetResult(String email)`
- **HTTP:** `PUT /v1/authentication/request-password-reset`
- **Auth:** Public (no SDK credential)
- **Side effects:** Sends password-reset instructions.
- **Contract notes:** Send the user an email with instructions to reset their password. Used when the password was forgotten or never set.

Parameters: none.

Request body: **required**.

`application/json`: object{email!: `string(email)`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `email` | `string(email)` | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: object{email: `string(email)`}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | object{email: `string(email)`} | no | no |  |

Documented statuses: `200` Reset email sent; `500` Unexpected server error.

### 4. Reset password

- **Java:** `client.authentication()` — `AuthenticationResource: public void resetPassword(String email, String token, String newPassword)`; `client.authentication()` — `AuthenticationResource: public Map<String, Object> resetPasswordResult(String email, String token, String newPassword)`
- **HTTP:** `PUT /v1/authentication/reset-password`
- **Auth:** Public (no SDK credential)
- **Side effects:** Mutates server state: reset password.
- **Contract notes:** Reset the user's password using the token received by email.

Parameters: none.

Request body: **required**.

`application/json`: object{email!: `string(email)`; token: `string`; new_password!: `string(password)`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `email` | `string(email)` | yes | no |  |
| `token` | `string` | no | no | Token received by email. |
| `new_password` | `string(password)` | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: object{email: `string(email)`}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | object{email: `string(email)`} | no | no |  |

Documented statuses: `200` Password reset; `400` One or more fields failed validation; `500` Unexpected server error.

### 5. Social login

- **Java:** `client.authentication()` — `AuthenticationResource: public AuthSession socialLogin(String provider, String token, boolean hasAcceptedTerms)`
- **HTTP:** `POST /v1/authentication/social-login`
- **Auth:** Public (no SDK credential)
- **Side effects:** Issues or links authentication credentials/session state.
- **Contract notes:** Exchange a token from a social login provider (currently only `google`) for an Assinafy access token.

Parameters: none.

Request body: **required**.

`application/json`: object{provider!: `string enum[google]`; token!: `string`; has_accepted_terms!: `boolean`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `provider` | `string enum[google]` | yes | no |  |
| `token` | `string` | yes | no | Access/ID token from the provider. |
| `has_accepted_terms` | `boolean` | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [AuthSession](#schema-authsession)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [AuthSession](#schema-authsession) | no | no |  |

Documented statuses: `200` Access token, user and accounts; `400` One or more fields failed validation; `500` Unexpected server error.

### 6. Login

- **Java:** `client.authentication()` — `AuthenticationResource: public AuthSession login(String email, String password)`
- **HTTP:** `POST /v1/login`
- **Auth:** Public (no SDK credential)
- **Side effects:** Issues or links authentication credentials/session state.
- **Contract notes:** Authenticate with email and password and receive a JWT access token.

Parameters: none.

Request body: **required**.

`application/json`: object{email!: `string(email)`; password!: `string(password)`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `email` | `string(email)` | yes | no |  |
| `password` | `string(password)` | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [AuthSession](#schema-authsession)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [AuthSession](#schema-authsession) | no | no |  |

Documented statuses: `200` Access token, user and accounts; `400` One or more fields failed validation; `500` Unexpected server error.

### 7. Delete API key

- **Java:** `client.apiKeys()` — `ApiKeyResource: public void delete()`
- **HTTP:** `DELETE /v1/users/api-keys`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Deletes server state: delete api key.
- **Contract notes:** Delete the existing API key.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<`any`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<`any`> | no | no |  |

Documented statuses: `200` API key deleted; `401` Missing or invalid credentials; `500` Unexpected server error.

### 8. Get API key

- **Java:** `client.apiKeys()` — `ApiKeyResource: public ApiKey get()`
- **HTTP:** `GET /v1/users/api-keys`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Retrieve a masked version of the existing API key. The full key cannot be retrieved. Returns `null` when no key has been generated yet.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [ApiKey](#schema-apikey)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [ApiKey](#schema-apikey) | no | no |  |

Documented statuses: `200` The masked API key; `401` Missing or invalid credentials; `500` Unexpected server error.

### 9. Create API key

- **Java:** `client.apiKeys()` — `ApiKeyResource: public ApiKey create(String password)`
- **HTTP:** `POST /v1/users/api-keys`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: create api key.
- **Contract notes:** Generate an API key for the user, used via the `X-Api-Key` header. Generating a new key deletes the previous one. Never use an API key from a front-end application.

Parameters: none.

Request body: **required**.

`application/json`: object{password!: `string(password)`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `password` | `string(password)` | yes | no | The user's password. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [ApiKey](#schema-apikey)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [ApiKey](#schema-apikey) | no | no |  |

Documented statuses: `200` The generated API key (shown in full only once); `401` Missing or invalid credentials; `500` Unexpected server error.

## Accounts

### 10. List my accounts

- **Java:** `client.workspaces()` — `WorkspaceResource: public PaginatedResult<Workspace> list()`
- **HTTP:** `GET /v1/accounts`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** List the workspace accounts the authenticated user belongs to.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[Account](#schema-account)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[Account](#schema-account)> | no | no |  |

Documented statuses: `200` The user's accounts; `401` Missing or invalid credentials; `500` Unexpected server error.

### 11. Create account

- **Java:** `client.workspaces()` — `WorkspaceResource: public Workspace create(CreateWorkspaceRequest request)`
- **HTTP:** `POST /v1/accounts`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: create account.
- **Contract notes:** Create a new workspace account owned by the authenticated user.

Parameters: none.

Request body: **required**.

`application/json`: object{name!: `string`; notification_sender_type: `string enum[User, Account]`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | `string` | yes | no |  |
| `notification_sender_type` | `string enum[User, Account]` | no | no | Who signers see as the notification sender for documents in this account. `User` (default) shows the document owner's name; `Account` shows this account's name. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Account](#schema-account)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Account](#schema-account) | no | no |  |

Documented statuses: `200` The created account; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

### 12. Delete account

- **Java:** `client.workspaces()` — `WorkspaceResource: public void delete(String accountId)`; `client.workspaces()` — `WorkspaceResource: public void delete(String accountId, boolean force)`
- **HTTP:** `DELETE /v1/accounts/{accountId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Deletes server state: delete account.
- **Contract notes:** Delete a workspace account. By default the request fails with `400` when the workspace has an active paid subscription — the `restrictions` array in the response lists each blocker by code so you can address them individually before retrying. Pass `force: true` to cancel any active paid subscription automatically and proceed with immediate deletion.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: optional.

`application/json`: object{force: `boolean`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `force` | `boolean` | no | no | When `true`, cancels any active paid subscription on this workspace and proceeds with deletion immediately. Defaults to `false`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<`any`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<`any`> | no | no |  |

Documented statuses: `200` Account deleted; `400` Deletion blocked by active restrictions. Each `restrictions` entry describes one blocker; resolve them individually, or retry with `force: true` to cancel blocking subscriptions/documents automatically; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 13. Get account

- **Java:** `client.workspaces()` — `WorkspaceResource: public Workspace get(String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Retrieve a workspace account the user belongs to.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Account](#schema-account)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Account](#schema-account) | no | no |  |

Documented statuses: `200` The account; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 14. Update account

- **Java:** `client.workspaces()` — `WorkspaceResource: public Workspace update(String accountId, UpdateWorkspaceRequest request)`
- **HTTP:** `PUT /v1/accounts/{accountId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: update account.
- **Contract notes:** Update a workspace account's profile.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: **required**.

`application/json`: object{name: `string`; notification_sender_type: `string enum[User, Account]`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | `string` | no | no |  |
| `notification_sender_type` | `string enum[User, Account]` | no | no | Who signers see as the notification sender for documents in this account. `User` (default) shows the document owner's name; `Account` shows this account's name. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Account](#schema-account)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Account](#schema-account) | no | no |  |

Documented statuses: `200` The updated account; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

### 15. Delete account logo

- **Java:** `client.workspaces()` — `WorkspaceResource: public void deleteLogo(String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/logo`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Deletes server state: delete account logo.
- **Contract notes:** Remove the account logo image.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |

Documented statuses: `200` Logo deleted; `401` Missing or invalid credentials; `500` Unexpected server error.

### 16. Download account logo

- **Java:** `client.workspaces()` — `WorkspaceResource: public byte[] downloadLogo(String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/logo`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Download the account logo image binary.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: none.

Success `200` `image/*`: `string(binary)`.

Documented statuses: `200` The logo image; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 17. Upload account logo

- **Java:** `client.workspaces()` — `WorkspaceResource: public void uploadLogo(String accountId, byte[] imageData, String fileName)`
- **HTTP:** `POST /v1/accounts/{accountId}/logo`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: upload account logo.
- **Contract notes:** Upload or replace the account logo image.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: **required**.

`multipart/form-data`: object{file!: `string(binary)`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `file` | `string(binary)` | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |

Documented statuses: `200` Logo updated; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

### 18. Account document KPIs

- **Java:** `client.workspaces()` — `WorkspaceResource: public List<DocumentStatsRow> stats(String accountId)`; `client.workspaces()` — `WorkspaceResource: public List<DocumentStatsRow> stats(String accountId, String granularity, String month)`
- **HTTP:** `GET /v1/accounts/{accountId}/stats`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Precomputed per-account document-funnel KPIs. `granularity=monthly` (default) returns the last 12 months, most recent first; `granularity=daily` with `month=YYYY-MM` returns that month's days. Series are zero-filled.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `granularity` | query | `string enum[monthly, daily]` | no | no | `monthly` (default) or `daily`. |
| `month` | query | `string` | no | no | Target month `YYYY-MM` (required when `granularity=daily`). |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[DocumentStatsRow](#schema-documentstatsrow)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[DocumentStatsRow](#schema-documentstatsrow)> | no | no |  |

Documented statuses: `200` KPI series; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

### 19. Get account theme

- **Java:** `client.workspaces()` — `WorkspaceResource: public AccountTheme getTheme(String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/theme`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Retrieve account theme information (branding name, colors, and logo URL).

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [AccountTheme](#schema-accounttheme)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [AccountTheme](#schema-accounttheme) | no | no |  |

Documented statuses: `200` The theme; `401` Missing or invalid credentials; `500` Unexpected server error.

## Signers

### 20. List signers

- **Java:** `client.signers()` — `SignerResource: public PaginatedResult<Signer> list()`; `client.signers()` — `SignerResource: public PaginatedResult<Signer> list(ListParams params)`; `client.signers()` — `SignerResource: public PaginatedResult<Signer> list(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/signers`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** List the signers of a workspace.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `search` | query | `string` | no | no | Filter by full_name or email. |
| `page` | query | `integer` | no | no | Page number. |
| `per-page` | query | `integer` | no | no | Records per page (max 100). |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[Signer](#schema-signer)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[Signer](#schema-signer)> | no | no |  |

Documented statuses: `200` A page of signers; `401` Missing or invalid credentials; `500` Unexpected server error.

### 21. Create signer

- **Java:** `client.signers()` — `SignerResource: public Signer create(CreateSignerRequest request)`; `client.signers()` — `SignerResource: public Signer create(CreateSignerRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/signers`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Changes signing state: create signer.
- **Contract notes:** Create a signer in the workspace.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: **required**.

`application/json`: object{full_name!: `string`; email: `string(email)`; whatsapp_phone_number: `string`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `full_name` | `string` | yes | no |  |
| `email` | `string(email)` | no | no |  |
| `whatsapp_phone_number` | `string` | no | no | E.164; normalized on save. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Signer](#schema-signer)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Signer](#schema-signer) | no | no |  |

Documented statuses: `200` The created signer; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

### 22. Delete signer

- **Java:** `client.signers()` — `SignerResource: public void delete(String signerId)`; `client.signers()` — `SignerResource: public void delete(String signerId, String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/signers/{signerId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Deletes server state: delete signer.
- **Contract notes:** Delete a signer.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `signerId` | path | `string` | yes | no | The signer ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<`any`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<`any`> | no | no |  |

Documented statuses: `200` Signer deleted; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 23. Get signer

- **Java:** `client.signers()` — `SignerResource: public Signer get(String signerId)`; `client.signers()` — `SignerResource: public Signer get(String signerId, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/signers/{signerId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Retrieve a signer's information.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `signerId` | path | `string` | yes | no | The signer ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Signer](#schema-signer)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Signer](#schema-signer) | no | no |  |

Documented statuses: `200` The signer; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 24. Update signer

- **Java:** `client.signers()` — `SignerResource: public Signer update(String signerId, UpdateSignerRequest request)`; `client.signers()` — `SignerResource: public Signer update(String signerId, UpdateSignerRequest request, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/signers/{signerId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Changes signing state: update signer.
- **Contract notes:** Update a signer's information. **Verification integrity:** `email` / `whatsapp_phone_number` cannot be changed while the signer has verified that channel on an in-flight (not yet certificated) document — the response is `400` naming the offending document(s). Already-certificated documents do not block updates. Changing a channel that has *unverified* in-flight requests rotates their access/verification codes (invalidating previously sent links/OTPs); use the resend endpoint to redeliver. `full_name` can always be updated.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `signerId` | path | `string` | yes | no | The signer ID. |

Request body: **required**.

`application/json`: object{full_name: `string`; email: `string(email)`; whatsapp_phone_number: `string`; government_id: `string`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `full_name` | `string` | no | no |  |
| `email` | `string(email)` | no | no |  |
| `whatsapp_phone_number` | `string` | no | no | E.164; normalized on save. |
| `government_id` | `string` | no | no | Signer's CPF/CNPJ; digits only on save. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Signer](#schema-signer)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Signer](#schema-signer) | no | no |  |

Documented statuses: `200` The updated signer; `400` One or more fields failed validation; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

## Documents

### 25. List documents

- **Java:** `client.documents()` — `DocumentResource: public PaginatedResult<Document> list()`; `client.documents()` — `DocumentResource: public PaginatedResult<Document> list(ListParams params)`; `client.documents()` — `DocumentResource: public PaginatedResult<Document> list(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/documents`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** List documents of the workspace.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `status` | query | `string` | no | no | Status filter, e.g. `pending_signature`. |
| `method` | query | `string enum[virtual, collect]` | no | no | Signature method filter. |
| `search` | query | `string` | no | no | Partial match on document.name, signer.full_name, signer.email. |
| `tags` | query | `string` | no | no | Comma-separated tag IDs; returns documents having ALL listed tags. |
| `sort` | query | `string` | no | no | Sort by `name` or `updated_at`. |
| `page` | query | `integer` | no | no | Page number. |
| `per-page` | query | `integer` | no | no | Records per page (max 100). |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[Document](#schema-document)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[Document](#schema-document)> | no | no |  |

Documented statuses: `200` A page of documents; `401` Missing or invalid credentials; `500` Unexpected server error.

### 26. Upload and create document

- **Java:** `client.documents()` — `DocumentResource: public Document upload(byte[] fileData, String fileName)`; `client.documents()` — `DocumentResource: public Document upload(byte[] fileData, String fileName, Map<String, Object> metadata, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/documents`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Creates a document and starts asynchronous processing.
- **Contract notes:** Create a document from an uploaded file. Maximum file size 25MB; maximum 2000 pages.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: **required**.

`multipart/form-data`: object{file!: `string(binary)`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `file` | `string(binary)` | yes | no | The PDF file to upload. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Document](#schema-document)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Document](#schema-document) | no | no |  |

Documented statuses: `200` The created document; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

### 27. List document tags

- **Java:** `client.documents()` — `DocumentResource: public List<Tag> listTags(String documentId)`; `client.documents()` — `DocumentResource: public List<Tag> listTags(String documentId, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/documents/{documentId}/tags`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** List the tags attached to a document.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `documentId` | path | `string` | yes | no | The document ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[Tag](#schema-tag)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[Tag](#schema-tag)> | no | no |  |

Documented statuses: `200` Attached tags; `401` Missing or invalid credentials; `500` Unexpected server error.

### 28. Attach document tags

- **Java:** `client.documents()` — `DocumentResource: public List<Tag> appendTagIds(String documentId, List<String> tagIds)`; `client.documents()` — `DocumentResource: public List<Tag> appendTagIds(String documentId, List<String> tagIds, String accountId)`; name-based convenience overloads: `appendTags(String documentId, List<String> tagNames)` and `appendTags(String documentId, List<String> tagNames, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/documents/{documentId}/tags`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: attach document tags.
- **Contract notes:** Attach one or more tags to a document. `appendTagIds` resolves every workspace
  ID before changing the document. `appendTags` accepts tag names and creates unknown names.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `documentId` | path | `string` | yes | no | The document ID. |

Request body: **required**.

`application/json`: object{tags: array<`string`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `tags` | array<`string`> | no | no | Tag IDs. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[Tag](#schema-tag)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[Tag](#schema-tag)> | no | no |  |

Documented statuses: `200` Attached tags; `401` Missing or invalid credentials; `500` Unexpected server error.

### 29. Replace document tags

- **Java:** `client.documents()` — `DocumentResource: public List<Tag> replaceTagIds(String documentId, List<String> tagIds)`; `client.documents()` — `DocumentResource: public List<Tag> replaceTagIds(String documentId, List<String> tagIds, String accountId)`; name-based convenience overloads: `replaceTags(String documentId, List<String> tagNames)` and `replaceTags(String documentId, List<String> tagNames, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/documents/{documentId}/tags`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: replace document tags.
- **Contract notes:** Replace the full set of tags attached to a document. `replaceTagIds` resolves
  every workspace ID before changing the document. `replaceTags` accepts tag names and creates
  unknown names.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `documentId` | path | `string` | yes | no | The document ID. |

Request body: **required**.

`application/json`: object{tags: array<`string`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `tags` | array<`string`> | no | no | Tag IDs. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[Tag](#schema-tag)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[Tag](#schema-tag)> | no | no |  |

Documented statuses: `200` Updated tags; `401` Missing or invalid credentials; `500` Unexpected server error.

### 30. Detach document tag

- **Java:** `client.documents()` — `DocumentResource: public void detachTag(String documentId, String tagId)`; `client.documents()` — `DocumentResource: public void detachTag(String documentId, String tagId, String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/documents/{documentId}/tags/{tagId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: detach document tag.
- **Contract notes:** Detach a single tag from a document.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `documentId` | path | `string` | yes | no | The document ID. |
| `tagId` | path | `string` | yes | no | The tag ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: object{detached: `boolean`}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | object{detached: `boolean`} | no | no |  |

Documented statuses: `200` Tag detached; `401` Missing or invalid credentials; `500` Unexpected server error.

### 31. Search documents (lightweight)

- **Java:** `client.documents()` — `DocumentResource: public PaginatedResult<Document> search(ListParams params)`; `client.documents()` — `DocumentResource: public PaginatedResult<Document> search(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/documents/search`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Search documents of the workspace, returning a compact representation (no expanded assignment/pages).

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `search` | query | `string` | no | no | Search term. |
| `status` | query | `string` | no | no |  |
| `page` | query | `integer` | no | no | Page number. |
| `per-page` | query | `integer` | no | no | Records per page (max 100). |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[Document](#schema-document)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[Document](#schema-document)> | no | no |  |

Documented statuses: `200` Matching documents; `401` Missing or invalid credentials; `500` Unexpected server error.

### 32. Create document from template

- **Java:** `client.documents()` — `DocumentResource: public Document createFromTemplate(String templateId, CreateDocumentFromTemplateRequest request)`; `client.documents()` — `DocumentResource: public Document createFromTemplate(String templateId, CreateDocumentFromTemplateRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/templates/{templateId}/documents`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: create document from template.
- **Contract notes:** Generate a new document from a template, creating its assignment in the same call. Provide one signer entry per template role; the signers must already exist in the account. The Java SDK requires nonblank role and signer IDs, accepts only the documented verification and notification methods, permits at most one notification method per signer, and validates signing steps as an all-or-none contiguous sequence starting at 1. A `DigitalCertificate` signer must be alone in its step.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `templateId` | path | `string` | yes | no | The template ID. |

Request body: **required**.

`application/json`: object{signers!: array<object{role_id!: `string`; id!: `string`; verification_method: `string enum[Email, Whatsapp, DigitalCertificate]`; notification_methods: array<`string`>; step: `integer`}>; editor_fields: array<object{field_id!: `string`; value!: `string`}>; name: `string`; message: `string`; expires_at: `string(date-time)`; tags: array<`string`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `signers` | array<object{role_id!: `string`; id!: `string`; verification_method: `string enum[Email, Whatsapp, DigitalCertificate]`; notification_methods: array<`string`>; step: `integer`}> | yes | no | One entry per template role. |
| `editor_fields` | array<object{field_id!: `string`; value!: `string`}> | no | no | Editor field values to bake into the generated document. |
| `name` | `string` | no | no | Title for the document. Defaults to the template name. |
| `message` | `string` | no | no | Optional message sent to signers. |
| `expires_at` | `string(date-time)` | no | no | Assignment expiration date (ISO 8601). No expiration by default. |
| `tags` | array<`string`> | no | no | Tag names to attach to the new document. Names that don't exist are auto-created. The template's default-document-tags are always applied; values here are merged on top (duplicates removed). |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Document](#schema-document)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Document](#schema-document) | no | no |  |

Documented statuses: `200` The created document; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

### 33. Estimate document-from-template cost

- **Java:** `client.documents()` — `DocumentResource: public Map<String, Object> estimateCostFromTemplate(String templateId, CreateDocumentFromTemplateRequest request)`; `client.documents()` — `DocumentResource: public Map<String, Object> estimateCostFromTemplate(String templateId, CreateDocumentFromTemplateRequest request, String accountId)`; `client.documents()` — `DocumentResource: public CostEstimate estimateCostFromTemplateTyped(String templateId, CreateDocumentFromTemplateRequest request)`; `client.documents()` — `DocumentResource: public CostEstimate estimateCostFromTemplateTyped(String templateId, CreateDocumentFromTemplateRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/templates/{templateId}/documents/estimate-cost`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None persisted; calculates cost only.
- **Contract notes:** Estimate the cost of creating a document from a template without creating it. Contact information is not required — only the `role_id` and optionally a verification or notification method are needed. The Java SDK requires a nonblank role ID, validates the documented delivery-method values, and omits signer IDs, signing steps, and creation-only document settings. Each document always consumes 1 document from the plan's monthly allowance; if exhausted, the `ExtraDocument` cost is charged from credits (`needs_extra_document = true`). `blocking_reason` may be `PendingPayment`, `InsufficientDocuments`, or `InsufficientCredits`.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `templateId` | path | `string` | yes | no | The template ID. |

Request body: **required**.

`application/json`: object{signers!: array<object{role_id!: `string`; verification_method: `string enum[Email, Whatsapp, DigitalCertificate]`; notification_methods: array<`string`>}>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `signers` | array<object{role_id!: `string`; verification_method: `string enum[Email, Whatsapp, DigitalCertificate]`; notification_methods: array<`string`>}> | yes | no | One entry per template role (editor roles are ignored for cost calculation). |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [CostEstimate](#schema-costestimate)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [CostEstimate](#schema-costestimate) | no | no |  |

Documented statuses: `200` Cost estimate; `401` Missing or invalid credentials; `500` Unexpected server error.

### 34. Delete document

- **Java:** `client.documents()` — `DocumentResource: public void delete(String documentId)`
- **HTTP:** `DELETE /v1/documents/{documentId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Deletes server state: delete document.
- **Contract notes:** Delete a document by its ID. Only documents in a deletable status can be removed (see GET /v1/documents/statuses).

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<`any`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<`any`> | no | no |  |

Documented statuses: `200` Document deleted; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 35. Get document

- **Java:** `client.documents()` — `DocumentResource: public Document details(String documentId)`
- **HTTP:** `GET /v1/documents/{documentId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Get a document by its ID. `decline_reason` is only present when the access token belongs to the document's creator.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Document](#schema-document)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Document](#schema-document) | no | no |  |

Documented statuses: `200` The document; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 36. Rename document

- **Java:** `client.documents()` — `DocumentResource: public Document rename(String documentId, String newName)`
- **HTTP:** `PATCH /v1/documents/{documentId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: rename document.
- **Contract notes:** Update a document's name. Only allowed before any assignment is created (i.e. while the document is in `uploaded` or `metadata_ready` status and has no signers yet); once the signature process has started or the document is certificated, the name is locked. The name is normalized: diacritics are removed and unsupported characters are replaced with dashes.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |

Request body: **required**.

`application/json`: object{name!: `string`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | `string` | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Document](#schema-document)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Document](#schema-document) | no | no |  |

Documented statuses: `200` The updated document; `400` One or more fields failed validation; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 37. List document activities

- **Java:** `client.documents()` — `DocumentResource: public List<DocumentActivity> activities(String documentId)`
- **HTTP:** `GET /v1/documents/{documentId}/activities`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** List the activities recorded for a document. Each entry carries an event-specific `payload` snapshot (keys vary per event) and the request `origin` (`ip`, `user-agent`).

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[DocumentActivity](#schema-documentactivity)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[DocumentActivity](#schema-documentactivity)> | no | no |  |

Documented statuses: `200` Document activities; `401` Missing or invalid credentials; `500` Unexpected server error.

### 38. Download document artifact

- **Java:** `client.documents()` — `DocumentResource: public byte[] download(String documentId)`; `client.documents()` — `DocumentResource: public byte[] download(String documentId, String artifactName)`
- **HTTP:** `GET /v1/documents/{documentId}/download/{artifactName}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Download a document artifact. Artifact types: original, certificated, certificate-page, pades, bundle. The pades artifact (signers' ICP-Brasil signatures + platform certification box) is only present on documents that had digital-certificate signers; `bundle` is a zip of the original, certificated and certificate-page artifacts, plus the pades artifact on documents that have one.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |
| `artifactName` | path | `string enum[original, certificated, certificate-page, pades, bundle]` | yes | no | Artifact type. |

Request body: none.

Success `200` `application/pdf`: `string(binary)`.

Documented statuses: `200` The artifact binary; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 39. Download document page

- **Java:** `client.documents()` — `DocumentResource: public byte[] downloadPage(String documentId, String pageId)`
- **HTTP:** `GET /v1/documents/{documentId}/pages/{pageId}/download`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Download the rendered image of a specific document page.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |
| `pageId` | path | `string` | yes | no | The page ID. |

Request body: none.

Success `200` `image/*`: `string(binary)`.

Documented statuses: `200` The page image; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 40. Download document thumbnail

- **Java:** `client.documents()` — `DocumentResource: public byte[] thumbnail(String documentId)`
- **HTTP:** `GET /v1/documents/{documentId}/thumbnail`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Download the thumbnail image of a document's first page.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |

Request body: none.

Success `200` `image/*`: `string(binary)`.

Documented statuses: `200` The thumbnail image; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 41. Verify a signed document

- **Java:** `client.documents()` — `DocumentResource: public Map<String, Object> verify(String hash)`; `client.documents()` — `DocumentResource: public DocumentVerification verifyTyped(String hash)`
- **HTTP:** `GET /v1/documents/{documentSignatureHash}/verify`
- **Auth:** Public (no SDK credential)
- **Side effects:** None; read-only.
- **Contract notes:** Verify a document by its signature hash (found on a signed document) and return its certification details. Always returns `200`: when the hash is not found or the document is not signed, `is_valid` is `false`, the other fields are `null`, and `message` explains why. Public endpoint.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentSignatureHash` | path | `string` | yes | no | The document signature hash. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [DocumentVerification](#schema-documentverification)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [DocumentVerification](#schema-documentverification) | no | no |  |

Documented statuses: `200` Verification result; `500` Unexpected server error.

### 42. List document statuses

- **Java:** `client.documents()` — `DocumentResource: public List<DocumentStatusInfo> getStatuses()`
- **HTTP:** `GET /v1/documents/statuses`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** The supported document statuses and whether a document in each status can be deleted.

| Status | Deletable | Description |
|---|---:|---|
| `uploading` | no | The document upload is in process. |
| `uploaded` | no | The document has been uploaded. |
| `metadata_processing` | no | The initial processing is under way. |
| `metadata_ready` | yes | The initial processing has been completed. |
| `expired` | yes | The signature deadline has been reached. |
| `certificating` | no | The document has been signed and is being certificated. |
| `certificated` | no | The document is certificated. |
| `rejected_by_signer` | yes | A signer declined signing the document. |
| `pending_signature` | yes | The document is waiting for signatures. |
| `rejected_by_user` | yes | The signature process was cancelled by a user. |
| `failed` | yes | The document processing has failed. |

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[DocumentStatus](#schema-documentstatus)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[DocumentStatus](#schema-documentstatus)> | no | no |  |

Documented statuses: `200` Supported statuses; `401` Missing or invalid credentials; `500` Unexpected server error.

## Assignments

### 43. List assignments

- **Java:** `client.assignments()` — `AssignmentResource: public PaginatedResult<Assignment> list(ListParams params)`; `client.assignments()` — `AssignmentResource: public PaginatedResult<Assignment> list(ListParams params, String accountId)`; `client.assignments()` — `AssignmentResource: public PaginatedResult<Assignment> list()`
- **HTTP:** `GET /v1/assignments`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** List the assignments belonging to the authenticated user's current account.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `page` | query | `integer` | no | no | Page number. |
| `per-page` | query | `integer` | no | no | Records per page (max 100). |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[Assignment](#schema-assignment)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[Assignment](#schema-assignment)> | no | no |  |

Documented statuses: `200` A page of assignments; `401` Missing or invalid credentials; `500` Unexpected server error.

### 44. Create assignment (request signatures)

- **Java:** `client.assignments()` — `AssignmentResource: public Assignment create(String documentId, CreateAssignmentRequest request)`
- **HTTP:** `POST /v1/documents/{documentId}/assignments`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Creates the assignment and dispatches configured signature requests.
- **Contract notes:** Request signatures on a document. Use `method: virtual` to sign without input fields, or `method: collect` to place input fields on specific pages. For **virtual**, the document may be in `uploaded`, `metadata_processing` or `metadata_ready`; it is promoted to `pending_signature` automatically once metadata processing completes. For **collect**, the document must be in `metadata_ready` (fields reference specific pages). The Java SDK requires signer IDs, validates the documented delivery-method values, and requires nonempty `entries` for `collect`. `step` controls signing order: signers sharing a step sign in parallel, and the next step is notified only after the previous step completes. If supplied, every signer must supply it and values must be contiguous starting at 1. A `DigitalCertificate` signer must be alone in its step.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |

Request body: **required**.

`application/json`: object{method!: `string enum[virtual, collect]`; signers!: array<object{id!: `string`; verification_method: `string enum[Email, Whatsapp, DigitalCertificate]`; notification_methods: array<`string enum[Email, Whatsapp]`>; step: `integer`}>; entries: array<object{page_id: `string`; fields: array<object{signer_id: `string`; field_id: `string`; display_settings: [DisplaySettings](#schema-displaysettings)}>}>; message: `string`; expires_at: `string(date-time)`; copy_receivers: array<`string`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `method` | `string enum[virtual, collect]` | yes | no |  |
| `signers` | array<object{id!: `string`; verification_method: `string enum[Email, Whatsapp, DigitalCertificate]`; notification_methods: array<`string enum[Email, Whatsapp]`>; step: `integer`}> | yes | no |  |
| `entries` | array<object{page_id: `string`; fields: array<object{signer_id: `string`; field_id: `string`; display_settings: [DisplaySettings](#schema-displaysettings)}>}> | no | no | Required for `collect`: field placements per page. |
| `message` | `string` | no | no | Text included in the invitation email. |
| `expires_at` | `string(date-time)` | no | no | ISO 8601; default is no expiration. |
| `copy_receivers` | array<`string`> | no | no | Signer IDs that only receive a copy. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Assignment](#schema-assignment)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Assignment](#schema-assignment) | no | no |  |

Documented statuses: `200` The created assignment; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

### 45. Reset assignment expiration

- **Java:** `client.assignments()` — `AssignmentResource: public Assignment resetExpiration(String documentId, String assignmentId, String expiresAt)`
- **HTTP:** `PUT /v1/documents/{documentId}/assignments/{assignmentId}/reset-expiration`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Changes signing state: reset assignment expiration.
- **Contract notes:** Set a new expiration date for an assignment.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |
| `assignmentId` | path | `string` | yes | no | The assignment ID. |

Request body: **required**.

`application/json`: object{expires_at: `string(date-time)`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `expires_at` | `string(date-time)` | no | no | New expiration date (ISO 8601). |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Assignment](#schema-assignment)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Assignment](#schema-assignment) | no | no |  |

Documented statuses: `200` The updated assignment; `400` One or more fields failed validation; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 46. Estimate resend cost

- **Java:** `client.assignments()` — `AssignmentResource: public Map<String, Object> estimateResendCost(String documentId, String assignmentId, String signerId)`; `client.assignments()` — `AssignmentResource: public CostEstimate estimateResendCostTyped(String documentId, String assignmentId, String signerId)`
- **HTTP:** `POST /v1/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/estimate-resend-cost`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None persisted; calculates cost only.
- **Contract notes:** Estimate the cost of resending the signature request to a signer.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |
| `assignmentId` | path | `string` | yes | no | The assignment ID. |
| `signerId` | path | `string` | yes | no | The signer ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [CostEstimate](#schema-costestimate)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [CostEstimate](#schema-costestimate) | no | no |  |

Documented statuses: `200` Cost estimate; `401` Missing or invalid credentials; `500` Unexpected server error.

### 47. Resend signature request

- **Java:** `client.assignments()` — `AssignmentResource: public ResendNotificationResponse resendNotification(String documentId, String assignmentId, String signerId)`
- **HTTP:** `PUT /v1/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/resend`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Sends a notification/token and may consume credits.
- **Contract notes:** Resend the signature-request notification to a specific signer of an assignment.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |
| `assignmentId` | path | `string` | yes | no | The assignment ID. |
| `signerId` | path | `string` | yes | no | The signer ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: object{is_sent: `boolean`; document_id: `string`; signer_id: `string`}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | object{is_sent: `boolean`; document_id: `string`; signer_id: `string`} | no | no |  |

Documented statuses: `200` Resend result; `401` Missing or invalid credentials; `500` Unexpected server error.

### 48. List WhatsApp notifications

- **Java:** `client.assignments()` — `AssignmentResource: public List<Map<String, Object>> getWhatsappNotifications(String documentId, String assignmentId)`; `client.assignments()` — `AssignmentResource: public List<WhatsappNotification> getWhatsappNotificationsTyped(String documentId, String assignmentId)`
- **HTTP:** `GET /v1/documents/{documentId}/assignments/{assignmentId}/whatsapp-notifications`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** List all WhatsApp notification messages sent for an assignment. The response includes the rendered template text split into `header`, `body` and `buttons` — exactly what the signer would see. In sandbox/stage, WhatsApp messages are simulated (no real delivery) and button URLs include access/verification codes you can use to simulate the signing flow.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |
| `assignmentId` | path | `string` | yes | no | The assignment ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[WhatsappNotification](#schema-whatsappnotification)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[WhatsappNotification](#schema-whatsappnotification)> | no | no |  |

Documented statuses: `200` WhatsApp notifications; `401` Missing or invalid credentials; `500` Unexpected server error.

### 49. Estimate assignment cost

- **Java:** `client.assignments()` — `AssignmentResource: public Map<String, Object> estimateCost(String documentId, CreateAssignmentRequest request)`; `client.assignments()` — `AssignmentResource: public CostEstimate estimateCostTyped(String documentId, CreateAssignmentRequest request)`
- **HTTP:** `POST /v1/documents/{documentId}/assignments/estimate-cost`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None persisted; calculates cost only.
- **Contract notes:** Estimate the cost of creating an assignment without creating it, returning a cost breakdown and the current account balances. Signer IDs and signing steps are not required and are omitted by the Java SDK; delivery-method values are validated. A virtual estimate requires at least one signer, while a collect estimate requires nonempty `entries`. Each assignment consumes one document from the plan allowance; if exhausted, an extra document is charged from credits (`needs_extra_document = true`). `blocking_reason` may be `PendingPayment`, `InsufficientDocuments`, or `InsufficientCredits`. A `DigitalCertificate` signer adds its signature cost on top of its notification cost and appears in `breakdown` under `SignatureDigitalCertificate`.

| Item | Cost |
|---|---:|
| Extra document | 1 credit |
| Email notification | 0 credits |
| WhatsApp notification | 0.45 credits |
| Digital certificate signature (per signer) | 2 credits |

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |

Request body: **required**.

`application/json`: object{method: `string enum[virtual, collect]`; signers: array<object{verification_method: `string enum[Email, Whatsapp, DigitalCertificate]`; notification_methods: array<`string enum[Email, Whatsapp]`>}>; entries: array<`object`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `method` | `string enum[virtual, collect]` | no | no |  |
| `signers` | array<object{verification_method: `string enum[Email, Whatsapp, DigitalCertificate]`; notification_methods: array<`string enum[Email, Whatsapp]`>}> | no | no | Required for `virtual`; each entry may be `{}` to default to Email. |
| `entries` | array<`object`> | no | no | Required for `collect`. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [CostEstimate](#schema-costestimate)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [CostEstimate](#schema-costestimate) | no | no |  |

Documented statuses: `200` Cost estimate and balances; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

## Signing

### 50. Sign assignment items

- **Java:** `client.assignments()` — `AssignmentResource: public Map<String, Object> sign(String documentId, String assignmentId, String signerAccessCode, List<Map<String, Object>> items)`
- **HTTP:** `POST /v1/documents/{documentId}/assignments/{assignmentId}`
- **Auth:** `signer-access-code` query credential
- **Side effects:** Changes signing state: sign assignment items.
- **Contract notes:** Sign a document with input fields (collect method): submit the signer's item values, completing their items. For **virtual** assignments the signer must first confirm their data via `PUT /v1/documents/{documentId}/signers/confirm-data`, otherwise this returns `400` (Signer data must be confirmed before signing). Signers whose verification method is `DigitalCertificate` cannot use this endpoint — their signature must be produced through `POST /v1/signers/certificate/start` + `/complete`, and this returns `400`. The request body is a JSON array of item entries. Uses the signer access code.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |
| `assignmentId` | path | `string` | yes | no | The assignment ID. |

Request body: **required**.

`application/json`: array<object{itemId!: `string`; fieldId!: `string`; pageId!: `string`; value!: `string`}>.

| Array-item field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `itemId` | `string` | yes | no | The assignment item ID. |
| `fieldId` | `string` | yes | no | Field associated with the item. |
| `pageId` | `string` | yes | no | The page ID. |
| `value` | `string` | yes | no | String representation of the value. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: `object`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | `object` | no | no |  |

Documented statuses: `200` Signing result; `400` Signer data must be confirmed before signing (virtual assignments), or the signer must sign with a digital certificate through the digital certificate endpoints; `401` Missing or invalid credentials; `409` The document is not ready to be signed yet; `500` Unexpected server error.

### 51. Reject (decline) assignment

- **Java:** `client.assignments()` — `AssignmentResource: public Map<String, Object> decline(String documentId, String assignmentId, String signerAccessCode, String declineReason)`
- **HTTP:** `PUT /v1/documents/{documentId}/assignments/{assignmentId}/reject`
- **Auth:** `signer-access-code` query credential
- **Side effects:** Changes signing state: reject (decline) assignment.
- **Contract notes:** The signer declines to sign the document, giving a reason. Uses the signer access code.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |
| `assignmentId` | path | `string` | yes | no | The assignment ID. |

Request body: **required**.

`application/json`: object{decline_reason!: `string`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `decline_reason` | `string` | yes | no | Descriptive reason for declining. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<`any`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<`any`> | no | no | Empty array. |

Documented statuses: `200` Assignment declined; `401` Missing or invalid credentials; `500` Unexpected server error.

### 52. Confirm signer data

- **Java:** `client.signers()` — `SignerResource: public Signer confirmSignerData(String documentId, String signerAccessCode, Map<String, Object> data)`
- **HTTP:** `PUT /v1/documents/{documentId}/signers/confirm-data`
- **Auth:** `signer-access-code` query credential
- **Side effects:** Changes signing state: confirm signer data.
- **Contract notes:** The signer confirms or updates their data before signing. Uses the signer access code.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | Document ID. |

Request body: **required**.

`application/json`: object{full_name: `string`; email: `string(email)`; government_id: `string`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `full_name` | `string` | no | no |  |
| `email` | `string(email)` | no | no |  |
| `government_id` | `string` | no | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Signer](#schema-signer)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Signer](#schema-signer) | no | no |  |

Documented statuses: `200` Data confirmed; `401` Missing or invalid credentials; `500` Unexpected server error.

### 53. View public document

- **Java:** `client.publicDocuments()` — `PublicDocumentResource: public Document getBasicInfo(String documentId)`
- **HTTP:** `GET /v1/public/documents/{documentId}`
- **Auth:** Public (no SDK credential)
- **Side effects:** None; read-only.
- **Contract notes:** Retrieve a publicly shared document by ID. Public endpoint.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | The document ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Document](#schema-document)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Document](#schema-document) | no | no |  |

Documented statuses: `200` The public document; `404` The requested resource does not exist; `500` Unexpected server error.

### 54. Send access token for public document

- **Java:** `client.publicDocuments()` — `PublicDocumentResource: public Map<String, Object> sendToken(String documentId, String email)`; `client.publicDocuments()` — `PublicDocumentResource: public Map<String, Object> sendToken(String documentId)`; `client.publicDocuments()` — `PublicDocumentResource: public Map<String, Object> sendToken(String documentId, String recipient, String channel)`
- **HTTP:** `PUT /v1/public/documents/{documentId}/send-token`
- **Auth:** Public (no SDK credential)
- **Side effects:** Sends a notification/token and may consume credits.
- **Contract notes:** Send a one-time access token (email/WhatsApp) to view a public document. Public endpoint.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `documentId` | path | `string` | yes | no | The document ID. |

Request body: optional.

`application/json`: object{email: `string(email)`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `email` | `string(email)` | no | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |

Documented statuses: `200` Token sent; `500` Unexpected server error.

### 55. View document to sign

- **Java:** `client.assignments()` — `AssignmentResource: public Map<String, Object> getForSigner(String signerAccessCode)`; `client.assignments()` — `AssignmentResource: public Map<String, Object> getForSigner(String signerAccessCode, Boolean hasAcceptedTerms)`; `client.assignments()` — `AssignmentResource: public Document getForSignerTyped(String signerAccessCode)`; `client.assignments()` — `AssignmentResource: public Document getForSignerTyped(String signerAccessCode, Boolean hasAcceptedTerms)`
- **HTTP:** `GET /v1/sign`
- **Auth:** `signer-access-code` query credential
- **Side effects:** Marks the signer document as viewed; otherwise read-only.
- **Contract notes:** Retrieve the invited document using the signer access code and mark it viewed. A 409 means processing is still in progress. The `has_accepted_terms` query can record acceptance; `PUT /v1/signers/accept-terms` is the explicit alternative. Confirm-data accepts `full_name`, `email`, and `government_id`.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `has_accepted_terms` | query | `boolean` | no | no | Set true to record terms acceptance. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Document](#schema-document)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Document](#schema-document) | no | no |  |

Documented statuses: `200` The document with the signer's assignment; `400` A digital-certificate signer has not yet confirmed their data or accepted the terms; `401` Missing or invalid credentials; `409` The document is not ready to be viewed yet; `500` Unexpected server error.

### 56. Upload signature image

- **Java:** `client.signers()` — `SignerResource: public void uploadSignature(String signerAccessCode, String type, byte[] imageData)`; `client.signers()` — `SignerResource: public void uploadSignature(String signerAccessCode, String type, byte[] imageData, Boolean reuse)`
- **HTTP:** `POST /v1/signature`
- **Auth:** `signer-access-code` query credential
- **Side effects:** Changes signing state: upload signature image.
- **Contract notes:** Upload the signer's signature (or initials) image as the raw request body. Uses the signer access code.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `type` | query | `string` | no | no | Image type, e.g. `signature` or `initial`. |
| `reuse` | query | `boolean` | no | no | Whether the signer opted to reuse this signature in future processes. When set, updates the signer's `is_signature_reusable` flag; when omitted, the flag is left unchanged. |

Request body: **required**.

`image/png`: `string(binary)`.

Success `200` `application/json`: [Envelope](#schema-envelope).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |

Documented statuses: `200` Signature stored; `401` Missing or invalid credentials; `500` Unexpected server error.

### 57. Download signature image

- **Java:** `client.signers()` — `SignerResource: public byte[] downloadSignature(String signerAccessCode, String type)`
- **HTTP:** `GET /v1/signature/{signatureType}`
- **Auth:** `signer-access-code` query credential
- **Side effects:** None; read-only.
- **Contract notes:** Download the signer's stored signature/initials image. Uses the signer access code.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `signatureType` | path | `string` | yes | no | Image type (e.g. `signature`, `initial`). |

Request body: none.

Success `200` `image/*`: `string(binary)`.

Documented statuses: `200` The signature image; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 58. Get signer's document

- **Java:** `client.signers()` — `SignerResource: public Map<String, Object> getCurrentDocument(String signerId, String signerAccessCode)`; `client.signers()` — `SignerResource: public Document getCurrentDocumentTyped(String signerId, String signerAccessCode)`
- **HTTP:** `GET /v1/signers/{signerId}/document`
- **Auth:** `signer-access-code` query credential
- **Side effects:** None; read-only.
- **Contract notes:** Return the document and the signer's assignment items, scoped to the signer access code.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `signerId` | path | `string` | yes | no | The signer ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Document](#schema-document)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Document](#schema-document) | no | no |  |

Documented statuses: `200` The document with the signer's items; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 59. List signer's documents

- **Java:** `client.signers()` — `SignerResource: public PaginatedResult<Document> listDocuments(String signerId, String signerAccessCode)`; `client.signers()` — `SignerResource: public PaginatedResult<Document> listDocuments(String signerId, String signerAccessCode, ListParams params)`
- **HTTP:** `GET /v1/signers/{signerId}/documents`
- **Auth:** `signer-access-code` query credential
- **Side effects:** None; read-only.
- **Contract notes:** List the documents a signer is party to. Uses the signer access code.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `signerId` | path | `string` | yes | no | The signer ID. |
| `page` | query | `integer` | no | no | Page number. |
| `per-page` | query | `integer` | no | no | Records per page (max 100). |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[Document](#schema-document)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[Document](#schema-document)> | no | no |  |

Documented statuses: `200` The signer's documents; `401` Missing or invalid credentials; `500` Unexpected server error.

### 60. Download signer's document artifact

- **Java:** `client.signers()` — `SignerResource: public byte[] downloadDocument(String signerId, String documentId, String artifactName, String signerAccessCode)`; `client.signers()` — `SignerResource: public byte[] downloadDocument(String signerId, String documentId, String artifactName)`
- **HTTP:** `GET /v1/signers/{signerId}/documents/{documentId}/download/{artifactName}`
- **Auth:** Public (no SDK credential)
- **Side effects:** None; read-only.
- **Contract notes:** Download an artifact of a document the signer is party to. Public (signer-link) endpoint. Artifact types: original, certificated, certificate-page, pades, bundle. The pades artifact (signers' ICP-Brasil signatures + platform certification box) is only present on documents that had digital-certificate signers; `bundle` is a zip of the original, certificated and certificate-page artifacts, plus the pades artifact on documents that have one.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `signerId` | path | `string` | yes | no | The signer ID. |
| `documentId` | path | `string` | yes | no | Document ID. |
| `artifactName` | path | `string enum[original, certificated, certificate-page, pades, bundle]` | yes | no | Artifact type. |

Request body: none.

Success `200` `application/pdf`: `string(binary)`.

Documented statuses: `200` The artifact binary; `404` The requested resource does not exist; `500` Unexpected server error.

### 61. Search signer's documents

- **Java:** `client.signers()` — `SignerResource: public PaginatedResult<Document> searchDocuments(String signerId, String signerAccessCode, String search)`
- **HTTP:** `GET /v1/signers/{signerId}/documents/search`
- **Auth:** `signer-access-code` query credential
- **Side effects:** None; read-only.
- **Contract notes:** Search the documents a signer is party to (compact representation). Uses the signer access code.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `signerId` | path | `string` | yes | no | The signer ID. |
| `search` | query | `string` | no | no | Search term. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[Document](#schema-document)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[Document](#schema-document)> | no | no |  |

Documented statuses: `200` Matching documents; `401` Missing or invalid credentials; `500` Unexpected server error.

### 62. Accept terms (signer)

- **Java:** `client.signers()` — `SignerResource: public void acceptTerms(String signerAccessCode)`
- **HTTP:** `PUT /v1/signers/accept-terms`
- **Auth:** `signer-access-code` query credential
- **Side effects:** Changes signing state: accept terms (signer).
- **Contract notes:** Record that the signer accepted the terms of use. Uses the signer access code.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |

Documented statuses: `200` Terms accepted; `401` Missing or invalid credentials; `500` Unexpected server error.

### 63. Decline multiple documents

- **Java:** `client.signers()` — `SignerResource: public Map<String, Object> declineMultiple(String signerAccessCode, List<String> documentIds, String declineReason)`
- **HTTP:** `PUT /v1/signers/documents/decline-multiple`
- **Auth:** `signer-access-code` query credential
- **Side effects:** Changes signing state: decline multiple documents.
- **Contract notes:** Decline several documents in one request. Uses the signer access code.

Parameters: none.

Request body: **required**.

`application/json`: object{document_ids!: array<`string`>; decline_reason!: `string`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `document_ids` | array<`string`> | yes | no | IDs of the documents to decline. |
| `decline_reason` | `string` | yes | no | Reason for declining. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<`any`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<`any`> | no | no | Empty array. |

Documented statuses: `200` Decline result; `401` Missing or invalid credentials; `500` Unexpected server error.

### 64. Sign multiple documents

- **Java:** `client.signers()` — `SignerResource: public Map<String, Object> signMultiple(String signerAccessCode, List<String> documentIds)`
- **HTTP:** `PUT /v1/signers/documents/sign-multiple`
- **Auth:** `signer-access-code` query credential
- **Side effects:** Changes signing state: sign multiple documents.
- **Contract notes:** Sign several documents in one request, for a signer with multiple pending documents. Each document must be prepared for the **virtual** signature method. Uses the signer access code.

Parameters: none.

Request body: **required**.

`application/json`: object{document_ids!: array<`string`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `document_ids` | array<`string`> | yes | no | IDs of the documents to sign. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<`any`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<`any`> | no | no | Empty array. |

Documented statuses: `200` Signing result; `401` Missing or invalid credentials; `500` Unexpected server error.

### 65. Get current signer

- **Java:** `client.signers()` — `SignerResource: public Signer getSelf(String signerAccessCode)`
- **HTTP:** `GET /v1/signers/self`
- **Auth:** `signer-access-code` query credential
- **Side effects:** None; read-only.
- **Contract notes:** Return the signer identified by the signer access code, including the `has_signature`/`has_initial`/`is_signature_reusable` flags.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [SignerSelf](#schema-signerself)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [SignerSelf](#schema-signerself) | no | no |  |

Documented statuses: `200` The signer; `401` Missing or invalid credentials; `500` Unexpected server error.

### 66. Verify signer code (OTP)

- **Java:** `client.signers()` — `SignerResource: public Map<String, Object> verifyEmail(String signerAccessCode, String verificationCode)`
- **HTTP:** `POST /v1/verify`
- **Auth:** `signer-access-code` query credential
- **Side effects:** Changes signing state: verify signer code (otp).
- **Contract notes:** Submit the verification code (OTP) sent to the signer to unlock the signing flow. Uses the signer access code.

Parameters: none.

Request body: **required**.

`application/json`: object{verification-code!: `string`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `verification-code` | `string` | yes | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |

Documented statuses: `200` Code verified; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

## Templates

### 67. List templates

- **Java:** `client.templates()` — `TemplateResource: public PaginatedResult<Template> list()`; `client.templates()` — `TemplateResource: public PaginatedResult<Template> list(ListParams params)`; `client.templates()` — `TemplateResource: public PaginatedResult<Template> list(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/templates`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** List the templates of a workspace. The `status` field uses one of the values below.

| Status | Description |
|---|---|
| `uploading` | The template is being uploaded. |
| `uploaded` | The template has been uploaded. |
| `processing` | The template is being processed. |
| `ready` | The template is ready to use. |
| `failed` | The template processing has failed. |

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `search` | query | `string` | no | no | Search term. |
| `page` | query | `integer` | no | no | Page number. |
| `per-page` | query | `integer` | no | no | Records per page (max 100). |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[Template](#schema-template)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[Template](#schema-template)> | no | no |  |

Documented statuses: `200` A page of templates (default_document_tags omitted in the list); `401` Missing or invalid credentials; `500` Unexpected server error.

## Fields

### 68. List fields

- **Java:** `client.fields()` — `FieldResource: public PaginatedResult<FieldDefinition> list()`; `client.fields()` — `FieldResource: public PaginatedResult<FieldDefinition> list(ListParams params)`; `client.fields()` — `FieldResource: public PaginatedResult<FieldDefinition> list(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/fields`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** List the field definitions of a workspace. When `include_standard` is enabled, records of type `signature`, `initial` and `signatureDate` are also returned.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `include_inactive` | query | `boolean` | no | no | Include inactive field definitions. |
| `include_standard` | query | `boolean` | no | no | Include standard field types (signature, initial, signatureDate). |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[Field](#schema-field)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[Field](#schema-field)> | no | no |  |

Documented statuses: `200` Field definitions; `401` Missing or invalid credentials; `500` Unexpected server error.

### 69. Create field

- **Java:** `client.fields()` — `FieldResource: public FieldDefinition create(CreateFieldRequest request)`; `client.fields()` — `FieldResource: public FieldDefinition create(CreateFieldRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/fields`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: create field.
- **Contract notes:** Create a field definition in the workspace.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: **required**.

`application/json`: object{name!: `string`; type!: `string`; regex: `string`?; is_required: `boolean`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | `string` | yes | no |  |
| `type` | `string` | yes | no |  |
| `regex` | `string`? | no | yes |  |
| `is_required` | `boolean` | no | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Field](#schema-field)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Field](#schema-field) | no | no |  |

Documented statuses: `200` The created field; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

### 70. Delete field

- **Java:** `client.fields()` — `FieldResource: public void delete(String fieldId)`; `client.fields()` — `FieldResource: public void delete(String fieldId, String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/fields/{fieldId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Deletes server state: delete field.
- **Contract notes:** Delete a field definition.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `fieldId` | path | `string` | yes | no | The field ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<`any`>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<`any`> | no | no |  |

Documented statuses: `200` Field deleted; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 71. Get field

- **Java:** `client.fields()` — `FieldResource: public FieldDefinition get(String fieldId)`; `client.fields()` — `FieldResource: public FieldDefinition get(String fieldId, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/fields/{fieldId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Retrieve a single field definition.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `fieldId` | path | `string` | yes | no | The field ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Field](#schema-field)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Field](#schema-field) | no | no |  |

Documented statuses: `200` The field; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 72. Update field

- **Java:** `client.fields()` — `FieldResource: public FieldDefinition update(String fieldId, UpdateFieldRequest request)`; `client.fields()` — `FieldResource: public FieldDefinition update(String fieldId, UpdateFieldRequest request, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/fields/{fieldId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: update field.
- **Contract notes:** Update a field definition.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `fieldId` | path | `string` | yes | no | The field ID. |

Request body: **required**.

`application/json`: object{name: `string`; regex: `string`?; is_active: `boolean`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | `string` | no | no |  |
| `regex` | `string`? | no | yes |  |
| `is_active` | `boolean` | no | no |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Field](#schema-field)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Field](#schema-field) | no | no |  |

Documented statuses: `200` The updated field; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 73. Validate field value

- **Java:** `client.fields()` — `FieldResource: public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode)`; `client.fields()` — `FieldResource: public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/fields/{fieldId}/validate`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: validate field value.
- **Contract notes:** Validate an input value against a field definition. Typically called with a signer access code during the signing flow.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `fieldId` | path | `string` | yes | no | The field ID. |

Request body: **required**.

`application/json`: object{value!: `any`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `value` | `any` | yes | no | The input value to validate. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [FieldValidation](#schema-fieldvalidation)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [FieldValidation](#schema-fieldvalidation) | no | no |  |

Documented statuses: `200` Validation result; `401` Missing or invalid credentials; `500` Unexpected server error.

### 74. Validate multiple field values

- **Java:** `client.fields()` — `FieldResource: public List<FieldValidationResult> validateMultiple(List<Map<String, Object>> entries, String signerAccessCode)`; `client.fields()` — `FieldResource: public List<FieldValidationResult> validateMultiple(List<Map<String, Object>> entries, String signerAccessCode, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/fields/validate-multiple`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: validate multiple field values.
- **Contract notes:** Validate multiple input values at once. The request body is a JSON array of `{field_id, value}` objects.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: **required**.

`application/json`: array<object{field_id!: `string`; value!: `any`}>.

| Array-item field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `field_id` | `string` | yes | no | The field definition ID. |
| `value` | `any` | yes | no | The input value to validate. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[FieldValidationResult](#schema-fieldvalidationresult)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[FieldValidationResult](#schema-fieldvalidationresult)> | no | no |  |

Documented statuses: `200` Validation results; `401` Missing or invalid credentials; `500` Unexpected server error.

### 75. List field types

- **Java:** `client.fields()` — `FieldResource: public List<FieldType> listTypes()`
- **HTTP:** `GET /v1/field-types`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** List the possible field types. `cpf` expects 11 digits; `cnpj` accepts 14-char values (letters A-Z allowed in positions 1–12 per the CNPJ Alfanumérico rule; check digits 13–14 stay numeric). Punctuation is ignored during validation.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[FieldType](#schema-fieldtype)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[FieldType](#schema-fieldtype)> | no | no |  |

Documented statuses: `200` Field types; `401` Missing or invalid credentials; `500` Unexpected server error.

## Tags

### 76. List tags

- **Java:** `client.tags()` — `TagResource: public PaginatedResult<Tag> list()`; `client.tags()` — `TagResource: public PaginatedResult<Tag> list(ListParams params)`; `client.tags()` — `TagResource: public PaginatedResult<Tag> list(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/tags`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** List the tags of a workspace.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `search` | query | `string` | no | no | Search term. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[Tag](#schema-tag)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[Tag](#schema-tag)> | no | no |  |

Documented statuses: `200` The workspace tags; `401` Missing or invalid credentials; `500` Unexpected server error.

### 77. Create tag

- **Java:** `client.tags()` — `TagResource: public Tag create(CreateTagRequest request)`; `client.tags()` — `TagResource: public Tag create(CreateTagRequest request, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/tags`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: create tag.
- **Contract notes:** Create a tag in the workspace. Names are unique per workspace (case-insensitive); a collision returns 409.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: **required**.

`application/json`: object{name!: `string`; color: `string`?}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | `string` | yes | no | Trimmed; whitespace collapsed; max 64 chars. |
| `color` | `string`? | no | yes | 6-char hex (with or without leading #). |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Tag](#schema-tag)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Tag](#schema-tag) | no | no |  |

Documented statuses: `200` The created tag; `400` One or more fields failed validation; `401` Missing or invalid credentials; `409` A tag with the same name already exists; `500` Unexpected server error.

### 78. Delete tag

- **Java:** `client.tags()` — `TagResource: public void delete(String tagId)`; `client.tags()` — `TagResource: public void delete(String tagId, boolean force)`; `client.tags()` — `TagResource: public void delete(String tagId, boolean force, String accountId)`
- **HTTP:** `DELETE /v1/accounts/{accountId}/tags/{tagId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Deletes server state: delete tag.
- **Contract notes:** Delete a tag. Pass `?force=true` to detach it from any documents/templates it is attached to.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `tagId` | path | `string` | yes | no | The tag ID. |
| `force` | query | `boolean` | no | no | Detach from resources before deleting. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: object{deleted: `boolean`}}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | object{deleted: `boolean`} | no | no |  |

Documented statuses: `200` Tag deleted; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 79. Update tag

- **Java:** `client.tags()` — `TagResource: public Tag rename(String tagId, RenameTagRequest request)`; `client.tags()` — `TagResource: public Tag rename(String tagId, RenameTagRequest request, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/tags/{tagId}`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: update tag.
- **Contract notes:** Update a tag's name or color.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `tagId` | path | `string` | yes | no | The tag ID. |

Request body: **required**.

`application/json`: object{name: `string`; color: `string`?}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `name` | `string` | no | no |  |
| `color` | `string`? | no | yes |  |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [Tag](#schema-tag)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [Tag](#schema-tag) | no | no |  |

Documented statuses: `200` The updated tag; `400` One or more fields failed validation; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

## Webhooks

### 80. List webhook deliveries

- **Java:** `client.webhooks()` — `WebhookResource: public PaginatedResult<WebhookDispatch> listDispatches()`; `client.webhooks()` — `WebhookResource: public PaginatedResult<WebhookDispatch> listDispatches(ListParams params)`; `client.webhooks()` — `WebhookResource: public PaginatedResult<WebhookDispatch> listDispatches(ListParams params, String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/webhooks`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Retrieve the delivery history for webhooks sent to the account's configured endpoint — use it to monitor status, debug failures, and verify payloads. Pagination is returned in the `X-Pagination-*` response headers.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `event` | query | `string` | no | no | Filter by event type (e.g. `document_ready`). |
| `delivered` | query | `string enum[true, false]` | no | no | Filter by delivery status: `true` or `false`. |
| `from` | query | `integer` | no | no | Unix timestamp — only entries after this time. |
| `to` | query | `integer` | no | no | Unix timestamp — only entries before this time. |
| `page` | query | `integer` | no | no | Page number. |
| `per-page` | query | `integer` | no | no | Items per page (default: 20). |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[WebhookDispatch](#schema-webhookdispatch)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[WebhookDispatch](#schema-webhookdispatch)> | no | no |  |

Documented statuses: `200` Delivery history; `401` Missing or invalid credentials; `500` Unexpected server error.

### 81. Retry webhook delivery

- **Java:** `client.webhooks()` — `WebhookResource: public WebhookDispatch retryDispatch(String dispatchId)`; `client.webhooks()` — `WebhookResource: public WebhookDispatch retryDispatch(String dispatchId, String accountId)`
- **HTTP:** `POST /v1/accounts/{accountId}/webhooks/{historyId}/retry`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: retry webhook delivery.
- **Contract notes:** Manually retry a webhook delivery for a specific entry, without waiting for automatic retries. Returns the newly created dispatch entry.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |
| `historyId` | path | `string` | yes | no | The webhook dispatch entry ID to retry. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [WebhookDispatch](#schema-webhookdispatch)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [WebhookDispatch](#schema-webhookdispatch) | no | no |  |

Documented statuses: `200` The new dispatch entry; `400` One or more fields failed validation; `401` Missing or invalid credentials; `404` The requested resource does not exist; `500` Unexpected server error.

### 82. Inactivate webhook subscription

- **Java:** `client.webhooks()` — `WebhookResource: public WebhookSubscription inactivate()`; `client.webhooks()` — `WebhookResource: public WebhookSubscription inactivate(String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/webhooks/inactivate`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: inactivate webhook subscription.
- **Contract notes:** Deactivate the webhook integration for the account. While inactive, no events are sent to the configured endpoint.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [WebhookSubscription](#schema-webhooksubscription)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [WebhookSubscription](#schema-webhooksubscription) | no | no |  |

Documented statuses: `200` The inactivated subscription; `401` Missing or invalid credentials; `500` Unexpected server error.

### 83. Get webhook subscription

- **Java:** `client.webhooks()` — `WebhookResource: public WebhookSubscription get()`; `client.webhooks()` — `WebhookResource: public WebhookSubscription get(String accountId)`
- **HTTP:** `GET /v1/accounts/{accountId}/webhooks/subscriptions`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Retrieve the current webhook subscription for the account — which events it is subscribed to and the delivery configuration.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [WebhookSubscription](#schema-webhooksubscription)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [WebhookSubscription](#schema-webhooksubscription) | no | no |  |

Documented statuses: `200` The subscription; `401` Missing or invalid credentials; `500` Unexpected server error.

### 84. Update webhook subscription

- **Java:** `client.webhooks()` — `WebhookResource: public WebhookSubscription register(RegisterWebhookRequest request)`; `client.webhooks()` — `WebhookResource: public WebhookSubscription register(RegisterWebhookRequest request, String accountId)`
- **HTTP:** `PUT /v1/accounts/{accountId}/webhooks/subscriptions`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: update webhook subscription.
- **Contract notes:** Update the webhook subscription settings for the account — which events are monitored, whether delivery is enabled, and the delivery/contact details.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `accountId` | path | `string` | yes | no | Workspace account ID. |

Request body: **required**.

`application/json`: object{events!: array<`string`>; is_active!: `boolean`; url!: `string(uri)`; email!: `string(email)`}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `events` | array<`string`> | yes | no | Event type codes to subscribe to (see `GET /v1/webhooks/event-types`). |
| `is_active` | `boolean` | yes | no | Whether events should be delivered to the webhook. |
| `url` | `string(uri)` | yes | no | The URL that will receive events. |
| `email` | `string(email)` | yes | no | Email that receives important webhook-communication notices. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [WebhookSubscription](#schema-webhooksubscription)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [WebhookSubscription](#schema-webhooksubscription) | no | no |  |

Documented statuses: `200` The updated subscription; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

### 85. List webhook event types

- **Java:** `client.webhooks()` — `WebhookResource: public List<WebhookEventTypeInfo> listEventTypes()`
- **HTTP:** `GET /v1/webhooks/event-types`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** List all available event types that can be subscribed to via webhooks.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[WebhookEventType](#schema-webhookeventtype)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[WebhookEventType](#schema-webhookeventtype)> | no | no |  |

Documented statuses: `200` Event types; `401` Missing or invalid credentials; `500` Unexpected server error.

## Users

### 86. Get the authenticated user

- **Java:** `client.users()` — `UserResource: public AuthUser get()`
- **HTTP:** `GET /v1/users/self`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Returns the profile of the user owning the access token.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [AuthUser](#schema-authuser)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [AuthUser](#schema-authuser) | no | no |  |

Documented statuses: `200` The current user; `401` Missing or invalid credentials; `500` Unexpected server error.

### 87. Get my notification preferences

- **Java:** `client.users()` — `UserResource: public NotificationPreferences getNotificationPreferences()`
- **HTTP:** `GET /v1/users/self/notification-preferences`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** Which owner-facing document notifications the authenticated user receives by e-mail. All nine keys are always returned; everything defaults to `true`. Account and security e-mail (welcome, password reset, invitations, account deletion) is not configurable and never appears here.

Parameters: none.

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [NotificationPreferences](#schema-notificationpreferences)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [NotificationPreferences](#schema-notificationpreferences) | no | no |  |

Documented statuses: `200` The current preferences; `401` Missing or invalid credentials; `500` Unexpected server error.

### 88. Update my notification preferences

- **Java:** `client.users()` — `UserResource: public NotificationPreferences updateNotificationPreferences(Map<String, Boolean> changes)`
- **HTTP:** `PUT /v1/users/self/notification-preferences`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** Mutates server state: update my notification preferences.
- **Contract notes:** Merges the supplied map into the authenticated user's preferences. Send only the keys you want to change — omitted keys keep their current value. Setting a key to `false` stops that e-mail for this user in every account they belong to. Returns the full map. An unknown code, a non-boolean value, or an empty body is rejected with 400 and nothing is written.

Parameters: none.

Request body: **required**.

`application/json`: [NotificationPreferences](#schema-notificationpreferences).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `DocumentCompleted` | `boolean` | no | no | Every signer has signed and the document is certified. |
| `SignerDeclined` | `boolean` | no | no | A signer declined to sign. |
| `DocumentCancelled` | `boolean` | no | no | The document was cancelled. |
| `DocumentAboutToExpire` | `boolean` | no | no | The signature deadline is approaching. |
| `DocumentExpired` | `boolean` | no | no | The signature deadline passed. |
| `DocumentExpirationReset` | `boolean` | no | no | The signature deadline was extended. |
| `DocumentProcessingFailed` | `boolean` | no | no | An uploaded document could not be processed. |
| `TemplateProcessingFailed` | `boolean` | no | no | A template could not be processed. |
| `SignerWhatsappFailed` | `boolean` | no | no | A WhatsApp notification to a signer could not be delivered. |

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: [NotificationPreferences](#schema-notificationpreferences)}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | [NotificationPreferences](#schema-notificationpreferences) | no | no |  |

Documented statuses: `200` The updated preferences; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

### 89. My cross-account document KPIs

- **Java:** `client.users()` — `UserResource: public List<DocumentStatsRow> stats()`; `client.users()` — `UserResource: public List<DocumentStatsRow> stats(String granularity, String month)`
- **HTTP:** `GET /v1/users/self/stats`
- **Auth:** Bearer JWT or `X-Api-Key`
- **Side effects:** None; read-only.
- **Contract notes:** The authenticated user's document-funnel KPIs summed across all accounts they currently belong to. `granularity=monthly` (default) returns the last 12 months, most recent first; `granularity=daily` with `month=YYYY-MM` returns that month's days. Series are zero-filled.

| Parameter | Location | Type | Required | Nullable | Notes |
|---|---|---|---:|---:|---|
| `granularity` | query | `string enum[monthly, daily]` | no | no | `monthly` (default) or `daily`. |
| `month` | query | `string` | no | no | Target month `YYYY-MM` (required when `granularity=daily`). |

Request body: none.

Success `200` `application/json`: [Envelope](#schema-envelope) + object{data: array<[DocumentStatsRow](#schema-documentstatsrow)>}.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |
| `data` | array<[DocumentStatsRow](#schema-documentstatsrow)> | no | no |  |

Documented statuses: `200` KPI series; `400` One or more fields failed validation; `401` Missing or invalid credentials; `500` Unexpected server error.

## Component payload schemas

These tables complete every component referenced by the operation response/request sections above. Fields are wire JSON names.

### Schema: Envelope

Standard success wrapper. Operations add their own `data`.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no | HTTP status code, mirrored in the body. |
| `message` | `string` | no | no | Human-readable message; empty on success. |

### Schema: ErrorEnvelope

Standard error wrapper. `status` mirrors the HTTP status code.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `status` | `integer` | no | no |  |
| `message` | `string` | no | no | Human-readable error message. |
| `data` | `object`? | no | yes |  |

### Schema: ApiKey

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `api_key` | `string`? | no | yes |  |

### Schema: AuthUser

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | `string` | no | no |  |
| `name` | `string` | no | no |  |
| `email` | `string(email)` | no | no |  |
| `telephone` | `string`? | no | yes |  |
| `government_id` | `string`? | no | yes |  |
| `is_email_verified` | `boolean` | no | no |  |
| `has_accepted_terms` | `boolean` | no | no |  |
| `is_password_set` | `boolean` | no | no | Whether the user has configured a password. |
| `created_at` | `string(date-time)` | no | no |  |
| `to_be_deleted_at` | `string(date-time)`? | no | yes |  |

### Schema: AuthAccount

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | `string` | no | no |  |
| `name` | `string` | no | no |  |
| `roles` | array<`string`> | no | no |  |
| `is_delete_allowed` | `boolean` | no | no |  |
| `created_at` | `string(date-time)` | no | no |  |

### Schema: Signer

A signing party belonging to a workspace account.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | `string` | no | no | Present in single-resource responses. |
| `id` | `string` | no | no |  |
| `full_name` | `string` | no | no |  |
| `email` | `string(email)`? | no | yes |  |
| `whatsapp_phone_number` | `string`? | no | yes | E.164 format; normalized on save. |
| `has_accepted_terms` | `boolean` | no | no |  |

### Schema: SignerSelf

The current signer, as returned by `GET /v1/signers/self`. Extends Signer with the signature-state flags that are only computed for the authenticated signer.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | `string` | no | no | Present in single-resource responses. |
| `id` | `string` | no | no |  |
| `full_name` | `string` | no | no |  |
| `email` | `string(email)`? | no | yes |  |
| `whatsapp_phone_number` | `string`? | no | yes | E.164 format; normalized on save. |
| `has_accepted_terms` | `boolean` | no | no |  |
| `has_signature` | `boolean` | no | no | Whether the signer has a saved signature image stored. |
| `has_initial` | `boolean` | no | no | Whether the signer has a saved initials image stored. |
| `is_signature_reusable` | `boolean` | no | no | Whether the signer opted to reuse their saved signature/initials in future processes. When false, clients should not pre-render the saved image even if `has_signature`/`has_initial` is true. |

### Schema: DocumentPage

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | `string` | no | no |  |
| `number` | `integer` | no | no |  |
| `height` | `integer` | no | no |  |
| `width` | `integer` | no | no |  |
| `download_url` | `string` | no | no |  |

### Schema: DisplaySettings

A field placement rectangle on a document page. Geometry values are pixels in Assinafy's 150-DPI page image, measured from the upper-left corner. Clients must keep the rectangle within the selected page's width and height; the API does not clamp out-of-bounds values.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `left` | `number(float)` | yes | no | Horizontal distance from the page's left edge, in page-image pixels. |
| `top` | `number(float)` | yes | no | Vertical distance from the page's top edge, in page-image pixels. |
| `width` | `number(float)` | yes | no | Width of the placement rectangle, in page-image pixels. |
| `height` | `number(float)` | yes | no | Height of the placement rectangle, in page-image pixels. |
| `fontFamily` | `string` | no | no | Font-family presentation metadata. |
| `fontSize` | `number(float)` | yes | no | Font size in the 150-DPI page-image coordinate system. |
| `backgroundColor` | `string` | no | no | CSS-compatible background-color presentation metadata. |

### Schema: DocumentStatus

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `code` | `string` | no | no |  |
| `deletable` | `boolean` | no | no |  |

### Schema: Document

A document and its current lifecycle state.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | `string` | no | no | Present in single-resource responses. |
| `id` | `string` | no | no |  |
| `account_id` | `string` | no | no |  |
| `template_id` | `string`? | no | yes |  |
| `name` | `string` | no | no |  |
| `status` | `string` | no | no | Status code — see GET /v1/documents/statuses. |
| `artifacts` | [DocumentArtifacts](#documentartifacts) | no | no | Available artifact download URLs, including thumbnail and PAdES when applicable. |
| `is_closed` | `boolean` | no | no |  |
| `signing_url` | `string` | no | no |  |
| `decline_reason` | `string`? | no | yes |  |
| `declined_by` | [Signer](#schema-signer)? | no | yes |  |
| `tags` | array<object{id: `string`; name: `string`}> | no | no |  |
| `assignment` | [Assignment](#schema-assignment)? | no | yes | Expanded assignment data when included via ?expand=assignment; null otherwise. |
| `pages` | array<[DocumentPage](#schema-documentpage)> | no | no |  |
| `created_at` | `string(date-time)` | no | no |  |
| `updated_at` | `string(date-time)` | no | no |  |

### Schema: Account

A workspace account (organization).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | `string` | no | no |  |
| `id` | `string` | no | no |  |
| `name` | `string` | no | no |  |
| `primary_color` | `string`? | no | yes |  |
| `secondary_color` | `string`? | no | yes |  |
| `notification_sender_type` | `string enum[User, Account]` | no | no |  |
| `roles` | array<`string`> | no | no |  |
| `is_delete_allowed` | `boolean` | no | no |  |
| `created_at` | `string(date-time)` | no | no |  |

### Schema: Field

A reusable field definition.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | `string` | no | no |  |
| `id` | `string` | no | no |  |
| `name` | `string` | no | no |  |
| `type` | `string` | no | no |  |
| `regex` | `string`? | no | yes |  |
| `is_pre_defined` | `boolean` | no | no |  |
| `is_active` | `boolean` | no | no |  |
| `is_required` | `boolean` | no | no |  |
| `is_standard` | `boolean` | no | no |  |
| `is_read_only` | `boolean` | no | no |  |
| `is_visible` | `boolean` | no | no |  |

### Schema: Tag

A workspace-scoped label. Names are unique per workspace (case-insensitive).

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | `string` | no | no |  |
| `id` | `string` | no | no |  |
| `name` | `string` | no | no |  |
| `color` | `string`? | no | yes | 6-char hex without leading #. |
| `created_at` | `string(date-time)` | no | no |  |
| `updated_at` | `string(date-time)` | no | no |  |

### Schema: SigningUrl

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `signer_id` | `string` | no | no |  |
| `url` | `string` | no | no |  |

### Schema: AssignmentSigner

A signer within an assignment: the base Signer plus per-assignment verification/notification details.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | `string` | no | no | Present in single-resource responses. |
| `id` | `string` | no | no |  |
| `full_name` | `string` | no | no |  |
| `email` | `string(email)`? | no | yes |  |
| `whatsapp_phone_number` | `string`? | no | yes | E.164 format; normalized on save. |
| `has_accepted_terms` | `boolean` | no | no |  |
| `verification_method` | `string`? | no | yes |  |
| `notification_methods` | array<`string`>? | no | yes |  |
| `step` | `integer`? | no | yes | Sequential signing step (defaults to 1). |
| `notified` | `boolean`? | no | yes |  |
| `completed` | `boolean`? | no | yes | Only present in account-owner contexts. |
| `notification_history` | array<[NotificationHistoryEntry](#schema-notificationhistoryentry)>? | no | yes | Per-channel delivery history for this signer (email + WhatsApp), most-recent send order. |

### Schema: NotificationHistoryEntry

A single notification delivery record for a signer channel.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `event` | `string` | no | no |  |
| `status` | `string enum[sent, failed]` | no | no |  |
| `error_code` | `string`? | no | yes |  |
| `error_message` | `string`? | no | yes |  |
| `sent_at` | `string(date-time)`? | no | yes |  |
| `failed_at` | `string(date-time)`? | no | yes |  |

### Schema: AssignmentItem

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | `string` | no | no |  |
| `page` | [DocumentPage](#schema-documentpage)? | no | yes |  |
| `signer` | `object` | no | no | Signer responsible for this item. |
| `field` | `object`? | no | yes | Field definition associated with the item. |
| `display_settings` | `any` | no | no | Rendering metadata for the item. Collect items use the DisplaySettings schema; virtual and legacy items may return an empty or non-object value. |
| `value` | `any`? | no | yes | Captured value when completed. |
| `completed` | `boolean` | no | no |  |

### Schema: AssignmentSummary

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `signer_count` | `integer` | no | no |  |
| `completed_count` | `integer` | no | no |  |
| `signers` | array<`object`> | no | no |  |

### Schema: Assignment

A request for signers to sign a document.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | `string` | no | no |  |
| `id` | `string` | no | no |  |
| `sender_email` | `string(email)` | no | no |  |
| `method` | `string enum[virtual, collect]` | no | no |  |
| `expires_at` | `string(date-time)`? | no | yes |  |
| `message` | `string`? | no | yes |  |
| `signers` | array<[AssignmentSigner](#schema-assignmentsigner)> | no | no |  |
| `copy_receivers` | array<`object`> | no | no |  |
| `items` | array<[AssignmentItem](#schema-assignmentitem)> | no | no |  |
| `summary` | [AssignmentSummary](#schema-assignmentsummary) | no | no |  |
| `signing_urls` | array<[SigningUrl](#schema-signingurl)> | no | no |  |

### Schema: CostEstimateBreakdownItem

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `code` | `string` | no | no |  |
| `name` | `string` | no | no |  |
| `cost` | `number` | no | no |  |
| `quantity` | `integer` | no | no |  |
| `unit_cost` | `number` | no | no |  |

### Schema: CostEstimate

Cost breakdown for an assignment plus current account balances.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `documents` | `integer` | no | no | Documents consumed (always 1). |
| `credits` | `number` | no | no | Total notification credits needed. |
| `needs_extra_document` | `boolean` | no | no | True when the plan's document allowance is exhausted and an extra document will be charged from credits. |
| `extra_document_cost` | `number` | no | no | Credits charged for the extra document when `needs_extra_document` is true. |
| `total_credits` | `number` | no | no |  |
| `breakdown` | array<[CostEstimateBreakdownItem](#schema-costestimatebreakdownitem)> | no | no |  |
| `document_balance` | `number` | no | no |  |
| `credit_balance` | `number` | no | no |  |
| `has_sufficient_resources` | `boolean` | no | no |  |
| `blocking_reason` | `string enum[PendingPayment, InsufficientDocuments, InsufficientCredits]`? | no | yes |  |
| `message` | `string`? | no | yes |  |

### Schema: TemplateFieldPlacement

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | `string` | no | no |  |
| `field_id` | `string` | no | no |  |
| `role_id` | `string` | no | no |  |
| `label` | `string` | no | no |  |
| `display_settings` | `any` | no | no | Rendering metadata for the placement. |
| `created_at` | `string(date-time)` | no | no |  |
| `updated_at` | `string(date-time)` | no | no |  |

### Schema: TemplatePage

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | `string` | no | no |  |
| `number` | `integer` | no | no |  |
| `height` | `integer` | no | no |  |
| `width` | `integer` | no | no |  |
| `download_url` | `string` | no | no |  |
| `fields` | array<[TemplateFieldPlacement](#schema-templatefieldplacement)> | no | no |  |

### Schema: TemplateRole

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | `string` | no | no |  |
| `name` | `string` | no | no |  |
| `assignment_type` | `string` | no | no |  |
| `created_at` | `string(date-time)` | no | no |  |
| `updated_at` | `string(date-time)` | no | no |  |

### Schema: Template

A reusable document template.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | `string` | no | no |  |
| `id` | `string` | no | no |  |
| `name` | `string` | no | no |  |
| `document_name` | `string`? | no | yes | Default name for documents created from this template. |
| `message` | `string`? | no | yes | Default invitation message. |
| `status` | `string` | no | no | One of uploading, uploaded, processing, ready, failed. |
| `pages` | array<[TemplatePage](#schema-templatepage)> | no | no |  |
| `roles` | array<[TemplateRole](#schema-templaterole)> | no | no |  |
| `tags` | array<object{id: `string`; name: `string`}> | no | no |  |
| `default_document_tags` | array<object{id: `string`; name: `string`}> | no | no | Applied to documents created from this template; only returned by the single-template endpoint. |
| `created_at` | `string(date-time)` | no | no |  |
| `updated_at` | `string(date-time)` | no | no |  |

### Schema: WebhookSubscription

An account's webhook subscription configuration.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `events` | array<`string`> | no | no | Event types subscribed for delivery. |
| `is_active` | `boolean` | no | no | Whether webhook delivery is active. |
| `url` | `string`? | no | yes | Webhook endpoint URL. |
| `email` | `string`? | no | yes | Contact email for delivery notices. |
| `updated_at` | `string(date-time)`? | no | yes |  |

### Schema: WebhookDispatch

A single webhook delivery-history entry.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `resource` | `string` | no | no | Always `activity_dispatching_history` in single-resource responses. |
| `id` | `string` | no | no | Dispatch entry ID. |
| `event` | `string` | no | no | Event type that triggered the dispatch. |
| `activity_id` | `integer` | no | no | Internal activity ID associated with the dispatch. |
| `endpoint` | `string`? | no | yes | URL that received the request. |
| `payload` | `object`? | no | yes | JSON payload sent to the endpoint. |
| `delivered` | `boolean` | no | no | Whether delivery succeeded. |
| `http_status` | `integer`? | no | yes | HTTP status returned (null if connection failed). |
| `response_body` | `string`? | no | yes | Endpoint response body, truncated to 2000 chars. |
| `error` | `string`? | no | yes | Delivery error message, if any. |
| `created_at` | `string(date-time)` | no | no |  |
| `updated_at` | `string(date-time)` | no | no |  |

### Schema: WebhookEventType

A subscribable webhook event type.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | `string` | no | no | Event type code. |
| `description` | `string` | no | no | When the event is triggered. |

### Schema: AccountTheme

An account's branding theme.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `account_name` | `string` | no | no |  |
| `primary_color` | `string` | no | no | Hex color without leading `#`. |
| `secondary_color` | `string`? | no | yes |  |
| `logo` | `string` | no | no | URL to the account logo. |

### Schema: FieldType

A supported field/validation type.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `type` | `string` | no | no |  |
| `name` | `string` | no | no |  |

### Schema: FieldValidation

The result of validating a value against a field definition.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `type` | `string` | no | no | The field's validation type. |
| `success` | `boolean` | no | no |  |
| `error_message` | `string` | no | no | Empty when valid. |

### Schema: FieldValidationResult

A per-field result from a multi-field validation.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `field_id` | `string` | no | no |  |
| `type` | `string` | no | no |  |
| `success` | `boolean` | no | no |  |
| `error_message` | `string` | no | no |  |

### Schema: DocumentVerification

The verification result for a document looked up by signature hash. When not verified, most fields are null and `is_valid` is false.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `hash` | `string` | no | no |  |
| `id` | `string`? | no | yes |  |
| `status` | `string`? | no | yes |  |
| `page_count` | `string`? | no | yes |  |
| `signer_count` | `string`? | no | yes |  |
| `completed_count` | `integer`? | no | yes |  |
| `completed_at` | `string(date-time)`? | no | yes |  |
| `verified_at` | `string(date-time)` | no | no |  |
| `is_valid` | `boolean` | no | no |  |
| `message` | `string` | no | no | Reason when not valid. |

### Schema: DocumentActivity

A document activity event.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `id` | `integer` | no | no |  |
| `event` | `string` | no | no | Event type code. |
| `message` | `string` | no | no |  |
| `payload` | `object`? | no | yes | Event-specific payload snapshot. Keys vary per event. |
| `origin` | object{ip: `string`; user-agent: `string`}? | no | yes | Request origin when available. |
| `created_at` | `string(date-time)` | no | no |  |

### Schema: WhatsappNotification

A rendered WhatsApp notification sent for an assignment, split into header/body/buttons as the signer would see them.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `sent_at` | `integer` | no | no | Unix timestamp when sent. |
| `header` | `string` | no | no |  |
| `body` | `string` | no | no |  |
| `buttons` | array<object{text: `string`}> | no | no |  |
| `phone_number` | `string` | no | no | Recipient phone (E.164). |
| `signer_id` | `string` | no | no |  |

### Schema: AuthSession

A JWT access token plus the authenticated user and the accounts they belong to.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `access_token` | `string` | no | no |  |
| `user` | [AuthUser](#schema-authuser) | no | no |  |
| `accounts` | array<[AuthAccount](#schema-authaccount)> | no | no |  |

### Schema: DocumentStatsRow

One period of the document-funnel KPI series. `period` is `YYYY-MM` (monthly) or `YYYY-MM-DD` (daily); series are zero-filled, no gaps. Notification counters split requests by every channel used, so a request notified through multiple channels appears in each channel count. Verification counters are mutually exclusive and add up to `signature_requests`.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `period` | `string` | no | no | `YYYY-MM` (monthly) or `YYYY-MM-DD` (daily). |
| `documents_uploaded` | `integer` | no | no |  |
| `documents_sent` | `integer` | no | no |  |
| `signature_requests` | `integer` | no | no |  |
| `signature_requests_notification_email` | `integer` | no | no | Requests notified by email. |
| `signature_requests_notification_whatsapp` | `integer` | no | no | Requests notified by WhatsApp. |
| `signature_requests_notification_bypass` | `integer` | no | no | Requests with no notification sent (`Bypass`). |
| `signature_requests_verification_email` | `integer` | no | no | Requests verified by an email token. |
| `signature_requests_verification_whatsapp` | `integer` | no | no | Requests verified by a WhatsApp token. |
| `signature_requests_verification_bypass` | `integer` | no | no | Requests signed without token verification (`Bypass`). |
| `signature_requests_verification_digital_certificate` | `integer` | no | no | Requests signed with the signer's ICP-Brasil digital certificate. |
| `signature_requests_viewed` | `integer` | no | no | Signature requests whose document was first viewed during the period. |
| `signature_requests_completed` | `integer` | no | no | Signature requests completed by individual signers during the period. |
| `documents_certified` | `integer` | no | no |  |

### Schema: NotificationPreferences

Owner-facing document notifications, keyed by notification type. `true` means the e-mail is sent.

| JSON field | Type | Required | Nullable | Notes |
|---|---|---:|---:|---|
| `DocumentCompleted` | `boolean` | no | no | Every signer has signed and the document is certified. |
| `SignerDeclined` | `boolean` | no | no | A signer declined to sign. |
| `DocumentCancelled` | `boolean` | no | no | The document was cancelled. |
| `DocumentAboutToExpire` | `boolean` | no | no | The signature deadline is approaching. |
| `DocumentExpired` | `boolean` | no | no | The signature deadline passed. |
| `DocumentExpirationReset` | `boolean` | no | no | The signature deadline was extended. |
| `DocumentProcessingFailed` | `boolean` | no | no | An uploaded document could not be processed. |
| `TemplateProcessingFailed` | `boolean` | no | no | A template could not be processed. |
| `SignerWhatsappFailed` | `boolean` | no | no | A WhatsApp notification to a signer could not be delivered. |

## Java convenience APIs

- `DocumentResource: public Document get(String documentId)` aliases `details`. `public Document waitUntilReady(String documentId)`, `public Document waitUntilReady(String documentId, long maxWaitMs, long pollIntervalMs)`, `public boolean isFullySigned(String documentId)`, and `public SigningProgress getSigningProgress(String documentId)` compose documented document reads locally.
- `DocumentResource.download(String)` and `SignerResource.downloadDocument(...)` supply the `certificated` artifact default; signer-download overloads also accept an access code for deployments that require it.
- `DocumentResource.upload(byte[], String, Map<String,Object>, String)` sends multipart `file`, `name`, and optional `metadata` parts.
- `DocumentResource.appendTagIds(...)` and `replaceTagIds(...)` resolve workspace tag IDs before
  changing a document. The existing `appendTags(...)` and `replaceTags(...)` methods accept tag
  names. Returned `Tag` records carry the attached-tag IDs accepted by `detachTag(...)`.
- `AssinafyClient: public UploadAndRequestSignaturesResult uploadAndRequestSignatures(UploadAndRequestSignaturesRequest request)` composes upload, polling, signer resolution, and assignment creation. On an ordinary later-step failure, it attempts to delete the uploaded document and signer records whose create responses returned valid IDs; cleanup failures are suppressed on the original exception. Signers recovered after an indeterminate create response are not updated or deleted. A recovered entry containing CPF/CNPJ fails before assignment creation. If assignment creation has an indeterminate result and reconciliation cannot find it, the resources are retained to avoid deleting a potentially active request.
- `SignerResource.create(...)` always sends the create POST. A supplied `CreateSignerRequest.cpf` (CPF or CNPJ) is persisted through a follow-up `government_id` update; if that update fails, the new signer is deleted. `findByEmail(...)` is a list/search convenience. `findOrCreate(...)` reuses an exact case-insensitive email match unchanged and handles a concurrent duplicate-create response.
- `TemplateResource: public Template get(String templateId)` and `public Template get(String templateId, String accountId)` call the deployment extension `GET /accounts/{accountId}/templates/{templateId}`. Confirm endpoint support before using it.
- `WebhookResource: public void delete()` and `public void delete(String accountId)` call an optional DELETE subscription route and are deprecated; use `inactivate`.
- `AssignmentResource.resetExpiration(..., null)` sends `expires_at: null`; use this form only where clearing expiration is supported.
- `PublicDocumentResource.sendToken(String)` follows the optional/bodyless form, and `sendToken(String, String)` sends only `email`. The deployment-specific `sendToken(String, String, String)` sends `email`, `recipient`, and `channel` for the email channel, and `recipient` plus `channel` for other channels.
- `AssignmentResource.list(..., accountId)` adds optional `accountId` query context.
- `CreateFieldRequest` exposes optional deployment fields `is_active`, `is_read_only`, and `is_visible`. `UpdateFieldRequest` also exposes `type`, `is_required`, `is_read_only`, and `is_visible` for deployments that accept them.
- Field validation overloads can add the deployment-specific `signer-access-code` query input.
- `SignerResource.uploadSignature` accepts PNG or JPEG bytes. Use PNG unless the target tenant accepts JPEG uploads.
- `WebhookResource.get` returns `null` when no subscription exists (HTTP 404).
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
| `expiresAt` | `String` | optional | ISO-8601 assignment expiration timestamp; use whole-second UTC form such as `2026-12-31T23:59:59Z`. |
| `copyReceivers` | `List<String>` | optional | Assignment copy recipients. |
| `accountId` | `String` | client default | Explicit workspace account ID. |

`SignerEntry` fields are `name` (required), `email`, `whatsappPhoneNumber`, and `cpf`. Each entry
requires an email or WhatsApp number. Email entries reuse an exact
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

Assinafy publishes no webhook signature header, signing scheme, or shared-secret registration, so
there is nothing in a delivery a client library can verify. Authenticate deliveries at a trusted
network boundary and re-read the affected entity through the API before acting on it.

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
