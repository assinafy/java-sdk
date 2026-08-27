package com.assinafy.sdk.resources;

import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.CostEstimate;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.ResendNotificationResponse;
import com.assinafy.sdk.models.WhatsappNotification;
import com.assinafy.sdk.request.CreateAssignmentRequest;
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
        String response = "{\"status\":200,\"data\":{" +
                "\"documents\":1,\"credits\":0.9,\"needs_extra_document\":true," +
                "\"extra_document_cost\":1,\"total_credits\":1.9," +
                "\"breakdown\":[{\"code\":\"NotificationWhatsapp\"," +
                "\"name\":\"Whatsapp Notification\",\"cost\":0.9,\"quantity\":2,\"unit_cost\":0.45}]," +
                "\"document_balance\":0,\"credit_balance\":5.5," +
                "\"has_sufficient_resources\":true,\"blocking_reason\":null,\"message\":null}}";
        http.enqueue(200, response).enqueue(200, response);

        CostEstimate assignmentCost = assignments.estimateCostTyped(
                "d1", CreateAssignmentRequest.builder().build());
        CostEstimate resendCost = assignments.estimateResendCostTyped("d1", "a1", "s1");

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
        assertThat(resendCost.getTotalCredits()).isEqualByComparingTo("1.9");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/documents/d1/assignments/a1/signers/s1/estimate-resend-cost");
    }

    @Test
    void typedWhatsappNotificationsAndSignerDocumentUseDocumentedModels() {
        http.enqueue(200, "{\"status\":200,\"data\":[{\"sent_at\":1710000000," +
                "\"header\":\"Signature requested\",\"body\":\"Open the document\"," +
                "\"buttons\":[{\"text\":\"Open\"}],\"phone_number\":\"+15555550123\"," +
                "\"signer_id\":\"s1\"}]}");
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d1\"," +
                "\"status\":\"pending_signature\",\"assignment\":{\"id\":\"a1\"}}}");

        List<WhatsappNotification> notifications =
                assignments.getWhatsappNotificationsTyped("d1", "a1");
        DocumentDetails document = assignments.getForSignerTyped("code 1", true);

        assertThat(notifications).singleElement().satisfies(notification -> {
            assertThat(notification.getSentAt()).isEqualTo(1_710_000_000L);
            assertThat(notification.getHeader()).isEqualTo("Signature requested");
            assertThat(notification.getBody()).isEqualTo("Open the document");
            assertThat(notification.getButtons()).extracting(WhatsappNotification.Button::getText)
                    .containsExactly("Open");
            assertThat(notification.getPhoneNumber()).isEqualTo("+15555550123");
            assertThat(notification.getSignerId()).isEqualTo("s1");
        });
        assertThat(document.getId()).isEqualTo("d1");
        assertThat(document.getAssignment().getId()).isEqualTo("a1");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/sign?signer-access-code=code+1&has_accepted_terms=true");
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
        http.enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"a1\",\"method\":\"virtual\"}]}");
        var result = assignments.list(com.assinafy.sdk.request.ListParams.builder().page(2).perPage(10).build());
        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/assignments");
        assertThat(http.lastCaptured().getQueryParams()).containsEntry("page", 2).containsEntry("per-page", 10);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getId()).isEqualTo("a1");
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
