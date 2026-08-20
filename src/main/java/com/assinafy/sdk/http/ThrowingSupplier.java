package com.assinafy.sdk.http;

import java.io.IOException;

/**
 * Supplies a value from an operation that may fail with an {@link IOException}.
 *
 * @param <T> supplied value type
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {
    /**
     * Run the operation.
     *
     * @return the supplied value
     * @throws IOException if the operation cannot complete
     */
    T get() throws IOException;
}
