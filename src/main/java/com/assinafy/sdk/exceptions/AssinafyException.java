package com.assinafy.sdk.exceptions;

import java.util.Collections;
import java.util.Map;

public class AssinafyException extends RuntimeException {

    private final Map<String, Object> context;

    public AssinafyException(String message) {
        super(message);
        this.context = Collections.emptyMap();
    }

    public AssinafyException(String message, Map<String, Object> context) {
        super(message);
        this.context = context != null ? Collections.unmodifiableMap(context) : Collections.emptyMap();
    }

    public AssinafyException(String message, Map<String, Object> context, Throwable cause) {
        super(message, cause);
        this.context = context != null ? Collections.unmodifiableMap(context) : Collections.emptyMap();
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
