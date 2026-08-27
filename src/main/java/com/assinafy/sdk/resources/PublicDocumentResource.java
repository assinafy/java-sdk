package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.Document;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public document endpoints — basic info lookup and send-token. These endpoints do not
 * require an authenticated session; the SDK still passes any configured credentials.
 */
public class PublicDocumentResource extends BaseResource {

    /**
     * Create public-document operations with a logger.
     *
     * @param http HTTP transport
     * @param logger diagnostic logger
     */
    public PublicDocumentResource(ApiHttpClient http, Logger logger) {
        super(http, null, logger);
    }

    /**
     * Create public-document operations with no-op logging.
     *
     * @param http HTTP transport
     */
    public PublicDocumentResource(ApiHttpClient http) {
        super(http);
    }

    /**
     * {@code GET /public/documents/{documentId}} — basic info about a document, no auth required.
     * Returns the same document shape as {@link DocumentResource#details(String)}.
     *
     * @param documentId document ID
     * @return public document details
     */
    public Document getBasicInfo(String documentId) {
        String id = pathSegment(documentId, "Document ID");
        return call("Failed to fetch public document info",
                () -> http.get("/public/documents/" + id),
                Document.class);
    }

    /**
     * {@code PUT /public/documents/{documentId}/send-token} — send a one-time access token by
     * email so the recipient can view/sign a public document. Sends the documented
     * {@code {"email":"..."}} request body.
     *
     * @param documentId target document ID
     * @param email      recipient email address
     * @return the API response payload
     */
    public Map<String, Object> sendToken(String documentId, String email) {
        String id = pathSegment(documentId, "Document ID");
        requireEmail(email);
        return callMap("Failed to send signer token",
                () -> http.put("/public/documents/" + id + "/send-token",
                        serialise(Map.of("email", email))));
    }

    /**
     * Send a token using the document's configured recipient, omitting the request body.
     *
     * @param documentId target document ID
     * @return the API response payload
     */
    public Map<String, Object> sendToken(String documentId) {
        String id = pathSegment(documentId, "Document ID");
        return callMap("Failed to send signer token",
                () -> http.put("/public/documents/" + id + "/send-token", null));
    }

    /**
     * Send a token through an explicitly selected delivery channel. For {@code email}, the request
     * contains {@code email}, {@code recipient}, and {@code channel}; other channels send
     * {@code recipient} and {@code channel}.
     *
     * @param documentId target document ID
     * @param recipient  recipient email address or phone number
     * @param channel    delivery channel, such as {@code email} or {@code whatsapp}
     * @return the API response payload
     */
    public Map<String, Object> sendToken(String documentId, String recipient, String channel) {
        String id = pathSegment(documentId, "Document ID");
        requireId(recipient, "Recipient");
        requireId(channel, "Channel");
        Map<String, Object> body = new LinkedHashMap<>();
        if ("email".equalsIgnoreCase(channel)) {
            requireEmail(recipient);
            body.put("email", recipient);
        }
        body.put("recipient", recipient);
        body.put("channel", channel);
        return callMap("Failed to send signer token",
                () -> http.put("/public/documents/" + id + "/send-token", serialise(body)));
    }
}
