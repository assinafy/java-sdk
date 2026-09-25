package com.assinafy.sdk.http;

import com.assinafy.sdk.exceptions.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.TlsVersion;
import okio.BufferedSink;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe OkHttp transport for the Assinafy API.
 *
 * <p>The transport does not follow redirects. It prefers API-key authentication when both an API
 * key and bearer token are supplied.
 */
public class OkHttpApiClient implements ApiHttpClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType PDF = MediaType.parse("application/pdf");
    private static final MediaType PNG = MediaType.parse("image/png");
    private static final MediaType JPEG = MediaType.parse("image/jpeg");
    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    /** Lenient mapper used only to extract an error message from a failed binary download. */
    private static final ObjectMapper ERROR_MAPPER = new ObjectMapper();

    private static final String SDK_VERSION = OkHttpApiClient.class.getPackage().getImplementationVersion() != null
            ? OkHttpApiClient.class.getPackage().getImplementationVersion()
            : "development";

    /** HTTPS requires TLS 1.2 or later; cleartext remains only for the loopback URLs this class accepts. */
    private static final List<ConnectionSpec> CONNECTION_SPECS = List.of(
            new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2).build(),
            ConnectionSpec.CLEARTEXT);

    final OkHttpClient client;
    private final String baseUrl;

    /**
     * Create an OkHttp transport.
     *
     * @param baseUrl HTTPS API base URL without a query or fragment; HTTP is accepted only for a
     *                loopback host used in local tests
     * @param apiKey API key sent in {@code X-Api-Key}, or {@code null}
     * @param token bearer token used when {@code apiKey} is blank, or {@code null}
     * @param timeoutMs positive call, connection, read, and write timeout in milliseconds
     * @throws IllegalArgumentException if the base URL is invalid or the timeout is not positive
     */
    public OkHttpApiClient(String baseUrl, String apiKey, String token, long timeoutMs) {
        this(baseUrl, apiKey, token, timeoutMs, true);
    }

    /**
     * Create an OkHttp transport, choosing whether OkHttp may re-send a request on its own.
     *
     * <p>Pass {@code false} for a transport that calls the OAuth token endpoint. OkHttp otherwise
     * re-sends a request after some connection failures, and when the server answers {@code 408}
     * or {@code 503} with {@code Retry-After: 0}. A re-sent refresh grant replays a refresh token
     * the first attempt may already have retired, which ends the user's whole connection. With
     * {@code false}, OkHttp never retries after a connection failure and never sends a request
     * body twice, so the failure or the response reaches the caller instead.
     *
     * @param baseUrl HTTPS API base URL without a query or fragment; HTTP is accepted only for a
     *                loopback host used in local tests
     * @param apiKey API key sent in {@code X-Api-Key}, or {@code null}
     * @param token bearer token used when {@code apiKey} is blank, or {@code null}
     * @param timeoutMs positive call, connection, read, and write timeout in milliseconds
     * @param allowRetries whether OkHttp may re-send a request on its own
     * @throws IllegalArgumentException if the base URL is invalid or the timeout is not positive
     */
    public OkHttpApiClient(String baseUrl, String apiKey, String token, long timeoutMs,
                           boolean allowRetries) {
        if (timeoutMs <= 0) throw new IllegalArgumentException("timeoutMs must be greater than zero");
        this.baseUrl = normaliseBaseUrl(baseUrl);
        this.client = new OkHttpClient.Builder()
                .connectionSpecs(CONNECTION_SPECS)
                .retryOnConnectionFailure(allowRetries)
                .followRedirects(false)
                .followSslRedirects(false)
                .callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Request.Builder builder = request.newBuilder()
                            .header("User-Agent", "assinafy-java-sdk/" + SDK_VERSION);
                    if (request.header("Accept") == null) {
                        builder.header("Accept", "application/json");
                    }
                    if (apiKey != null && !apiKey.isBlank()) {
                        builder.header("X-Api-Key", apiKey);
                    } else if (token != null && !token.isBlank()) {
                        builder.header("Authorization", "Bearer " + token);
                    }
                    // retryOnConnectionFailure(false) does not stop OkHttp repeating a request
                    // answered 503 with Retry-After: 0; a one-shot body is never sent twice.
                    if (!allowRetries && request.body() != null) {
                        builder.method(request.method(), oneShot(request.body()));
                    }
                    return chain.proceed(builder.build());
                })
                .build();
    }

    private static RequestBody oneShot(RequestBody body) {
        return new RequestBody() {
            @Override public MediaType contentType() { return body.contentType(); }
            @Override public long contentLength() throws IOException { return body.contentLength(); }
            @Override public void writeTo(BufferedSink sink) throws IOException { body.writeTo(sink); }
            @Override public boolean isOneShot() { return true; }
        };
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
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(baseUrl + path), "Invalid request URL")
                .newBuilder();
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
        RequestBody body = RequestBody.create(jsonBody != null ? jsonBody : "", JSON);
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
    public HttpRawResponse postFile(String path, String partName, String fileName, byte[] data, String contentType) throws IOException {
        MediaType mediaType = contentType != null ? MediaType.parse(contentType) : null;
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(partName, fileName, RequestBody.create(data, mediaType))
                .build();
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .post(body)
                .build();
        return execute(request);
    }

    @Override
    public HttpRawResponse put(String path, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody != null ? jsonBody : "", JSON);
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .put(body)
                .build();
        return execute(request);
    }

    @Override
    public HttpRawResponse patch(String path, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody != null ? jsonBody : "", JSON);
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .patch(body)
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
    public HttpRawResponse delete(String path, String jsonBody) throws IOException {
        RequestBody body = jsonBody != null ? RequestBody.create(jsonBody, JSON) : null;
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .delete(body)
                .build();
        return execute(request);
    }

    @Override
    public byte[] getBinary(String path) throws IOException {
        return getBinary(path, "*/*");
    }

    @Override
    public byte[] getBinary(String path, String acceptMediaType) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .header("Accept", acceptMediaType)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            Map<String, String> headers = responseHeaders(response);
            // A non-2xx download returns the JSON error envelope as the body. Fail loudly
            // instead of handing those bytes back as if they were the requested artifact.
            if (!response.isSuccessful()) {
                throw ApiException.fromResponse(response.code(), parseErrorBody(responseBody.string()), headers);
            }
            MediaType contentType = responseBody.contentType();
            byte[] bytes = responseBody.bytes();
            boolean declaredJson = contentType != null && (contentType.subtype().equalsIgnoreCase("json")
                    || contentType.subtype().toLowerCase(Locale.ROOT).endsWith("+json"));
            Object parsed = declaredJson
                    ? parseErrorBody(new String(bytes, StandardCharsets.UTF_8))
                    : parseJsonObject(bytes);
            if (parsed instanceof Map<?, ?> map && map.get("status") instanceof Number status
                    && (status.intValue() < 200 || status.intValue() >= 300)) {
                throw ApiException.fromResponse(status.intValue(), parsed, headers);
            }
            return bytes;
        }
    }

    private static Object parseJsonObject(byte[] bytes) {
        int index = 0;
        while (index < bytes.length && (bytes[index] == ' ' || bytes[index] == '\t'
                || bytes[index] == '\r' || bytes[index] == '\n')) index++;
        if (index >= bytes.length || bytes[index] != '{') return null;
        return parseErrorBody(new String(bytes, StandardCharsets.UTF_8));
    }

    /** Parse a binary-endpoint error body into a Map (for a useful message), falling back to the raw text. */
    private static Object parseErrorBody(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return ERROR_MAPPER.readValue(body, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return body;
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
        if (data != null && data.length >= 4 && (data[0] & 0xff) == 0x89
                && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') return PNG;
        throw new IllegalArgumentException("Signature image must be PNG or JPEG");
    }

    private HttpRawResponse execute(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            Map<String, String> headers = responseHeaders(response);
            ResponseBody responseBody = response.body();
            String body = responseBody.string();
            return new HttpRawResponse(response.code(), body, headers);
        }
    }

    private static Map<String, String> responseHeaders(Response response) {
        Map<String, String> headers = new HashMap<>();
        for (String name : response.headers().names()) {
            headers.put(name.toLowerCase(Locale.ROOT), response.header(name));
        }
        return headers;
    }

    private static String normaliseBaseUrl(String url) {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("baseUrl is required");
        String value = url;
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        HttpUrl parsed = HttpUrl.parse(value);
        if (parsed == null || parsed.query() != null || parsed.fragment() != null
                || !parsed.username().isEmpty() || !parsed.password().isEmpty()) {
            throw new IllegalArgumentException(
                    "baseUrl must be an HTTP(S) URL without credentials, query, or fragment");
        }
        String host = parsed.host();
        boolean loopback = host.equals("localhost") || host.equals("127.0.0.1") || host.equals("::1");
        if (!parsed.isHttps() && !loopback) {
            throw new IllegalArgumentException("baseUrl must use HTTPS except for loopback testing");
        }
        return value;
    }
}
