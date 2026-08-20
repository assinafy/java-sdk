package com.assinafy.sdk;

import java.util.Map;

/** Receives SDK diagnostic events without prescribing a logging framework. */
public interface Logger {

    /**
     * Log diagnostic detail.
     *
     * @param message event message
     * @param context structured event fields
     */
    void debug(String message, Map<String, Object> context);

    /**
     * Log normal operational information.
     *
     * @param message event message
     * @param context structured event fields
     */
    void info(String message, Map<String, Object> context);

    /**
     * Log a recoverable or unexpected condition.
     *
     * @param message event message
     * @param context structured event fields
     */
    void warn(String message, Map<String, Object> context);

    /**
     * Log a failed operation.
     *
     * @param message event message
     * @param context structured event fields
     */
    void error(String message, Map<String, Object> context);

    /**
     * Log diagnostic detail without structured context.
     *
     * @param message diagnostic event message
     */
    default void debug(String message) {
        debug(message, Map.of());
    }

    /**
     * Log operational information without structured context.
     *
     * @param message informational event message
     */
    default void info(String message) {
        info(message, Map.of());
    }

    /**
     * Log a warning without structured context.
     *
     * @param message warning event message
     */
    default void warn(String message) {
        warn(message, Map.of());
    }

    /**
     * Log an error without structured context.
     *
     * @param message error event message
     */
    default void error(String message) {
        error(message, Map.of());
    }
}
