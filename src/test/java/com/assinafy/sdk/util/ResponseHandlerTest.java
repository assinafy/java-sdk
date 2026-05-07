package com.assinafy.sdk.util;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.http.HttpRawResponse;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ResponseHandlerTest {

    @Test
    void handleUnwrapsEnvelopeOnSuccessStatus() {
        HttpRawResponse response = new HttpRawResponse(200, "{\"status\":200,\"data\":{\"id\":\"123\"}}", Map.of());
        Signer signer = ResponseHandler.handle(response, Signer.class);
        assertThat(signer.getId()).isEqualTo("123");
    }

    @Test
    void handleThrowsApiExceptionOnNonSuccessEnvelope() {
        HttpRawResponse response = new HttpRawResponse(200, "{\"status\":400,\"message\":\"Bad\",\"data\":{}}", Map.of());
        assertThatThrownBy(() -> ResponseHandler.handle(response, Signer.class))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Bad");
    }

    @Test
    void handleThrowsApiExceptionOnHttpError() {
        HttpRawResponse response = new HttpRawResponse(422, "{\"message\":\"Unprocessable\"}", Map.of());
        assertThatThrownBy(() -> ResponseHandler.handle(response, Signer.class))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatusCode()).isEqualTo(422));
    }

    @Test
    void handlePassesThroughDirectObjectWhenNoEnvelope() {
        HttpRawResponse response = new HttpRawResponse(200, "{\"id\":\"abc\"}", Map.of());
        Signer signer = ResponseHandler.handle(response, Signer.class);
        assertThat(signer.getId()).isEqualTo("abc");
    }

    @Test
    void handleListReturnsDataAndMeta() {
        Map<String, String> headers = Map.of(
                "x-pagination-current-page", "2",
                "x-pagination-per-page", "20",
                "x-pagination-total-count", "45",
                "x-pagination-page-count", "3"
        );
        String body = "{\"status\":200,\"data\":[{\"id\":\"s1\"},{\"id\":\"s2\"}]}";
        HttpRawResponse response = new HttpRawResponse(200, body, headers);
        PaginatedResult<Signer> result = ResponseHandler.handleList(response, Signer.class);
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getMeta()).isNotNull();
        assertThat(result.getMeta().getCurrentPage()).isEqualTo(2);
        assertThat(result.getMeta().getPerPage()).isEqualTo(20);
        assertThat(result.getMeta().getTotal()).isEqualTo(45);
        assertThat(result.getMeta().getLastPage()).isEqualTo(3);
    }

    @Test
    void handleListReturnsEmptyListWithoutHeaders() {
        HttpRawResponse response = new HttpRawResponse(200, "{\"status\":200,\"data\":[]}", Map.of());
        PaginatedResult<Signer> result = ResponseHandler.handleList(response, Signer.class);
        assertThat(result.getData()).isEmpty();
        assertThat(result.getMeta()).isNull();
    }

    @Test
    void toSdkExceptionPassesThroughAssinafyException() {
        AssinafyException original = new AssinafyException("original");
        AssinafyException result = ResponseHandler.toSdkException(original, "ignored");
        assertThat(result).isSameAs(original);
    }

    @Test
    void toSdkExceptionWrapsIoExceptionAsNetworkException() {
        IOException ioe = new IOException("connect refused");
        AssinafyException result = ResponseHandler.toSdkException(ioe, "upload");
        assertThat(result).isInstanceOf(NetworkException.class);
        assertThat(result.getMessage()).contains("upload");
    }

    @Test
    void toSdkExceptionWrapsGenericExceptionAsAssinafyException() {
        RuntimeException rte = new RuntimeException("boom");
        AssinafyException result = ResponseHandler.toSdkException(rte, "failed");
        assertThat(result).isInstanceOf(AssinafyException.class);
        assertThat(result.getMessage()).contains("failed").contains("boom");
    }
}
