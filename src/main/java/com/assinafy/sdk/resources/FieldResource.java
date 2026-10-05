package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.FieldDefinition;
import com.assinafy.sdk.models.FieldType;
import com.assinafy.sdk.models.FieldValidationResult;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.request.CreateFieldRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.UpdateFieldRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Field Definition resource — manages input field definitions used to build
 * collect-method assignments and template editor fields.
 *
 * <p>Maps to the {@code /accounts/{accountId}/fields/...} and {@code /field-types} endpoints.
 */
public class FieldResource extends BaseResource {

    /**
     * Create field operations bound to a default account and logger.
     *
     * @param http HTTP transport
     * @param defaultAccountId default account ID
     * @param logger diagnostic logger
     */
    public FieldResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    /**
     * Create field operations bound to a default account.
     *
     * @param http HTTP transport
     * @param defaultAccountId default account ID
     */
    public FieldResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    /**
     * Create a field definition ({@code POST /accounts/{accountId}/fields}). {@code type} and
     * {@code name} are required; {@code regex} and {@code is_required} are optional. Returns the
     * created {@link FieldDefinition}.
     *
     * <p>Wire contract, payloads and failures: {@link #create(CreateFieldRequest, String)}.</p>
     *
     * @param request field definition to create
     * @return the created field definition
     */
    public FieldDefinition create(CreateFieldRequest request) {
        return create(request, null);
    }

    /**
     * Create a field definition in an explicit or default account.
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/accounts/{accountId}/fields</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "name": "Full name",
     *   "type": "text",
     *   "regex": null,
     *   "is_required": false
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "field",
     *     "id": "6152120297080d55bdd13197",
     *     "name": "Signature",
     *     "type": "signature",
     *     "regex": null,
     *     "is_pre_defined": false,
     *     "is_active": false,
     *     "is_required": false,
     *     "is_standard": false,
     *     "is_read_only": false,
     *     "is_visible": false
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The created field; 400 One or more fields failed validation.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param request field definition to create
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the created field definition
     */
    public FieldDefinition create(CreateFieldRequest request, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        if (request == null || request.getType() == null || request.getType().isBlank()) {
            throw new ValidationException("Field type is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("Field name is required");
        }
        String body = serialise(request);
        return call("Failed to create field definition",
                () -> http.post("/accounts/" + id + "/fields", body),
                FieldDefinition.class);
    }

    /**
     * List field definitions for the default account.
     *
     * <p>Wire contract, payloads and failures: {@link #list(ListParams, String)}.</p>
     *
     * @return paginated field definitions
     */
    public PaginatedResult<FieldDefinition> list() {
        return list(new ListParams(), null);
    }

    /**
     * List field definitions for the default account.
     *
     * <p>Wire contract, payloads and failures: {@link #list(ListParams, String)}.</p>
     *
     * @param params paging and filter options; {@code null} sends no query parameters
     * @return paginated field definitions
     */
    public PaginatedResult<FieldDefinition> list(ListParams params) {
        return list(params, null);
    }

    /**
     * List field definitions ({@code GET /accounts/{accountId}/fields}).
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts/{accountId}/fields</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>include_inactive</code> (query, optional): Include inactive field definitions.</li>
     * <li><code>include_standard</code> (query, optional): Include standard field types (signature, initial, signatureDate).</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     {
     *       "resource": "field",
     *       "id": "6152120297080d55bdd13197",
     *       "name": "Signature",
     *       "type": "signature",
     *       "regex": null,
     *       "is_pre_defined": false,
     *       "is_active": false,
     *       "is_required": false,
     *       "is_standard": false,
     *       "is_read_only": false,
     *       "is_visible": false
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Field definitions; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param params paging and filter options; {@code null} sends no query parameters
     * @param accountId explicit account ID, or {@code null} for the default
     * @return paginated field definitions
     */
    public PaginatedResult<FieldDefinition> list(ListParams params, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list field definitions",
                () -> http.get("/accounts/" + id + "/fields", queryParams),
                FieldDefinition.class);
    }

    /**
     * Fetch a field definition by ID from the default account.
     *
     * <p>Wire contract, payloads and failures: {@link #get(String, String)}.</p>
     *
     * @param fieldId field definition ID
     * @return the field definition
     */
    public FieldDefinition get(String fieldId) {
        return get(fieldId, null);
    }

    /**
     * Fetch a field definition ({@code GET /accounts/{accountId}/fields/{fieldId}}).
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/accounts/{accountId}/fields/{fieldId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>fieldId</code> (path, required): The field ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "field",
     *     "id": "6152120297080d55bdd13197",
     *     "name": "Signature",
     *     "type": "signature",
     *     "regex": null,
     *     "is_pre_defined": false,
     *     "is_active": false,
     *     "is_required": false,
     *     "is_standard": false,
     *     "is_read_only": false,
     *     "is_visible": false
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The field; 404 The requested resource does not exist.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param fieldId field definition ID
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the field definition
     */
    public FieldDefinition get(String fieldId, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String fid = pathSegment(fieldId, "Field ID");
        return call("Failed to fetch field definition",
                () -> http.get("/accounts/" + id + "/fields/" + fid),
                FieldDefinition.class);
    }

    /**
     * Update a field definition ({@code PUT /accounts/{accountId}/fields/{fieldId}}) and return the
     * updated {@link FieldDefinition}.
     *
     * <p>Wire contract, payloads and failures: {@link #update(String, UpdateFieldRequest, String)}.</p>
     *
     * @param fieldId field definition ID
     * @param request fields to update
     * @return the updated field definition
     */
    public FieldDefinition update(String fieldId, UpdateFieldRequest request) {
        return update(fieldId, request, null);
    }

    /**
     * Update a field definition in an explicit or default account.
     *
     * <p><strong>HTTP:</strong> <code>PUT /v1/accounts/{accountId}/fields/{fieldId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>fieldId</code> (path, required): The field ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "name": "name_example",
     *   "regex": null,
     *   "is_active": false
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "field",
     *     "id": "6152120297080d55bdd13197",
     *     "name": "Signature",
     *     "type": "signature",
     *     "regex": null,
     *     "is_pre_defined": false,
     *     "is_active": false,
     *     "is_required": false,
     *     "is_standard": false,
     *     "is_read_only": false,
     *     "is_visible": false
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The updated field; 404 The requested resource does not exist.;
     * 401 Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param fieldId field definition ID
     * @param request fields to update
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the updated field definition
     */
    public FieldDefinition update(String fieldId, UpdateFieldRequest request, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String fid = pathSegment(fieldId, "Field ID");
        if (request == null) throw new ValidationException("Field update is required");
        String body = serialise(request);
        return call("Failed to update field definition",
                () -> http.put("/accounts/" + id + "/fields/" + fid, body),
                FieldDefinition.class);
    }

    /**
     * Delete a field definition from the default account.
     *
     * <p>Wire contract, payloads and failures: {@link #delete(String, String)}.</p>
     *
     * @param fieldId field definition ID
     */
    public void delete(String fieldId) {
        delete(fieldId, null);
    }

    /**
     * Delete a field definition ({@code DELETE /accounts/{accountId}/fields/{fieldId}}).
     *
     * <p><strong>HTTP:</strong> <code>DELETE /v1/accounts/{accountId}/fields/{fieldId}</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>fieldId</code> (path, required): The field ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     []
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Field deleted; 404 The requested resource does not exist.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param fieldId field definition ID
     * @param accountId explicit account ID, or {@code null} for the default
     */
    public void delete(String fieldId, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String fid = pathSegment(fieldId, "Field ID");
        callVoid("Failed to delete field definition",
                () -> http.delete("/accounts/" + id + "/fields/" + fid));
    }

    /**
     * Validate a single value against a field definition. Authenticated callers may omit
     * {@code signerAccessCode}; signer self-service callers must supply it.
     *
     * <p>Wire contract, payloads and failures: {@link #validate(String, Object, String, String)}.</p>
     *
     * @param fieldId field definition ID
     * @param value value to validate; may be {@code null}
     * @param signerAccessCode signer query credential, or {@code null} for normal authentication
     * @return validation result
     */
    public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode) {
        return validate(fieldId, value, signerAccessCode, null);
    }

    /**
     * Validate one value in an explicit or default account.
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/accounts/{accountId}/fields/{fieldId}/validate</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * <li><code>fieldId</code> (path, required): The field ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "value": "400.676.228-36"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "type": "cpf",
     *     "success": true,
     *     "error_message": ""
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Validation result; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param fieldId field definition ID
     * @param value value to validate; may be {@code null}
     * @param signerAccessCode signer query credential, or {@code null} for normal authentication
     * @param accountId explicit account ID, or {@code null} for the default
     * @return validation result
     */
    public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String fid = pathSegment(fieldId, "Field ID");
        Map<String, Object> payload = new HashMap<>();
        payload.put("value", value);
        String body = serialise(payload);
        String path = "/accounts/" + id + "/fields/" + fid + "/validate";
        if (signerAccessCode != null && !signerAccessCode.isBlank()) {
            path = withAccessCode(path, signerAccessCode);
        }
        String finalPath = path;
        return call("Failed to validate field",
                () -> http.post(finalPath, body),
                FieldValidationResult.class);
    }

    /**
     * Validate multiple values in one round-trip. {@code entries} is the list of
     * {@code {field_id, value}} objects to validate.
     *
     * <p>Wire contract, payloads and failures: {@link #validateMultiple(List, String, String)}.</p>
     *
     * @param entries field IDs and values; {@code null} sends an empty array
     * @param signerAccessCode signer query credential, or {@code null} for normal authentication
     * @return one result per submitted field
     */
    public List<FieldValidationResult> validateMultiple(List<Map<String, Object>> entries, String signerAccessCode) {
        return validateMultiple(entries, signerAccessCode, null);
    }

    /**
     * Validate multiple values in an explicit or default account.
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/accounts/{accountId}/fields/validate-multiple</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>accountId</code> (path, required): Workspace account ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>[
     *   {
     *     "field_id": "63488ffb7adf435aba319787",
     *     "value": "1111111111111"
     *   }
     * ]</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     {
     *       "field_id": "63488ffb7adf435aba319787",
     *       "type": "cpf",
     *       "success": false,
     *       "error_message": "Invalid CPF."
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Validation results; 401 Missing or invalid credentials.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param entries field IDs and values; {@code null} sends an empty array
     * @param signerAccessCode signer query credential, or {@code null} for normal authentication
     * @param accountId explicit account ID, or {@code null} for the default
     * @return one result per submitted field
     */
    public List<FieldValidationResult> validateMultiple(List<Map<String, Object>> entries, String signerAccessCode, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        String body = serialise(entries != null ? entries : List.of());
        String path = "/accounts/" + id + "/fields/validate-multiple";
        if (signerAccessCode != null && !signerAccessCode.isBlank()) {
            path = withAccessCode(path, signerAccessCode);
        }
        String finalPath = path;
        return callList("Failed to validate fields",
                () -> http.post(finalPath, body),
                FieldValidationResult.class).getData();
    }

    /**
     * List supported input types ({@code /field-types}). Workspace-independent.
     *
     * <p><strong>HTTP:</strong> <code>GET /v1/field-types</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": [
     *     {
     *       "type": "cpf",
     *       "name": "CPF"
     *     }
     *   ]
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Field types; 401 Missing or invalid credentials.; 500 Unexpected
     * server error. Non-2xx HTTP or numeric envelope statuses raise <code>ApiException</code>; 401/403
     * raise <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O
     * failures raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * @return supported field types
     */
    public List<FieldType> listTypes() {
        return callList("Failed to list field types",
                () -> http.get("/field-types"),
                FieldType.class).getData();
    }
}
