package com.assinafy.sdk.http;

import java.util.Collections;
import java.util.Map;

public final class HttpRawResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    public HttpRawResponse(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers != null ? Collections.unmodifiableMap(headers) : Collections.emptyMap();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        if (name == null) return null;
        String value = headers.get(name.toLowerCase());
        if (value != null) return value;
        return headers.get(name);
    }
}
