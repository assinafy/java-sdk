package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.NoOpLogger;
import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.http.HttpRawResponse;
import com.assinafy.sdk.http.ThrowingSupplier;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.util.ResponseHandler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class BaseResource {

    protected final ApiHttpClient http;
    protected final String defaultAccountId;
    protected final Logger logger;

    protected BaseResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        this.http = http;
        this.defaultAccountId = defaultAccountId;
        this.logger = logger != null ? logger : NoOpLogger.INSTANCE;
    }

    protected BaseResource(ApiHttpClient http, String defaultAccountId) {
        this(http, defaultAccountId, NoOpLogger.INSTANCE);
    }

    protected BaseResource(ApiHttpClient http) {
        this(http, null, NoOpLogger.INSTANCE);
    }

    protected String accountId(String explicit) {
        String id = explicit != null ? explicit : defaultAccountId;
        if (id == null || id.isBlank()) {
            throw new ValidationException(
                    "Account ID is required. Provide it as a parameter or set a default in the client."
            );
        }
        return id;
    }

    protected String requireId(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(name + " is required");
        }
        return value;
    }

    /**
     * Run {@code op} and translate any failure into the SDK exception hierarchy: existing
     * {@link AssinafyException}s pass through unchanged; anything else is mapped by
     * {@link ResponseHandler#toSdkException}. All the {@code call*} wrappers share this policy.
     */
    private <R> R execute(String label, ThrowingSupplier<R> op) {
        try {
            return op.get();
        } catch (AssinafyException e) {
            throw e;
        } catch (Exception e) {
            throw ResponseHandler.toSdkException(e, label);
        }
    }

    protected <T> T call(String label, ThrowingSupplier<HttpRawResponse> request, Class<T> type) {
        return execute(label, () -> ResponseHandler.handle(request.get(), type));
    }

    protected <T> T callOptional(String label, ThrowingSupplier<HttpRawResponse> request, Class<T> type) {
        try {
            return call(label, request, type);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) return null;
            throw e;
        }
    }

    protected void callVoid(String label, ThrowingSupplier<HttpRawResponse> request) {
        execute(label, () -> {
            ResponseHandler.handleVoid(request.get());
            return null;
        });
    }

    protected byte[] callBinary(String label, ThrowingSupplier<byte[]> request) {
        return execute(label, request);
    }

    protected <T> PaginatedResult<T> callList(String label, ThrowingSupplier<HttpRawResponse> request, Class<T> elementType) {
        return execute(label, () -> ResponseHandler.handleList(request.get(), elementType));
    }

    protected Map<String, Object> callMap(String label, ThrowingSupplier<HttpRawResponse> request) {
        return execute(label, () -> ResponseHandler.handleMap(request.get()));
    }

    protected String serialise(Object obj) {
        try {
            return ResponseHandler.MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new AssinafyException("Failed to serialise request: " + e.getMessage(), Map.of(), e);
        }
    }

    /**
     * Convert a request DTO into a mutable wire map using the DTO's own Jackson annotations
     * ({@code @JsonProperty} names and {@code @JsonInclude(NON_NULL)}), so callers can apply a
     * small post-transform without restating the field names. Returns an empty map for {@code null}.
     */
    protected Map<String, Object> toMap(Object dto) {
        if (dto == null) return new java.util.HashMap<>();
        return ResponseHandler.MAPPER.convertValue(dto, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
    }

    protected static String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }

    /** Append a URL-encoded {@code signer-access-code} query parameter, choosing {@code ?} or {@code &}. */
    protected static String withAccessCode(String path, String signerAccessCode) {
        String sep = path.indexOf('?') >= 0 ? "&" : "?";
        return path + sep + "signer-access-code=" + encode(signerAccessCode);
    }
}
