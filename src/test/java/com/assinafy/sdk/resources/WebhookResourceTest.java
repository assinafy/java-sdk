package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.WebhookDispatch;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.RegisterWebhookRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class WebhookResourceTest {

    private static final String WEBHOOK_ACTIVE = "{\"status\":200,\"data\":{\"is_active\":true}}";
    private static final String WEBHOOK_INACTIVE = "{\"status\":200,\"data\":{\"is_active\":false}}";

    @Test
    void registerDefaultsIncludeDocumentPrepared() {
        MockApiHttpClient mock = new MockApiHttpClient();
        mock.enqueue(200, WEBHOOK_ACTIVE);

        WebhookResource resource = new WebhookResource(mock, "acc");
        resource.register(RegisterWebhookRequest.builder()
                .url("https://example.com/webhook")
                .email("ops@example.com")
                .build());

        String body = mock.lastCaptured().getJsonBody();
        assertThat(body).contains("document_ready");
        assertThat(body).contains("document_prepared");
        assertThat(body).contains("signer_signed_document");
        assertThat(body).contains("signer_rejected_document");
        assertThat(body).contains("document_processing_failed");
        assertThat(body).contains("\"is_active\":true");
    }

    @Test
    void listEventTypesCallsGlobalEndpoint() {
        MockApiHttpClient mock = new MockApiHttpClient();
        mock.enqueue(200, "{\"status\":200,\"data\":[]}");

        WebhookResource resource = new WebhookResource(mock);
        resource.listEventTypes();

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/webhooks/event-types");
    }

    @Test
    void listDispatchesPassesFiltersAndPaginationHeaders() {
        Map<String, String> headers = Map.of(
                "x-pagination-current-page", "1",
                "x-pagination-per-page", "20",
                "x-pagination-total-count", "2",
                "x-pagination-page-count", "1"
        );
        MockApiHttpClient mock = new MockApiHttpClient();
        mock.enqueue(200, "{\"status\":200,\"data\":[]}", headers);

        WebhookResource resource = new WebhookResource(mock, "acc");
        ListParams params = ListParams.builder().perPage(20).build();
        PaginatedResult<WebhookDispatch> result = resource.listDispatches(params);

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/webhooks");
        assertThat(result.getMeta()).isNotNull();
        assertThat(result.getMeta().getCurrentPage()).isEqualTo(1);
        assertThat(result.getMeta().getPerPage()).isEqualTo(20);
        assertThat(result.getMeta().getTotal()).isEqualTo(2);
        assertThat(result.getMeta().getLastPage()).isEqualTo(1);
    }

    @Test
    void retryDispatchRequiresDispatchId() {
        MockApiHttpClient mock = new MockApiHttpClient();
        WebhookResource resource = new WebhookResource(mock, "acc");
        assertThatThrownBy(() -> resource.retryDispatch(""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void inactivateHitsDocumentedEndpoint() {
        MockApiHttpClient mock = new MockApiHttpClient();
        mock.enqueue(200, WEBHOOK_INACTIVE);

        WebhookResource resource = new WebhookResource(mock, "acc");
        resource.inactivate();

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/webhooks/inactivate");
    }

    @Test
    void registerRequiresUrl() {
        MockApiHttpClient mock = new MockApiHttpClient();
        WebhookResource resource = new WebhookResource(mock, "acc");

        assertThatThrownBy(() -> resource.register(RegisterWebhookRequest.builder()
                .email("ops@example.com")
                .build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void registerRequiresEmail() {
        MockApiHttpClient mock = new MockApiHttpClient();
        WebhookResource resource = new WebhookResource(mock, "acc");

        assertThatThrownBy(() -> resource.register(RegisterWebhookRequest.builder()
                .url("https://example.com/webhook")
                .build()))
                .isInstanceOf(ValidationException.class);
    }
}
