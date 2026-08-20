package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.AuthAccount;
import com.assinafy.sdk.models.AuthSession;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticationResourceTest {

    private static final String SESSION = "{\"status\":200,\"data\":{" +
            "\"access_token\":\"token\",\"user\":{\"id\":\"u1\",\"email\":\"user@example.invalid\"}," +
            "\"accounts\":[{\"id\":\"a1\",\"name\":\"Example\",\"roles\":[\"owner\"]," +
            "\"is_delete_allowed\":true,\"created_at\":\"2026-08-20T12:00:00Z\"}]}}";
    private static final String SUCCESS = "{\"status\":200,\"message\":\"\"}";

    @Test
    void mapsAllDocumentedAuthenticationOperations() {
        MockApiHttpClient http = new MockApiHttpClient()
                .enqueue(200, SESSION)
                .enqueue(200, SESSION)
                .enqueue(200, SUCCESS)
                .enqueue(200, SUCCESS)
                .enqueue(200, SUCCESS)
                .enqueue(200, SUCCESS);
        AuthenticationResource authentication = new AuthenticationResource(http);

        AuthSession login = authentication.login("user@example.invalid", "password");
        AuthSession social = authentication.socialLogin("google", "provider-token", true);
        authentication.linkSocialLogin("google", "provider-token");
        authentication.changePassword("user@example.invalid", "old", "new");
        authentication.requestPasswordReset("user@example.invalid");
        authentication.resetPassword("user@example.invalid", null, "new");

        assertThat(login.getAccessToken()).isEqualTo("token");
        assertThat(login.getUser().getId()).isEqualTo("u1");
        assertThat(login.getAccounts())
                .singleElement()
                .isInstanceOfSatisfying(AuthAccount.class, account -> {
                    assertThat(account.getId()).isEqualTo("a1");
                    assertThat(account.getName()).isEqualTo("Example");
                    assertThat(account.getRoles()).containsExactly("owner");
                    assertThat(account.getIsDeleteAllowed()).isTrue();
                    assertThat(account.getCreatedAt()).isEqualTo("2026-08-20T12:00:00Z");
                });
        assertThat(social.getAccessToken()).isEqualTo("token");
        assertThat(http.capturedAt(0).getMethod()).isEqualTo("POST");
        assertThat(http.capturedAt(0).getPath()).isEqualTo("/login");
        assertThat(http.capturedAt(1).getMethod()).isEqualTo("POST");
        assertThat(http.capturedAt(1).getPath()).isEqualTo("/authentication/social-login");
        assertThat(http.capturedAt(1).getJsonBody()).contains("\"has_accepted_terms\":true");
        assertThat(http.capturedAt(2).getMethod()).isEqualTo("POST");
        assertThat(http.capturedAt(2).getPath()).isEqualTo("/auth/link-social-login");
        assertThat(http.capturedAt(3).getMethod()).isEqualTo("PUT");
        assertThat(http.capturedAt(3).getPath()).isEqualTo("/authentication/change-password");
        assertThat(http.capturedAt(4).getMethod()).isEqualTo("PUT");
        assertThat(http.capturedAt(4).getPath()).isEqualTo("/authentication/request-password-reset");
        assertThat(http.capturedAt(5).getMethod()).isEqualTo("PUT");
        assertThat(http.capturedAt(5).getPath()).isEqualTo("/authentication/reset-password");
        assertThat(http.capturedAt(5).getJsonBody()).doesNotContain("token");
    }

    @Test
    void validatesRequiredAuthenticationFieldsBeforeSending() {
        MockApiHttpClient http = new MockApiHttpClient();
        AuthenticationResource authentication = new AuthenticationResource(http);

        assertThatThrownBy(() -> authentication.login("invalid", "password"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> authentication.socialLogin("", "token", false))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> authentication.socialLogin("github", "token", false))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> authentication.linkSocialLogin("github", "token"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> authentication.resetPassword("user@example.invalid", null, ""))
                .isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }
}
