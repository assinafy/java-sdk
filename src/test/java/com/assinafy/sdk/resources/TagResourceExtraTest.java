package com.assinafy.sdk.resources;

import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.request.RenameTagRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Covers the documented tri-state {@code color} behaviour of Rename Tag. */
class TagResourceExtraTest {

    private MockApiHttpClient http;
    private TagResource tags;

    @BeforeEach
    void setUp() {
        http = new MockApiHttpClient();
        tags = new TagResource(http, "acc");
    }

    private void enqueueTag() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"t1\",\"name\":\"X\",\"color\":\"112233\"}}");
    }

    @Test
    void renameSetsColorWhenProvided() {
        enqueueTag();
        tags.rename("t1", RenameTagRequest.builder().name("X").color("112233").build());
        String body = http.lastCaptured().getJsonBody();
        assertThat(http.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/acc/tags/t1");
        assertThat(body).contains("\"color\":\"112233\"");
    }

    @Test
    void renameSendsExplicitNullColorWhenCleared() {
        enqueueTag();
        tags.rename("t1", RenameTagRequest.builder().name("X").clearColor().build());
        assertThat(http.lastCaptured().getJsonBody()).contains("\"color\":null");
    }

    @Test
    void renameOmitsColorWhenNeitherSetNorCleared() {
        enqueueTag();
        tags.rename("t1", RenameTagRequest.builder().name("Only Name").build());
        assertThat(http.lastCaptured().getJsonBody()).doesNotContain("color");
    }
}
