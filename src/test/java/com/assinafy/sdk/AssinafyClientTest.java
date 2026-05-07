package com.assinafy.sdk;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AssinafyClientTest {

    @Test
    void throwsWhenNoCredentialsProvided() {
        assertThatThrownBy(() ->
                new AssinafyClient(AssinafyClientOptions.builder().accountId("acc").build())
        ).isInstanceOf(ValidationException.class);
    }

    @Test
    void acceptsApiKeyCredentials() {
        AssinafyClient client = new AssinafyClient(AssinafyClientOptions.builder()
                .apiKey("k")
                .accountId("acc")
                .build());
        assertThat(client.documents()).isNotNull();
        assertThat(client.signers()).isNotNull();
        assertThat(client.workspaces()).isNotNull();
        assertThat(client.assignments()).isNotNull();
        assertThat(client.webhooks()).isNotNull();
        assertThat(client.webhookVerifier()).isNotNull();
    }

    @Test
    void acceptsLegacyTokenCredentials() {
        AssinafyClient client = new AssinafyClient(AssinafyClientOptions.builder()
                .token("t")
                .accountId("acc")
                .build());
        assertThat(client.documents()).isNotNull();
    }

    @Test
    void staticCreateBuildsConfiguredClient() {
        AssinafyClient client = AssinafyClient.create("k", "acc");
        assertThat(client.documents()).isNotNull();
    }

    @Test
    void staticCreateWithOptionsBuildsConfiguredClient() {
        AssinafyClientOptions extras = AssinafyClientOptions.builder()
                .webhookSecret("s")
                .build();
        AssinafyClient client = AssinafyClient.create("k", "acc", extras);
        assertThat(client.webhookVerifier()).isNotNull();
    }

    @Test
    void throwsValidationExceptionWithInjectableHttpClient() {
        MockApiHttpClient mock = new MockApiHttpClient();
        assertThatThrownBy(() ->
                new AssinafyClient(mock, AssinafyClientOptions.builder().accountId("acc").build())
        ).isInstanceOf(ValidationException.class);
    }

    @Test
    void buildsClientWithInjectableHttpClient() {
        MockApiHttpClient mock = new MockApiHttpClient();
        AssinafyClient client = new AssinafyClient(mock, AssinafyClientOptions.builder()
                .apiKey("k")
                .accountId("acc")
                .build());
        assertThat(client.documents()).isNotNull();
        assertThat(client.signers()).isNotNull();
        assertThat(client.templates()).isNotNull();
    }
}
