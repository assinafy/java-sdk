package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public DocumentPage() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}
