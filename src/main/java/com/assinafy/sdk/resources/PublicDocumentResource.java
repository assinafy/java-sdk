package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.DocumentDetails;

import java.util.Map;

/**
 * Public document endpoints — basic info lookup and send-token. These endpoints do not
 * require an authenticated session; the SDK still passes any configured credentials.
 */
public class PublicDocumentResource extends BaseResource {

    public PublicDocumentResource(ApiHttpClient http, Logger logger) {
        super(http, null, logger);
    }

    public PublicDocumentResource(ApiHttpClient http) {
        super(http);
    }

    /**
     * {@code GET /public/documents/{documentId}} — basic info about a document, no auth required.
     * Returns the same document shape as {@link DocumentResource#details(String)}.
     */
    public DocumentDetails getBasicInfo(String documentId) {
        String id = requireId(documentId, "Document ID");
        return call("Failed to fetch public document info",
                () -> http.get("/public/documents/" + id),
                DocumentDetails.class);
    }

    /**
     * {@code PUT /public/documents/{documentId}/send-token} — send a one-time access token by
     * email so the recipient can view/sign a public document. The documented body carries a single
     * {@code email} field.
     *
     * @param documentId target document ID
     * @param email      recipient email address
     */
    public Map<String, Object> sendToken(String documentId, String email) {
        String id = requireId(documentId, "Document ID");
        requireId(email, "Email");
        String json = serialise(Map.of("email", email));
        return callMap("Failed to send signer token",
                () -> http.put("/public/documents/" + id + "/send-token", json));
    }

    /**
     * @deprecated The send-token endpoint only accepts an {@code email}; the {@code channel}
     * argument is ignored. Use {@link #sendToken(String, String)} instead. Delegates with
     * {@code recipient} as the email address.
     */
    @Deprecated
    public Map<String, Object> sendToken(String documentId, String recipient, String channel) {
        return sendToken(documentId, recipient);
    }
}
