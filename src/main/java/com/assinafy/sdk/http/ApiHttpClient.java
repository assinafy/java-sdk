package com.assinafy.sdk.http;

import java.io.IOException;
import java.util.Map;

public interface ApiHttpClient {

    HttpRawResponse get(String path) throws IOException;

    HttpRawResponse get(String path, Map<String, Object> queryParams) throws IOException;

    HttpRawResponse post(String path, String jsonBody) throws IOException;

    HttpRawResponse postMultipart(String path, String fileName, byte[] fileData, String name, String metadata) throws IOException;

    /**
     * Single-file {@code multipart/form-data} upload with a caller-chosen part name and content
     * type (e.g. account-logo uploads use part {@code file} with an image media type).
     */
    HttpRawResponse postFile(String path, String partName, String fileName, byte[] data, String contentType) throws IOException;

    HttpRawResponse put(String path, String jsonBody) throws IOException;

    HttpRawResponse patch(String path, String jsonBody) throws IOException;

    HttpRawResponse delete(String path) throws IOException;

    /** DELETE with a JSON request body (e.g. account deletion with {@code {"force": true}}). */
    HttpRawResponse delete(String path, String jsonBody) throws IOException;

    byte[] getBinary(String path) throws IOException;

    HttpRawResponse postSignature(String path, byte[] imageData) throws IOException;
}
