package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaginationMeta {

    @JsonProperty("current_page")
    private Integer currentPage;

    @JsonProperty("last_page")
    private Integer lastPage;

    @JsonProperty("per_page")
    private Integer perPage;

    @JsonProperty("total")
    private Integer total;

    public PaginationMeta() {}

    public PaginationMeta(Integer currentPage, Integer lastPage, Integer perPage, Integer total) {
        this.currentPage = currentPage;
        this.lastPage = lastPage;
        this.perPage = perPage;
        this.total = total;
    }

    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }

    public Integer getLastPage() { return lastPage; }
    public void setLastPage(Integer lastPage) { this.lastPage = lastPage; }

    public Integer getPerPage() { return perPage; }
    public void setPerPage(Integer perPage) { this.perPage = perPage; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
}
