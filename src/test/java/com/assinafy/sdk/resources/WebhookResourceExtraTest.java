package com.assinafy.sdk.resources;

import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.WebhookSubscription;
import com.assinafy.sdk.request.RegisterWebhookRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** Locks down the webhook subscription verb/path and the get()-404-to-null contract. */
class WebhookResourceExtraTest {

    private MockApiHttpClient http;
    private WebhookResource webhooks;

    @BeforeEach
    void setUp() {
        http = new MockApiHttpClient();
        webhooks = new WebhookResource(http, "acc");
    }

    @Test
    void registerUsesPutToSubscriptionsPath() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"url\":\"https://e.com\",\"email\":\"a@e.com\"," +
                "\"events\":[\"document_ready\"],\"is_active\":true}}");
        webhooks.register(RegisterWebhookRequest.builder()
                .url("https://e.com").email("a@e.com").events(List.of("document_ready")).build());

        assertThat(http.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/acc/webhooks/subscriptions");
    }

    @Test
    void getReturnsNullOn404() {
        http.enqueue(404, "{\"status\":404,\"message\":\"no subscription\",\"data\":null}");
        assertThat(webhooks.get()).isNull();
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/acc/webhooks/subscriptions");
    }

    @Test
    void getParsesSubscriptionOn200() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"url\":\"https://e.com\",\"email\":\"a@e.com\"," +
                "\"events\":[\"document_ready\"],\"is_active\":false,\"updated_at\":\"2026-01-01T00:00:00Z\"}}");
        WebhookSubscription sub = webhooks.get();
        assertThat(sub).isNotNull();
        assertThat(sub.getUrl()).isEqualTo("https://e.com");
        assertThat(sub.getIsActive()).isFalse();
    }
}
