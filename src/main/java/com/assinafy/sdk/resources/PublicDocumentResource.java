package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.http.ApiHttpClient;

import java.util.HashMap;
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
     * {@code GET /public/documents/{document_id}} — basic info about a document, no auth required.
     */
    public Map<String, Object> getBasicInfo(String documentId) {
        String id = requireId(documentId, "Document ID");
        return callMap("Failed to fetch public document info",
                () -> http.get("/public/documents/" + id));
    }

    /**
     * {@code PUT /public/documents/{document_id}/send-token} — request a new signer access
     * token to be delivered via the configured channel (e.g. email).
     *
     * @param documentId target document ID
     * @param recipient  recipient email or phone number
     * @param channel    delivery channel ({@code email} or {@code whatsapp})
     */
    public Map<String, Object> sendToken(String documentId, String recipient, String channel) {
        String id = requireId(documentId, "Document ID");
        requireId(recipient, "Recipient");
        requireId(channel, "Channel");
        Map<String, Object> body = new HashMap<>();
        body.put("recipient", recipient);
        body.put("channel", channel);
        String json = serialise(body);
        return callMap("Failed to send signer token",
                () -> http.put("/public/documents/" + id + "/send-token", json));
    }
}
