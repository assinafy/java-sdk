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
                {"resource":"account","id":"a1"}
                """, Workspace.class);
        WorkspaceListItem workspaceListItem = decode("""
                {"resource":"account","id":"a1","primary_color":"112233",
                 "secondary_color":"445566","notification_sender_type":"Account"}
                """, WorkspaceListItem.class);
        Signer signer = decode("""
                {"resource":"signer","id":"s1"}
                """, Signer.class);
        FieldDefinition field = decode("""
                {"resource":"field","id":"f1"}
                """, FieldDefinition.class);
        Template template = decode("""
                {"resource":"template","id":"t1"}
                """, Template.class);
        TemplateListItem templateListItem = decode("""
                {"resource":"template","id":"t1",
                 "default_document_tags":[{"id":"tag1","name":"Legal"}]}
                """, TemplateListItem.class);

        assertThat(authAccount.getId()).isEqualTo("a1");
        assertThat(authAccount.getName()).isEqualTo("Example");
        assertThat(authAccount.getRoles()).containsExactly("owner");
        assertThat(authAccount.getIsDeleteAllowed()).isTrue();
        assertThat(authAccount.getCreatedAt()).isEqualTo("2026-08-20T12:00:00Z");
        assertThat(workspace.getResource()).isEqualTo("account");
        assertThat(workspaceListItem.getResource()).isEqualTo("account");
        assertThat(workspaceListItem.getPrimaryColor()).isEqualTo("112233");
        assertThat(workspaceListItem.getSecondaryColor()).isEqualTo("445566");
        assertThat(workspaceListItem.getNotificationSenderType()).isEqualTo("Account");
        assertThat(signer.getResource()).isEqualTo("signer");
        assertThat(field.getResource()).isEqualTo("field");
        assertThat(template.getResource()).isEqualTo("template");
        assertThat(templateListItem.getResource()).isEqualTo("template");
        assertThat(templateListItem.getDefaultDocumentTags())
                .singleElement()
                .extracting(Tag::getId)
                .isEqualTo("tag1");
    }

    @Test
    void deserializesDocumentFieldsAndPadesArtifact() {
        DocumentDetails details = decode("""
                {"resource":"document","id":"d1",
                 "artifacts":{"pades":"https://example.test/pades.pdf"},
                 "declined_by":{"id":"s1","full_name":"Declining Signer"}}
                """, DocumentDetails.class);
        DocumentListItem listItem = decode("""
                {"resource":"document","id":"d1","assignment":{"id":"as1"},
                 "declined_by":{"id":"s1","full_name":"Declining Signer"}}
                """, DocumentListItem.class);
        DocumentUploadResponse upload = decode("""
                {"resource":"document","id":"d1","signing_url":"https://example.test/sign",
                 "tags":[{"id":"tag1","name":"Legal"}],"assignment":{"id":"as1"},
                 "declined_by":{"id":"s1","full_name":"Declining Signer"}}
                """, DocumentUploadResponse.class);

        assertThat(details.getResource()).isEqualTo("document");
        assertThat(details.getArtifacts().getPades()).isEqualTo("https://example.test/pades.pdf");
        assertThat(details.getDeclinedBy()).isInstanceOf(Map.class);
        assertThat(details.getDeclinedBySigner().getId()).isEqualTo("s1");
        assertThat(listItem.getResource()).isEqualTo("document");
        assertThat(listItem.getAssignment().getId()).isEqualTo("as1");
        assertThat(listItem.getDeclinedBy()).isInstanceOf(Map.class);
        assertThat(listItem.getDeclinedBySigner().getFullName()).isEqualTo("Declining Signer");
        assertThat(upload.getResource()).isEqualTo("document");
        assertThat(upload.getSigningUrl()).isEqualTo("https://example.test/sign");
        assertThat(upload.getTags()).extracting(Tag::getId).containsExactly("tag1");
        assertThat(upload.getAssignment()).isInstanceOf(Map.class);
        assertThat(upload.getAssignmentDetails().getId()).isEqualTo("as1");
        assertThat(upload.getDeclinedBy()).isInstanceOf(Map.class);
        assertThat(upload.getDeclinedBySigner().getId()).isEqualTo("s1");
        assertThat(DocumentArtifactName.PADES.getValue()).isEqualTo("pades");
    }

    @Test
    void providesTypedNotificationHistoryWithoutChangingLegacyAccessor() {
        AssignmentSigner signer = decode("""
                {"resource":"signer","id":"s1","notification_history":[{
                  "event":"email","status":"failed","error_code":"E1",
                  "error_message":"Delivery failed","sent_at":null,
                  "failed_at":"2026-08-20T12:00:00Z"}]}
                """, AssignmentSigner.class);

        assertThat(signer.getResource()).isEqualTo("signer");
        assertThat(signer.getNotificationHistory()).hasSize(1);
        assertThat(signer.getNotificationHistoryEntries())
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

    private static <T> T decode(String data, Class<T> type) {
        String envelope = "{\"status\":200,\"data\":" + data + "}";
        return ResponseHandler.handle(new HttpRawResponse(200, envelope, Map.of()), type);
    }
}
