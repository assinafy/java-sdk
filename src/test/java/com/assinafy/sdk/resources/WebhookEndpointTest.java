package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.AuthenticationException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.WebhookDispatch;
import com.assinafy.sdk.models.WebhookEndpoint;
import com.assinafy.sdk.request.WebhookEndpointRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class WebhookEndpointTest {

    private static final String ENDPOINT = """
            {"id":"ep1","name":"ERP","url":"https://example.com/hook","email":"ops@example.invalid",
             "events":["document_ready"],"is_active":true,"signing_enabled":true,
             "created_at":"2026-10-01T12:00:00Z","updated_at":"2026-10-01T12:00:00Z"}""";

    private final MockApiHttpClient mock = new MockApiHttpClient();
    private final WebhookResource webhooks = new WebhookResource(mock, "acc");

    @Test
    void listEndpointsDecodesEveryField() {
        mock.enqueue(200, "{\"status\":200,\"data\":[" + ENDPOINT + "]}");

        List<WebhookEndpoint> endpoints = webhooks.listEndpoints();

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/webhooks/endpoints");
        WebhookEndpoint ep = endpoints.getFirst();
        assertThat(ep.getId()).isEqualTo("ep1");
        assertThat(ep.getName()).isEqualTo("ERP");
        assertThat(ep.getUrl()).isEqualTo("https://example.com/hook");
        assertThat(ep.getEmail()).isEqualTo("ops@example.invalid");
        assertThat(ep.getEvents()).containsExactly("document_ready");
        assertThat(ep.getIsActive()).isTrue();
        assertThat(ep.getSigningEnabled()).isTrue();
        assertThat(ep.getCreatedAt()).isEqualTo("2026-10-01T12:00:00Z");
        assertThat(ep.getUpdatedAt()).isEqualTo("2026-10-01T12:00:00Z");
    }

    @Test
    void createEndpointSendsOnlySetFieldsAndDefaultsEvents() {
        mock.enqueue(200, "{\"status\":200,\"data\":" + ENDPOINT + "}");

        webhooks.createEndpoint(WebhookEndpointRequest.builder()
                .url("https://example.com/hook").email("ops@example.invalid")
                .name("ERP").signingEnabled(true).build(), "other");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/other/webhooks/endpoints");
        assertThat(mock.lastCaptured().getJsonBody())
                .contains("\"url\":\"https://example.com/hook\"", "\"email\":\"ops@example.invalid\"",
                        "\"name\":\"ERP\"", "\"signing_enabled\":true", "\"document_prepared\"")
                .doesNotContain("is_active");
    }

    @Test
    void createEndpointKeepsExplicitEvents() {
        mock.enqueue(200, "{\"status\":200,\"data\":" + ENDPOINT + "}");

        webhooks.createEndpoint(WebhookEndpointRequest.builder()
                .url("https://example.com/hook").email("ops@example.invalid")
                .events(List.of("signer_signed_document")).build());

        assertThat(mock.lastCaptured().getJsonBody())
                .contains("\"events\":[\"signer_signed_document\"]")
                .doesNotContain("document_ready");
    }

    @Test
    void createEndpointValidatesLocally() {
        assertThatThrownBy(() -> webhooks.createEndpoint(null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> webhooks.createEndpoint(WebhookEndpointRequest.builder()
                .url("ftp://example.com").email("ops@example.invalid").build()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> webhooks.createEndpoint(WebhookEndpointRequest.builder()
                .url("https://example.com/hook").email("not-an-email").build()))
                .isInstanceOf(ValidationException.class);
        assertThat(mock.capturedCount()).isZero();
    }

    @Test
    void createPastThePlanLimitRaisesAuthenticationException() {
        mock.enqueue(403, "{\"status\":403,\"message\":\"Limite de webhooks atingido.\",\"data\":null}");

        assertThatThrownBy(() -> webhooks.createEndpoint(WebhookEndpointRequest.builder()
                .url("https://example.com/hook").email("ops@example.invalid").build()))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void getEndpointEncodesPath() {
        mock.enqueue(200, "{\"status\":200,\"data\":" + ENDPOINT + "}");

        assertThat(webhooks.getEndpoint("ep1").getId()).isEqualTo("ep1");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/webhooks/endpoints/ep1");
        assertThatThrownBy(() -> webhooks.getEndpoint("..")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> webhooks.getEndpoint(" ")).isInstanceOf(ValidationException.class);
        assertThat(mock.capturedCount()).isEqualTo(1);
    }

    @Test
    void updateEndpointSendsOnlyChangedFields() {
        mock.enqueue(200, "{\"status\":200,\"data\":" + ENDPOINT + "}");

        webhooks.updateEndpoint("ep1", WebhookEndpointRequest.builder().isActive(false).build());

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/webhooks/endpoints/ep1");
        assertThat(mock.lastCaptured().getJsonBody()).isEqualTo("{\"is_active\":false}");
    }

    @Test
    void updateEndpointRejectsEmptyOrInvalidChanges() {
        assertThatThrownBy(() -> webhooks.updateEndpoint("ep1", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> webhooks.updateEndpoint("ep1", new WebhookEndpointRequest()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> webhooks.updateEndpoint("ep1",
                WebhookEndpointRequest.builder().url("relative/path").build()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> webhooks.updateEndpoint("ep1",
                WebhookEndpointRequest.builder().email("bad").build()))
                .isInstanceOf(ValidationException.class);
        assertThat(mock.capturedCount()).isZero();
    }

    @Test
    void deleteEndpointSendsDelete() {
        mock.enqueue(200, "{\"status\":200,\"message\":\"\",\"data\":[]}");

        webhooks.deleteEndpoint("ep1", "other");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("DELETE");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/other/webhooks/endpoints/ep1");
    }

    @Test
    void secretOperationsReturnTheSecretString() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"secret\":\"whsec_old\"}}");
        mock.enqueue(200, "{\"status\":200,\"data\":{\"secret\":\"whsec_new\"}}");

        assertThat(webhooks.getEndpointSecret("ep1")).isEqualTo("whsec_old");
        assertThat(webhooks.rotateEndpointSecret("ep1")).isEqualTo("whsec_new");

        assertThat(mock.capturedAt(0).getMethod()).isEqualTo("GET");
        assertThat(mock.capturedAt(0).getPath()).isEqualTo("/accounts/acc/webhooks/endpoints/ep1/secret");
        assertThat(mock.capturedAt(1).getMethod()).isEqualTo("POST");
        assertThat(mock.capturedAt(1).getPath()).isEqualTo("/accounts/acc/webhooks/endpoints/ep1/secret/rotate");
    }

    @Test
    void dispatchExposesEndpointId() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\",\"endpoint_id\":\"ep1\"}}");

        WebhookDispatch dispatch = webhooks.retryDispatch("d1");

        assertThat(dispatch.getEndpointId()).isEqualTo("ep1");
    }
}
