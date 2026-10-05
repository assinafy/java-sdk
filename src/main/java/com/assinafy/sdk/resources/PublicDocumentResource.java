package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.Document;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public document endpoints — basic info lookup and send-token. These endpoints do not
 * require an authenticated session; the default transport omits configured workspace credentials.
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
     * <p><strong>HTTP:</strong> <code>GET /v1/public/documents/{documentId}</code>.
     * <strong>Authentication:</strong> Public (no SDK credential).</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): The document ID.</li>
     * </ul>
     * <p>Request body: none.</p>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "resource": "document",
     *     "id": "615601fab04c0a3147bb1246",
     *     "account_id": "d199996981dbd199996981db",
     *     "template_id": null,
     *     "name": "document.pdf",
     *     "status": "metadata_ready",
     *     "artifacts": {
     *       "original": "https://api.assinafy.com.br/v1/documents/doc1/download/original"
     *     },
     *     "is_closed": false,
     *     "signing_url": "https://api.assinafy.com.br/v1/sign/doc1",
     *     "decline_reason": null,
     *     "declined_by": null,
     *     "tags": [
     *       {
     *         "id": "id_example",
     *         "name": "name_example"
     *       }
     *     ],
     *     "assignment": null,
     *     "pages": [
     *       {
     *         "id": "615601faf166d6d1d8e7dc30",
     *         "number": 1,
     *         "height": 2100,
     *         "width": 1275,
     *         "download_url": "https://api.assinafy.com.br/v1/documents/doc1/pages/1a/download"
     *       }
     *     ],
     *     "created_at": "2026-06-03T03:54:16Z",
     *     "updated_at": "2026-06-03T03:54:16Z"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 The public document; 404 The requested resource does not exist.;
     * 500 Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
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
     * <p>Wire contract, payloads and failures: {@link #sendToken(String, String, String)}.</p>
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
     * <p>Wire contract, payloads and failures: {@link #sendToken(String, String, String)}.</p>
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
     * <p><strong>HTTP:</strong> <code>PUT /v1/public/documents/{documentId}/send-token</code>.
     * <strong>Authentication:</strong> Public (no SDK credential).</p>
     * <p>Wire parameters:</p><ul>
     * <li><code>documentId</code> (path, required): The document ID.</li>
     * </ul>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "email": "signer@example.invalid"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": ""
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Token sent; 500 Unexpected server error. Non-2xx HTTP or numeric
     * envelope statuses raise <code>ApiException</code>; 401/403 raise
     * <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O failures
     * raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
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
