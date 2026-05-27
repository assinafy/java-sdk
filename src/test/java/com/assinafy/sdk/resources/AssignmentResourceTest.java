package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.SignerReference;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class AssignmentResourceTest {

    private static final String ASSIGNMENT_RESPONSE = "{\"status\":200,\"data\":{\"id\":\"assignment-1\"}}";

    @Test
    void buildPayloadNormalisesStringSignerIds() {
        CreateAssignmentRequest req = CreateAssignmentRequest.builder()
                .signers(List.of(SignerReference.ofId("a"), SignerReference.ofId("b")))
                .build();
        Map<String, Object> body = AssignmentResource.buildAssignmentPayload(req, false);

        assertThat(body.get("method")).isEqualTo("virtual");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> signers = (List<Map<String, Object>>) body.get("signers");
        assertThat(signers).hasSize(2);
        assertThat(signers.get(0).get("id")).isEqualTo("a");
        assertThat(signers.get(1).get("id")).isEqualTo("b");
    }

    @Test
    void buildPayloadIncludesOptionalFields() {
        CreateAssignmentRequest req = CreateAssignmentRequest.builder()
                .signers(List.of(SignerReference.ofId("a")))
                .message("hi")
                .expiresAt("2024-12-31")
                .copyReceivers(List.of("c"))
                .build();
        Map<String, Object> body = AssignmentResource.buildAssignmentPayload(req, false);

        assertThat(body.get("message")).isEqualTo("hi");
        assertThat(body.get("expires_at")).isEqualTo("2024-12-31");
        assertThat(body.get("copy_receivers")).isEqualTo(List.of("c"));
    }

    @Test
    void buildPayloadOmitsNullOptionalFields() {
        CreateAssignmentRequest req = CreateAssignmentRequest.builder()
                .signers(List.of(SignerReference.ofId("a")))
                .build();
        Map<String, Object> body = AssignmentResource.buildAssignmentPayload(req, false);

        assertThat(body).doesNotContainKey("message");
        assertThat(body).doesNotContainKey("expires_at");
    }

    @Test
    void buildPayloadThrowsOnEmptySigners() {
        CreateAssignmentRequest req = CreateAssignmentRequest.builder()
                .signers(List.of())
                .build();
        assertThatThrownBy(() -> AssignmentResource.buildAssignmentPayload(req, false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void buildPayloadThrowsOnSignerWithoutIdWhenNotAllowed() {
        CreateAssignmentRequest req = CreateAssignmentRequest.builder()
                .signers(List.of(SignerReference.builder().verificationMethod("Email").build()))
                .build();
        assertThatThrownBy(() -> AssignmentResource.buildAssignmentPayload(req, false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void buildPayloadAllowsSignersWithoutIdForEstimation() {
        SignerReference ref = SignerReference.builder().verificationMethod("Whatsapp").build();
        CreateAssignmentRequest req = CreateAssignmentRequest.builder()
                .signers(List.of(ref))
                .build();
        Map<String, Object> body = AssignmentResource.buildAssignmentPayload(req, true);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> signers = (List<Map<String, Object>>) body.get("signers");
        assertThat(signers.get(0).get("verification_method")).isEqualTo("Whatsapp");
        assertThat(signers.get(0)).doesNotContainKey("id");
    }

    @Test
    void createPostsToDocumentAssignmentsWithNormalisedBody() {
        MockApiHttpClient mock = new MockApiHttpClient();
        mock.enqueue(200, ASSIGNMENT_RESPONSE);

        AssignmentResource resource = new AssignmentResource(mock, "acc");
        CreateAssignmentRequest req = CreateAssignmentRequest.builder()
                .signers(List.of(SignerReference.ofId("s1"), SignerReference.ofId("s2")))
                .build();

        Assignment result = resource.create("doc-1", req);

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/documents/doc-1/assignments");
        assertThat(result.getId()).isEqualTo("assignment-1");

        String body = mock.lastCaptured().getJsonBody();
        assertThat(body).contains("\"method\":\"virtual\"");
        assertThat(body).contains("\"id\":\"s1\"");
        assertThat(body).contains("\"id\":\"s2\"");
    }

    @Test
    void declineRequiresAccessCode() {
        MockApiHttpClient mock = new MockApiHttpClient();
        AssignmentResource resource = new AssignmentResource(mock, "acc");
        assertThatThrownBy(() -> resource.decline("doc", "asg", "", "no"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void declinePutsToRejectEndpointWithEncodedAccessCode() {
        MockApiHttpClient mock = new MockApiHttpClient();
        mock.enqueue(200, "{\"status\":200,\"data\":{}}");

        AssignmentResource resource = new AssignmentResource(mock, "acc");
        resource.decline("doc", "asg", "code with space", "no");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(mock.lastCaptured().getPath())
                .isEqualTo("/documents/doc/assignments/asg/reject?signer-access-code=code+with+space");
        assertThat(mock.lastCaptured().getJsonBody()).contains("\"decline_reason\":\"no\"");
    }

    @Test
    void getWhatsappNotificationsGetsCorrectPath() {
        MockApiHttpClient mock = new MockApiHttpClient();
        mock.enqueue(200, "{\"status\":200,\"data\":[]}");
        AssignmentResource resource = new AssignmentResource(mock, "acc");
        resource.getWhatsappNotifications("doc", "asg");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(mock.lastCaptured().getPath())
                .isEqualTo("/documents/doc/assignments/asg/whatsapp-notifications");
    }

    @Test
    void resendNotificationRequiresAllThreeIds() {
        MockApiHttpClient mock = new MockApiHttpClient();
        AssignmentResource resource = new AssignmentResource(mock, "acc");

        assertThatThrownBy(() -> resource.resendNotification("", "a", "s"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.resendNotification("d", "", "s"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.resendNotification("d", "a", ""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createParsesSigningUrlsAsArrayOfObjects() {
        // Regression: signing_urls is an array of {signer_id, url}; it must not be typed as a Map.
        MockApiHttpClient mock = new MockApiHttpClient();
        mock.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"a1\",\"signing_urls\":"
                + "[{\"signer_id\":\"s1\",\"url\":\"https://app/sign/x\"}]}}");

        AssignmentResource resource = new AssignmentResource(mock, "acc");
        Assignment result = resource.create("doc-1", CreateAssignmentRequest.builder()
                .signers(List.of(SignerReference.ofId("s1")))
                .build());

        assertThat(result.getSigningUrls()).hasSize(1);
        assertThat(result.getSigningUrls().get(0).getSignerId()).isEqualTo("s1");
        assertThat(result.getSigningUrls().get(0).getUrl()).isEqualTo("https://app/sign/x");
    }

    @Test
    void resetExpirationAcceptsNullToClearExpiration() {
        MockApiHttpClient mock = new MockApiHttpClient();
        mock.enqueue(200, ASSIGNMENT_RESPONSE);

        AssignmentResource resource = new AssignmentResource(mock, "acc");
        resource.resetExpiration("doc", "asg", null); // must not throw NullPointerException

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(mock.lastCaptured().getPath())
                .isEqualTo("/documents/doc/assignments/asg/reset-expiration");
        assertThat(mock.lastCaptured().getJsonBody()).contains("expires_at");
    }

    @Test
    void declineRequiresReason() {
        MockApiHttpClient mock = new MockApiHttpClient();
        AssignmentResource resource = new AssignmentResource(mock, "acc");
        assertThatThrownBy(() -> resource.decline("doc", "asg", "code", ""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void buildPayloadIncludesStepForSequentialSigning() {
        CreateAssignmentRequest req = CreateAssignmentRequest.builder()
                .signers(List.of(SignerReference.builder().id("s1").step(2).build()))
                .build();
        Map<String, Object> body = AssignmentResource.buildAssignmentPayload(req, false);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> signers = (List<Map<String, Object>>) body.get("signers");
        assertThat(signers.get(0).get("step")).isEqualTo(2);
    }

    @Test
    void getWhatsappNotificationsReturnsList() {
        MockApiHttpClient mock = new MockApiHttpClient();
        mock.enqueue(200, "{\"status\":200,\"data\":[{\"signer_id\":\"s1\",\"sent_at\":\"2026-01-01T00:00:00Z\"}]}");

        AssignmentResource resource = new AssignmentResource(mock, "acc");
        List<Map<String, Object>> result = resource.getWhatsappNotifications("doc", "asg");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("signer_id")).isEqualTo("s1");
    }

    @Test
    void estimateCostAcceptsSignerDescriptorsWithoutIds() {
        MockApiHttpClient mock = new MockApiHttpClient();
        mock.enqueue(200, "{\"status\":200,\"data\":{\"total_credits\":0.45}}");

        AssignmentResource resource = new AssignmentResource(mock, "acc");
        SignerReference ref = SignerReference.builder().verificationMethod("Whatsapp").build();
        CreateAssignmentRequest req = CreateAssignmentRequest.builder()
                .signers(List.of(ref))
                .build();

        resource.estimateCost("doc-1", req);

        String body = mock.lastCaptured().getJsonBody();
        assertThat(body).contains("\"verification_method\":\"Whatsapp\"");
        assertThat(body).doesNotContain("\"id\"");
    }
}
