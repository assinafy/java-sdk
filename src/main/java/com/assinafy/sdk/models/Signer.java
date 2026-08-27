package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Represents the signer in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Signer {

    @JsonProperty("resource")
    private String resource;

    @JsonProperty("id")
    private String id;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    @JsonProperty("cpf")
    private String cpf;

    @JsonProperty("has_accepted_terms")
    private Boolean hasAcceptedTerms;

    @JsonProperty("has_signature")
    private Boolean hasSignature;

    @JsonProperty("has_initial")
    private Boolean hasInitial;

    /**
     * Whether the signer opted to reuse their signature across processes. Returned by the
     * signer self-service {@code GET /signers/self} endpoint; set via the {@code reuse}
     * parameter on {@code uploadSignature}.
     */
    @JsonProperty("is_signature_reusable")
    private Boolean isSignatureReusable;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    /**
     * Creates an empty signer.
     */
    public Signer() {}

    /**
     * Returns the resource type.
     *
     * @return the resource type
     */
    public String getResource() { return resource; }

    /**
     * Sets the resource type.
     *
     * @param resource the resource type
     */
    public void setResource(String resource) { this.resource = resource; }

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
     * Returns the full name.
     *
     * @return the full name
     */
    public String getFullName() { return fullName; }

    /**
     * Sets the full name.
     *
     * @param fullName the full name
     */
    public void setFullName(String fullName) { this.fullName = fullName; }

    /**
     * Returns the email address.
     *
     * @return the email address
     */
    public String getEmail() { return email; }

    /**
     * Sets the email address.
     *
     * @param email the email address
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the WhatsApp phone number.
     *
     * @return the WhatsApp phone number
     */
    public String getWhatsappPhoneNumber() { return whatsappPhoneNumber; }

    /**
     * Sets the WhatsApp phone number.
     *
     * @param whatsappPhoneNumber the WhatsApp phone number
     */
    public void setWhatsappPhoneNumber(String whatsappPhoneNumber) { this.whatsappPhoneNumber = whatsappPhoneNumber; }

    /**
     * @deprecated Signer responses do not provide this value. Use
     * {@code UpdateSignerRequest.governmentId(...)} when updating a signer.
     * @return a retained CPF/CNPJ response value, or {@code null}
     */
    @Deprecated
    public String getCpf() { return cpf; }

    /**
     * @deprecated Retained for decoding older response payloads.
     * @param cpf the retained CPF/CNPJ value
     */
    @Deprecated
    public void setCpf(String cpf) { this.cpf = cpf; }

    /**
     * Returns the terms-acceptance flag.
     *
     * @return the terms-acceptance flag
     */
    public Boolean getHasAcceptedTerms() { return hasAcceptedTerms; }

    /**
     * Sets the terms-acceptance flag.
     *
     * @param hasAcceptedTerms the terms-acceptance flag
     */
    public void setHasAcceptedTerms(Boolean hasAcceptedTerms) { this.hasAcceptedTerms = hasAcceptedTerms; }

    /**
     * Returns the signature-presence flag.
     *
     * @return the signature-presence flag
     */
    public Boolean getHasSignature() { return hasSignature; }

    /**
     * Sets the signature-presence flag.
     *
     * @param hasSignature the signature-presence flag
     */
    public void setHasSignature(Boolean hasSignature) { this.hasSignature = hasSignature; }

    /**
     * Returns the initial-presence flag.
     *
     * @return the initial-presence flag
     */
    public Boolean getHasInitial() { return hasInitial; }

    /**
     * Sets the initial-presence flag.
     *
     * @param hasInitial the initial-presence flag
     */
    public void setHasInitial(Boolean hasInitial) { this.hasInitial = hasInitial; }

    /**
     * Returns the signature-reuse flag.
     *
     * @return the signature-reuse flag
     */
    public Boolean getIsSignatureReusable() { return isSignatureReusable; }

    /**
     * Sets the signature-reuse flag.
     *
     * @param isSignatureReusable the signature-reuse flag
     */
    public void setIsSignatureReusable(Boolean isSignatureReusable) { this.isSignatureReusable = isSignatureReusable; }

    /**
     * @deprecated Signer responses do not provide metadata.
     * @return retained signer metadata, or {@code null}
     */
    @Deprecated
    public Map<String, Object> getMetadata() { return metadata; }

    /**
     * @deprecated Retained for decoding older response payloads.
     * @param metadata retained signer metadata
     */
    @Deprecated
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
