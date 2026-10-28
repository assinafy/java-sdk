package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.CostEstimate;
import com.assinafy.sdk.models.Document;
import com.assinafy.sdk.models.ResendNotificationResponse;
import com.assinafy.sdk.models.ResendCostEstimate;
import com.assinafy.sdk.models.WhatsappNotification;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.SignerReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/** Coverage for the assignment signer-flow and cost/notification endpoints. */
class AssignmentResourceExtraTest {

    private MockApiHttpClient http;
    private AssignmentResource assignments;

    @BeforeEach
    void setUp() {
        http = new MockApiHttpClient();
        assignments = new AssignmentResource(http);
    }

    @Test
    void signPostsItemsArrayWithAccessCode() {
        http.enqueue(200, "{\"status\":200,\"data\":[]}");
        assignments.sign("d1", "a1", "code1",
                List.of(Map.of("itemId", "i1", "fieldId", "f1", "pageId", "p1", "value", "John")));
        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/documents/d1/assignments/a1?signer-access-code=code1");
        assertThat(http.lastCaptured().getJsonBody()).contains("\"itemId\":\"i1\"");
    }

    @Test
    void signValidatesRequiredItemShapeBeforeSending() {
        assertThatThrownBy(() -> assignments.sign("d1", "a1", "code1", null))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> assignments.sign("d1", "a1", "code1",
                java.util.Arrays.asList((Map<String, Object>) null)))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        for (String key : List.of("itemId", "fieldId", "pageId")) {
            Map<String, Object> item = new java.util.HashMap<>(Map.of(
                    "itemId", "i1", "fieldId", "f1", "pageId", "p1", "value", ""));
            item.remove(key);
            assertThatThrownBy(() -> assignments.sign("d1", "a1", "code1", List.of(item)))
                    .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        }
        assertThatThrownBy(() -> assignments.sign("d1", "a1", "code1",
                List.of(Map.of("itemId", "i1", "fieldId", "f1", "pageId", "p1", "value", 1))))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void signPassesSchemaValidEmptyItemsAndBlankStringIds() {
        http.enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueue(200, "{\"status\":200,\"data\":[]}");

        assignments.sign("d1", "a1", "code1", List.of());
        assignments.sign("d1", "a1", "code1",
                List.of(Map.of("itemId", "", "fieldId", " ", "pageId", "", "value", "")));

        assertThat(http.capturedAt(0).getJsonBody()).isEqualTo("[]");
        assertThat(http.capturedAt(1).getJsonBody()).contains("\"fieldId\":\" \"");
    }

    @Test
    void signAllowsAnEmptyStringValue() {
        http.enqueue(200, "{\"status\":200,\"data\":[]}");

        assignments.sign("d1", "a1", "code1",
                List.of(Map.of("itemId", "i1", "fieldId", "f1", "pageId", "p1", "value", "")));

        assertThat(http.lastCaptured().getJsonBody()).contains("\"value\":\"\"");
    }

    @Test
    void getForSignerHitsSignEndpoint() {
        http.enqueue(200, "{\"id\":\"x\",\"status\":\"pending\"}");
        Map<String, Object> result = assignments.getForSigner("code1");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/sign?signer-access-code=code1");
        assertThat(result).containsEntry("id", "x");
    }

    @Test
    void estimateResendCostPostsToEstimatePath() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"total\":0.2,\"has_sufficient_credits\":true}}");
        Map<String, Object> cost = assignments.estimateResendCost("d1", "a1", "s1");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/documents/d1/assignments/a1/signers/s1/estimate-resend-cost");
        assertThat(cost).containsEntry("has_sufficient_credits", true);
    }

    @Test
    void typedCostMethodsReturnCompleteEstimateModels() {
        String assignmentResponse = "{\"status\":200,\"data\":{" +
                "\"documents\":1,\"credits\":0.9,\"needs_extra_document\":true," +
                "\"extra_document_cost\":1,\"total_credits\":1.9," +
                "\"breakdown\":[{\"code\":\"NotificationWhatsapp\"," +
                "\"name\":\"Whatsapp Notification\",\"cost\":0.9,\"quantity\":2,\"unit_cost\":0.45}]," +
                "\"document_balance\":0,\"credit_balance\":5.5," +
                "\"has_sufficient_resources\":true,\"blocking_reason\":null,\"message\":null}}";
        String resendResponse = "{\"status\":200,\"data\":{" +
                "\"total\":0.45,\"breakdown\":[{\"code\":\"NotificationWhatsappResend\"," +
                "\"name\":\"Whatsapp Notification Resend\",\"cost\":0.45}]," +
                "\"credit_balance\":5.5,\"has_sufficient_credits\":true}}";
        http.enqueue(200, assignmentResponse).enqueue(200, resendResponse);

        CostEstimate assignmentCost = assignments.estimateCostTyped("d1",
                CreateAssignmentRequest.builder()
                        .signers(List.of(SignerReference.builder()
                                .verificationMethod("Whatsapp").build()))
                        .build());
        ResendCostEstimate resendCost = assignments.estimateResendCostTyped("d1", "a1", "s1");

        assertThat(assignmentCost.getDocuments()).isEqualTo(1);
        assertThat(assignmentCost.getCredits()).isEqualByComparingTo("0.9");
        assertThat(assignmentCost.getNeedsExtraDocument()).isTrue();
        assertThat(assignmentCost.getExtraDocumentCost()).isEqualByComparingTo("1");
        assertThat(assignmentCost.getTotalCredits()).isEqualByComparingTo("1.9");
        assertThat(assignmentCost.getBreakdown()).singleElement().satisfies(item -> {
            assertThat(item.getCode()).isEqualTo("NotificationWhatsapp");
            assertThat(item.getName()).isEqualTo("Whatsapp Notification");
            assertThat(item.getCost()).isEqualByComparingTo("0.9");
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getUnitCost()).isEqualByComparingTo("0.45");
        });
        assertThat(assignmentCost.getDocumentBalance()).isEqualByComparingTo("0");
        assertThat(assignmentCost.getCreditBalance()).isEqualByComparingTo("5.5");
        assertThat(assignmentCost.getHasSufficientResources()).isTrue();
        assertThat(assignmentCost.getBlockingReason()).isNull();
        assertThat(assignmentCost.getMessage()).isNull();
        assertThat(resendCost.getTotal()).isEqualByComparingTo("0.45");
        assertThat(resendCost.getBreakdown()).singleElement().satisfies(item ->
                assertThat(item.getCode()).isEqualTo("NotificationWhatsappResend"));
        assertThat(resendCost.getCreditBalance()).isEqualByComparingTo("5.5");
        assertThat(resendCost.getHasSufficientCredits()).isTrue();
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/documents/d1/assignments/a1/signers/s1/estimate-resend-cost");
    }

    @Test
    void createAndEstimateRejectCertificateCollectBeforeSending() {
        CreateAssignmentRequest request = CreateAssignmentRequest.builder()
                .method("collect")
                .signers(List.of(SignerReference.builder().id("s1")
                        .verificationMethod("DigitalCertificate").build()))
                .entries(List.of(Map.of("page_id", "p1", "fields", List.of())))
                .build();

        for (Runnable call : List.<Runnable>of(
                () -> assignments.create("d1", request),
                () -> assignments.estimateCost("d1", request))) {
            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(call::run)
                    .satisfies(error -> assertThat(error.getErrors())
                            .containsEntry("code",
                                    AssignmentResource.DIGITAL_CERTIFICATE_REQUIRES_VIRTUAL_METHOD)
                            .containsEntry("method", "collect"));
        }
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void typedWhatsappNotificationsAndSignerDocumentUseDocumentedModels() {
        http.enqueue(200, "{\"status\":200,\"data\":[{\"sent_at\":\"2026-10-28T12:00:00Z\"," +
                "\"header\":\"Signature requested\",\"body\":\"Open the document\"," +
                "\"buttons\":[{\"text\":\"Open\"}],\"phone_number\":\"+15555550123\"," +
                "\"signer_id\":\"s1\"}]}");
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\"," +
                "\"status\":\"pending_signature\",\"assignment\":{\"id\":\"a1\"}}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d2\"," +
                        "\"status\":\"pending_signature\"}}");

        List<WhatsappNotification> notifications =
                assignments.getWhatsappNotificationsTyped("d1", "a1");
        Document document = assignments.getForSignerTyped("code 1", true);
        Document documentWithoutTermsFlag = assignments.getForSignerTyped("code 2");

        assertThat(notifications).singleElement().satisfies(notification -> {
            assertThat(notification.getSentAt()).isEqualTo("2026-10-28T12:00:00Z");
            assertThat(notification.getHeader()).isEqualTo("Signature requested");
            assertThat(notification.getBody()).isEqualTo("Open the document");
            assertThat(notification.getButtons()).extracting(WhatsappNotification.Button::getText)
                    .containsExactly("Open");
            assertThat(notification.getPhoneNumber()).isEqualTo("+15555550123");
            assertThat(notification.getSignerId()).isEqualTo("s1");
        });
        assertThat(document.getId()).isEqualTo("d1");
        assertThat(document.getAssignment().getId()).isEqualTo("a1");
        assertThat(http.capturedAt(1).getPath())
                .isEqualTo("/sign?signer-access-code=code+1&has_accepted_terms=true");
        assertThat(documentWithoutTermsFlag.getId()).isEqualTo("d2");
        assertThat(http.capturedAt(2).getPath())
                .isEqualTo("/sign?signer-access-code=code+2");
    }

    @Test
    void resendNotificationPutsAndParsesTypedResponse() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"is_sent\":true,\"document_id\":\"d1\",\"signer_id\":\"s1\"}}");
        ResendNotificationResponse res = assignments.resendNotification("d1", "a1", "s1");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/documents/d1/assignments/a1/signers/s1/resend");
        assertThat(res.getIsSent()).isTrue();
        assertThat(res.getDocumentId()).isEqualTo("d1");
    }

    @Test
    void listHitsAssignmentsPathWithPaging() {
        http.enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"a1\",\"method\":\"virtual\"}]}")
                .enqueue(200, "{\"status\":200,\"data\":[]}");
        var result = assignments.list(com.assinafy.sdk.request.ListParams.builder().page(2).perPage(10).build());
        assertThat(assignments.list().getData()).isEmpty();
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(http.capturedAt(0).getPath()).isEqualTo("/assignments");
        assertThat(http.capturedAt(0).getQueryParams()).containsEntry("page", 2).containsEntry("per-page", 10);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getId()).isEqualTo("a1");
        assertThat(http.capturedAt(1).getPath()).isEqualTo("/assignments");
        assertThat(http.capturedAt(1).getQueryParams()).isEmpty();
    }

    @Test
    void resetExpirationSendsExpiresAtWhenProvided() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"a1\",\"expires_at\":\"2030-08-03T21:00:00Z\"}}");
        Assignment a = assignments.resetExpiration("d1", "a1", "2030-08-03T21:00:00Z");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/documents/d1/assignments/a1/reset-expiration");
        assertThat(http.lastCaptured().getJsonBody()).contains("\"expires_at\":\"2030-08-03T21:00:00Z\"");
        assertThat(a.getExpiresAt()).isEqualTo("2030-08-03T21:00:00Z");
    }
}
