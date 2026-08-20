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
     * @param request field definition to create
     * @return the created field definition
     */
    public FieldDefinition create(CreateFieldRequest request) {
        return create(request, null);
    }

    /**
     * Create a field definition in an explicit or default account.
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
     * @return paginated field definitions
     */
    public PaginatedResult<FieldDefinition> list() {
        return list(new ListParams(), null);
    }

    /**
     * List field definitions for the default account.
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
     * @param fieldId field definition ID
     * @return the field definition
     */
    public FieldDefinition get(String fieldId) {
        return get(fieldId, null);
    }

    /**
     * Fetch a field definition ({@code GET /accounts/{accountId}/fields/{fieldId}}).
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
     * @param fieldId field definition ID
     */
    public void delete(String fieldId) {
        delete(fieldId, null);
    }

    /**
     * Delete a field definition ({@code DELETE /accounts/{accountId}/fields/{fieldId}}).
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
     * @return supported field types
     */
    public List<FieldType> listTypes() {
        return callList("Failed to list field types",
                () -> http.get("/field-types"),
                FieldType.class).getData();
    }
}
