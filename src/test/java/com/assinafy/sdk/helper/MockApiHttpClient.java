package com.assinafy.sdk.helper;

import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.http.HttpRawResponse;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class MockApiHttpClient implements ApiHttpClient {

    private final Deque<HttpRawResponse> queue = new ArrayDeque<>();
    private final List<CapturedRequest> captured = new ArrayList<>();

    public MockApiHttpClient enqueue(int statusCode, String body) {
        return enqueue(new HttpRawResponse(statusCode, body, Collections.emptyMap()));
    }

    public MockApiHttpClient enqueue(int statusCode, String body, Map<String, String> headers) {
        return enqueue(new HttpRawResponse(statusCode, body, headers));
    }

    public MockApiHttpClient enqueue(HttpRawResponse response) {
        queue.add(response);
        return this;
    }

    public List<CapturedRequest> getCaptured() {
        return Collections.unmodifiableList(captured);
    }

    public CapturedRequest lastCaptured() {
        if (captured.isEmpty()) throw new IllegalStateException("No requests captured");
        return captured.get(captured.size() - 1);
    }

    public CapturedRequest capturedAt(int index) {
        return captured.get(index);
    }

    public int capturedCount() {
        return captured.size();
    }

    @Override
    public HttpRawResponse get(String path) throws IOException {
        captured.add(new CapturedRequest("GET", path, null, null, null));
        return next();
    }

    @Override
    public HttpRawResponse get(String path, Map<String, Object> queryParams) throws IOException {
        captured.add(new CapturedRequest("GET", path, queryParams, null, null));
        return next();
    }

    @Override
    public HttpRawResponse post(String path, String jsonBody) throws IOException {
        captured.add(new CapturedRequest("POST", path, null, jsonBody, null));
        return next();
    }

    @Override
    public HttpRawResponse postMultipart(String path, String fileName, byte[] fileData, String name, String metadata) throws IOException {
        captured.add(new CapturedRequest("POST_MULTIPART", path, null, null, new MultipartData(fileName, fileData, name, metadata)));
        return next();
    }

    @Override
    public HttpRawResponse put(String path, String jsonBody) throws IOException {
        captured.add(new CapturedRequest("PUT", path, null, jsonBody, null));
        return next();
    }

    @Override
    public HttpRawResponse delete(String path) throws IOException {
        captured.add(new CapturedRequest("DELETE", path, null, null, null));
        return next();
    }

    @Override
    public byte[] getBinary(String path) throws IOException {
        captured.add(new CapturedRequest("GET_BINARY", path, null, null, null));
        HttpRawResponse response = next();
        return response.getBody() != null ? response.getBody().getBytes() : new byte[0];
    }

    @Override
    public HttpRawResponse postSignature(String path, byte[] imageData) throws IOException {
        captured.add(new CapturedRequest("POST_SIGNATURE", path, null, null, null));
        return next();
    }

    private HttpRawResponse next() throws IOException {
        HttpRawResponse response = queue.poll();
        if (response == null) {
            throw new IOException("No more enqueued responses in MockApiHttpClient");
        }
        return response;
    }

    public static class CapturedRequest {
        private final String method;
        private final String path;
        private final Map<String, Object> queryParams;
        private final String jsonBody;
        private final MultipartData multipartData;

        public CapturedRequest(String method, String path, Map<String, Object> queryParams, String jsonBody, MultipartData multipartData) {
            this.method = method;
            this.path = path;
            this.queryParams = queryParams;
            this.jsonBody = jsonBody;
            this.multipartData = multipartData;
        }

        public String getMethod() { return method; }
        public String getPath() { return path; }
        public Map<String, Object> getQueryParams() { return queryParams; }
        public String getJsonBody() { return jsonBody; }
        public MultipartData getMultipartData() { return multipartData; }
    }

    public static class MultipartData {
        private final String fileName;
        private final byte[] fileData;
        private final String name;
        private final String metadata;

        public MultipartData(String fileName, byte[] fileData, String name, String metadata) {
            this.fileName = fileName;
            this.fileData = fileData;
            this.name = name;
            this.metadata = metadata;
        }

        public String getFileName() { return fileName; }
        public byte[] getFileData() { return fileData; }
        public String getName() { return name; }
        public String getMetadata() { return metadata; }
    }
}
