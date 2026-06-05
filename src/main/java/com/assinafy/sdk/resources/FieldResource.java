package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
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

    public FieldResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    public FieldResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    public FieldDefinition create(CreateFieldRequest request) {
        return create(request, null);
    }

    public FieldDefinition create(CreateFieldRequest request, String accountId) {
        String id = accountId(accountId);
        if (request == null || request.getType() == null || request.getType().isBlank()) {
            throw new com.assinafy.sdk.exceptions.ValidationException("Field type is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new com.assinafy.sdk.exceptions.ValidationException("Field name is required");
        }
        String body = serialise(request);
        return call("Failed to create field definition",
                () -> http.post("/accounts/" + id + "/fields", body),
                FieldDefinition.class);
    }

    public PaginatedResult<FieldDefinition> list() {
        return list(new ListParams(), null);
    }

    public PaginatedResult<FieldDefinition> list(ListParams params) {
        return list(params, null);
    }

    public PaginatedResult<FieldDefinition> list(ListParams params, String accountId) {
        String id = accountId(accountId);
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list field definitions",
                () -> http.get("/accounts/" + id + "/fields", queryParams),
                FieldDefinition.class);
    }

    public FieldDefinition get(String fieldId) {
        return get(fieldId, null);
    }

    public FieldDefinition get(String fieldId, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        return call("Failed to fetch field definition",
                () -> http.get("/accounts/" + id + "/fields/" + fid),
                FieldDefinition.class);
    }

    public FieldDefinition update(String fieldId, UpdateFieldRequest request) {
        return update(fieldId, request, null);
    }

    public FieldDefinition update(String fieldId, UpdateFieldRequest request, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        String body = serialise(request);
        return call("Failed to update field definition",
                () -> http.put("/accounts/" + id + "/fields/" + fid, body),
                FieldDefinition.class);
    }

    public void delete(String fieldId) {
        delete(fieldId, null);
    }

    public void delete(String fieldId, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        callVoid("Failed to delete field definition",
                () -> http.delete("/accounts/" + id + "/fields/" + fid));
    }

    /**
     * Validate a single value against a field definition. Authenticated callers may omit
     * {@code signerAccessCode}; signer self-service callers must supply it.
     */
    public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode) {
        return validate(fieldId, value, signerAccessCode, null);
    }

    public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        Map<String, Object> payload = new HashMap<>();
        payload.put("value", value);
        String body = serialise(payload);
        String path = "/accounts/" + id + "/fields/" + fid + "/validate";
        if (signerAccessCode != null && !signerAccessCode.isBlank()) {
            path = path + "?signer-access-code=" + encode(signerAccessCode);
        }
        String finalPath = path;
        return call("Failed to validate field",
                () -> http.post(finalPath, body),
                FieldValidationResult.class);
    }

    /**
     * Validate multiple values in one round-trip. {@code entries} is the list of
     * {@code {field_id, value}} objects to validate.
     */
    public List<FieldValidationResult> validateMultiple(List<Map<String, Object>> entries, String signerAccessCode) {
        return validateMultiple(entries, signerAccessCode, null);
    }

    public List<FieldValidationResult> validateMultiple(List<Map<String, Object>> entries, String signerAccessCode, String accountId) {
        String id = accountId(accountId);
        String body = serialise(entries != null ? entries : List.of());
        String path = "/accounts/" + id + "/fields/validate-multiple";
        if (signerAccessCode != null && !signerAccessCode.isBlank()) {
            path = path + "?signer-access-code=" + encode(signerAccessCode);
        }
        String finalPath = path;
        return callList("Failed to validate fields",
                () -> http.post(finalPath, body),
                FieldValidationResult.class).getData();
    }

    /**
     * List supported input types ({@code /field-types}). Workspace-independent.
     */
    public List<FieldType> listTypes() {
        return callList("Failed to list field types",
                () -> http.get("/field-types"),
                FieldType.class).getData();
    }
}
