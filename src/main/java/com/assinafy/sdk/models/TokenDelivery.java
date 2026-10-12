package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of {@code PUT /v1/public/documents/{documentId}/send-token}: the document a one-time access
 * token was sent for, and where it was sent.
 *
 * <pre>{
 *   "document": {
 *     "resource": "document",
 *     "id": "615601fab04c0a3147bb1246",
 *     "name": "contract.pdf",
 *     "page_count": 1,
 *     "created_by": "Workspace owner"
 *   },
 *   "channel": "email",
 *   "recipient": "signer@example.com"
 * }</pre>
 *
 * @param document  summary of the document the token opens
 * @param channel   delivery channel, always {@code email}
 * @param recipient the signer address the token was sent to
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TokenDelivery(
        @JsonProperty("document") DocumentSummary document,
        @JsonProperty("channel") String channel,
        @JsonProperty("recipient") String recipient) {

    /**
     * Summary of the document a token was sent for.
     *
     * @param resource  resource type, {@code document}
     * @param id        document ID
     * @param name      document name
     * @param pageCount number of pages
     * @param createdBy name of the user who created the document
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DocumentSummary(
            @JsonProperty("resource") String resource,
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("page_count") Integer pageCount,
            @JsonProperty("created_by") String createdBy) {
    }
}
