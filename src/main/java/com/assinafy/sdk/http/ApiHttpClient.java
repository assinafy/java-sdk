package com.assinafy.sdk.http;

import java.io.IOException;
import java.util.Map;

/** Low-level HTTP transport used by SDK resources. Paths are relative to the configured base URL. */
public interface ApiHttpClient {

    /**
     * Send a GET request.
     *
     * @param path relative request path
     * @return raw status, body, and headers
     * @throws IOException if the transport cannot complete the request
     */
    HttpRawResponse get(String path) throws IOException;

    /**
     * Send a GET request with query parameters; {@code null} values are omitted.
     *
     * @param path relative request path
     * @param queryParams query parameter names and values, or {@code null}
     * @return raw status, body, and headers
     * @throws IOException if the transport cannot complete the request
     */
    HttpRawResponse get(String path, Map<String, Object> queryParams) throws IOException;

    /**
     * Send a JSON POST request.
     *
     * @param path relative request path
     * @param jsonBody serialized JSON body; {@code null} sends an empty body
     * @return raw status, body, and headers
     * @throws IOException if the transport cannot complete the request
     */
    HttpRawResponse post(String path, String jsonBody) throws IOException;

    /**
     * Upload a PDF document as multipart form data.
     *
     * @param path relative request path
     * @param fileName file name reported in the multipart upload
     * @param fileData PDF bytes
     * @param name document name; {@code null} uses {@code fileName}
     * @param metadata optional serialized metadata field
     * @return raw status, body, and headers
     * @throws IOException if the transport cannot complete the request
     */
    HttpRawResponse postMultipart(String path, String fileName, byte[] fileData, String name, String metadata) throws IOException;

    /**
     * Upload one file as multipart form data.
     *
     * @param path relative request path
     * @param partName multipart form field name
     * @param fileName uploaded file name
     * @param data file bytes
     * @param contentType file media type, or {@code null} when unspecified
     * @return raw status, body, and headers
     * @throws IOException if the transport cannot complete the request
     */
    HttpRawResponse postFile(String path, String partName, String fileName, byte[] data, String contentType) throws IOException;

    /**
     * Send a JSON PUT request.
     *
     * @param path relative request path
     * @param jsonBody serialized JSON body; {@code null} sends an empty body
     * @return raw status, body, and headers
     * @throws IOException if the transport cannot complete the request
     */
    HttpRawResponse put(String path, String jsonBody) throws IOException;

    /**
     * Send a JSON PATCH request.
     *
     * @param path relative request path
     * @param jsonBody serialized JSON body; {@code null} sends an empty body
     * @return raw status, body, and headers
     * @throws IOException if the transport cannot complete the request
     */
    HttpRawResponse patch(String path, String jsonBody) throws IOException;

    /**
     * Send a DELETE request without a body.
     *
     * @param path relative request path
     * @return raw status, body, and headers
     * @throws IOException if the transport cannot complete the request
     */
    HttpRawResponse delete(String path) throws IOException;

    /**
     * Send a DELETE request with an optional JSON body.
     *
     * @param path relative request path
     * @param jsonBody serialized JSON body, or {@code null} for no body
     * @return raw status, body, and headers
     * @throws IOException if the transport cannot complete the request
     */
    HttpRawResponse delete(String path, String jsonBody) throws IOException;

    /**
     * Download binary response bytes using the transport's default accepted media types.
     *
     * @param path relative request path
     * @return response body bytes
     * @throws IOException if the transport cannot complete or read the request
     */
    byte[] getBinary(String path) throws IOException;

    /**
     * Download bytes while requesting a specific response media type.
     *
     * @param path relative request path
     * @param acceptMediaType value for the HTTP {@code Accept} header
     * @return response body bytes
     * @throws IOException if the transport cannot complete or read the request
     */
    default byte[] getBinary(String path, String acceptMediaType) throws IOException {
        return getBinary(path);
    }

    /**
     * Upload raw signature-image bytes.
     *
     * @param path relative request path
     * @param imageData PNG or JPEG image bytes
     * @return raw status, body, and headers
     * @throws IOException if the transport cannot complete the request
     */
    HttpRawResponse postSignature(String path, byte[] imageData) throws IOException;
}
