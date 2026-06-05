package com.assinafy.sdk.exceptions;

import java.util.Map;

public class ApiException extends AssinafyException {

    private final int statusCode;
    private final Object responseData;

    public ApiException(String message, int statusCode, Object responseData) {
        // statusCode and responseData are the typed source of truth (getStatusCode/getResponseData);
        // the context map only carries the HTTP status for generic AssinafyException consumers.
        super(message, Map.of("statusCode", statusCode));
        this.statusCode = statusCode;
        this.responseData = responseData;
    }

    public ApiException(String message, int statusCode, Object responseData, Throwable cause) {
        super(message, Map.of("statusCode", statusCode), cause);
        this.statusCode = statusCode;
        this.responseData = responseData;
    }

    /**
     * Build the most specific {@link ApiException} subtype for the HTTP status:
     * {@link AuthenticationException} for 401/403, {@link RateLimitException} for 429,
     * otherwise a plain {@code ApiException}. The message is taken from the response
     * envelope's {@code message}/{@code error} field when present.
     */
    public static ApiException fromResponse(int statusCode, Object responseData) {
        String message = extractMessage(responseData, statusCode);
        return switch (statusCode) {
            case 401, 403 -> new AuthenticationException(message, statusCode, responseData);
            case 429 -> new RateLimitException(message, statusCode, responseData);
            default -> new ApiException(message, statusCode, responseData);
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

    public int getStatusCode() {
        return statusCode;
    }

    public Object getResponseData() {
        return responseData;
    }
}
