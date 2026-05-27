package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Tag;
import com.assinafy.sdk.request.CreateTagRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.RenameTagRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TagResourceTest {

    private MockApiHttpClient mock;
    private TagResource resource;

    @BeforeEach
    void setUp() {
        mock = new MockApiHttpClient();
        resource = new TagResource(mock, "acc");
    }

    @Test
    void listGetsAccountTagsAndParsesItems() {
        mock.enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"t1\",\"name\":\"Contracts\",\"color\":\"FF0000\"}]}");
        PaginatedResult<Tag> result = resource.list(ListParams.builder().search("con").build());

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/tags");
        assertThat(mock.lastCaptured().getQueryParams()).containsEntry("search", "con");
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getName()).isEqualTo("Contracts");
        assertThat(result.getData().get(0).getColor()).isEqualTo("FF0000");
    }

    @Test
    void createPostsNameAndColor() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"t1\",\"name\":\"Urgent\",\"color\":\"00FF00\"}}");
        Tag tag = resource.create(CreateTagRequest.builder().name("Urgent").color("00FF00").build());

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/tags");
        assertThat(mock.lastCaptured().getJsonBody()).contains("Urgent").contains("00FF00");
        assertThat(tag.getId()).isEqualTo("t1");
    }

    @Test
    void createRequiresName() {
        assertThatThrownBy(() -> resource.create(CreateTagRequest.builder().build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void renamePutsToTagEndpoint() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"t1\",\"name\":\"Renamed\"}}");
        resource.rename("t1", RenameTagRequest.builder().name("Renamed").build());

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/tags/t1");
        assertThat(mock.lastCaptured().getJsonBody()).contains("Renamed");
    }

    @Test
    void deleteWithoutForceOmitsQueryParam() {
        mock.enqueue(200, "{\"status\":200,\"data\":{}}");
        resource.delete("t1");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("DELETE");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/tags/t1");
    }

    @Test
    void deleteWithForceAppendsQueryParam() {
        mock.enqueue(200, "{\"status\":200,\"data\":{}}");
        resource.delete("t1", true);

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/tags/t1?force=true");
    }
}
