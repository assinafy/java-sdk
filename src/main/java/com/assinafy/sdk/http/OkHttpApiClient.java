package com.assinafy.sdk.http;

import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkHttpApiClient implements ApiHttpClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType PDF = MediaType.parse("application/pdf");
    private static final MediaType PNG = MediaType.parse("image/png");
    private static final MediaType JPEG = MediaType.parse("image/jpeg");
    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    private static final String SDK_VERSION = "1.3.0";

    private final OkHttpClient client;
    private final String baseUrl;

    public OkHttpApiClient(String baseUrl, String apiKey, String token, long timeoutMs) {
        this.baseUrl = normaliseBaseUrl(baseUrl);
        this.client = new OkHttpClient.Builder()
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder()
                            .header("Accept", "application/json")
                            .header("User-Agent", "assinafy-java-sdk/" + SDK_VERSION);
                    if (apiKey != null && !apiKey.isBlank()) {
                        builder.header("X-Api-Key", apiKey);
                    } else if (token != null && !token.isBlank()) {
                        builder.header("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                .build();
    }

    OkHttpApiClient(OkHttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = normaliseBaseUrl(baseUrl);
    }

    @Override
    public HttpRawResponse get(String path) throws IOException {
        return get(path, null);
    }

    @Override
    public HttpRawResponse get(String path, Map<String, Object> queryParams) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + path).newBuilder();
        if (queryParams != null) {
            queryParams.forEach((k, v) -> {
                if (v != null) {
                    urlBuilder.addQueryParameter(k, String.valueOf(v));
                }
            });
        }
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();
        return execute(request);
    }

    @Override
    public HttpRawResponse post(String path, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody != null ? jsonBody : "{}", JSON);
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .post(body)
                .build();
        return execute(request);
    }

    @Override
    public HttpRawResponse postMultipart(String path, String fileName, byte[] fileData, String name, String metadata) throws IOException {
        MultipartBody.Builder formBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, RequestBody.create(fileData, PDF))
                .addFormDataPart("name", name != null ? name : fileName);
        if (metadata != null) {
            formBuilder.addFormDataPart("metadata", metadata);
        }
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .post(formBuilder.build())
                .build();
        return execute(request);
    }

    @Override
    public HttpRawResponse put(String path, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody != null ? jsonBody : "{}", JSON);
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .put(body)
                .build();
        return execute(request);
    }

    @Override
    public HttpRawResponse delete(String path) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .delete()
                .build();
        return execute(request);
    }

    @Override
    public byte[] getBinary(String path) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return new byte[0];
            }
            return responseBody.bytes();
        }
    }

    @Override
    public HttpRawResponse postSignature(String path, byte[] imageData) throws IOException {
        MediaType mediaType = detectImageMediaType(imageData);
        RequestBody body = RequestBody.create(imageData, mediaType);
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .post(body)
                .build();
        return execute(request);
    }

    private static MediaType detectImageMediaType(byte[] data) {
        if (data != null && data.length >= 3
                && data[0] == JPEG_MAGIC[0] && data[1] == JPEG_MAGIC[1] && data[2] == JPEG_MAGIC[2]) {
            return JPEG;
        }
        return PNG;
    }

    private HttpRawResponse execute(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            Map<String, String> headers = new HashMap<>();
            for (String name : response.headers().names()) {
                headers.put(name.toLowerCase(), response.header(name));
            }
            ResponseBody responseBody = response.body();
            String body = responseBody != null ? responseBody.string() : null;
            return new HttpRawResponse(response.code(), body, headers);
        }
    }

    private static String normaliseBaseUrl(String url) {
        if (url == null) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
