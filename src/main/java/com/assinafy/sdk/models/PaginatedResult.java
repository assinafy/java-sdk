package com.assinafy.sdk.models;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the paginated result in an Assinafy API response.
 *
 * @param <T> item type
 */
public class PaginatedResult<T> {

    private final List<T> data;
    private final PaginationMeta meta;

    /**
     * Creates a paginated result.
     *
     * @param data result items
     * @param meta pagination metadata
     */
    public PaginatedResult(List<T> data, PaginationMeta meta) {
        this.data = data != null
                ? Collections.unmodifiableList(new ArrayList<>(data))
                : Collections.emptyList();
        this.meta = meta;
    }

    /**
     * Creates a paginated result.
     *
     * @param data result items
     */
    public PaginatedResult(List<T> data) {
        this(data, null);
    }

    /**
     * Returns the data.
     *
     * @return the data
     */
    public List<T> getData() {
        return data;
    }

    /**
     * Returns the meta.
     *
     * @return the meta
     */
    public PaginationMeta getMeta() {
        return meta;
    }

    /**
     * Returns whether pagination metadata is available.
     *
     * @return true when pagination metadata is present; otherwise false
     */
    public boolean hasMeta() {
        return meta != null;
    }
}
