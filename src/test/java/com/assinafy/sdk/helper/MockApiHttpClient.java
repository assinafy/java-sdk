package com.assinafy.sdk.helper;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.http.HttpRawResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class MockApiHttpClient implements ApiHttpClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Deque<Object> queue = new ArrayDeque<>();
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

    public MockApiHttpClient enqueueFailure(IOException failure) {
        queue.add(failure);
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
    public HttpRawResponse postFile(String path, String partName, String fileName, byte[] data, String contentType) throws IOException {
        captured.add(new CapturedRequest("POST_FILE", path, null, null, new MultipartData(fileName, data, partName, contentType)));
        return next();
    }

    @Override
    public HttpRawResponse put(String path, String jsonBody) throws IOException {
        captured.add(new CapturedRequest("PUT", path, null, jsonBody, null));
        return next();
    }

    @Override
    public HttpRawResponse patch(String path, String jsonBody) throws IOException {
        captured.add(new CapturedRequest("PATCH", path, null, jsonBody, null));
        return next();
    }

    @Override
    public HttpRawResponse delete(String path) throws IOException {
        captured.add(new CapturedRequest("DELETE", path, null, null, null));
        return next();
    }

    @Override
    public HttpRawResponse delete(String path, String jsonBody) throws IOException {
        captured.add(new CapturedRequest("DELETE", path, null, jsonBody, null));
        return next();
    }

    @Override
    public byte[] getBinary(String path) throws IOException {
        return getBinary(path, null);
    }

    @Override
    public byte[] getBinary(String path, String accept) throws IOException {
        captured.add(new CapturedRequest("GET_BINARY", path, null, null, null, accept));
        HttpRawResponse response = next();
        // Mirror OkHttpApiClient: a non-2xx download is an error, not file bytes.
        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
            throw ApiException.fromResponse(response.getStatusCode(), parseError(response.getBody()), response.getHeaders());
        }
        String contentType = response.getHeader("Content-Type");
        Object parsed = contentType != null && contentType.toLowerCase(java.util.Locale.ROOT).contains("json")
                ? parseError(response.getBody()) : null;
        if (parsed instanceof Map<?, ?> map && map.get("status") instanceof Number status
                && (status.intValue() < 200 || status.intValue() >= 300)) {
            throw ApiException.fromResponse(status.intValue(), parsed, response.getHeaders());
        }
        return response.getBody() != null ? response.getBody().getBytes(StandardCharsets.UTF_8) : new byte[0];
    }

    @Override
    public HttpRawResponse postSignature(String path, byte[] imageData) throws IOException {
        captured.add(new CapturedRequest("POST_SIGNATURE", path, null, null,
                new MultipartData(null, imageData, null, null)));
        return next();
    }

    private static Object parseError(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            return MAPPER.readValue(body, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ignored) {
            return body;
        }
    }

    private HttpRawResponse next() throws IOException {
        Object queued = queue.poll();
        if (queued == null) {
            throw new IOException("No more enqueued responses in MockApiHttpClient");
        }
        if (queued instanceof IOException failure) throw failure;
        return (HttpRawResponse) queued;
    }

    public static class CapturedRequest {
        private final String method;
        private final String path;
        private final Map<String, Object> queryParams;
        private final String jsonBody;
        private final MultipartData multipartData;
        private final String accept;

        public CapturedRequest(String method, String path, Map<String, Object> queryParams, String jsonBody, MultipartData multipartData) {
            this(method, path, queryParams, jsonBody, multipartData, null);
        }

        public CapturedRequest(String method, String path, Map<String, Object> queryParams,
                               String jsonBody, MultipartData multipartData, String accept) {
            this.method = method;
            this.path = path;
            this.queryParams = queryParams != null
                    ? Collections.unmodifiableMap(new LinkedHashMap<>(queryParams))
                    : null;
            this.jsonBody = jsonBody;
            this.multipartData = multipartData;
            this.accept = accept;
        }

        public String getMethod() { return method; }
        public String getPath() { return path; }
        public Map<String, Object> getQueryParams() { return queryParams; }
        public String getJsonBody() { return jsonBody; }
        public MultipartData getMultipartData() { return multipartData; }
        public String getAccept() { return accept; }
    }

    public static class MultipartData {
        private final String fileName;
        private final byte[] fileData;
        private final String name;
        private final String metadata;

        public MultipartData(String fileName, byte[] fileData, String name, String metadata) {
            this.fileName = fileName;
            this.fileData = fileData != null ? fileData.clone() : null;
            this.name = name;
            this.metadata = metadata;
        }

        public String getFileName() { return fileName; }
        public byte[] getFileData() { return fileData != null ? fileData.clone() : null; }
        public String getName() { return name; }
        public String getMetadata() { return metadata; }
    }
}
