package com.assinafy.sdk.request;

import com.assinafy.sdk.exceptions.ValidationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Query parameters for list endpoints: paging ({@code page}/{@code per-page}), {@code search},
 * {@code sort}, plus the optional resource filters {@code status}, {@code method} and
 * {@code tags} (documents/templates) and {@code include_inactive}/{@code include_standard}
 * (field definitions). Any other parameter can be supplied through {@link Builder#extra}.
 */
public class ListParams {

    private Integer page;
    private Integer perPage;
    private String search;
    private String sort;
    private String status;
    private String method;
    private String tags;
    private Boolean includeInactive;
    private Boolean includeStandard;
    private final Map<String, Object> extra = new HashMap<>();

    /**
     * Creates an empty list parameters.
     */
    public ListParams() {}

    /**
     * Creates a builder for a list parameters.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the page.
     *
     * @return the page
     */
    public Integer getPage() { return page; }

    /**
     * Sets the page.
     *
     * @param page the page
     */
    public void setPage(Integer page) { this.page = page; }

    /**
     * Returns the items per page.
     *
     * @return the items per page
     */
    public Integer getPerPage() { return perPage; }

    /**
     * Sets the items per page.
     *
     * @param perPage the items per page
     */
    public void setPerPage(Integer perPage) { this.perPage = perPage; }

    /**
     * Returns the search.
     *
     * @return the search
     */
    public String getSearch() { return search; }

    /**
     * Sets the search.
     *
     * @param search the search
     */
    public void setSearch(String search) { this.search = search; }

    /**
     * Returns the sort.
     *
     * @return the sort
     */
    public String getSort() { return sort; }

    /**
     * Sets the sort.
     *
     * @param sort the sort
     */
    public void setSort(String sort) { this.sort = sort; }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public String getStatus() { return status; }

    /**
     * Sets the status.
     *
     * @param status the status
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Returns the method.
     *
     * @return the method
     */
    public String getMethod() { return method; }

    /**
     * Sets the method.
     *
     * @param method the method
     */
    public void setMethod(String method) { this.method = method; }

    /**
     * Returns the tags.
     *
     * @return the tags
     */
    public String getTags() { return tags; }

    /**
     * Sets the tags.
     *
     * @param tags the tags
     */
    public void setTags(String tags) { this.tags = tags; }

    /**
     * Returns the inactive-field inclusion flag.
     *
     * @return the inactive-field inclusion flag
     */
    public Boolean getIncludeInactive() { return includeInactive; }

    /**
     * Sets the inactive-field inclusion flag.
     *
     * @param includeInactive the inactive-field inclusion flag
     */
    public void setIncludeInactive(Boolean includeInactive) { this.includeInactive = includeInactive; }

    /**
     * Returns the standard-field inclusion flag.
     *
     * @return the standard-field inclusion flag
     */
    public Boolean getIncludeStandard() { return includeStandard; }

    /**
     * Sets the standard-field inclusion flag.
     *
     * @param includeStandard the standard-field inclusion flag
     */
    public void setIncludeStandard(Boolean includeStandard) { this.includeStandard = includeStandard; }

    /**
     * Returns the extra.
     *
     * @return the extra
     */
    public Map<String, Object> getExtra() { return extra; }

    /**
     * Converts these options to API query parameters.
     *
     * @return API query parameters
     * @throws ValidationException if the effective paging values violate the API schema
     */
    public Map<String, Object> toQueryParams() {
        Map<String, Object> params = new HashMap<>();
        if (page != null) params.put("page", page);
        if (perPage != null) params.put("per-page", perPage);
        if (search != null && !search.isBlank()) params.put("search", search);
        if (sort != null && !sort.isBlank()) params.put("sort", sort);
        if (status != null && !status.isBlank()) params.put("status", status);
        if (method != null && !method.isBlank()) params.put("method", method);
        if (tags != null && !tags.isBlank()) params.put("tags", tags);
        if (includeInactive != null) params.put("include_inactive", includeInactive);
        if (includeStandard != null) params.put("include_standard", includeStandard);
        extra.keySet().forEach(ListParams::validateExtraKey);
        params.putAll(extra);

        if (params.containsKey("page") && requireInteger(params, "page", "Page") < 1) {
            throw new ValidationException("Page must be at least 1");
        }
        if (params.containsKey("per-page")) {
            int effectivePerPage = requireInteger(params, "per-page", "Per-page");
            if (effectivePerPage < 1 || effectivePerPage > 100) {
                throw new ValidationException("Per-page must be between 1 and 100");
            }
        }
        return params;
    }

    private static int requireInteger(Map<String, Object> params, String key, String label) {
        Object value = params.get(key);
        if (!(value instanceof Integer)) {
            throw new ValidationException(label + " must be an integer");
        }
        return (Integer) value;
    }

    private static void validateExtraKey(String key) {
        if (key == null || key.isBlank()) {
            throw new ValidationException("Extra query parameter key must not be blank");
        }
    }

    /**
     * Builder for {@link ListParams}.
     */
    public static final class Builder {
        private final ListParams params = new ListParams();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets the page for the object being built.
         *
         * @param page the page
         * @return this builder
         */
        public Builder page(int page) { params.setPage(page); return this; }

        /**
         * Sets the items per page for the object being built.
         *
         * @param perPage the items per page
         * @return this builder
         */
        public Builder perPage(int perPage) { params.setPerPage(perPage); return this; }

        /**
         * Sets the search for the object being built.
         *
         * @param search the search
         * @return this builder
         */
        public Builder search(String search) { params.setSearch(search); return this; }

        /**
         * Sets the sort for the object being built.
         *
         * @param sort the sort
         * @return this builder
         */
        public Builder sort(String sort) { params.setSort(sort); return this; }

        /**
         * Sets the status for the object being built.
         *
         * @param status the status
         * @return this builder
         */
        public Builder status(String status) { params.setStatus(status); return this; }

        /**
         * Sets the method for the object being built.
         *
         * @param method the method
         * @return this builder
         */
        public Builder method(String method) { params.setMethod(method); return this; }

        /**
         * Comma-separated tag IDs to filter by.
         *
         * @param tags the tags
         * @return this builder
         */
        public Builder tags(String tags) { params.setTags(tags); return this; }

        /**
         * Sets the inactive-field inclusion flag for the object being built.
         *
         * @param includeInactive the inactive-field inclusion flag
         * @return this builder
         */
        public Builder includeInactive(boolean includeInactive) { params.setIncludeInactive(includeInactive); return this; }

        /**
         * Sets the standard-field inclusion flag for the object being built.
         *
         * @param includeStandard the standard-field inclusion flag
         * @return this builder
         */
        public Builder includeStandard(boolean includeStandard) { params.setIncludeStandard(includeStandard); return this; }

        /**
         * Sets the extra for the object being built.
         *
         * @param key query parameter name
         * @param value query parameter value
         * @return this builder
         * @throws ValidationException if {@code key} is null or blank
         */
        public Builder extra(String key, Object value) {
            validateExtraKey(key);
            params.getExtra().put(key, value);
            return this;
        }

        /**
         * Builds the configured list parameters.
         *
         * @return the configured list parameters
         */
        public ListParams build() { return params; }
    }
}
