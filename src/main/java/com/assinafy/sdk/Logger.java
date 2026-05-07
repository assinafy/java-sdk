package com.assinafy.sdk;

import java.util.Map;

public interface Logger {

    void debug(String message, Map<String, Object> context);

    void info(String message, Map<String, Object> context);

    void warn(String message, Map<String, Object> context);

    void error(String message, Map<String, Object> context);

    default void debug(String message) {
        debug(message, Map.of());
    }

    default void info(String message) {
        info(message, Map.of());
    }

    default void warn(String message) {
        warn(message, Map.of());
    }

    default void error(String message) {
        error(message, Map.of());
    }
}
