package com.assinafy.sdk.util;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.http.HttpRawResponse;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.PaginationMeta;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Validates Assinafy responses and converts JSON envelopes into SDK return types. */
public final class ResponseHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // The API occasionally emits "" for a typed-object field (e.g. an activity's
            // origin) instead of null; coerce that to null rather than failing the whole parse.
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

    private ResponseHandler() {}

    /**
     * Serialize an SDK request with the same mapper configuration used for responses.
     *
     * @param value request value
     * @return serialized JSON
     * @throws JsonProcessingException if the value cannot be serialized
     */
    public static String serialize(Object value) throws JsonProcessingException {
        return MAPPER.writeValueAsString(value);
    }

    /**
     * Convert a decoded JSON-compatible value to an SDK model.
     *
     * @param <T> model type
     * @param value decoded value
     * @param type target model class
     * @return the converted model
     * @throws AssinafyException if the value cannot be converted
     */
    public static <T> T convert(Object value, Class<T> type) {
        try {
            return MAPPER.convertValue(value, type);
        } catch (IllegalArgumentException e) {
            throw new AssinafyException("Failed to convert response: " + e.getMessage(), Map.of(), e);
        }
    }

    /**
     * Convert a request DTO to its Jackson-annotated wire field map.
     *
     * @param value request DTO
     * @return serialized field names and values
     * @throws AssinafyException if the value cannot be converted
     */
    public static Map<String, Object> convertToMap(Object value) {
        try {
            return MAPPER.convertValue(value, new TypeReference<>() {});
        } catch (IllegalArgumentException e) {
            throw new AssinafyException("Failed to convert request: " + e.getMessage(), Map.of(), e);
        }
    }

    /**
     * Validate and decode a typed response, unwrapping {@code data} when an envelope is present.
     *
     * @param <T> response model type
     * @param response raw HTTP response
     * @param type target model class
     * @return decoded data, or {@code null} for a null envelope data field
     * @throws AssinafyException if the response failed validation or cannot be decoded
     */
    public static <T> T handle(HttpRawResponse response, Class<T> type) {
        validateHttpStatus(response);
        return parseEnvelope(response.getBody(), response.getHeaders(), type);
    }

    /**
     * Validate and decode a response as a field map.
     *
     * @param response raw HTTP response
     * @return decoded data, or an empty map for an empty or null payload
     * @throws AssinafyException if the response failed validation or cannot be decoded
     */
    public static Map<String, Object> handleMap(HttpRawResponse response) {
        validateHttpStatus(response);
        return parseEnvelopeAsMap(response.getBody(), response.getHeaders());
    }

    /**
     * Validate and decode a list response with pagination metadata from response headers.
     *
     * @param <T> list element type
     * @param response raw HTTP response
     * @param elementType target element class
     * @return decoded data and optional pagination metadata
     * @throws AssinafyException if the response failed validation or does not contain a list
     */
    public static <T> PaginatedResult<T> handleList(HttpRawResponse response, Class<T> elementType) {
        validateHttpStatus(response);
        List<T> data = parseListData(response.getBody(), response.getHeaders(), elementType);
        PaginationMeta meta = parsePaginationMeta(response.getHeaders());
        return new PaginatedResult<>(data, meta);
    }

    /**
     * Validate a response whose successful body has no return value.
     *
     * @param response raw HTTP response
     * @throws AssinafyException if the HTTP or envelope status failed, or the body is malformed
     */
    public static void handleVoid(HttpRawResponse response) {
        validateHttpStatus(response);
        String body = response.getBody();
        if (body == null || body.isBlank()) return;
        try {
            validateEnvelopeStatus(MAPPER.readTree(body), response.getHeaders());
        } catch (AssinafyException e) {
            throw e;
        } catch (Exception e) {
            throw new AssinafyException("Failed to parse response: " + e.getMessage(), Map.of(), e);
        }
    }

    /**
     * Normalize a caught operation failure to the SDK exception hierarchy.
     *
     * @param e caught failure
     * @param label operation label prepended to newly wrapped errors
     * @return {@code e} when it is already an SDK exception, a network exception for I/O failures,
     *         or a general SDK exception otherwise
     */
    public static AssinafyException toSdkException(Exception e, String label) {
        if (e instanceof AssinafyException ae) {
            return ae;
        }
        if (e instanceof IOException ioe) {
            return new NetworkException(label + ": " + ioe.getMessage(), ioe);
        }
        return new AssinafyException(label + ": " + e.getMessage(), Map.of(), e);
    }

    private static void validateHttpStatus(HttpRawResponse response) {
        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
            Object responseData = tryParseBody(response.getBody());
            throw ApiException.fromResponse(response.getStatusCode(), responseData, response.getHeaders());
        }
    }

    private static Object tryParseBody(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            return MAPPER.readValue(body, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return body;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T parseEnvelope(String body, Map<String, String> headers, Class<T> type) {
        if (body == null || body.isBlank()) {
            throw new AssinafyException("Response body is empty");
        }
        try {
            JsonNode root = MAPPER.readTree(body);
            validateEnvelopeStatus(root, headers);
            if (isEnvelope(root)) {
                JsonNode dataNode = root.get("data");
                if (dataNode == null || dataNode.isNull()) return null;
                return MAPPER.convertValue(dataNode, type);
            }
            return MAPPER.convertValue(root, type);
        } catch (AssinafyException e) {
            throw e;
        } catch (Exception e) {
            throw new AssinafyException("Failed to parse response: " + e.getMessage(), Map.of(), e);
        }
    }

    private static Map<String, Object> parseEnvelopeAsMap(String body, Map<String, String> headers) {
        if (body == null || body.isBlank()) {
            return Map.of();
        }
        try {
            JsonNode root = MAPPER.readTree(body);
            validateEnvelopeStatus(root, headers);
            if (isEnvelope(root)) {
                JsonNode dataNode = root.get("data");
                if (dataNode == null || dataNode.isNull()) return Map.of();
                if (dataNode.isObject()) {
                    return MAPPER.convertValue(dataNode, new TypeReference<>() {});
                }
                return Collections.singletonMap("data", MAPPER.convertValue(dataNode, Object.class));
            }
            if (root.isObject()) return MAPPER.convertValue(root, new TypeReference<>() {});
            return Collections.singletonMap("data", MAPPER.convertValue(root, Object.class));
        } catch (AssinafyException e) {
            throw e;
        } catch (Exception e) {
            throw new AssinafyException("Failed to parse response: " + e.getMessage(), Map.of(), e);
        }
    }

    private static <T> List<T> parseListData(String body, Map<String, String> headers,
                                              Class<T> elementType) {
        if (body == null || body.isBlank()) {
            throw new AssinafyException("List response body is empty");
        }
        try {
            JsonNode root = MAPPER.readTree(body);
            validateEnvelopeStatus(root, headers);

            if (isEnvelope(root)) {
                return extractArray(root.get("data"), elementType);
            }

            if (root.isArray()) {
                return extractArray(root, elementType);
            }

            if (root.isObject() && root.has("data")) {
                return extractArray(root.get("data"), elementType);
            }

            throw new AssinafyException("List response data is not an array");
        } catch (AssinafyException e) {
            throw e;
        } catch (Exception e) {
            throw new AssinafyException("Failed to parse list response: " + e.getMessage(), Map.of(), e);
        }
    }

    private static <T> List<T> extractArray(JsonNode node, Class<T> elementType) throws IOException {
        if (node == null || node.isNull()) {
            throw new AssinafyException("List response is missing data");
        }
        if (node.isArray()) {
            List<T> result = new ArrayList<>();
            for (JsonNode item : node) {
                result.add(MAPPER.convertValue(item, elementType));
            }
            return result;
        }
        if (node.isObject() && node.has("data") && node.get("data").isArray()) {
            return extractArray(node.get("data"), elementType);
        }
        throw new AssinafyException("List response data is not an array");
    }

    private static void validateEnvelopeStatus(JsonNode root, Map<String, String> headers) {
        if (!isEnvelope(root)) return;
        int status = root.get("status").asInt();
        if (status < 200 || status >= 300) {
            Map<String, Object> response = MAPPER.convertValue(root, new TypeReference<>() {});
            throw ApiException.fromResponse(status, response, headers);
        }
    }

    private static boolean isEnvelope(JsonNode root) {
        return root.isObject() && root.has("status") && root.get("status").isIntegralNumber();
    }

    private static PaginationMeta parsePaginationMeta(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) return null;

        Integer currentPage = parseIntHeader(headers, "x-pagination-current-page");
        Integer perPage = parseIntHeader(headers, "x-pagination-per-page");
        Integer total = parseIntHeader(headers, "x-pagination-total-count");
        Integer lastPage = parseIntHeader(headers, "x-pagination-page-count");

        if (currentPage == null && perPage == null && total == null && lastPage == null) {
            return null;
        }

        PaginationMeta meta = new PaginationMeta();
        meta.setCurrentPage(currentPage);
        meta.setPerPage(perPage);
        meta.setTotal(total);
        meta.setLastPage(lastPage);
        return meta;
    }

    private static Integer parseIntHeader(Map<String, String> headers, String name) {
        String value = headers.get(name);
        if (value == null) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
