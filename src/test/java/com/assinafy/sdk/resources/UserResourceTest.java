package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.NotificationPreferences;
import com.assinafy.sdk.models.AuthUser;
import com.assinafy.sdk.models.DocumentStatsRow;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserResourceTest {

    private static final String RESPONSE = "{\"status\":200,\"data\":{" +
            "\"DocumentCompleted\":false,\"SignerDeclined\":true,\"DocumentCancelled\":true," +
            "\"DocumentAboutToExpire\":true,\"DocumentExpired\":true," +
            "\"DocumentExpirationReset\":true,\"DocumentProcessingFailed\":true," +
            "\"TemplateProcessingFailed\":true,\"SignerWhatsappFailed\":true}}";

    @Test
    void getsAndUpdatesNotificationPreferencesWithExactContractKeys() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, RESPONSE)
                .enqueue(200, RESPONSE);
        UserResource users = new UserResource(http);

        NotificationPreferences current = users.getNotificationPreferences();
        NotificationPreferences updated = users.updateNotificationPreferences(
                Map.of("DocumentCompleted", false));

        assertThat(current.getDocumentCompleted()).isFalse();
        assertThat(updated.getSignerDeclined()).isTrue();
        assertThat(http.capturedAt(0).getMethod()).isEqualTo("GET");
        assertThat(http.capturedAt(0).getPath()).isEqualTo("/users/self/notification-preferences");
        assertThat(http.capturedAt(1).getMethod()).isEqualTo("PUT");
        assertThat(http.capturedAt(1).getPath()).isEqualTo("/users/self/notification-preferences");
        assertThat(http.capturedAt(1).getJsonBody()).isEqualTo("{\"DocumentCompleted\":false}");
    }

    @Test
    void passesThroughEmptyUpdateAndRejectsUnknownKeys() {
        MockApiHttpClient http = new MockApiHttpClient().enqueue(200, RESPONSE);
        UserResource users = new UserResource(http);

        users.updateNotificationPreferences(Map.of());
        assertThat(http.lastCaptured().getJsonBody()).isEqualTo("{}");
        assertThatThrownBy(() -> users.updateNotificationPreferences(Map.of("Unknown", true)))
                .isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isEqualTo(1);
    }

    @Test
    void rejectsEmptyAuthenticatedUserPayload() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":null}")
                .enqueue(200, "{\"status\":200,\"data\":{\"user\":null,\"accounts\":[]}}");
        UserResource users = new UserResource(http);

        assertThatThrownBy(users::get).isExactlyInstanceOf(AssinafyException.class);
        assertThatThrownBy(users::get).isExactlyInstanceOf(AssinafyException.class);
    }

    @Test
    void normalizesCurrentSandboxProfileAndMapsStatistics() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"user\":{" +
                        "\"id\":\"u1\",\"email\":\"user@example.invalid\"," +
                        "\"is_password_set\":true},\"accounts\":[]}}")
                .enqueue(200, "{\"status\":200,\"data\":[{" +
                        "\"period\":\"2026-08\",\"documents_uploaded\":3," +
                        "\"signature_requests_completed\":2}]}");
        UserResource users = new UserResource(http);

        AuthUser user = users.get();
        List<DocumentStatsRow> rows = users.stats("daily", "2026-08");

        assertThat(http.capturedAt(0).getMethod()).isEqualTo("GET");
        assertThat(http.capturedAt(0).getPath()).isEqualTo("/users/self");
        assertThat(user.getId()).isEqualTo("u1");
        assertThat(user.getIsPasswordSet()).isTrue();
        assertThat(rows).singleElement().satisfies(row -> {
            assertThat(row.getPeriod()).isEqualTo("2026-08");
            assertThat(row.getDocumentsUploaded()).isEqualTo(3);
            assertThat(row.getSignatureRequestsCompleted()).isEqualTo(2);
        });
        assertThat(http.capturedAt(1).getMethod()).isEqualTo("GET");
        assertThat(http.capturedAt(1).getPath()).isEqualTo("/users/self/stats");
        assertThat(http.capturedAt(1).getQueryParams())
                .containsEntry("granularity", "daily").containsEntry("month", "2026-08");
    }

    @Test
    void validatesStatisticsQueryBeforeSending() {
        MockApiHttpClient http = new MockApiHttpClient();
        UserResource users = new UserResource(http);

        assertThatThrownBy(() -> users.stats("weekly", null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> users.stats("daily", null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> users.stats("daily", "2026-13"))
                .isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }
}
