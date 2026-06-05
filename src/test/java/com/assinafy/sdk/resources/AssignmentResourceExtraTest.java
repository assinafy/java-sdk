package com.assinafy.sdk.resources;

import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.ResendNotificationResponse;
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
