package com.assinafy.sdk.http;

import java.io.IOException;
import java.util.Map;

public interface ApiHttpClient {

    HttpRawResponse get(String path) throws IOException;

    HttpRawResponse get(String path, Map<String, Object> queryParams) throws IOException;

    HttpRawResponse post(String path, String jsonBody) throws IOException;

    HttpRawResponse postMultipart(String path, String fileName, byte[] fileData, String name, String metadata) throws IOException;

    HttpRawResponse put(String path, String jsonBody) throws IOException;

    HttpRawResponse delete(String path) throws IOException;

    byte[] getBinary(String path) throws IOException;

    HttpRawResponse postSignature(String path, byte[] imageData) throws IOException;
}
