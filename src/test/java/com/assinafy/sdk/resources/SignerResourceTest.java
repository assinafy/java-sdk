package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.request.CreateSignerRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.UpdateSignerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class SignerResourceTest {

    private MockApiHttpClient mock;
    private SignerResource resource;

    private static final String EMPTY_LIST = "{\"status\":200,\"data\":[]}";
    private static final String SIGNER_123 = "{\"status\":200,\"data\":{\"id\":\"123\"}}";

    @BeforeEach
    void setUp() {
        mock = new MockApiHttpClient();
        resource = new SignerResource(mock, "test-account");
    }

    @Test
    void throwsWhenUpdatingWithoutSignerId() {
        assertThatThrownBy(() -> resource.update("", UpdateSignerRequest.builder().fullName("Test").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void throwsWhenDeletingWithoutSignerId() {
        assertThatThrownBy(() -> resource.delete(""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void throwsWhenNoAccountIdAvailable() {
        SignerResource noAccountResource = new SignerResource(mock);
        assertThatThrownBy(() -> noAccountResource.create(CreateSignerRequest.builder()
                .fullName("Test")
                .email("test@test.com")
                .build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void rejectsInvalidEmail() {
        assertThatThrownBy(() -> resource.create(CreateSignerRequest.builder()
                .fullName("Test")
                .email("not-an-email")
                .build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void usesCustomAccountIdWhenProvided() {
        mock.enqueue(200, EMPTY_LIST)
            .enqueue(200, SIGNER_123);

        resource.create(CreateSignerRequest.builder()
                .fullName("Test")
                .email("test@test.com")
                .build(), "custom-account");

        assertThat(mock.capturedAt(1).getPath()).isEqualTo("/accounts/custom-account/signers");
    }

    @Test
    void usesDefaultAccountIdWhenCustomNotProvided() {
        mock.enqueue(200, EMPTY_LIST)
            .enqueue(200, SIGNER_123);

        resource.create(CreateSignerRequest.builder()
                .fullName("Test")
                .email("test@test.com")
                .build());

        assertThat(mock.capturedAt(1).getPath()).isEqualTo("/accounts/test-account/signers");
    }

    @Test
    void listPassesSearchViaQueryParams() {
        mock.enqueue(200, EMPTY_LIST);
        resource.list(ListParams.builder().search("john@example.com").build());

        MockApiHttpClient.CapturedRequest req = mock.lastCaptured();
        assertThat(req.getQueryParams()).containsEntry("search", "john@example.com");
    }

    @Test
    void listReturnsParsedPaginationMeta() {
        Map<String, String> headers = Map.of(
                "x-pagination-current-page", "2",
                "x-pagination-per-page", "20",
                "x-pagination-total-count", "45",
                "x-pagination-page-count", "3"
        );
        mock.enqueue(200, "{\"status\":200,\"data\":[]}", headers);

        PaginatedResult<Signer> result = resource.list(ListParams.builder().page(2).build());
        assertThat(result.getMeta()).isNotNull();
        assertThat(result.getMeta().getCurrentPage()).isEqualTo(2);
        assertThat(result.getMeta().getPerPage()).isEqualTo(20);
        assertThat(result.getMeta().getTotal()).isEqualTo(45);
        assertThat(result.getMeta().getLastPage()).isEqualTo(3);
    }

    @Test
    void findByEmailReturnsNullWhenNoMatch() {
        mock.enqueue(200, EMPTY_LIST);
        Signer result = resource.findByEmail("nobody@example.com");
        assertThat(result).isNull();
    }

    @Test
    void findByEmailReturnsMatchingSigner() {
        mock.enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"1\",\"full_name\":\"John\",\"email\":\"JOHN@EXAMPLE.COM\"}]}");
        Signer result = resource.findByEmail("john@example.com");
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
    }

    @Test
    void createReusesExistingSignerByEmail() {
        mock.enqueue(200, "{\"status\":200,\"data\":[{\"id\":\"existing\",\"full_name\":\"John\",\"email\":\"john@example.com\"}]}");

        Signer result = resource.create(CreateSignerRequest.builder()
                .fullName("John")
                .email("john@example.com")
                .build());

        assertThat(result.getId()).isEqualTo("existing");
        assertThat(mock.capturedCount()).isEqualTo(1);
    }

    @Test
    void createMapsPhoneToWhatsappPhoneNumber() {
        mock.enqueue(200, EMPTY_LIST)
            .enqueue(200, SIGNER_123);

        resource.create(CreateSignerRequest.builder()
                .fullName("John")
                .email("john@example.com")
                .phone("+5548999990000")
                .build());

        String body = mock.capturedAt(1).getJsonBody();
        assertThat(body).contains("whatsapp_phone_number");
        assertThat(body).contains("+5548999990000");
        assertThat(body).doesNotContain("\"phone\"");
    }

    @Test
    void listUsesHyphenatedPerPageParam() {
        mock.enqueue(200, EMPTY_LIST);
        resource.list(ListParams.builder().perPage(25).build());

        MockApiHttpClient.CapturedRequest req = mock.lastCaptured();
        assertThat(req.getQueryParams()).containsEntry("per-page", 25);
        assertThat(req.getQueryParams()).doesNotContainKey("per_page");
    }

    @Test
    void getSelfUrlEncodesSignerAccessCode() {
        mock.enqueue(200, SIGNER_123);
        resource.getSelf("abc def");
        assertThat(mock.lastCaptured().getPath())
                .isEqualTo("/signers/self?signer-access-code=abc+def");
    }

    @Test
    void signMultipleRequiresDocumentIds() {
        assertThatThrownBy(() -> resource.signMultiple("code", java.util.List.of()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void signMultiplePutsToCorrectEndpoint() {
        mock.enqueue(200, "{\"status\":200,\"data\":[]}");
        resource.signMultiple("code", java.util.List.of("d1", "d2"));
        assertThat(mock.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(mock.lastCaptured().getPath())
                .isEqualTo("/signers/documents/sign-multiple?signer-access-code=code");
        assertThat(mock.lastCaptured().getJsonBody()).contains("d1").contains("d2");
    }

    @Test
    void declineMultipleIncludesDeclineReason() {
        mock.enqueue(200, "{\"status\":200,\"data\":[]}");
        resource.declineMultiple("code", java.util.List.of("d1"), "no");
        assertThat(mock.lastCaptured().getJsonBody()).contains("decline_reason").contains("no");
    }

    @Test
    void createNormalisesCpfByStrippingNonDigits() {
        mock.enqueue(200, EMPTY_LIST)
            .enqueue(200, SIGNER_123);

        resource.create(CreateSignerRequest.builder()
                .fullName("John")
                .email("john@example.com")
                .cpf("123.456.789-00")
                .build());

        String body = mock.capturedAt(1).getJsonBody();
        assertThat(body).contains("\"cpf\":\"12345678900\"");
    }
}
