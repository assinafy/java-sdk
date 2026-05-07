package com.assinafy.sdk.models;

import java.util.List;

public class UploadAndRequestSignaturesResult {

    private final DocumentUploadResponse document;
    private final Assignment assignment;
    private final List<String> signerIds;

    public UploadAndRequestSignaturesResult(DocumentUploadResponse document, Assignment assignment, List<String> signerIds) {
        this.document = document;
        this.assignment = assignment;
        this.signerIds = signerIds;
    }

    public DocumentUploadResponse getDocument() { return document; }
    public Assignment getAssignment() { return assignment; }
    public List<String> getSignerIds() { return signerIds; }
}
