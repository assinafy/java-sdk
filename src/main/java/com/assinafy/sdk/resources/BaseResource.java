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

    protected <T> T call(String label, ThrowingSupplier<HttpRawResponse> request, Class<T> type) {
        try {
            HttpRawResponse response = request.get();
            return ResponseHandler.handle(response, type);
        } catch (AssinafyException e) {
            throw e;
        } catch (Exception e) {
            throw ResponseHandler.toSdkException(e, label);
        }
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
        try {
            HttpRawResponse response = request.get();
            ResponseHandler.handleVoid(response);
        } catch (AssinafyException e) {
            throw e;
        } catch (Exception e) {
            throw ResponseHandler.toSdkException(e, label);
        }
    }

    protected byte[] callBinary(String label, ThrowingSupplier<byte[]> request) {
        try {
            return request.get();
        } catch (AssinafyException e) {
            throw e;
        } catch (Exception e) {
            throw ResponseHandler.toSdkException(e, label);
        }
    }

    protected <T> PaginatedResult<T> callList(String label, ThrowingSupplier<HttpRawResponse> request, Class<T> elementType) {
        try {
            HttpRawResponse response = request.get();
            return ResponseHandler.handleList(response, elementType);
        } catch (AssinafyException e) {
            throw e;
        } catch (Exception e) {
            throw ResponseHandler.toSdkException(e, label);
        }
    }

    protected Map<String, Object> callMap(String label, ThrowingSupplier<HttpRawResponse> request) {
        try {
            HttpRawResponse response = request.get();
            return ResponseHandler.handleMap(response);
        } catch (AssinafyException e) {
            throw e;
        } catch (Exception e) {
            throw ResponseHandler.toSdkException(e, label);
        }
    }

    protected String serialise(Object obj) {
        try {
            return ResponseHandler.MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialise request", e);
        }
    }

    protected static String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }
}
