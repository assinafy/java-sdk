package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** What the embed page gets for a session token. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmbedSessionExchange {

    /** Creates an empty response. */
    public EmbedSessionExchange() {}

    @JsonProperty("signer_access_code")
    private String signerAccessCode;

    /** The signer&#x27;s access code, used by the embed page only.
     * @return the signer_access_code value, or null when absent
     */
    public String getSignerAccessCode() { return signerAccessCode; }

    /** Sets signer_access_code.
     * @param value the signer_access_code value
     */
    public void setSignerAccessCode(String value) { signerAccessCode = value; }

    @JsonProperty("signer_id")
    private String signerId;

    /** signer id
     * @return the signer_id value, or null when absent
     */
    public String getSignerId() { return signerId; }

    /** Sets signer_id.
     * @param value the signer_id value
     */
    public void setSignerId(String value) { signerId = value; }

    @JsonProperty("document_id")
    private String documentId;

    /** document id
     * @return the document_id value, or null when absent
     */
    public String getDocumentId() { return documentId; }

    /** Sets document_id.
     * @param value the document_id value
     */
    public void setDocumentId(String value) { documentId = value; }

    @JsonProperty("verification_method")
    private String verificationMethod;

    /** verification method
     * @return the verification_method value, or null when absent
     */
    public String getVerificationMethod() { return verificationMethod; }

    /** Sets verification_method.
     * @param value the verification_method value
     */
    public void setVerificationMethod(String value) { verificationMethod = value; }

    @JsonProperty("embed_origins")
    private List<String> embedOrigins;

    /** embed origins
     * @return the embed_origins value, or null when absent
     */
    public List<String> getEmbedOrigins() { return embedOrigins; }

    /** Sets embed_origins.
     * @param value the embed_origins value
     */
    public void setEmbedOrigins(List<String> value) { embedOrigins = value; }

}
