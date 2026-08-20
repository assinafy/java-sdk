package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.AccountTheme;
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

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts");
        assertThat(workspace.getId()).isEqualTo("ws-1");
    }

    @Test
    void listGetsAccountsEndpoint() {
        mock.enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"acc-123\",\"name\":\"Test\"}]}");

        var result = resource.list();

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts");
        assertThat(result.getData()).extracting("id").containsExactly("acc-123");
    }

    @Test
    void getHitsAccountsWithIdPath() {
        mock.enqueue(200, WORKSPACE_RESPONSE);
        resource.get("acc-123");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
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
    void deleteDefaultIssuesBodylessDelete() {
        mock.enqueue(200, "{\"status\":200,\"data\":[]}");
        resource.delete("acc-123");

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc-123");
        assertThat(mock.lastCaptured().getMethod()).isEqualTo("DELETE");
        assertThat(mock.lastCaptured().getJsonBody()).as("default delete carries no body").isNull();
    }

    @Test
    void deleteWithForceSendsForceTrueBody() {
        mock.enqueue(200, "{\"status\":200,\"data\":[]}");
        resource.delete("acc-123", true);

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("DELETE");
        assertThat(mock.lastCaptured().getJsonBody()).isEqualTo("{\"force\":true}");
    }

    @Test
    void createSerialisesNotificationSenderType() {
        mock.enqueue(200, WORKSPACE_RESPONSE);
        resource.create(CreateWorkspaceRequest.builder().name("Acme").notificationSenderType("Account").build());

        assertThat(mock.lastCaptured().getJsonBody()).contains("\"notification_sender_type\":\"Account\"");
    }

    @Test
    void getThemeHitsThemePathAndParses() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"account_name\":\"MT\",\"primary_color\":\"2072b9\",\"secondary_color\":\"ffffff\",\"logo\":null}}");
        AccountTheme theme = resource.getTheme("acc-123");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc-123/theme");
        assertThat(theme.getAccountName()).isEqualTo("MT");
        assertThat(theme.getPrimaryColor()).isEqualTo("2072b9");
        assertThat(theme.getLogo()).isNull();
    }

    @Test
    void downloadLogoGetsBinaryFromLogoPath() {
        mock.enqueue(200, "PNGDATA");
        byte[] logo = resource.downloadLogo("acc-123");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET_BINARY");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc-123/logo");
        assertThat(new String(logo)).isEqualTo("PNGDATA");
    }

    @Test
    void uploadLogoPostsMultipartFilePart() {
        mock.enqueue(200, "{\"status\":200,\"data\":[]}");
        byte[] png = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
        resource.uploadLogo("acc-123", png, "logo.png");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("POST_FILE");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc-123/logo");
        assertThat(mock.lastCaptured().getMultipartData().getName()).isEqualTo("file"); // part name
        assertThat(mock.lastCaptured().getMultipartData().getMetadata()).isEqualTo("image/png"); // content type
    }

    @Test
    void uploadLogoRejectsEmptyData() {
        assertThatThrownBy(() -> resource.uploadLogo("acc-123", new byte[0], "logo.png"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.uploadLogo("acc-123", new byte[]{1, 2, 3}, "logo.png"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void deleteLogoCallsDeleteOnLogoPath() {
        mock.enqueue(200, "{\"status\":200,\"data\":[]}");
        resource.deleteLogo("acc-123");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("DELETE");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc-123/logo");
    }

    @Test
    void getsAccountStatisticsWithValidatedQuery() {
        mock.enqueue(200, "{\"status\":200,\"data\":[{" +
                "\"period\":\"2026-08\",\"documents_certified\":4}]}");

        var rows = resource.stats("acc-123", "monthly", "2026-08");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(rows).singleElement()
                .satisfies(row -> assertThat(row.getDocumentsCertified()).isEqualTo(4));
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc-123/stats");
        assertThat(mock.lastCaptured().getQueryParams())
                .containsEntry("granularity", "monthly").containsEntry("month", "2026-08");
    }

    @Test
    void validatesWorkspaceRequestsBeforeSending() {
        assertThatThrownBy(() -> resource.create(null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.create(new CreateWorkspaceRequest("")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.update("acc", null)).isInstanceOf(ValidationException.class);
        assertThat(mock.capturedCount()).isZero();
    }
}
