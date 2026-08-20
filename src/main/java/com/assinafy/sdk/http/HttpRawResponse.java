package com.assinafy.sdk.http;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Immutable HTTP status, text body, and headers returned by {@link ApiHttpClient}. */
public final class HttpRawResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    /**
     * Create a raw response snapshot.
     *
     * @param statusCode HTTP status code
     * @param body response text, or {@code null} when the response has no body
     * @param headers response headers; names are normalized for case-insensitive lookup
     */
    public HttpRawResponse(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        if (headers == null || headers.isEmpty()) {
            this.headers = Collections.emptyMap();
        } else {
            Map<String, String> copy = new LinkedHashMap<>();
            headers.forEach((name, value) -> copy.put(name.toLowerCase(Locale.ROOT), value));
            this.headers = Collections.unmodifiableMap(copy);
        }
    }

    /** {@return the HTTP status code} */
    public int getStatusCode() {
        return statusCode;
    }

    /** {@return the response text, or {@code null} when absent} */
    public String getBody() {
        return body;
    }

    /** {@return immutable response headers with lowercase names} */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Return one response header using a case-insensitive name.
     *
     * @param name header name; {@code null} returns {@code null}
     * @return the header value, or {@code null} when absent
     */
    public String getHeader(String name) {
        if (name == null) return null;
        return headers.get(name.toLowerCase(Locale.ROOT));
    }
}
