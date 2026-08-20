package com.assinafy.sdk.models;

import java.util.List;

/**
 * Combined document, assignment, and signer IDs returned by the upload-and-request workflow.
 */
public class UploadAndRequestSignaturesResult {

    private final DocumentUploadResponse document;
    private final Assignment assignment;
    private final List<String> signerIds;

    /**
     * Creates an upload-and-request-signatures result.
     *
     * @param document the uploaded document
     * @param assignment the created assignment
     * @param signerIds IDs of the created signers
     */
    public UploadAndRequestSignaturesResult(DocumentUploadResponse document, Assignment assignment, List<String> signerIds) {
        this.document = document;
        this.assignment = assignment;
        this.signerIds = signerIds != null ? List.copyOf(signerIds) : List.of();
    }

    /**
     * Returns the document.
     *
     * @return the document
     */
    public DocumentUploadResponse getDocument() { return document; }

    /**
     * Returns the assignment.
     *
     * @return the assignment
     */
    public Assignment getAssignment() { return assignment; }

    /**
     * Returns the created signer IDs.
     *
     * @return signer IDs
     */
    public List<String> getSignerIds() { return signerIds; }
}
