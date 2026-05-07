package com.assinafy.sdk.request;

import java.util.HashMap;
import java.util.Map;

public class ListParams {

    private Integer page;
    private Integer perPage;
    private String search;
    private String sort;
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

    public Map<String, Object> getExtra() { return extra; }

    public Map<String, Object> toQueryParams() {
        Map<String, Object> params = new HashMap<>();
        if (page != null) params.put("page", page);
        if (perPage != null) params.put("per_page", perPage);
        if (search != null && !search.isBlank()) params.put("search", search);
        if (sort != null && !sort.isBlank()) params.put("sort", sort);
        params.putAll(extra);
        return params;
    }

    public static final class Builder {
        private final ListParams params = new ListParams();

        public Builder page(int page) { params.setPage(page); return this; }
        public Builder perPage(int perPage) { params.setPerPage(perPage); return this; }
        public Builder search(String search) { params.setSearch(search); return this; }
        public Builder sort(String sort) { params.setSort(sort); return this; }
        public Builder extra(String key, Object value) { params.getExtra().put(key, value); return this; }
        public ListParams build() { return params; }
    }
}
