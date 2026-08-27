package com.assinafy.sdk.models;

import java.util.List;

/**
 * Combined document, assignment, and signer IDs returned by the upload-and-request workflow.
 */
public class UploadAndRequestSignaturesResult {

    private final Document document;
    private final Assignment assignment;
    private final List<String> signerIds;

    /**
     * Creates an upload-and-request-signatures result.
     *
     * @param document the uploaded document
     * @param assignment the created assignment
     * @param signerIds IDs of the resolved signers, whether created or reused
     */
    public UploadAndRequestSignaturesResult(Document document, Assignment assignment, List<String> signerIds) {
        this.document = document;
        this.assignment = assignment;
        this.signerIds = signerIds != null ? List.copyOf(signerIds) : List.of();
    }

    /**
     * Returns the document.
     *
     * @return the document
     */
    public Document getDocument() { return document; }

    /**
     * Returns the assignment.
     *
     * @return the assignment
     */
    public Assignment getAssignment() { return assignment; }

    /**
     * Returns the resolved signer IDs in request order.
     *
     * @return IDs of signers created or reused by the workflow
     */
    public List<String> getSignerIds() { return signerIds; }
}
