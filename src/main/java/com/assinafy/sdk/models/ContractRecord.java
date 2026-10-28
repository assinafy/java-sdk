package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A contract response returned either as root terms or as a linked amendment instrument.
 * Inspect {@link #getResource()} or use {@code instanceof} to distinguish the two shapes.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "resource",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DocumentContract.class, name = "document_contract"),
        @JsonSubTypes.Type(value = DocumentContractAmendment.class, name = "document_contract_amendment")
})
public sealed interface ContractRecord permits DocumentContract, DocumentContractAmendment {

    /** {@return the API resource discriminator} */
    String getResource();

    /** {@return the document whose contract terms or amendment link were returned} */
    String getDocumentId();
}
