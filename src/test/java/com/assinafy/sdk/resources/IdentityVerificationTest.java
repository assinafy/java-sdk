package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.SignerIdentityReview;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.CreateDocumentFromTemplateRequest;
import com.assinafy.sdk.request.SignerReference;
import com.assinafy.sdk.request.TemplateSigner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class IdentityVerificationTest {
    private final MockApiHttpClient http = new MockApiHttpClient();
    private final SignerResource signers = new SignerResource(http, "account");
    private final AssignmentResource assignments = new AssignmentResource(http, "account");
    private static final String PATH = "/documents/d%2F1/assignments/a%3F1/signers/s%231/identity-verification";
    private static final String REVIEW = """
            {"id":"v1","status":"InReview","provider":"AssinafyVerify",
             "verification_method":"IdDocumentFaceMatchBasic",
             "reason_codes":[{"code":"NAME_MISMATCH","description":"Name differs"}],
             "evidence_available":true,"expected":{"name":"Maria Silva","cpf":"52998224725"},
             "document":{"type":"CNH","name":"MARIA SILVA","cpf":"52998224725"},
             "matches":{"cpf":true,"name":0.52,"birth_date":null},
             "checks":[{"name":"name","result":"review","score":0.52,"reasons":["NAME_MISMATCH"]}],
             "images":["document_front","document_pdf","face"],"ruleset_version":"2026-10-12.2",
             "evidence_sha256":"hash","reviewed_by":{"id":"u1","name":"Reviewer"},
             "reviewed_at":"2026-10-15T12:00:00Z","review_note":"Checked",
             "completed_at":"2026-10-15T12:01:00Z","future_field":true}
            """;

    @Test
    void readsSignerStateAndPreservesTheBearerUrlFragment() {
        http.enqueue(200, """
                {"status":200,"data":{"can_start":false,"status":"Pending",
                "url":"https://verify.example.invalid/c/v1#t=secret",
                "expires_at":"2026-10-20T12:00:00Z","completed_at":null}}
                """);
        var result = signers.identityVerification("code +&");
        assertThat(result.getCanStart()).isFalse();
        assertThat(result.getStatus()).isEqualTo("Pending");
        assertThat(result.getUrl()).isEqualTo("https://verify.example.invalid/c/v1#t=secret");
        assertThat(result.getExpiresAt()).isEqualTo("2026-10-20T12:00:00Z");
        assertThat(result.getCompletedAt()).isNull();
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/signers/identity-verification?signer-access-code=code+%2B%26");
    }

    @Test
    void startsOnlyWithExplicitConsent() {
        http.enqueue(200, """
                {"status":200,"data":{"id":"v1","url":"https://verify.example.invalid/c/v1#t=secret",
                "expires_at":null}}
                """);
        var result = signers.startIdentityVerification("code", true);
        assertThat(result.getId()).isEqualTo("v1");
        assertThat(result.getUrl()).endsWith("#t=secret");
        assertThat(result.getExpiresAt()).isNull();
        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/signers/identity-verification?signer-access-code=code");
        assertThat(http.lastCaptured().getJsonBody()).isEqualTo("{\"consent\":true}");
    }

    @Test
    void rejectsMissingCredentialsAndConsentBeforeSending() {
        assertThatThrownBy(() -> signers.identityVerification(" ")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> signers.startIdentityVerification(null, true)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> signers.startIdentityVerification("code", false)).isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void readsEveryReviewField() {
        http.enqueue(200, "{\"status\":200,\"data\":" + REVIEW + "}");
        SignerIdentityReview review = assignments.identityVerification("d/1", "a?1", "s#1");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(http.lastCaptured().getPath()).isEqualTo(PATH);
        assertThat(review.getId()).isEqualTo("v1");
        assertThat(review.getStatus()).isEqualTo("InReview");
        assertThat(review.getProvider()).isEqualTo("AssinafyVerify");
        assertThat(review.getVerificationMethod()).isEqualTo("IdDocumentFaceMatchBasic");
        assertThat(review.getReasonCodes().getFirst()).containsEntry("code", "NAME_MISMATCH");
        assertThat(review.getEvidenceAvailable()).isTrue();
        assertThat(review.getExpected()).containsEntry("cpf", "52998224725");
        assertThat(review.getDocument()).containsEntry("type", "CNH");
        assertThat(review.getMatches()).containsEntry("name", 0.52).containsEntry("birth_date", null);
        assertThat(review.getChecks().getFirst()).containsEntry("reasons", List.of("NAME_MISMATCH"));
        assertThat(review.getImages()).containsExactly("document_front", "document_pdf", "face");
        assertThat(review.getRulesetVersion()).isEqualTo("2026-10-12.2");
        assertThat(review.getEvidenceSha256()).isEqualTo("hash");
        assertThat(review.getReviewedBy()).containsEntry("id", "u1");
        assertThat(review.getReviewedAt()).isEqualTo("2026-10-15T12:00:00Z");
        assertThat(review.getReviewNote()).isEqualTo("Checked");
        assertThat(review.getCompletedAt()).isEqualTo("2026-10-15T12:01:00Z");
    }

    @ParameterizedTest
    @ValueSource(strings = {"document_front", "document_back", "document_pdf", "face"})
    void downloadsEachDocumentedEvidenceName(String name) {
        http.enqueue(200, "raw-evidence");
        assertThat(assignments.identityVerificationImage("d/1", "a?1", "s#1", name))
                .isEqualTo("raw-evidence".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET_BINARY");
        assertThat(http.lastCaptured().getPath()).isEqualTo(PATH + "/images/" + name);
        assertThat(http.lastCaptured().getAccept()).isEqualTo("image/jpeg, image/png, application/pdf");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Approved", "Rejected"})
    void sendsReviewDecisionAndOptionalNote(String decision) {
        http.enqueue(200, "{\"status\":200,\"data\":" + REVIEW + "}");
        assignments.reviewIdentityVerification("d/1", "a?1", "s#1", decision, null);
        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(http.lastCaptured().getPath()).isEqualTo(PATH + "/review");
        assertThat(http.lastCaptured().getJsonBody()).isEqualTo("{\"decision\":\"" + decision + "\"}");
        http.enqueue(200, "{\"status\":200,\"data\":" + REVIEW + "}");
        assignments.reviewIdentityVerification("d", "a", "s", decision, "😀".repeat(2000));
        assertThat(http.lastCaptured().getJsonBody()).contains("note");
    }

    @Test
    void validatesReviewInputsBeforeSending() {
        assertThatThrownBy(() -> assignments.identityVerification("..", "a", "s")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> assignments.identityVerification("d", "", "s")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> assignments.identityVerification("d", "a", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> assignments.identityVerificationImage("d", "a", "s", "../face"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> assignments.identityVerificationImage("d", "a", "s", null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> assignments.reviewIdentityVerification("d", "a", "s", "approved", null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> assignments.reviewIdentityVerification("d", "a", "s", null, null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> assignments.reviewIdentityVerification("d", "a", "s", "Approved", "😀".repeat(2001)))
                .isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void propagatesApiAndNetworkFailures() {
        http.enqueue(404, "{\"status\":404,\"message\":\"Not found\"}");
        assertThatThrownBy(() -> assignments.identityVerification("d", "a", "s")).isInstanceOf(ApiException.class);
        http.enqueue(400, "{\"status\":400,\"message\":\"Already started\"}");
        assertThatThrownBy(() -> signers.startIdentityVerification("code", true)).isInstanceOf(ApiException.class);
        http.enqueueFailure(new IOException("offline"));
        assertThatThrownBy(() -> signers.identityVerification("code")).isInstanceOf(NetworkException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Email", "Whatsapp"})
    void biometricAssignmentsAndTemplatesAcceptEitherChannel(String channel) {
        DocumentResource documents = new DocumentResource(http, "account");
        var signer = SignerReference.builder().id("s").verificationMethod("IdDocumentFaceMatchBasic")
                .notificationMethods(List.of(channel)).build();
        var assignment = CreateAssignmentRequest.builder().method("virtual")
                .signers(List.of(signer, SignerReference.ofId("other"))).build();
        var template = CreateDocumentFromTemplateRequest.builder().signers(List.of(
                TemplateSigner.builder().id("s").roleId("r").verificationMethod("IdDocumentFaceMatchBasic")
                        .notificationMethods(List.of(channel)).build())).build();
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"a\"}}");
        assignments.create("d", assignment);
        http.enqueue(200, "{\"status\":200,\"data\":{}}");
        assignments.estimateCost("d", assignment);
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d\"}}");
        documents.createFromTemplate("t", template);
        http.enqueue(200, "{\"status\":200,\"data\":{}}");
        documents.estimateCostFromTemplate("t", template);
        for (var captured : http.getCaptured()) {
            assertThat(captured.getJsonBody()).contains("\"verification_method\":\"IdDocumentFaceMatchBasic\"",
                    "\"notification_methods\":[\"" + channel + "\"]");
        }
    }
}
