package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.ContractExternalReference;
import com.assinafy.sdk.models.ContractRecord;
import com.assinafy.sdk.models.DocumentContract;
import com.assinafy.sdk.models.DocumentContractAmendment;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.RenewContractRequest;
import com.assinafy.sdk.request.SetContractRequest;
import com.assinafy.sdk.request.TerminateContractRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ContractResourceTest {

    private MockApiHttpClient http;
    private ContractResource contracts;

    @BeforeEach
    void setUp() {
        http = new MockApiHttpClient();
        contracts = new ContractResource(http, "account");
    }

    @Test
    void listsContractsWithEveryDocumentedFilterAndPagination() {
        http.enqueue(200, "{\"status\":200,\"data\":[{" +
                        "\"resource\":\"document_contract\",\"document_id\":\"d1\"," +
                        "\"status\":\"active\",\"value\":120000.50,\"term\":1}]}",
                Map.of("x-pagination-current-page", "1", "x-pagination-per-page", "20",
                        "x-pagination-total-count", "1", "x-pagination-page-count", "1"));

        PaginatedResult<DocumentContract> result = contracts.list(ListParams.builder()
                .status("active,expired")
                .search("Service")
                .sort("-updated_at")
                .page(1)
                .perPage(20)
                .extra("counterparty", "Example")
                .extra("end_date_from", "2026-11-01")
                .extra("end_date_to", "2027-10-31")
                .extra("ending_within_days", 30)
                .extra("external_system", "crm")
                .extra("external_id", "98765")
                .extra("expand", "document")
                .build());

        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/account/contracts");
        assertThat(http.lastCaptured().getQueryParams())
                .containsEntry("status", "active,expired")
                .containsEntry("ending_within_days", 30)
                .containsEntry("expand", "document");
        assertThat(result.getData()).singleElement().satisfies(contract -> {
            assertThat(contract.getDocumentId()).isEqualTo("d1");
            assertThat(contract.getValue()).isEqualByComparingTo("120000.50");
        });
        assertThat(result.getMeta().getTotal()).isEqualTo(1);
    }

    @Test
    void listRejectsMalformedFiltersBeforeSending() {
        for (ListParams params : List.of(
                ListParams.builder().status("active,other").build(),
                ListParams.builder().sort("name").build(),
                ListParams.builder().extra("end_date_from", "31/12/2026").build(),
                ListParams.builder().extra("ending_within_days", 3651).build(),
                ListParams.builder().extra("expand", "assignment").build())) {
            assertThatThrownBy(() -> contracts.list(params)).isInstanceOf(ValidationException.class);
        }
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void getDiscriminatesRootAndAmendmentResponsesAndEncodesPaths() {
        http.enqueue(200, "{\"status\":200,\"data\":{" +
                        "\"resource\":\"document_contract\",\"document_id\":\"root\"," +
                        "\"status\":\"active\",\"amendments\":[]}}")
                .enqueue(200, "{\"status\":200,\"data\":{" +
                        "\"resource\":\"document_contract_amendment\",\"document_id\":\"amend\"," +
                        "\"parent_document_id\":\"root\",\"relation_type\":\"amendment\"," +
                        "\"status\":\"pending\"}}");

        ContractRecord root = contracts.get("root/id", "account/id");
        ContractRecord amendment = contracts.get("amend");

        assertThat(http.capturedAt(0).getPath())
                .isEqualTo("/accounts/account%2Fid/documents/root%2Fid/contract");
        assertThat(root).isInstanceOf(DocumentContract.class);
        assertThat(amendment).isInstanceOfSatisfying(DocumentContractAmendment.class,
                value -> assertThat(value.getParentDocumentId()).isEqualTo("root"));
    }

    @Test
    void setsTermsWithExplicitNullsAndReturnsRootContract() {
        http.enqueue(200, "{\"status\":200,\"data\":{" +
                "\"resource\":\"document_contract\",\"document_id\":\"d1\"," +
                "\"status\":\"active\",\"currency\":\"BRL\"}}");
        SetContractRequest request = SetContractRequest.builder()
                .status("active")
                .counterpartyName("Example Ltda")
                .value(new BigDecimal("120000.50"))
                .currency("BRL")
                .startDate("2026-11-01")
                .endDate("2027-10-31")
                .autoRenew(true)
                .renewalTermMonths(12)
                .noticePeriodDays(30)
                .metadata(Map.of("crm_stage", "won"))
                .externalReferences(List.of(new ContractExternalReference("crm", "98765")))
                .ownerId(null)
                .updatedAt("2026-10-23T12:00:00Z")
                .build();

        ContractRecord result = contracts.set("d1", request);

        assertThat(http.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(http.lastCaptured().getJsonBody())
                .contains("\"value\":120000.50")
                .contains("\"external_references\":[{\"system\":\"crm\",\"id\":\"98765\"}]")
                .contains("\"owner_id\":null")
                .doesNotContain("attributes");
        assertThat(result).isInstanceOf(DocumentContract.class);
    }

    @Test
    void setCanLinkAnAmendmentAndDecodeItsDynamicChanges() {
        http.enqueue(200, "{\"status\":200,\"data\":{" +
                "\"resource\":\"document_contract_amendment\",\"document_id\":\"a1\"," +
                "\"parent_document_id\":\"d1\",\"relation_type\":\"amendment\"," +
                "\"status\":\"pending\",\"terms\":{\"value\":130000}," +
                "\"changes\":{\"value\":{\"from\":120000,\"to\":130000}}}}");

        ContractRecord result = contracts.set("a1", SetContractRequest.builder()
                .parentDocumentId("d1").relationType("amendment")
                .value(new BigDecimal("130000")).build());

        assertThat(result).isInstanceOfSatisfying(DocumentContractAmendment.class, amendment -> {
            assertThat(amendment.getTerms()).containsEntry("value", 130000);
            assertThat(amendment.getChanges()).containsKey("value");
        });
    }

    @Test
    void setRejectsInvalidTermsBeforeSending() {
        List<SetContractRequest> requests = List.of(
                new SetContractRequest(),
                SetContractRequest.builder().status("closed").build(),
                SetContractRequest.builder().currency("REAL").build(),
                SetContractRequest.builder().value(new BigDecimal("1.001")).build(),
                SetContractRequest.builder().startDate("2027-01-02").endDate("2027-01-01").build(),
                SetContractRequest.builder().renewalTermMonths(0).build(),
                SetContractRequest.builder().metadata(Map.of("bad key", "value")).build(),
                SetContractRequest.builder().externalReferences(List.of(
                        new ContractExternalReference("bad system", "1"))).build());

        requests.forEach(request ->
                assertThatThrownBy(() -> contracts.set("d1", request))
                        .isInstanceOf(ValidationException.class));
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void setPreservesExplicitNullAutoRenew() {
        http.enqueue(200, rootResponse("active", 1));

        contracts.set("d1", SetContractRequest.builder().autoRenew(null).build());

        assertThat(http.lastCaptured().getJsonBody()).isEqualTo("{\"auto_renew\":null}");
    }

    @Test
    void listsRelatedContractFamily() {
        http.enqueue(200, "{\"status\":200,\"data\":{" +
                "\"resource\":\"document_contract\",\"document_id\":\"d1\"," +
                "\"status\":\"active\",\"amendments\":[{" +
                "\"resource\":\"document_contract_amendment\",\"document_id\":\"a1\"," +
                "\"parent_document_id\":\"d1\",\"relation_type\":\"renewal\"," +
                "\"status\":\"applied\"}]}}");

        DocumentContract result = contracts.listRelated("a1");

        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/accounts/account/documents/a1/contract/related");
        assertThat(result.getAmendments()).singleElement().satisfies(amendment ->
                assertThat(amendment.getRelationType()).isEqualTo("renewal"));
    }

    @Test
    void renewsWithIdempotencyPreconditionsAndExplicitNullValue() {
        http.enqueue(200, rootResponse("active", 2));

        DocumentContract result = contracts.renew("d1", RenewContractRequest.builder()
                .term(1).value(null).updatedAt("2026-10-23T12:00:00Z").build());

        assertThat(http.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(http.lastCaptured().getPath())
                .isEqualTo("/accounts/account/documents/d1/contract/renew");
        assertThat(http.lastCaptured().getJsonBody())
                .contains("\"term\":1", "\"value\":null", "\"updated_at\":\"2026-10-23T12:00:00Z\"");
        assertThat(result.getTerm()).isEqualTo(2);
    }

    @Test
    void renewalRequiresTermOrEndDateBeforeSending() {
        assertThatThrownBy(() -> contracts.renew("d1", new RenewContractRequest()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> contracts.renew("d1",
                RenewContractRequest.builder().endDate(null).build()))
                .isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    @Test
    void renewalAcceptsBlankEndDateWhenCurrentTermIsPresent() {
        http.enqueue(200, rootResponse("active", 2));

        contracts.renew("d1", RenewContractRequest.builder().endDate("").term(1).build());

        assertThat(http.lastCaptured().getJsonBody()).contains("\"end_date\":\"\"", "\"term\":1");
    }

    @Test
    void terminatesWithOptionalReasonOrEmptyBody() {
        http.enqueue(200, rootResponse("terminated", 1))
                .enqueue(200, rootResponse("terminated", 1));

        contracts.terminate("d1", TerminateContractRequest.builder()
                .reason("Replaced by a new agreement")
                .updatedAt("2026-10-23T12:00:00Z").build());
        contracts.terminate("d2");

        assertThat(http.capturedAt(0).getJsonBody())
                .contains("\"reason\":\"Replaced by a new agreement\"");
        assertThat(http.capturedAt(1).getJsonBody()).isEqualTo("{}");
    }

    @Test
    void terminationValidatesReasonAndTimestampBeforeSending() {
        assertThatThrownBy(() -> contracts.terminate("d1", TerminateContractRequest.builder()
                .reason("x".repeat(501)).build())).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> contracts.terminate("d1", TerminateContractRequest.builder()
                .updatedAt("yesterday").build())).isInstanceOf(ValidationException.class);
        assertThat(http.capturedCount()).isZero();
    }

    private static String rootResponse(String status, int term) {
        return "{\"status\":200,\"data\":{\"resource\":\"document_contract\"," +
                "\"document_id\":\"d1\",\"status\":\"" + status + "\",\"term\":" + term + "}}";
    }
}
