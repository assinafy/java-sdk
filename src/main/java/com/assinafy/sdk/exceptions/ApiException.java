package com.assinafy.sdk.exceptions;

import java.util.Map;

public class ApiException extends AssinafyException {

    private final int statusCode;
    private final Object responseData;

    public ApiException(String message, int statusCode, Object responseData) {
        super(message, Map.of("statusCode", statusCode, "responseData", responseData != null ? responseData : ""));
        this.statusCode = statusCode;
        this.responseData = responseData;
    }

    public ApiException(String message, int statusCode, Object responseData, Throwable cause) {
        super(message, Map.of("statusCode", statusCode, "responseData", responseData != null ? responseData : ""), cause);
        this.statusCode = statusCode;
        this.responseData = responseData;
    }

    public static ApiException fromResponse(int statusCode, Object responseData) {
        String message = extractMessage(responseData, statusCode);
        return new ApiException(message, statusCode, responseData);
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
