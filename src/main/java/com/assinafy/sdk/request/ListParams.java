package com.assinafy.sdk.request;

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

    public ListParams() {}

    public static Builder builder() {
        return new Builder();
    }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getPerPage() { return perPage; }
    public void setPerPage(Integer perPage) { this.perPage = perPage; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Boolean getIncludeInactive() { return includeInactive; }
    public void setIncludeInactive(Boolean includeInactive) { this.includeInactive = includeInactive; }

    public Boolean getIncludeStandard() { return includeStandard; }
    public void setIncludeStandard(Boolean includeStandard) { this.includeStandard = includeStandard; }

    public Map<String, Object> getExtra() { return extra; }

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
        params.putAll(extra);
        return params;
    }

    public static final class Builder {
        private final ListParams params = new ListParams();

        public Builder page(int page) { params.setPage(page); return this; }
        public Builder perPage(int perPage) { params.setPerPage(perPage); return this; }
        public Builder search(String search) { params.setSearch(search); return this; }
        public Builder sort(String sort) { params.setSort(sort); return this; }
        public Builder status(String status) { params.setStatus(status); return this; }
        public Builder method(String method) { params.setMethod(method); return this; }
        /** Comma-separated tag IDs to filter by. */
        public Builder tags(String tags) { params.setTags(tags); return this; }
        public Builder includeInactive(boolean includeInactive) { params.setIncludeInactive(includeInactive); return this; }
        public Builder includeStandard(boolean includeStandard) { params.setIncludeStandard(includeStandard); return this; }
        public Builder extra(String key, Object value) { params.getExtra().put(key, value); return this; }
        public ListParams build() { return params; }
    }
}
