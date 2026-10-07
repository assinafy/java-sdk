package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.AuthSession;
import com.assinafy.sdk.models.MfaStatus;
import com.assinafy.sdk.models.TotpEnrollment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MfaTest {

    private final MockApiHttpClient mock = new MockApiHttpClient();
    private final AuthenticationResource auth = new AuthenticationResource(mock);
    private final UserResource users = new UserResource(mock);

    @Test
    void loginExposesTheChallengeAndVerifyCompletesIt() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"mfa_token\":\"challenge\"}}");
        mock.enqueue(200, "{\"status\":200,\"data\":{\"access_token\":\"jwt\",\"user\":{\"id\":\"u1\"},\"accounts\":[]}}");

        AuthSession first = auth.login("user@example.invalid", "secret");
        AuthSession session = auth.verifyMfa(first.getMfaToken(), " 123456 ");

        assertThat(first.getAccessToken()).isNull();
        assertThat(session.getAccessToken()).isEqualTo("jwt");
        assertThat(mock.capturedAt(1).getMethod()).isEqualTo("POST");
        assertThat(mock.capturedAt(1).getPath()).isEqualTo("/authentication/mfa/verify");
        assertThat(mock.capturedAt(1).getJsonBody())
                .contains("\"mfa_token\":\"challenge\"", "\"code\":\"123456\"");
    }

    @Test
    void verifyRequiresTokenAndCode() {
        assertThatThrownBy(() -> auth.verifyMfa(null, "123456")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> auth.verifyMfa("challenge", " ")).isInstanceOf(ValidationException.class);
        assertThat(mock.capturedCount()).isZero();
    }

    @Test
    void listMfaMethodsDecodesStatus() {
        mock.enqueue(200, """
                {"status":200,"data":{"methods":[{"id":"m1","type":"Totp","label":"Phone",
                 "confirmed_at":"2026-10-01T12:00:00Z","last_used_at":null}],"recovery_codes_remaining":9}}""");

        MfaStatus status = users.listMfaMethods();

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/users/self/mfa");
        assertThat(status.getRecoveryCodesRemaining()).isEqualTo(9);
        assertThat(status.getMethods().getFirst().getId()).isEqualTo("m1");
        assertThat(status.getMethods().getFirst().getType()).isEqualTo("Totp");
        assertThat(status.getMethods().getFirst().getLabel()).isEqualTo("Phone");
        assertThat(status.getMethods().getFirst().getConfirmedAt()).isEqualTo("2026-10-01T12:00:00Z");
        assertThat(status.getMethods().getFirst().getLastUsedAt()).isNull();
    }

    @Test
    void enrollmentStartsAndConfirms() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"m1\",\"secret\":\"S\",\"provisioning_uri\":\"otpauth://totp/x\"}}");
        mock.enqueue(200, "{\"status\":200,\"data\":{}}");
        mock.enqueue(200, "{\"status\":200,\"data\":{\"recovery_codes\":[\"AAAA-BBBB-CCCC\"]}}");

        TotpEnrollment enrollment = users.startTotpEnrollment("Phone");
        users.startTotpEnrollment(null);
        assertThat(users.confirmTotpEnrollment(enrollment.getId(), "123456", null, null))
                .containsExactly("AAAA-BBBB-CCCC");

        assertThat(enrollment.getSecret()).isEqualTo("S");
        assertThat(enrollment.getProvisioningUri()).isEqualTo("otpauth://totp/x");
        assertThat(mock.capturedAt(0).getPath()).isEqualTo("/users/self/mfa/totp");
        assertThat(mock.capturedAt(0).getJsonBody()).isEqualTo("{\"label\":\"Phone\"}");
        assertThat(mock.capturedAt(1).getJsonBody()).isEqualTo("{}");
        assertThat(mock.capturedAt(2).getMethod()).isEqualTo("PUT");
        assertThat(mock.capturedAt(2).getPath()).isEqualTo("/users/self/mfa/totp/confirm");
        assertThat(mock.capturedAt(2).getJsonBody()).isEqualTo("{\"id\":\"m1\",\"code\":\"123456\"}");
    }

    @Test
    void replacingAMethodSendsReauthentication() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"recovery_codes\":[]}}");

        users.confirmTotpEnrollment("m2", "123456", null, "654321");

        assertThat(mock.lastCaptured().getJsonBody()).contains("\"reauth_code\":\"654321\"").doesNotContain("password");
    }

    @Test
    void regenerateAndRemoveRequireReauthentication() {
        assertThatThrownBy(() -> users.regenerateRecoveryCodes(null, " ")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> users.removeMfaMethod("m1", "", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> users.confirmTotpEnrollment("m1", null, null, null)).isInstanceOf(ValidationException.class);
        assertThat(mock.capturedCount()).isZero();
    }

    @Test
    void regenerateAndRemoveSendTheProof() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"recovery_codes\":[\"A\",\"B\"]}}");
        mock.enqueue(200, "{\"status\":200,\"data\":{\"is_mfa_enabled\":false}}");

        assertThat(users.regenerateRecoveryCodes("pw", null)).containsExactly("A", "B");
        assertThat(users.removeMfaMethod("m1", null, "ABCD-EFGH-JKMN")).isFalse();

        assertThat(mock.capturedAt(0).getMethod()).isEqualTo("POST");
        assertThat(mock.capturedAt(0).getPath()).isEqualTo("/users/self/mfa/recovery-codes");
        assertThat(mock.capturedAt(0).getJsonBody()).isEqualTo("{\"password\":\"pw\"}");
        assertThat(mock.capturedAt(1).getMethod()).isEqualTo("DELETE");
        assertThat(mock.capturedAt(1).getPath()).isEqualTo("/users/self/mfa/m1");
        assertThat(mock.capturedAt(1).getJsonBody()).isEqualTo("{\"code\":\"ABCD-EFGH-JKMN\"}");
    }
}
