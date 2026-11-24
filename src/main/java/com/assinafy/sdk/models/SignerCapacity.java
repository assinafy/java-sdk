package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/** The capacity stated by the sender for an assignment signer. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignerCapacity {
    /** Creates an empty response model. */
    public SignerCapacity() {}

    @JsonProperty("signer_id")
    private String signerId;

    /** The contact ID.
     * @return the signer_id value, or null when absent
     */
    public String getSignerId() { return signerId; }

    /** Sets signer_id.
     * @param value the signer_id value
     */
    public void setSignerId(String value) { signerId = value; }

    @JsonProperty("sign_as")
    private String signAs;

    /** The signing capacity; Sign when none was supplied.
     * @return the sign_as value, or null when absent
     */
    public String getSignAs() { return signAs; }

    /** Sets sign_as.
     * @param value the sign_as value
     */
    public void setSignAs(String value) { signAs = value; }

}
