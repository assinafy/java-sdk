package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.ApiKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ApiKeyResourceTest {

    private MockApiHttpClient mock;
    private ApiKeyResource resource;

    @BeforeEach
    void setUp() {
        mock = new MockApiHttpClient();
        resource = new ApiKeyResource(mock);
    }

    @Test
    void getReturnsMaskedKey() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"api_key\":\"****nBNr\"}}");
        ApiKey key = resource.get();

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/users/api-keys");
        assertThat(key.getApiKey()).isEqualTo("****nBNr");
    }

    @Test
    void createPostsPasswordAndReturnsFullKey() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"api_key\":\"full-secret-key\"}}");
        ApiKey key = resource.create("hunter2");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/users/api-keys");
        assertThat(mock.lastCaptured().getJsonBody()).contains("password").contains("hunter2");
        assertThat(key.getApiKey()).isEqualTo("full-secret-key");
    }

    @Test
    void createRequiresPassword() {
        assertThatThrownBy(() -> resource.create(""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void deleteCallsDelete() {
        mock.enqueue(200, "{\"status\":200,\"data\":[]}");
        resource.delete();

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("DELETE");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/users/api-keys");
    }
}
