package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the pagination metadata in an Assinafy API response.
 */
public class PaginationMeta {

    @JsonProperty("current_page")
    private Integer currentPage;

    @JsonProperty("last_page")
    private Integer lastPage;

    @JsonProperty("per_page")
    private Integer perPage;

    @JsonProperty("total")
    private Integer total;

    /**
     * Creates empty pagination metadata.
     */
    public PaginationMeta() {}

    /**
     * Creates pagination metadata.
     *
     * @param currentPage the current page
     * @param lastPage the last page
     * @param perPage the items per page
     * @param total the total item count across all pages
     */
    public PaginationMeta(Integer currentPage, Integer lastPage, Integer perPage, Integer total) {
        this.currentPage = currentPage;
        this.lastPage = lastPage;
        this.perPage = perPage;
        this.total = total;
    }

    /**
     * Returns the current page.
     *
     * @return the current page
     */
    public Integer getCurrentPage() { return currentPage; }

    /**
     * Sets the current page.
     *
     * @param currentPage the current page
     */
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }

    /**
     * Returns the last page.
     *
     * @return the last page
     */
    public Integer getLastPage() { return lastPage; }

    /**
     * Sets the last page.
     *
     * @param lastPage the last page
     */
    public void setLastPage(Integer lastPage) { this.lastPage = lastPage; }

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
     * Returns the total item count across all pages.
     *
     * @return the total item count across all pages
     */
    public Integer getTotal() { return total; }

    /**
     * Sets the total item count across all pages.
     *
     * @param total the total item count across all pages
     */
    public void setTotal(Integer total) { this.total = total; }
}
