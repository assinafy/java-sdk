package com.assinafy.sdk.http;

import java.io.IOException;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws IOException;
}
