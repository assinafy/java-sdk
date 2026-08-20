package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the document artifacts in an Assinafy API response.
 */
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

    @JsonProperty("pades")
    private String pades;

    @JsonProperty("bundle")
    private String bundle;

    /**
     * Creates an empty document artifacts.
     */
    public DocumentArtifacts() {}

    /**
     * Returns the original.
     *
     * @return the original
     */
    public String getOriginal() { return original; }

    /**
     * Sets the original.
     *
     * @param original the original
     */
    public void setOriginal(String original) { this.original = original; }

    /**
     * Returns the thumbnail.
     *
     * @return the thumbnail
     */
    public String getThumbnail() { return thumbnail; }

    /**
     * Sets the thumbnail.
     *
     * @param thumbnail the thumbnail
     */
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    /**
     * Returns the certificated.
     *
     * @return the certificated
     */
    public String getCertificated() { return certificated; }

    /**
     * Sets the certificated.
     *
     * @param certificated the certificated
     */
    public void setCertificated(String certificated) { this.certificated = certificated; }

    /**
     * Returns the certificate page.
     *
     * @return the certificate page
     */
    public String getCertificatePage() { return certificatePage; }

    /**
     * Sets the certificate page.
     *
     * @param certificatePage the certificate page
     */
    public void setCertificatePage(String certificatePage) { this.certificatePage = certificatePage; }

    /**
     * Returns the PAdES artifact URL.
     *
     * @return the PAdES artifact URL
     */
    public String getPades() { return pades; }

    /**
     * Sets the PAdES artifact URL.
     *
     * @param pades the PAdES artifact URL
     */
    public void setPades(String pades) { this.pades = pades; }

    /**
     * Returns the bundle.
     *
     * @return the bundle
     */
    public String getBundle() { return bundle; }

    /**
     * Sets the bundle.
     *
     * @param bundle the bundle
     */
    public void setBundle(String bundle) { this.bundle = bundle; }
}
