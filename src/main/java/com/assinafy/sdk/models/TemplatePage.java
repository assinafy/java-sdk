package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A page of a template, as returned inside {@code template.pages}, including the
 * field placements configured on that page.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplatePage {

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

    @JsonProperty("fields")
    private List<TemplateFieldPlacement> fields;

    public TemplatePage() {}

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

    public List<TemplateFieldPlacement> getFields() { return fields; }
    public void setFields(List<TemplateFieldPlacement> fields) { this.fields = fields; }
}
