package com.assinafy.sdk;

import java.util.Map;

/** Logger implementation that discards every event. */
public final class NoOpLogger implements Logger {

    /** Shared stateless no-op logger. */
    public static final NoOpLogger INSTANCE = new NoOpLogger();

    private NoOpLogger() {}

    @Override public void debug(String message, Map<String, Object> context) {}
    @Override public void info(String message, Map<String, Object> context) {}
    @Override public void warn(String message, Map<String, Object> context) {}
    @Override public void error(String message, Map<String, Object> context) {}
}
