package com.assinafy.sdk.models;

import com.assinafy.sdk.http.HttpRawResponse;
import com.assinafy.sdk.models.enums.DocumentArtifactName;
import com.assinafy.sdk.util.ResponseHandler;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiModelConformanceTest {

    @Test
    void deserializesDocumentedResourceAndListFields() {
        AuthAccount authAccount = decode("""
                {"id":"a1","name":"Example","roles":["owner"],
                 "is_delete_allowed":true,"created_at":"2026-08-20T12:00:00Z"}
                """, AuthAccount.class);
        Workspace workspace = decode("""
                {"resource":"account","id":"a1","primary_color":"112233",
                 "secondary_color":"445566","notification_sender_type":"Account"}
                """, Workspace.class);
        Signer signer = decode("""
                {"resource":"signer","id":"s1"}
                """, Signer.class);
        FieldDefinition field = decode("""
                {"resource":"field","id":"f1"}
                """, FieldDefinition.class);
        Template template = decode("""
                {"resource":"template","id":"t1",
                 "default_document_tags":[{"id":"tag1","name":"Legal"}]}
                """, Template.class);

        assertThat(authAccount.getId()).isEqualTo("a1");
        assertThat(authAccount.getName()).isEqualTo("Example");
        assertThat(authAccount.getRoles()).containsExactly("owner");
        assertThat(authAccount.getIsDeleteAllowed()).isTrue();
        assertThat(authAccount.getCreatedAt()).isEqualTo("2026-08-20T12:00:00Z");
        assertThat(workspace.getResource()).isEqualTo("account");
        assertThat(workspace.getPrimaryColor()).isEqualTo("112233");
        assertThat(workspace.getSecondaryColor()).isEqualTo("445566");
        assertThat(workspace.getNotificationSenderType()).isEqualTo("Account");
        assertThat(signer.getResource()).isEqualTo("signer");
        assertThat(field.getResource()).isEqualTo("field");
        assertThat(template.getResource()).isEqualTo("template");
        assertThat(template.getDefaultDocumentTags())
                .singleElement()
                .extracting(Tag::getId)
                .isEqualTo("tag1");
    }

    /**
     * Upload, list, and get all return the API's single {@code Document} schema, so the one SDK
     * model must decode the union of the fields those responses populate.
     */
    @Test
    void deserializesEveryDocumentResponseShapeIntoOneModel() {
        Document uploaded = decode("""
                {"resource":"document","id":"d1","status":"uploaded",
                 "signing_url":"https://example.test/sign","tags":[{"id":"tag1","name":"Legal"}],
                 "artifacts":{"pades":"https://example.test/pades.pdf"}}
                """, Document.class);
        Document assigned = decode("""
                {"resource":"document","id":"d1","assignment":{"id":"as1"},
                 "declined_by":{"id":"s1","full_name":"Declining Signer"},
                 "decline_reason":"Unfavorable terms","is_closed":true}
                """, Document.class);

        assertThat(uploaded.getResource()).isEqualTo("document");
        assertThat(uploaded.getStatus()).isEqualTo("uploaded");
        assertThat(uploaded.getSigningUrl()).isEqualTo("https://example.test/sign");
        assertThat(uploaded.getTags()).extracting(Tag::getId).containsExactly("tag1");
        assertThat(uploaded.getArtifacts().getPades()).isEqualTo("https://example.test/pades.pdf");
        assertThat(assigned.getAssignment().getId()).isEqualTo("as1");
        assertThat(assigned.getDeclineReason()).isEqualTo("Unfavorable terms");
        assertThat(assigned.getIsClosed()).isTrue();
        assertThat(assigned.getDeclinedBy()).isInstanceOf(Map.class);
        assertThat(assigned.getDeclinedBySigner().getId()).isEqualTo("s1");
        assertThat(assigned.getDeclinedBySigner().getFullName()).isEqualTo("Declining Signer");
        assertThat(DocumentArtifactName.PADES.getValue()).isEqualTo("pades");
    }

    @Test
    void deserializesTypedNotificationHistory() {
        AssignmentSigner signer = decode("""
                {"resource":"signer","id":"s1","notification_history":[{
                  "event":"email","status":"failed","error_code":"E1",
                  "error_message":"Delivery failed","sent_at":null,
                  "failed_at":"2026-08-20T12:00:00Z"}]}
                """, AssignmentSigner.class);

        assertThat(signer.getResource()).isEqualTo("signer");
        assertThat(signer.getNotificationHistory())
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getEvent()).isEqualTo("email");
                    assertThat(entry.getStatus()).isEqualTo("failed");
                    assertThat(entry.getErrorCode()).isEqualTo("E1");
                    assertThat(entry.getErrorMessage()).isEqualTo("Delivery failed");
                    assertThat(entry.getSentAt()).isNull();
                    assertThat(entry.getFailedAt()).isEqualTo("2026-08-20T12:00:00Z");
                });
    }

    @Test
    @SuppressWarnings("deprecation")
    void preservesEveryCurrentStatisticsBreakdownAndLegacyAliases() {
        DocumentStatsRow row = decode("""
                {"period":"2026-08","signature_requests":28,
                 "signature_requests_notification_email":20,
                 "signature_requests_notification_whatsapp":9,
                 "signature_requests_notification_bypass":1,
                 "signature_requests_verification_email":18,
                 "signature_requests_verification_whatsapp":5,
                 "signature_requests_verification_bypass":3,
                 "signature_requests_verification_digital_certificate":2}
                """, DocumentStatsRow.class);

        assertThat(row.getSignatureRequests()).isEqualTo(28);
        assertThat(row.getSignatureRequestsNotificationEmail()).isEqualTo(20);
        assertThat(row.getSignatureRequestsNotificationWhatsapp()).isEqualTo(9);
        assertThat(row.getSignatureRequestsNotificationBypass()).isEqualTo(1);
        assertThat(row.getSignatureRequestsVerificationEmail()).isEqualTo(18);
        assertThat(row.getSignatureRequestsVerificationWhatsapp()).isEqualTo(5);
        assertThat(row.getSignatureRequestsVerificationBypass()).isEqualTo(3);
        assertThat(row.getSignatureRequestsVerificationDigitalCertificate()).isEqualTo(2);
        assertThat(row.getSignatureRequestsEmail()).isEqualTo(20);
        assertThat(row.getSignatureRequestsWhatsapp()).isEqualTo(9);
    }

    private static <T> T decode(String data, Class<T> type) {
        String envelope = "{\"status\":200,\"data\":" + data + "}";
        return ResponseHandler.handle(new HttpRawResponse(200, envelope, Map.of()), type);
    }
}
