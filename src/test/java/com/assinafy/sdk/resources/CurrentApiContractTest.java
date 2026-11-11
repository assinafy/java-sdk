package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.CreateDocumentFromTemplateRequest;
import com.assinafy.sdk.request.SignerReference;
import com.assinafy.sdk.request.TemplateSigner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class CurrentApiContractTest {
    @Test
    void signerConfirmationAcceptsTheCurrentEmptyResponse() throws Exception {
        var http = new MockApiHttpClient().enqueue(200, "{\"status\":200,\"data\":[]}");
        var signers = new SignerResource(http);
        assertThat(signers.confirmSignerData("doc", "secret", Map.of("email", "signer@example.invalid",
                "has_accepted_terms", true))).isNull();
        assertThat(new ObjectMapper().readTree(http.lastCaptured().getJsonBody())).isEqualTo(
                new ObjectMapper().readTree("{\"email\":\"signer@example.invalid\",\"has_accepted_terms\":true}"));
    }

    @Test
    void signerUpdatesCanClearContactsWithoutChangingOmittedFields() throws Exception {
        var http = new MockApiHttpClient().enqueue(200, "{\"status\":200,\"data\":{\"id\":\"signer\",\"email\":null,\"whatsapp_phone_number\":null}}");
        var signers = new SignerResource(http, "account");
        signers.update("signer", com.assinafy.sdk.request.UpdateSignerRequest.builder()
                .email("").whatsappPhoneNumber("").build());
        assertThat(new ObjectMapper().readTree(http.lastCaptured().getJsonBody())).isEqualTo(
                new ObjectMapper().readTree("{\"email\":\"\",\"whatsapp_phone_number\":\"\"}"));
        assertThatThrownBy(() -> signers.update("signer", com.assinafy.sdk.request.UpdateSignerRequest.builder()
                .email(" ").build())).isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isEqualTo(1);
    }

    @Test
    void findsAnExactSignerBeyondTheFirstSearchPageWithoutCreatingADuplicate() {
        var http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"near-match\",\"email\":\"other@example.invalid\"}]}",
                        Map.of("X-Pagination-Page-Count", "2", "X-Pagination-Per-Page", "50"))
                .enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"exact\",\"email\":\"SIGNER@example.invalid\"}]}",
                        Map.of("X-Pagination-Page-Count", "2"));
        var resource = new SignerResource(http, "account");
        assertThat(resource.findOrCreate(com.assinafy.sdk.request.CreateSignerRequest.builder()
                .fullName("Example").email("signer@example.invalid").build()).getId()).isEqualTo("exact");
        assertThat(http.getCaptured()).extracting(MockApiHttpClient.CapturedRequest::getMethod).containsExactly("GET", "GET");
        assertThat(http.capturedAt(0).getQueryParams()).containsEntry("page", 1).containsEntry("per-page", 50);
        assertThat(http.capturedAt(1).getQueryParams()).containsEntry("page", 2);
    }

    @Test
    void templateCopyReceiversDoNotParticipateInLocalSigningOrderChecks() {
        var http = new MockApiHttpClient().enqueue(200, "{\"status\":200,\"data\":{\"id\":\"doc\"}}");
        var request = CreateDocumentFromTemplateRequest.builder().signers(List.of(
                TemplateSigner.builder().id("signer").roleId("signing-role").step(1)
                        .verificationMethod("DigitalCertificate").build(),
                TemplateSigner.builder().id("receiver").roleId("copy-role").build())).build();
        new DocumentResource(http, "account").createFromTemplate("template", request);
        assertThat(http.lastCaptured().getJsonBody()).contains("\"step\":1", "\"role_id\":\"copy-role\"");
        assertThat(http.capturedCount()).isEqualTo(1);
    }

    @Test
    void supportsSignedRendersEnvelopeSearchAndRelatedDocumentExpansion() {
        var http = new MockApiHttpClient().enqueue(200, "image").enqueue(200, "image")
                .enqueue(200, "{\"status\":200,\"data\":[]}").enqueue(200, "{\"status\":200,\"data\":[]}")
                .enqueue(200, "{\"status\":200,\"data\":{\"resource\":\"document_contract\",\"document\":{\"id\":\"parent\"}}}");
        var docs = new DocumentResource(http, "account");
        docs.thumbnail("d/a", true);
        docs.downloadPage("d/a", "p/a", true);
        assertThat(http.capturedAt(0).getPath()).isEqualTo("/documents/d%2Fa/thumbnail?artifact=certificated");
        assertThat(http.capturedAt(1).getPath()).isEqualTo("/documents/d%2Fa/pages/p%2Fa/download?artifact=certificated");
        var signers = new SignerResource(http);
        signers.searchDocuments("s", "secret", null, "env");
        assertThat(http.lastCaptured().getQueryParams()).containsExactlyInAnyOrderEntriesOf(Map.of("signer-access-code", "secret", "envelope_id", "env"));
        signers.searchDocumentsAsWorkspace("s", "contract", "env");
        assertThat(http.lastCaptured().getQueryParams()).containsExactlyInAnyOrderEntriesOf(Map.of("search", "contract", "envelope_id", "env"));
        new ContractResource(http, "account").listRelated("doc", null, true);
        assertThat(http.lastCaptured().getQueryParams()).containsEntry("expand", "document");
    }
    @Test
    void webhookTestsAndOverlappingRotationPreserveNewResponseFields() {
        var http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"dispatch\",\"event\":\"webhook_test\",\"kind\":\"manual\",\"duration_ms\":12}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"secret\":\"whsec_dGVzdA==\",\"previous_secret_expires_at\":\"2026-11-12T12:00:00Z\"}}");
        var resource = new WebhookResource(http, "a/b");
        assertThat(resource.testEndpoint("e/a").getDurationMs()).isEqualTo(12L);
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/a%2Fb/webhooks/endpoints/e%2Fa/test");
        assertThat(resource.rotateEndpointSecret("e/a", true).getPreviousSecretExpiresAt()).isEqualTo("2026-11-12T12:00:00Z");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/a%2Fb/webhooks/endpoints/e%2Fa/secret/rotate");
        assertThat(http.lastCaptured().getJsonBody()).isEqualTo("{\"keep_previous\":true}");
    }

    @Test
    void binaryMockAlsoRejectsHeaderlessErrorEnvelopes() {
        var http = new MockApiHttpClient().enqueue(200, "{\"status\":403,\"message\":\"Forbidden\"}");
        assertThatThrownBy(() -> new SignerResource(http).downloadCertifiedCopy("token", false))
                .isInstanceOf(com.assinafy.sdk.exceptions.AuthenticationException.class);
    }
    @Test
    void preservesNewSignerAndDocumentFieldsAndExpansionQueries() {
        String document = """
                {"status":200,"data":{"id":"doc","envelope_id":"env","envelope":{"id":"env","name":"Agreement"},
                "current_signer":{"id":"signer","verification_method":"Email","notification_methods":["Email"],"completed":true},
                "is_signer_access_closed":true,"certified_copy":{"available":true,"notification_method":"Email","resends_left":null}}}
                """;
        var http = new MockApiHttpClient().enqueue(200, document).enqueue(200, document).enqueue(200, document);
        var details = new DocumentResource(http, "account").details("d/a", true);
        assertThat(details.getEnvelopeId()).isEqualTo("env");
        assertThat(details.getEnvelope().getName()).isEqualTo("Agreement");
        assertThat(http.lastCaptured().getQueryParams()).containsEntry("expand", "envelope_id,envelope");
        var signerView = new AssignmentResource(http).getForSignerTyped("secret+/", null, "d/a", true);
        assertThat(signerView.getCurrentSigner().getCompleted()).isTrue();
        assertThat(signerView.getCurrentSigner().getNotificationMethods()).containsExactly("Email");
        assertThat(signerView.getIsSignerAccessClosed()).isTrue();
        assertThat(signerView.getCertifiedCopy().getResendsLeft()).isNull();
        assertThat(http.lastCaptured().getPath()).isEqualTo("/sign?signer-access-code=secret%2B%2F&document_id=d%2Fa&expand=envelope");
        new SignerResource(http).getCurrentDocumentTyped("s/a", "secret", true);
        assertThat(http.lastCaptured().getPath()).isEqualTo("/signers/s%2Fa/document?signer-access-code=secret&expand=envelope");
    }

    @Test
    void preservesCreditCeilingsAndEmbeddedFlagsForAssignmentsAndTemplates() throws Exception {
        var http = new MockApiHttpClient().enqueue(200, "{\"status\":200,\"data\":{\"id\":\"a\"}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"id\":\"d\"}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"total_credits\":1}}");
        var reference = SignerReference.builder().id("s").embedded(true).build();
        var assignment = CreateAssignmentRequest.builder().signers(List.of(reference)).maxTotalCredits(new BigDecimal("1.25")).build();
        new AssignmentResource(http).create("doc", assignment);
        var json = new ObjectMapper().readTree(http.lastCaptured().getJsonBody());
        assertThat(json.get("max_total_credits").decimalValue()).isEqualByComparingTo("1.25");
        assertThat(json.get("signers").get(0).get("embedded").booleanValue()).isTrue();
        var template = CreateDocumentFromTemplateRequest.builder()
                .signers(List.of(TemplateSigner.builder().id("s").roleId("r").embedded(true).build()))
                .maxTotalCredits(BigDecimal.ZERO).build();
        var docs = new DocumentResource(http, "account");
        docs.createFromTemplate("t/a", template, null, "template-key");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/account/templates/t%2Fa/documents");
        assertThat(http.lastCaptured().getIdempotencyKey()).isEqualTo("template-key");
        assertThat(http.lastCaptured().getJsonBody()).contains("\"embedded\":true", "\"max_total_credits\":0");
        docs.estimateCostFromTemplate("template", template);
        assertThat(http.lastCaptured().getJsonBody()).doesNotContain("embedded", "max_total_credits", "\"id\"");
        template.setMaxTotalCredits(new BigDecimal("-1"));
        assertThatThrownBy(() -> docs.createFromTemplate("template", template)).isInstanceOf(ValidationException.class);
        reference.setVerificationMethod("IdDocumentFaceMatchBasic");
        assertThatThrownBy(() -> new AssignmentResource(http).create("doc", assignment)).isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isEqualTo(3);
    }

    @Test
    void workspaceUsageAndPartialPoliciesPreserveAllFields() throws Exception {
        var http = new MockApiHttpClient()
                .enqueue(200, "{\"status\":200,\"data\":{\"signing_features\":{\"Embed\":true,\"DigitalCertificate\":true}}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"plan\":\"Pro\",\"documents_renewal_date\":\"2026-12-01\",\"balances\":{\"credits\":{\"balance\":1.25,\"limit\":null,\"spent\":2}}}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"editable\":true,\"owners_receive_all\":true,\"events\":{\"SignerSigned\":false}}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"editable\":true,\"owners_receive_all\":false,\"events\":{\"SignerSigned\":false}}}");
        var workspaces = new WorkspaceResource(http);
        assertThat(workspaces.get("a/b", true).getSigningFeatures()).containsEntry("Embed", true);
        assertThat(http.lastCaptured().getQueryParams()).containsEntry("expand", "signing_features");
        var usage = workspaces.getUsage("a/b");
        assertThat(usage.getBalances().get("credits").getBalance()).isEqualByComparingTo("1.25");
        assertThat(usage.getBalances().get("credits").getLimit()).isNull();
        assertThat(workspaces.getNotificationPolicy("a/b").getEditable()).isTrue();
        assertThat(workspaces.updateNotificationPolicy("a/b", Map.of("owners_receive_all", false,
                "events", Map.of("SignerSigned", false))).getEvents().getSignerSigned()).isFalse();
        assertThat(new ObjectMapper().readTree(http.lastCaptured().getJsonBody())).isEqualTo(new ObjectMapper()
                .readTree("{\"owners_receive_all\":false,\"events\":{\"SignerSigned\":false}}"));
        assertThat(http.getCaptured()).extracting(MockApiHttpClient.CapturedRequest::getPath).containsExactly(
                "/accounts/a%2Fb", "/accounts/a%2Fb/usage", "/accounts/a%2Fb/notification-policy", "/accounts/a%2Fb/notification-policy");
    }

    @Test
    void pushPreferencesAndNotificationValidationUseTheirOwnCodes() {
        var http = new MockApiHttpClient().enqueue(200, "{\"status\":200,\"data\":{\"SignerViewed\":true}}")
                .enqueue(200, "{\"status\":200,\"data\":{\"SignerViewed\":false}}");
        var users = new UserResource(http);
        assertThat(users.getPushPreferences().getSignerViewed()).isTrue();
        assertThat(users.updatePushPreferences(Map.of("SignerViewed", false)).getSignerViewed()).isFalse();
        assertThat(http.lastCaptured().getPath()).isEqualTo("/users/self/push-preferences");
        assertThat(http.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(http.lastCaptured().getJsonBody()).isEqualTo("{\"SignerViewed\":false}");
        assertThatThrownBy(() -> users.updatePushPreferences(Map.of("ContractRenewed", true))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> users.updatePushPreferences(Map.of())).isInstanceOf(ValidationException.class);
        Map<String, Boolean> nullKey = new LinkedHashMap<>(); nullKey.put(null, true);
        assertThatThrownBy(() -> users.updateNotificationPreferences(nullKey)).isInstanceOf(ValidationException.class);
        var workspace = new WorkspaceResource(http);
        for (var bad : List.of(Map.<String, Object>of("editable", true), Map.<String, Object>of("events", Map.of("Unknown", true)),
                Map.<String, Object>of("owners_receive_all", "true"), Map.<String, Object>of("events", Map.of()))) {
            assertThatThrownBy(() -> workspace.updateNotificationPolicy("account", bad)).isInstanceOf(ValidationException.class);
        }
        assertThat(http.capturedCount()).isEqualTo(2);
    }

    @Test
    void signerBrandAndCertifiedCopyEndpointsUseOnlyTheSignerCredential() {
        var http = new MockApiHttpClient().enqueue(200, "{\"status\":200,\"data\":{\"account_name\":\"Example\",\"logo\":null}}")
                .enqueue(200, "image").enqueue(200, "{\"status\":200,\"data\":{\"available\":true,\"notification_method\":\"Email\",\"resends_left\":null,\"sent_to\":\"s***@example.invalid\"}}")
                .enqueue(200, "pdf").enqueue(200, "pdf-pades").enqueue(200, "{\"status\":200,\"data\":{\"available\":false}}");
        var signer = new SignerResource(http);
        assertThat(signer.getTheme("secret+/ ").getLogo()).isNull();
        signer.downloadLogo("secret");
        assertThat(http.lastCaptured().getAccept()).isEqualTo("*/*");
        assertThat(signer.resendCertifiedCopy("secret", "document").getNotificationMethod()).isEqualTo("Email");
        assertThat(http.lastCaptured().getJsonBody()).isEqualTo("{\"document_id\":\"document\"}");
        signer.downloadCertifiedCopy("token/part", false);
        assertThat(http.lastCaptured().getAccept()).isEqualTo("*/*");
        signer.downloadCertifiedCopy("token/part", true);
        assertThat(signer.resendCertifiedCopyFromToken("token/part").getAvailable()).isFalse();
        assertThat(http.getCaptured()).extracting(MockApiHttpClient.CapturedRequest::getPath).containsExactly(
                "/sign/theme?signer-access-code=secret%2B%2F+", "/sign/logo?signer-access-code=secret",
                "/sign/certified-copy?signer-access-code=secret", "/signers/downloads/token%2Fpart",
                "/signers/downloads/token%2Fpart/pades", "/signers/downloads/token%2Fpart/resend");
    }
}
