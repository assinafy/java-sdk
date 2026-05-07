package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.Workspace;
import com.assinafy.sdk.request.CreateWorkspaceRequest;
import com.assinafy.sdk.request.UpdateWorkspaceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class WorkspaceResourceTest {

    private MockApiHttpClient mock;
    private WorkspaceResource resource;

    private static final String WORKSPACE_RESPONSE = "{\"status\":200,\"data\":{\"id\":\"ws-1\",\"name\":\"Test\"}}";

    @BeforeEach
    void setUp() {
        mock = new MockApiHttpClient();
        resource = new WorkspaceResource(mock);
    }

    @Test
    void throwsWhenGettingWorkspaceWithoutAccountId() {
        assertThatThrownBy(() -> resource.get(""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void throwsWhenUpdatingWorkspaceWithoutAccountId() {
        assertThatThrownBy(() -> resource.update("", UpdateWorkspaceRequest.builder().name("Test").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void throwsWhenDeletingWorkspaceWithoutAccountId() {
        assertThatThrownBy(() -> resource.delete(""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createPostsToAccountsEndpoint() {
        mock.enqueue(200, WORKSPACE_RESPONSE);
        Workspace workspace = resource.create(new CreateWorkspaceRequest("My Workspace"));

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts");
        assertThat(workspace.getId()).isEqualTo("ws-1");
    }

    @Test
    void getHitsAccountsWithIdPath() {
        mock.enqueue(200, WORKSPACE_RESPONSE);
        resource.get("acc-123");

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc-123");
    }

    @Test
    void updatePutsToAccountsWithIdPath() {
        mock.enqueue(200, WORKSPACE_RESPONSE);
        resource.update("acc-123", UpdateWorkspaceRequest.builder().name("Renamed").build());

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc-123");
        assertThat(mock.lastCaptured().getMethod()).isEqualTo("PUT");
    }

    @Test
    void deleteCallsDeleteOnAccountsWithIdPath() {
        mock.enqueue(200, "{}");
        resource.delete("acc-123");

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc-123");
        assertThat(mock.lastCaptured().getMethod()).isEqualTo("DELETE");
    }
}
