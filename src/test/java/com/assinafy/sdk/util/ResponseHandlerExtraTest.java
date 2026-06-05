package com.assinafy.sdk.util;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.AuthenticationException;
import com.assinafy.sdk.exceptions.RateLimitException;
import com.assinafy.sdk.http.HttpRawResponse;
import com.assinafy.sdk.models.DocumentActivity;
import com.assinafy.sdk.models.PaginatedResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/** Error/edge-branch coverage for {@link ResponseHandler} beyond the original happy-path tests. */
class ResponseHandlerExtraTest {

    private static HttpRawResponse resp(int status, String body) {
        return new HttpRawResponse(status, body, Map.of());
    }

    @Test
    void handleVoidThrowsOnInBodyErrorEnvelopeDespiteHttp200() {
        // HTTP 200 but the envelope reports 403 — handleVoid must not treat this as success.
        assertThatThrownBy(() -> ResponseHandler.handleVoid(
                resp(200, "{\"status\":403,\"message\":\"Forbidden\",\"data\":null}")))
                .isInstanceOf(AuthenticationException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatusCode()).isEqualTo(403));
    }

    @Test
    void handleVoidThrowsOnHttpError() {
        assertThatThrownBy(() -> ResponseHandler.handleVoid(resp(404, "{\"message\":\"gone\"}")))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatusCode()).isEqualTo(404));
    }

    @Test
    void handleVoidAcceptsSuccessEnvelopeAndEmptyBody() {
        assertThatCode(() -> ResponseHandler.handleVoid(resp(200, "{\"status\":200,\"data\":[]}")))
                .doesNotThrowAnyException();
        assertThatCode(() -> ResponseHandler.handleVoid(resp(204, ""))).doesNotThrowAnyException();
    }

    @Test
    void mapsStatusCodesToSpecificExceptionSubtypes() {
        assertThatThrownBy(() -> ResponseHandler.handle(resp(401, "{\"message\":\"no\"}"), Map.class))
                .isInstanceOf(AuthenticationException.class);
        assertThatThrownBy(() -> ResponseHandler.handle(resp(403, "{\"message\":\"no\"}"), Map.class))
                .isInstanceOf(AuthenticationException.class);
        assertThatThrownBy(() -> ResponseHandler.handle(resp(429, "{\"message\":\"slow down\"}"), Map.class))
                .isInstanceOf(RateLimitException.class)
                .satisfies(e -> assertThat(e.getMessage()).isEqualTo("slow down"));
        assertThatThrownBy(() -> ResponseHandler.handle(resp(500, "{\"message\":\"boom\"}"), Map.class))
                .isInstanceOf(ApiException.class)
                .isNotInstanceOf(AuthenticationException.class)
                .isNotInstanceOf(RateLimitException.class);
    }

    @Test
    void usesPlainTextErrorBodyAsMessage() {
        assertThatThrownBy(() -> ResponseHandler.handle(resp(500, "Internal Server Error"), Map.class))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(e.getMessage()).contains("Internal Server Error"));
    }

    @Test
    void handleListThrowsOnErrorEnvelope() {
        assertThatThrownBy(() -> ResponseHandler.handleList(
                resp(200, "{\"status\":403,\"message\":\"nope\",\"data\":null}"), Map.class))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void handleListReadsBareArrayAndDoubleNestedData() {
        PaginatedResult<Map> bare = ResponseHandler.handleList(resp(200, "[{\"id\":\"1\"}]"), Map.class);
        assertThat(bare.getData()).hasSize(1);

        PaginatedResult<Map> nested = ResponseHandler.handleList(
                resp(200, "{\"status\":200,\"data\":{\"data\":[{\"id\":\"a\"},{\"id\":\"b\"}]}}"), Map.class);
        assertThat(nested.getData()).hasSize(2);
    }

    @Test
    void emptyStringObjectIsCoercedToNullRatherThanThrowing() {
        // The docs show an activity origin rendered as "" — must not fail the parse.
        DocumentActivity activity = ResponseHandler.handle(
                resp(200, "{\"id\":1,\"event\":\"x\",\"origin\":\"\"}"), DocumentActivity.class);
        assertThat(activity.getOrigin()).isNull();
        assertThat(activity.getEvent()).isEqualTo("x");
    }

    @Test
    void handleReturnsNullOnBlankBody() {
        assertThat(ResponseHandler.handle(resp(200, ""), Map.class)).isNull();
    }
}
