package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentArtifacts {

    @JsonProperty("original")
    private String original;

    /** Thumbnail image URL. Returned inline on document objects (alongside {@code original}). */
    @JsonProperty("thumbnail")
    private String thumbnail;

    @JsonProperty("certificated")
    private String certificated;

    @JsonProperty("certificate-page")
    private String certificatePage;

    @JsonProperty("bundle")
    private String bundle;

    public DocumentArtifacts() {}

    public String getOriginal() { return original; }
    public void setOriginal(String original) { this.original = original; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getCertificated() { return certificated; }
    public void setCertificated(String certificated) { this.certificated = certificated; }

    public String getCertificatePage() { return certificatePage; }
    public void setCertificatePage(String certificatePage) { this.certificatePage = certificatePage; }

    public String getBundle() { return bundle; }
    public void setBundle(String bundle) { this.bundle = bundle; }
}
