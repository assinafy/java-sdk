package com.assinafy.sdk.it;

import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;
import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.models.Document;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.request.CreateEnvelopeRequest;
import com.assinafy.sdk.request.CreateSignerRequest;
import com.assinafy.sdk.request.CreateWorkspaceRequest;
import com.assinafy.sdk.request.EnvelopeDocumentRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.SignerReference;
import com.assinafy.sdk.request.WebhookEndpointRequest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.Console;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

/** Opt-in live lifecycle checks; main additionally completes email signing with a participant's OTP. */
class LiveApiNewEndpointsIT {
    @Test
    void newEndpointLifecycles() throws Exception {
        Assumptions.assumeTrue("true".equals(System.getenv("ASSINAFY_LIVE_MUTATIONS")),
                "Set ASSINAFY_LIVE_MUTATIONS=true to create and clean up live test records");
        String key = System.getenv("ASSINAFY_API_KEY");
        String account = System.getenv("ASSINAFY_ACCOUNT_ID");
        String email = System.getenv("ASSINAFY_TEST_EMAIL_PRIMARY");
        Assumptions.assumeTrue(key != null && account != null && email != null, "Set live credentials and a test recipient");
        String base = System.getenv().getOrDefault("ASSINAFY_BASE_URL", AssinafyClientOptions.SANDBOX_BASE_URL);
        exercise(new AssinafyClient(AssinafyClientOptions.builder().apiKey(key).accountId(account).baseUrl(base).build()),
                account, email, null);
    }

    public static void main(String[] args) throws Exception {
        Console console = System.console();
        if (console == null) throw new IllegalStateException("An interactive console is required");
        String key = new String(console.readPassword("Production API key: "));
        String account = new String(console.readPassword("Workspace ID: "));
        String email = new String(console.readPassword("Test recipient email: "));
        try {
            exercise(AssinafyClient.create(key, account), account, email, console);
            System.out.println("LIVE FLOW PASSED; temporary resources cleaned up; certificated documents retained by API policy");
        } catch (ApiException failure) {
            System.out.println("LIVE FLOW FAILED: HTTP " + failure.getStatusCode());
            for (StackTraceElement frame : failure.getStackTrace()) {
                if (frame.getClassName().startsWith("com.assinafy.sdk")) System.out.println(frame);
            }
            throw new AssertionError("Live API request failed; no response payload logged");
        }
    }

    private static void exercise(AssinafyClient client, String account, String email, Console console) throws Exception {
        String run = "sdk-it-" + UUID.randomUUID();
        java.util.Set<String> retainedDocuments = new java.util.HashSet<>();
        try (Cleanup cleanup = new Cleanup()) {
            System.out.println("Checking workspace features, usage and preferences");
            assertThat(read(() -> client.workspaces().get(account, true)).getSigningFeatures()).isNotEmpty();
            assertThat(read(() -> client.workspaces().getUsage(account)).getBalances()).isNotEmpty();
            var push = read(() -> client.users().getPushPreferences());
            if (push.getSignerViewed() != null) {
                assertThat(client.users().updatePushPreferences(Map.of("SignerViewed", push.getSignerViewed())).getSignerViewed())
                        .isEqualTo(push.getSignerViewed());
            }
            String workspace = client.workspaces().create(CreateWorkspaceRequest.builder().name(run).build()).getId();
            cleanup.add(() -> client.workspaces().delete(workspace));
            assertThat(read(() -> client.workspaces().getNotificationPolicy(workspace)).getEditable()).isTrue();
            assertThat(client.workspaces().updateNotificationPolicy(workspace,
                    Map.of("events", Map.of("SignerSigned", false), "owners_receive_all", false)).getEvents().getSignerSigned()).isFalse();
            byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+a0qkAAAAASUVORK5CYII=");
            client.workspaces().uploadLogo(workspace, png, "logo.png");
            assertThat(read(() -> client.workspaces().downloadLogo(workspace))).isNotEmpty();
            client.workspaces().deleteLogo(workspace);

            System.out.println("Checking isolated webhook test delivery and overlapping secret rotation");
            var endpoint = client.webhooks().createEndpoint(WebhookEndpointRequest.builder()
                    .url("https://" + run + ".example.invalid/hook").email("sdk-it@example.invalid")
                    .events(List.of("document_ready")).signingEnabled(true).build(), workspace);
            var rotated = client.webhooks().rotateEndpointSecret(endpoint.getId(), true, workspace);
            assertThat(rotated.getPreviousSecretExpiresAt()).isNotBlank();
            assertThat(rotated.getSecret() != null && rotated.getSecret().startsWith("whsec_")).isTrue();
            var dispatch = client.webhooks().testEndpoint(endpoint.getId(), workspace);
            assertThat(dispatch.getKind()).isEqualTo("manual");
            assertThat(dispatch.getEvent()).isEqualTo("webhook_test");
            assertThat(dispatch.getDelivered()).isFalse();
            client.webhooks().deleteEndpointByUrl(endpoint.getUrl(), workspace);
            assertThat(read(() -> client.webhooks().listEndpoints(workspace))).isEmpty();

            System.out.println("Registering isolated embed origin");
            String origin = "https://" + run + ".example.invalid";
            var registered = client.embed().addOrigin(origin);
            cleanup.add(() -> client.embed().deleteOrigin(registered.getId()));
            assertThat(read(() -> client.embed().listOrigins()).stream().anyMatch(o -> registered.getId().equals(o.getId()))).isTrue();
            assertThat(client.embed().updateOrigin(registered.getId(), origin + ":443/").getOrigin()).isEqualTo(origin);

            Signer signer = read(() -> client.signers().findByEmail(email));
            if (signer == null) {
                signer = client.signers().create(CreateSignerRequest.builder().fullName("SDK Email Test").email(email).build());
                String createdSignerId = signer.getId();
                cleanup.add(() -> { if (retainedDocuments.isEmpty()) client.signers().delete(createdSignerId); });
            }
            String signerId = signer.getId();
            List<Document> documents = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Document document = client.documents().upload(LiveApiSmokeIT.minimalPdf(), run + "-" + i + ".pdf");
                documents.add(document);
                cleanup.add(() -> {
                    if ("certificated".equals(read(() -> client.documents().details(document.getId())).getStatus())) {
                        retainedDocuments.add(document.getId());
                        System.out.println("Retained certificated test document: " + document.getId());
                    } else deleteDocument(client, document.getId());
                });
                read(() -> client.documents().waitUntilReady(document.getId(), 60_000, 1500));
            }
            String first = documents.get(0).getId();
            String second = documents.get(1).getId();
            String third = documents.get(2).getId();
            var request = CreateEnvelopeRequest.builder().name(run).message("SDK email integration test; no legal agreement")
                    .signers(List.of(SignerReference.builder().id(signerId).verificationMethod("Email")
                            .notificationMethods(List.of("Email")).embedded(true).build()))
                    .documents(List.of(envelopeDocument(first, signerId), envelopeDocument(second, signerId))).build();
            var estimate = read(() -> client.envelopes().estimateCost(request));
            assertThat(estimate.getHasSufficientResources()).isTrue();
            request.setMaxTotalCredits(estimate.getTotalCredits());
            System.out.println("Creating email envelope and checking idempotency");
            String key = UUID.randomUUID().toString();
            var envelope = client.envelopes().create(request, null, key);
            String envelopeId = envelope.getId();
            cleanup.add(() -> {
                client.envelopes().cancel(envelopeId);
                client.envelopes().delete(envelopeId);
            });
            assertThat(client.envelopes().create(request, null, key).getId()).isEqualTo(envelopeId);
            assertThat(read(() -> client.envelopes().get(envelopeId)).getDocuments()).hasSize(2);
            assertThat(read(() -> client.envelopes().list(ListParams.builder().search(run).build())).getData())
                    .extracting(com.assinafy.sdk.models.DocumentEnvelope::getId).contains(envelopeId);
            Map<String, Object> changes = new LinkedHashMap<>();
            changes.put("message", null);
            changes.put("document_ids", List.of(second, first));
            assertThat(client.envelopes().update(envelopeId, changes).getDocuments().getFirst().getId()).isEqualTo(second);
            assertThat(client.envelopes().addDocument(envelopeId, envelopeDocument(third, signerId)).getDocuments()).hasSize(3);
            assertThat(client.envelopes().removeDocument(envelopeId, third).getDocuments()).hasSize(2);
            assertThat(read(() -> client.documents().details(first, true)).getEnvelopeId()).isEqualTo(envelopeId);
            System.out.println("Checking original envelope ZIP download");
            assertThat(read(() -> client.envelopes().download(envelopeId, "original"))).startsWith((byte) 'P', (byte) 'K');
            assertThat(read(() -> client.envelopes().activities(envelopeId, ListParams.builder().sort("id").build())).getData()).isNotEmpty();
            assertThat(read(() -> client.signers().searchDocumentsAsWorkspace(signerId, null, envelopeId)).getData()).isNotEmpty();
            assertThat(client.envelopes().resendNotification(envelopeId, signerId).getIsSent()).isTrue();

            System.out.println("Checking single-use embed exchange and signer branding");
            String assignmentId = read(() -> client.documents().details(first)).getAssignment().getId();
            var session = client.embed().createSession(first, assignmentId, signerId);
            String token = URI.create(session.getUrl()).getPath().replaceAll(".*/", "");
            var exchange = client.embed().exchangeSession(token, origin);
            String accessCode = exchange.getSignerAccessCode();
            assertThat(accessCode != null && !accessCode.isBlank()).isTrue();
            expectStatus(410, () -> client.embed().exchangeSession(token, origin));
            assertThat(client.signers().getTheme(accessCode).getAccountName()).isNotBlank();
            try { assertThat(client.signers().downloadLogo(accessCode)).isNotEmpty(); }
            catch (ApiException failure) { assertThat(failure.getStatusCode()).isEqualTo(404); }
            if (console != null) completeEmailSigning(client, signerId, email, accessCode, List.of(first, second), console);
            else System.out.println("Signing completion skipped: run main interactively to supply the email OTP");
            System.out.println("Checking repeatable cancellation and deletion");
            client.envelopes().cancel(envelopeId);
            assertThat(client.envelopes().cancel(envelopeId)).allSatisfy(value -> assertThat(value.getCancelled()).isFalse());
        }
    }

    private static void completeEmailSigning(AssinafyClient client, String signerId, String email,
                                             String accessCode, List<String> documentIds, Console console) throws Exception {
        client.publicDocuments().sendToken(documentIds.getFirst(), email);
        System.out.println("WAITING_FOR_EMAIL_OTP");
        String otp = new String(console.readPassword("Email verification code: "));
        client.signers().verifyEmail(accessCode, otp);
        client.signers().acceptTerms(accessCode);
        for (String documentId : documentIds) {
            Document document = client.assignments().getForSignerTyped(accessCode, null, documentId, true);
            client.signers().confirmSignerData(documentId, accessCode, Map.of("email", email, "has_accepted_terms", true));
            List<Map<String, Object>> items = new ArrayList<>();
            for (var item : document.getAssignment().getItems()) {
                items.add(Map.of("itemId", item.getId(), "fieldId", nestedId(item.getField()),
                        "pageId", nestedId(item.getPage()), "value", "SDK EMAIL INTEGRATION TEST"));
            }
            client.assignments().sign(documentId, document.getAssignment().getId(), accessCode, items);
        }
        for (String documentId : documentIds) {
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(120);
            Document document;
            do {
                document = read(() -> client.documents().details(documentId));
                if ("certificated".equals(document.getStatus())) break;
                Thread.sleep(1500);
            } while (System.nanoTime() < deadline);
            assertThat(document.getStatus()).isEqualTo("certificated");
            assertThat(read(() -> client.documents().download(documentId))).startsWith("%PDF".getBytes(StandardCharsets.US_ASCII));
            assertThat(read(() -> client.documents().download(documentId, "certificate-page"))).isNotEmpty();
            assertThat(read(() -> client.documents().thumbnail(documentId, true))).isNotEmpty();
            if (document.getPages() != null && !document.getPages().isEmpty()) {
                String pageId = document.getPages().getFirst().getId();
                assertThat(read(() -> client.documents().downloadPage(documentId, pageId, true))).isNotEmpty();
            }
            if (document.getVerificationCode() != null) {
                String verificationCode = document.getVerificationCode();
                assertThat(read(() -> client.documents().verifyTyped(verificationCode)).getIsValid()).isTrue();
            }
        }
        assertThat(client.signers().searchDocuments(signerId, accessCode, null,
                client.documents().details(documentIds.getFirst(), true).getEnvelopeId()).getData()).isNotEmpty();
        var delivery = client.signers().resendCertifiedCopy(accessCode, documentIds.getFirst());
        assertThat(delivery.getNotificationMethod()).isEqualTo("Email");
        assertThat(delivery.getSentTo()).isNotBlank();
        System.out.println("EMAIL SIGNATURES CERTIFICATED; final PDFs and signed page renders verified");
    }

    private static String nestedId(Object value) {
        if (value instanceof Map<?, ?> map && map.get("id") instanceof String id) return id;
        if (value instanceof String id) return id;
        return "";
    }

    private static EnvelopeDocumentRequest envelopeDocument(String documentId, String signerId) {
        return EnvelopeDocumentRequest.builder().id(documentId).signerIds(List.of(signerId)).build();
    }

    private static void expectStatus(int status, Runnable action) {
        try { action.run(); throw new AssertionError("Expected HTTP " + status); }
        catch (ApiException failure) { assertThat(failure.getStatusCode()).isEqualTo(status); }
    }

    private static <T> T read(Supplier<T> action) {
        for (int attempt = 0; ; attempt++) {
            try { return action.get(); }
            catch (ApiException failure) {
                if (attempt >= 2 || (failure.getStatusCode() != 429 && failure.getStatusCode() != 503)) throw failure;
                try { Thread.sleep(Math.min(30, Math.max(1, failure.getRetryAfter().orElse(3))) * 1000L); }
                catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); throw new AssertionError(interrupted); }
            }
        }
    }

    private static void deleteDocument(AssinafyClient client, String id) {
        for (int attempt = 0; attempt < 10; attempt++) {
            try { client.documents().delete(id); }
            catch (ApiException failure) {
                if (failure.getStatusCode() == 404) return;
                if (failure.getStatusCode() != 409 && failure.getStatusCode() != 429) throw failure;
            }
            try { client.documents().details(id); }
            catch (ApiException failure) { if (failure.getStatusCode() == 404) return; throw failure; }
            try { Thread.sleep(1500); }
            catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); throw new AssertionError(interrupted); }
        }
        throw new AssertionError("Created test document was not deleted: " + id);
    }

    private static final class Cleanup implements AutoCloseable {
        private final List<Runnable> actions = new ArrayList<>();
        void add(Runnable action) { actions.add(action); }
        @Override public void close() {
            AssertionError failure = null;
            for (Runnable action : actions.reversed()) {
                try { action.run(); }
                catch (RuntimeException | AssertionError error) {
                    if (failure == null) failure = new AssertionError("Live fixture cleanup failed");
                    failure.addSuppressed(error);
                }
            }
            if (failure != null) throw failure;
        }
    }
}
