package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the document page in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentPage {

    @JsonProperty("id")
    private String id;

    @JsonProperty("number")
    private Integer number;

    @JsonProperty("height")
    private Integer height;

    @JsonProperty("width")
    private Integer width;

    @JsonProperty("download_url")
    private String downloadUrl;

    /**
     * Creates an empty document page.
     */
    public DocumentPage() {}

    /**
     * Returns the ID.
     *
     * @return the ID
     */
    public String getId() { return id; }

    /**
     * Sets the ID.
     *
     * @param id the ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the number.
     *
     * @return the number
     */
    public Integer getNumber() { return number; }

    /**
     * Sets the number.
     *
     * @param number the number
     */
    public void setNumber(Integer number) { this.number = number; }

    /**
     * Returns the height.
     *
     * @return the height
     */
    public Integer getHeight() { return height; }

    /**
     * Sets the height.
     *
     * @param height the height
     */
    public void setHeight(Integer height) { this.height = height; }

    /**
     * Returns the width.
     *
     * @return the width
     */
    public Integer getWidth() { return width; }

    /**
     * Sets the width.
     *
     * @param width the width
     */
    public void setWidth(Integer width) { this.width = width; }

    /**
     * Returns the download URL.
     *
     * @return the download URL
     */
    public String getDownloadUrl() { return downloadUrl; }

    /**
     * Sets the download URL.
     *
     * @param downloadUrl the download URL
     */
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}
