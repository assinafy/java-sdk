package com.assinafy.sdk.exceptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Indicates an HTTP or response-envelope error returned by the Assinafy API.
 *
 * <p>Response collections and headers are exposed as immutable snapshots. Header lookup is
 * case-insensitive.
 *
 * <p>The API rate-limits requests per minute and reports the quota on every limited response with
 * {@code X-Rate-Limit-Limit}, {@code X-Rate-Limit-Remaining} and {@code X-Rate-Limit-Reset}; a
 * {@code 429 Too Many Requests} also carries {@code Retry-After}. {@link #getRateLimitLimit()},
 * {@link #getRateLimitRemaining()}, {@link #getRateLimitReset()} and {@link #getRetryAfter()}
 * return those values.
 */
public class ApiException extends AssinafyException {

    private static final long serialVersionUID = 1L;

    /** API status code. */
    private final int statusCode;
    /** Decoded response data. */
    @SuppressWarnings("serial") // Decoded API data may contain caller-defined value types.
    private final Object responseData;
    /** Normalized response headers. */
    @SuppressWarnings("serial") // This immutable view is diagnostic state, not a wire format.
    private final Map<String, String> responseHeaders;

    /**
     * Create an API exception without response headers or an underlying cause.
     *
     * @param message error message
     * @param statusCode HTTP or response-envelope status code
     * @param responseData decoded response body or envelope
     */
    public ApiException(String message, int statusCode, Object responseData) {
        this(message, statusCode, responseData, Map.of(), null);
    }

    /**
     * Create an API exception with an underlying cause.
     *
     * @param message error message
     * @param statusCode HTTP or response-envelope status code
     * @param responseData decoded response body or envelope
     * @param cause underlying failure
     */
    public ApiException(String message, int statusCode, Object responseData, Throwable cause) {
        this(message, statusCode, responseData, Map.of(), cause);
    }

    /**
     * Create an API exception with response headers.
     *
     * @param message error message
     * @param statusCode HTTP or response-envelope status code
     * @param responseData decoded response body or envelope
     * @param headers response headers; names are normalized for case-insensitive lookup
     */
    public ApiException(String message, int statusCode, Object responseData, Map<String, String> headers) {
        this(message, statusCode, responseData, headers, null);
    }

    /**
     * Create an API exception with response headers and an underlying cause.
     *
     * @param message error message
     * @param statusCode HTTP or response-envelope status code
     * @param responseData decoded response body or envelope
     * @param headers response headers; names are normalized for case-insensitive lookup
     * @param cause underlying failure
     */
    public ApiException(String message, int statusCode, Object responseData,
                        Map<String, String> headers, Throwable cause) {
        super(message, Map.of("statusCode", statusCode), cause);
        this.statusCode = statusCode;
        this.responseData = snapshot(responseData);
        Map<String, String> copy = new LinkedHashMap<>();
        if (headers != null) {
            headers.forEach((name, value) -> copy.put(name.toLowerCase(Locale.ROOT), value));
        }
        this.responseHeaders = Collections.unmodifiableMap(copy);
    }

    /**
     * Build the most specific {@link ApiException} subtype for the HTTP status:
     * {@link AuthenticationException} for 401/403, {@link RateLimitException} for 429,
     * otherwise a plain {@code ApiException}. The message is taken from the response
     * envelope's {@code message}/{@code error} field when present.
     *
     * @param statusCode HTTP or response-envelope status code
     * @param responseData decoded response body or envelope
     * @return the status-specific API exception
     */
    public static ApiException fromResponse(int statusCode, Object responseData) {
        return fromResponse(statusCode, responseData, Map.of());
    }

    /**
     * Build the most specific API exception and retain response headers.
     *
     * @param statusCode HTTP or response-envelope status code
     * @param responseData decoded response body or envelope
     * @param headers response headers
     * @return the status-specific API exception
     */
    public static ApiException fromResponse(int statusCode, Object responseData,
                                            Map<String, String> headers) {
        String message = extractMessage(responseData, statusCode);
        return switch (statusCode) {
            case 401, 403 -> new AuthenticationException(message, statusCode, responseData, headers);
            case 429 -> new RateLimitException(message, statusCode, responseData, headers);
            default -> new ApiException(message, statusCode, responseData, headers);
        };
    }

    private static String extractMessage(Object responseData, int statusCode) {
        if (responseData instanceof Map<?, ?> map) {
            Object msg = map.get("message");
            if (msg instanceof String s && !s.isBlank()) {
                return s;
            }
            Object err = map.get("error");
            if (err instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        if (responseData instanceof String s && !s.isBlank()) {
            return s;
        }
        return "API request failed with status " + statusCode;
    }

    /** {@return the HTTP or response-envelope status code} */
    public int getStatusCode() {
        return statusCode;
    }

    /** {@return an immutable shallow snapshot of decoded collection data, or the decoded scalar} */
    public Object getResponseData() {
        return responseData;
    }

    /** {@return immutable response headers with lowercase names} */
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Return one response header using a case-insensitive name.
     *
     * @param name header name; {@code null} returns {@code null}
     * @return the header value, or {@code null} when absent
     */
    public String getResponseHeader(String name) {
        return name != null ? responseHeaders.get(name.toLowerCase(Locale.ROOT)) : null;
    }

    /** {@return {@code X-Rate-Limit-Limit}, the requests allowed in the current window, when sent} */
    public OptionalInt getRateLimitLimit() {
        return intHeader("x-rate-limit-limit");
    }

    /** {@return {@code X-Rate-Limit-Remaining}, the requests left in the current window, when sent} */
    public OptionalInt getRateLimitRemaining() {
        return intHeader("x-rate-limit-remaining");
    }

    /** {@return {@code X-Rate-Limit-Reset}, the seconds until the window resets, when sent} */
    public OptionalInt getRateLimitReset() {
        return intHeader("x-rate-limit-reset");
    }

    /**
     * Return the {@code Retry-After} delay a {@code 429 Too Many Requests} response carries.
     *
     * @return the seconds to wait before retrying, or empty when the header is absent or is not a
     *         number of seconds
     */
    public OptionalInt getRetryAfter() {
        return intHeader("retry-after");
    }

    private OptionalInt intHeader(String name) {
        String value = responseHeaders.get(name);
        if (value == null) return OptionalInt.empty();
        try {
            return OptionalInt.of(Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    private static Object snapshot(Object value) {
        if (value instanceof Map<?, ?> map) {
            return Collections.unmodifiableMap(new LinkedHashMap<>(map));
        }
        if (value instanceof List<?> list) {
            return Collections.unmodifiableList(new ArrayList<>(list));
        }
        return value;
    }
}
