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
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/** Shared validation, serialization, account resolution, and response handling for API resources. */
public abstract class BaseResource {

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /** HTTP transport used by this resource. */
    protected final ApiHttpClient http;
    /** Default account used when an operation does not receive an explicit account. */
    protected final String defaultAccountId;
    /** Diagnostic logger. */
    protected final Logger logger;

    /**
     * Initialize shared resource dependencies.
     *
     * @param http HTTP transport
     * @param defaultAccountId optional default account ID
     * @param logger diagnostic logger; {@code null} selects the no-op logger
     * @throws NullPointerException if {@code http} is {@code null}
     */
    protected BaseResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        this.http = Objects.requireNonNull(http, "http");
        this.defaultAccountId = defaultAccountId;
        this.logger = logger != null ? logger : NoOpLogger.INSTANCE;
    }

    /**
     * Initialize a resource with the no-op logger.
     *
     * @param http HTTP transport
     * @param defaultAccountId optional default account ID
     * @throws NullPointerException if {@code http} is {@code null}
     */
    protected BaseResource(ApiHttpClient http, String defaultAccountId) {
        this(http, defaultAccountId, NoOpLogger.INSTANCE);
    }

    /**
     * Initialize an account-independent resource with the no-op logger.
     *
     * @param http HTTP transport
     * @throws NullPointerException if {@code http} is {@code null}
     */
    protected BaseResource(ApiHttpClient http) {
        this(http, null, NoOpLogger.INSTANCE);
    }

    /**
     * Resolve an explicit account ID, falling back to this resource's default.
     *
     * @param explicit optional explicit account ID
     * @return the resolved nonblank account ID
     * @throws ValidationException if neither value supplies an account ID
     */
    protected String accountId(String explicit) {
        String id = explicit != null ? explicit : defaultAccountId;
        if (id == null || id.isBlank()) {
            throw new ValidationException(
                    "Account ID is required. Provide it as a parameter or set a default in the client."
            );
        }
        return id;
    }

    /**
     * Require a nonblank identifier or token.
     *
     * @param value value to validate
     * @param name field name used in validation errors
     * @return the validated value
     * @throws ValidationException if the value is null or blank
     */
    protected String requireId(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(name + " is required");
        }
        return value;
    }

    /**
     * Require a syntactically valid email address.
     *
     * @param value email address to validate
     * @return the validated address
     * @throws ValidationException if the address is invalid
     */
    public static String requireEmail(String value) {
        if (value == null || !EMAIL.matcher(value).matches()) {
            throw new ValidationException("Invalid email address");
        }
        return value;
    }

    /**
     * Validate and percent-encode one dynamic URL path segment without allowing dot traversal.
     *
     * @param value path segment value
     * @param name field name used in validation errors
     * @return the encoded path segment
     * @throws ValidationException if the value is blank or a dot-traversal segment
     */
    protected String pathSegment(String value, String name) {
        String segment = requireId(value, name);
        if (segment.equals(".") || segment.equals("..")) {
            throw new ValidationException(name + " is invalid");
        }
        byte[] bytes = segment.getBytes(StandardCharsets.UTF_8);
        StringBuilder encoded = new StringBuilder(bytes.length);
        for (byte item : bytes) {
            int b = item & 0xff;
            if ((b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z') || (b >= '0' && b <= '9')
                    || b == '-' || b == '_' || b == '~') {
                encoded.append((char) b);
            } else {
                encoded.append('%').append(HEX[b >>> 4]).append(HEX[b & 0x0f]);
            }
        }
        return encoded.toString();
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

    /**
     * Execute and decode a typed API request.
     *
     * @param <T> decoded response type
     * @param label operation label for wrapped failures
     * @param request HTTP operation
     * @param type target response class
     * @return decoded response data
     * @throws AssinafyException if the request fails or cannot be decoded
     */
    protected <T> T call(String label, ThrowingSupplier<HttpRawResponse> request, Class<T> type) {
        return execute(label, () -> ResponseHandler.handle(request.get(), type));
    }

    /**
     * Execute a typed request whose HTTP 404 response represents absence.
     *
     * @param <T> decoded response type
     * @param label operation label for wrapped failures
     * @param request HTTP operation
     * @param type target response class
     * @return decoded response data, or {@code null} for HTTP 404
     * @throws AssinafyException if the request otherwise fails or cannot be decoded
     */
    protected <T> T callOptional(String label, ThrowingSupplier<HttpRawResponse> request, Class<T> type) {
        try {
            return call(label, request, type);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) return null;
            throw e;
        }
    }

    /**
     * Execute and validate an API request that has no return value.
     *
     * @param label operation label for wrapped failures
     * @param request HTTP operation
     * @throws AssinafyException if the request or response validation fails
     */
    protected void callVoid(String label, ThrowingSupplier<HttpRawResponse> request) {
        execute(label, () -> {
            ResponseHandler.handleVoid(request.get());
            return null;
        });
    }

    /**
     * Execute a binary download.
     *
     * @param label operation label for wrapped failures
     * @param request binary HTTP operation
     * @return downloaded bytes
     * @throws AssinafyException if the request fails
     */
    protected byte[] callBinary(String label, ThrowingSupplier<byte[]> request) {
        return execute(label, request);
    }

    /**
     * Execute and decode a paginated list request.
     *
     * @param <T> list element type
     * @param label operation label for wrapped failures
     * @param request HTTP operation
     * @param elementType target element class
     * @return decoded list and pagination metadata
     * @throws AssinafyException if the request fails or cannot be decoded
     */
    protected <T> PaginatedResult<T> callList(String label, ThrowingSupplier<HttpRawResponse> request, Class<T> elementType) {
        return execute(label, () -> ResponseHandler.handleList(request.get(), elementType));
    }

    /**
     * Execute and decode a response as a map.
     *
     * @param label operation label for wrapped failures
     * @param request HTTP operation
     * @return decoded response map
     * @throws AssinafyException if the request fails or cannot be decoded
     */
    protected Map<String, Object> callMap(String label, ThrowingSupplier<HttpRawResponse> request) {
        return execute(label, () -> ResponseHandler.handleMap(request.get()));
    }

    /**
     * Log diagnostic detail without allowing a logger failure to affect an API operation.
     *
     * @param message event message
     * @param context structured event fields
     */
    protected void logDebug(String message, Map<String, Object> context) {
        try {
            logger.debug(message, context);
        } catch (RuntimeException ignored) {
            // Logging is diagnostic only.
        }
    }

    /**
     * Log operational information without allowing a logger failure to affect an API operation.
     *
     * @param message event message
     * @param context structured event fields
     */
    protected void logInfo(String message, Map<String, Object> context) {
        try {
            logger.info(message, context);
        } catch (RuntimeException ignored) {
            // Logging is diagnostic only.
        }
    }

    /**
     * Log a warning without allowing a logger failure to affect an API operation.
     *
     * @param message event message
     * @param context structured event fields
     */
    protected void logWarn(String message, Map<String, Object> context) {
        try {
            logger.warn(message, context);
        } catch (RuntimeException ignored) {
            // Logging is diagnostic only.
        }
    }

    /**
     * Serialize a request value to JSON.
     *
     * @param obj request value
     * @return serialized JSON
     * @throws AssinafyException if serialization fails
     */
    protected String serialise(Object obj) {
        try {
            return ResponseHandler.serialize(obj);
        } catch (Exception e) {
            throw new AssinafyException("Failed to serialise request: " + e.getMessage(), Map.of(), e);
        }
    }

    /**
     * Convert a request DTO into a mutable wire map using the DTO's own Jackson annotations
     * ({@code @JsonProperty} names and {@code @JsonInclude(NON_NULL)}), so callers can apply a
     * small post-transform without restating the field names. Returns an empty map for {@code null}.
     *
     * @param dto request DTO, or {@code null}
     * @return a mutable wire-field map
     */
    protected Map<String, Object> toMap(Object dto) {
        if (dto == null) return new java.util.HashMap<>();
        return ResponseHandler.convertToMap(dto);
    }

    /**
     * URL-encode a query value as UTF-8 form data.
     *
     * @param value query value; {@code null} becomes empty
     * @return encoded value
     */
    protected static String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }

    /**
     * Append a URL-encoded signer access-code query parameter.
     *
     * @param path request path, optionally with existing query parameters
     * @param signerAccessCode signer access code
     * @return the path with {@code signer-access-code} appended
     */
    protected static String withAccessCode(String path, String signerAccessCode) {
        String sep = path.indexOf('?') >= 0 ? "&" : "?";
        return path + sep + "signer-access-code=" + encode(signerAccessCode);
    }

    /**
     * Build the query shared by account and user document-statistics endpoints.
     *
     * @param granularity {@code monthly}, {@code daily}, or {@code null} for monthly
     * @param month optional month in {@code YYYY-MM} format; required for daily data
     * @return validated query parameters
     * @throws ValidationException if granularity or month is invalid
     */
    protected static Map<String, Object> statsQuery(String granularity, String month) {
        String value = granularity != null ? granularity : "monthly";
        if (!value.equals("monthly") && !value.equals("daily")) {
            throw new ValidationException("Granularity must be monthly or daily");
        }
        if (value.equals("daily") && (month == null || month.isBlank())) {
            throw new ValidationException("Daily statistics require a month in YYYY-MM format");
        }
        if (month != null) {
            try {
                if (!month.matches("\\d{4}-\\d{2}")) throw new DateTimeParseException("", month, 0);
                YearMonth.parse(month);
            } catch (DateTimeParseException e) {
                throw new ValidationException("Month must be in YYYY-MM format");
            }
        }
        Map<String, Object> query = new HashMap<>();
        query.put("granularity", value);
        if (month != null) query.put("month", month);
        return query;
    }
}
