package com.assinafy.sdk.util;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.http.HttpRawResponse;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.PaginationMeta;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ResponseHandler {

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private ResponseHandler() {}

    public static <T> T handle(HttpRawResponse response, Class<T> type) {
        validateHttpStatus(response);
        return parseEnvelope(response.getBody(), type);
    }

    public static Map<String, Object> handleMap(HttpRawResponse response) {
        validateHttpStatus(response);
        return parseEnvelopeAsMap(response.getBody());
    }

    public static <T> PaginatedResult<T> handleList(HttpRawResponse response, Class<T> elementType) {
        validateHttpStatus(response);
        List<T> data = parseListData(response.getBody(), elementType);
        PaginationMeta meta = parsePaginationMeta(response.getHeaders());
        return new PaginatedResult<>(data, meta);
    }

    public static void handleVoid(HttpRawResponse response) {
        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
            throw ApiException.fromResponse(response.getStatusCode(), response.getBody());
        }
    }

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
            throw ApiException.fromResponse(response.getStatusCode(), responseData);
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
    private static <T> T parseEnvelope(String body, Class<T> type) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode root = MAPPER.readTree(body);
            if (root.isObject() && root.has("status") && root.has("data")) {
                int envelopeStatus = root.get("status").asInt();
                if (envelopeStatus >= 200 && envelopeStatus < 300) {
                    JsonNode dataNode = root.get("data");
                    return MAPPER.convertValue(dataNode, type);
                }
                Map<String, Object> responseMap = MAPPER.convertValue(root, new TypeReference<>() {});
                throw ApiException.fromResponse(envelopeStatus, responseMap);
            }
            return MAPPER.convertValue(root, type);
        } catch (AssinafyException e) {
            throw e;
        } catch (Exception e) {
            throw new AssinafyException("Failed to parse response: " + e.getMessage(), Map.of(), e);
        }
    }

    private static Map<String, Object> parseEnvelopeAsMap(String body) {
        if (body == null || body.isBlank()) {
            return Map.of();
        }
        try {
            JsonNode root = MAPPER.readTree(body);
            if (root.isObject() && root.has("status") && root.has("data")) {
                int envelopeStatus = root.get("status").asInt();
                if (envelopeStatus >= 200 && envelopeStatus < 300) {
                    JsonNode dataNode = root.get("data");
                    if (dataNode.isObject()) {
                        return MAPPER.convertValue(dataNode, new TypeReference<>() {});
                    }
                    return Map.of("data", MAPPER.convertValue(dataNode, Object.class));
                }
                Map<String, Object> responseMap = MAPPER.convertValue(root, new TypeReference<>() {});
                throw ApiException.fromResponse(envelopeStatus, responseMap);
            }
            return MAPPER.convertValue(root, new TypeReference<>() {});
        } catch (AssinafyException e) {
            throw e;
        } catch (Exception e) {
            throw new AssinafyException("Failed to parse response: " + e.getMessage(), Map.of(), e);
        }
    }

    private static <T> List<T> parseListData(String body, Class<T> elementType) {
        if (body == null || body.isBlank()) {
            return new ArrayList<>();
        }
        try {
            JsonNode root = MAPPER.readTree(body);

            if (root.isObject() && root.has("status") && root.has("data")) {
                int envelopeStatus = root.get("status").asInt();
                if (envelopeStatus < 200 || envelopeStatus >= 300) {
                    Map<String, Object> responseMap = MAPPER.convertValue(root, new TypeReference<>() {});
                    throw ApiException.fromResponse(envelopeStatus, responseMap);
                }
                JsonNode dataNode = root.get("data");
                return extractArray(dataNode, elementType);
            }

            if (root.isArray()) {
                return extractArray(root, elementType);
            }

            if (root.isObject() && root.has("data")) {
                return extractArray(root.get("data"), elementType);
            }

            return new ArrayList<>();
        } catch (AssinafyException e) {
            throw e;
        } catch (Exception e) {
            throw new AssinafyException("Failed to parse list response: " + e.getMessage(), Map.of(), e);
        }
    }

    private static <T> List<T> extractArray(JsonNode node, Class<T> elementType) throws IOException {
        if (node == null || node.isNull()) {
            return new ArrayList<>();
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
        return new ArrayList<>();
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
