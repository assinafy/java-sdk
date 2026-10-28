package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.ContractExternalReference;
import com.assinafy.sdk.models.ContractRecord;
import com.assinafy.sdk.models.DocumentContract;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.RenewContractRequest;
import com.assinafy.sdk.request.SetContractRequest;
import com.assinafy.sdk.request.TerminateContractRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Contract lifecycle operations for terms tracked on workspace documents. */
public class ContractResource extends BaseResource {

    private static final Set<String> STATUSES = Set.of("draft", "active", "expired", "terminated");
    private static final Set<String> RELATION_TYPES = Set.of("amendment", "renewal", "termination");
    private static final Set<String> SORTS = Set.of("end_date", "-end_date", "updated_at", "-updated_at");
    private static final Pattern CURRENCY = Pattern.compile("^[A-Za-z]{3}$");
    private static final Pattern METADATA_KEY = Pattern.compile("^[A-Za-z0-9_.-]{1,40}$");
    private static final Pattern EXTERNAL_SYSTEM = Pattern.compile("^[A-Za-z0-9_.-]{1,64}$");
    private static final BigDecimal MAX_VALUE = new BigDecimal("9999999999999.99");

    /**
     * Creates a contract resource.
     *
     * @param http HTTP transport
     * @param defaultAccountId default account ID
     * @param logger diagnostic logger
     */
    public ContractResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    /**
     * Creates a contract resource with the no-op logger.
     *
     * @param http HTTP transport
     * @param defaultAccountId default account ID
     */
    public ContractResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    /**
     * Creates a contract resource without a default account.
     *
     * @param http HTTP transport
     */
    public ContractResource(ApiHttpClient http) {
        super(http);
    }

    /**
     * Lists contracts in the default account with default paging.
     *
     * @return tracked contracts and pagination metadata
     */
    public PaginatedResult<DocumentContract> list() {
        return list(null, null);
    }

    /**
     * Lists contracts in the default account.
     *
     * @param params paging and filters, or {@code null}; use {@code status}, {@code search},
     *               {@code sort}, and extras {@code counterparty}, {@code end_date_from},
     *               {@code end_date_to}, {@code ending_within_days}, {@code external_system},
     *               {@code external_id}, and {@code expand=document}
     * @return tracked contracts and pagination metadata
     */
    public PaginatedResult<DocumentContract> list(ListParams params) {
        return list(params, null);
    }

    /**
     * Lists tracked contract terms ({@code GET /v1/accounts/{accountId}/contracts}).
     *
     * <p>The response data is an array of {@link DocumentContract}; pagination is returned in the
     * standard {@code X-Pagination-*} headers. Results default to descending {@code updated_at}.
     * OAuth callers need {@code documents:read}. Authentication is Bearer JWT or
     * {@code X-Api-Key}; there is no request body.</p>
     * <pre>{
     *   "status": 200,
     *   "data": [{
     *     "resource": "document_contract", "document_id": "66b2f0e1c3a4d5e6f7a8b9c0",
     *     "status": "active", "counterparty_name": "Example Ltda.",
     *     "value": 120000.50, "currency": "BRL", "start_date": "2026-11-01",
     *     "end_date": "2027-10-31", "auto_renew": true,
     *     "renewal_term_months": 12, "notice_period_days": 30, "term": 1,
     *     "owner_id": null, "metadata": {"crm_stage": "won"},
     *     "external_references": [{"system": "crm", "id": "98765"}],
     *     "created_at": "2026-10-23T12:00:00Z",
     *     "updated_at": "2026-10-23T12:00:00Z"
     *   }]
     * }</pre>
     * <p>Documented statuses: 200, 400 for invalid filters, 401, and 500.</p>
     *
     * @param params paging and filters, or {@code null}; statuses are comma-separated, dates use
     *               {@code YYYY-MM-DD}, {@code ending_within_days} is 0–3650, sort is
     *               {@code end_date}/{@code updated_at} with optional {@code -}, and expand is
     *               {@code document}
     * @param accountId explicit account ID, or {@code null} for the default
     * @return tracked contracts and pagination metadata
     * @throws ValidationException if the account or a locally checkable filter is invalid
     */
    public PaginatedResult<DocumentContract> list(ListParams params, String accountId) {
        String id = pathSegment(accountId(accountId), "Account ID");
        Map<String, Object> query = params != null ? params.toQueryParams() : Map.of();
        validateListParams(query);
        return callList("Failed to list contracts",
                () -> http.get("/accounts/" + id + "/contracts", query),
                DocumentContract.class);
    }

    /**
     * Reads a document's contract terms or amendment link from the default account.
     *
     * @param documentId contract or amendment document ID
     * @return root terms or a linked amendment
     */
    public ContractRecord get(String documentId) {
        return get(documentId, null);
    }

    /**
     * Reads a contract or amendment ({@code GET
     * /v1/accounts/{accountId}/documents/{documentId}/contract}).
     *
     * <p>A root response has {@code resource=document_contract}, all current terms, external
     * references, ordered amendments, and {@code created_at}/{@code updated_at}. A linked
     * instrument has {@code resource=document_contract_amendment}, its relation, delta, preview or
     * recorded changes, application state, and timestamps. Reads remain available when the plan
     * no longer includes contract tracking. Authentication is Bearer JWT or {@code X-Api-Key};
     * there is no request body.</p>
     * <pre>{
     *   "status": 200,
     *   "data": {
     *     "resource": "document_contract_amendment",
     *     "document_id": "7c3d1e2f4a5b6c7d8e9f0a1b",
     *     "document_name": "Amendment.pdf",
     *     "parent_document_id": "66b2f0e1c3a4d5e6f7a8b9c0",
     *     "relation_type": "amendment", "number": null, "status": "pending",
     *     "document_status": "pending_signature", "apply_error": null,
     *     "currency": "BRL", "terms": {"value": 130000},
     *     "changes": {"value": {"from": 120000.50, "to": 130000}},
     *     "applied_at": null, "created_at": "2026-10-23T12:00:00Z",
     *     "updated_at": "2026-10-23T12:00:00Z"
     *   }
     * }</pre>
     * <p>Documented statuses: 200, 401, 404 when no terms exist, and 500.</p>
     *
     * @param documentId contract or amendment document ID
     * @param accountId explicit account ID, or {@code null} for the default
     * @return root terms or a linked amendment
     * @throws ValidationException if an ID is blank or a dot-traversal segment
     */
    public ContractRecord get(String documentId, String accountId) {
        return call("Failed to fetch contract terms",
                () -> http.get(contractPath(documentId, accountId)),
                ContractRecord.class);
    }

    /**
     * Creates or partially updates terms in the default account.
     *
     * @param documentId document whose terms or amendment link will change
     * @param request at least one contract attribute
     * @return the saved root contract or amendment link
     */
    public ContractRecord set(String documentId, SetContractRequest request) {
        return set(documentId, request, null);
    }

    /**
     * Creates or partially updates contract terms ({@code PUT
     * /v1/accounts/{accountId}/documents/{documentId}/contract}).
     *
     * <p>The body may contain {@code status}, counterparty fields, {@code value}/{@code currency},
     * start/end dates, renewal settings, {@code owner_id}, string metadata, external references,
     * {@code parent_document_id}/{@code relation_type}, and an {@code updated_at} concurrency
     * precondition. Only supplied fields are sent; documented nullable fields are cleared by an
     * explicit JSON null. A parent document links this document as an amendment, renewal, or
     * termination. OAuth callers need {@code documents:write}. The API returns 403 when the plan
     * lacks contract tracking and 409 when {@code updated_at} is stale. Authentication is Bearer
     * JWT or {@code X-Api-Key}.</p>
     * <pre>{
     *   "status": "active", "counterparty_name": "Example Ltda.",
     *   "counterparty_taxpayer_id": null, "value": 120000.50, "currency": "BRL",
     *   "start_date": "2026-11-01", "end_date": "2027-10-31",
     *   "auto_renew": true, "renewal_term_months": 12, "notice_period_days": 30,
     *   "owner_id": null, "metadata": {"crm_stage": "won"},
     *   "external_references": [{"system": "crm", "id": "98765"}],
     *   "updated_at": "2026-10-23T12:00:00Z"
     * }</pre>
     * <p>An amendment link instead includes, for example,
     * {@code {"parent_document_id":"66b2...","relation_type":"amendment","value":130000}}.</p>
     * <p>A 200 envelope returns either the complete {@link DocumentContract} shape shown by
     * {@link #get(String, String)}, or {@link com.assinafy.sdk.models.DocumentContractAmendment}.
     * Documented statuses: 200, 400, 401, 403, 404, 409, and 500.</p>
     *
     * @param documentId document whose terms or amendment link will change
     * @param request at least one contract attribute
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the saved root contract or amendment link
     * @throws ValidationException if the request, an ID, date, amount, metadata field, or external
     *         reference violates a locally checkable API constraint
     */
    public ContractRecord set(String documentId, SetContractRequest request, String accountId) {
        if (request == null) throw new ValidationException("Contract request is required");
        validateSetRequest(request.getAttributes());
        String json = serialise(request);
        return call("Failed to set contract terms",
                () -> http.put(contractPath(documentId, accountId), json),
                ContractRecord.class);
    }

    /**
     * Reads the complete contract family from the default account.
     *
     * @param documentId any document in the contract family
     * @return the root terms with every linked instrument
     */
    public DocumentContract listRelated(String documentId) {
        return listRelated(documentId, null);
    }

    /**
     * Reads a complete contract family ({@code GET
     * /v1/accounts/{accountId}/documents/{documentId}/contract/related}).
     *
     * <p>The input may be the root or any linked instrument. The response is always the root
     * {@link DocumentContract}, with every amendment, renewal, and termination in signing order.
     * Authentication is Bearer JWT or {@code X-Api-Key}; there is no request body.</p>
     * <pre>{
     *   "status": 200,
     *   "data": {
     *     "resource": "document_contract", "document_id": "66b2f0e1c3a4d5e6f7a8b9c0",
     *     "status": "active", "term": 1,
     *     "amendments": [{
     *       "resource": "document_contract_amendment",
     *       "document_id": "7c3d1e2f4a5b6c7d8e9f0a1b",
     *       "parent_document_id": "66b2f0e1c3a4d5e6f7a8b9c0",
     *       "relation_type": "amendment", "status": "pending"
     *     }]
     *   }
     * }</pre>
     * <p>Documented statuses: 200, 401, 404 when no terms exist, and 500.</p>
     *
     * @param documentId any document in the contract family
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the root terms with every linked instrument
     * @throws ValidationException if an ID is blank or a dot-traversal segment
     */
    public DocumentContract listRelated(String documentId, String accountId) {
        return call("Failed to fetch related contracts",
                () -> http.get(contractPath(documentId, accountId) + "/related"),
                DocumentContract.class);
    }

    /**
     * Starts a contract's next term in the default account.
     *
     * @param documentId root contract document ID
     * @param request current term or later end date, optional value, and concurrency timestamp
     * @return the renewed contract
     */
    public DocumentContract renew(String documentId, RenewContractRequest request) {
        return renew(documentId, request, null);
    }

    /**
     * Starts the next contract term ({@code POST
     * /v1/accounts/{accountId}/documents/{documentId}/contract/renew}).
     *
     * <p>Send the current positive {@code term} to renew by {@code renewal_term_months}, or a later
     * nonnull {@code end_date}; {@code value} replaces or clears the amount and {@code updated_at}
     * is an optional concurrency precondition. A renewal reopens an expired contract, but not a
     * terminated one. The term/date precondition makes a retry safe from renewing twice.
     * Authentication is Bearer JWT or {@code X-Api-Key}.</p>
     * <pre>{
     *   "end_date": null, "value": 130000, "term": 1,
     *   "updated_at": "2026-10-23T12:00:00Z"
     * }</pre>
     * <pre>{
     *   "status": 200,
     *   "data": {
     *     "resource": "document_contract", "document_id": "66b2f0e1c3a4d5e6f7a8b9c0",
     *     "status": "active", "value": 130000, "currency": "BRL",
     *     "end_date": "2028-10-31", "term": 2,
     *     "updated_at": "2026-10-23T12:01:00Z"
     *   }
     * }</pre>
     * <p>Documented statuses: 200, 400, 401, 403, 404, 409, and 500.</p>
     *
     * @param documentId root contract document ID
     * @param request current term or later end date, optional value, and concurrency timestamp
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the renewed contract
     * @throws ValidationException if no positive term or nonnull end date is supplied, or an input
     *         violates the documented date, amount, or timestamp constraint
     */
    public DocumentContract renew(String documentId, RenewContractRequest request, String accountId) {
        if (request == null) throw new ValidationException("Contract renewal request is required");
        validateRenewRequest(request.getAttributes());
        String json = serialise(request);
        return call("Failed to renew contract",
                () -> http.post(contractPath(documentId, accountId) + "/renew", json),
                DocumentContract.class);
    }

    /**
     * Terminates a contract without a reason in the default account.
     *
     * @param documentId root contract document ID
     * @return the terminated contract
     */
    public DocumentContract terminate(String documentId) {
        return terminate(documentId, new TerminateContractRequest(), null);
    }

    /**
     * Terminates a contract in the default account.
     *
     * @param documentId root contract document ID
     * @param request optional reason and concurrency timestamp; {@code null} sends an empty body
     * @return the terminated contract
     */
    public DocumentContract terminate(String documentId, TerminateContractRequest request) {
        return terminate(documentId, request, null);
    }

    /**
     * Terminates a contract ({@code POST
     * /v1/accounts/{accountId}/documents/{documentId}/contract/terminate}).
     *
     * <p>The optional body contains a nullable {@code reason} of at most 500 Unicode characters
     * and an {@code updated_at} concurrency precondition. The API changes the status to
     * {@code terminated}, stops lifecycle alerts, and records the reason in the document activity.
     * It returns 403 when the plan lacks tracking and 409 for a stale timestamp. Authentication is
     * Bearer JWT or {@code X-Api-Key}.</p>
     * <pre>{
     *   "reason": "Replaced by a new agreement",
     *   "updated_at": "2026-10-23T12:00:00Z"
     * }</pre>
     * <pre>{
     *   "status": 200,
     *   "data": {
     *     "resource": "document_contract", "document_id": "66b2f0e1c3a4d5e6f7a8b9c0",
     *     "status": "terminated", "term": 1,
     *     "updated_at": "2026-10-23T12:01:00Z"
     *   }
     * }</pre>
     * <p>Documented statuses: 200, 400, 401, 403, 404, 409, and 500.</p>
     *
     * @param documentId root contract document ID
     * @param request optional reason and concurrency timestamp; {@code null} sends an empty body
     * @param accountId explicit account ID, or {@code null} for the default
     * @return the terminated contract
     * @throws ValidationException if the reason is too long or the timestamp/IDs are invalid
     */
    public DocumentContract terminate(
            String documentId, TerminateContractRequest request, String accountId) {
        TerminateContractRequest body = request != null ? request : new TerminateContractRequest();
        validateTerminateRequest(body.getAttributes());
        String json = serialise(body);
        return call("Failed to terminate contract",
                () -> http.post(contractPath(documentId, accountId) + "/terminate", json),
                DocumentContract.class);
    }

    private String contractPath(String documentId, String accountId) {
        String account = pathSegment(accountId(accountId), "Account ID");
        String document = pathSegment(documentId, "Document ID");
        return "/accounts/" + account + "/documents/" + document + "/contract";
    }

    private static void validateListParams(Map<String, Object> query) {
        Object status = query.get("status");
        if (status != null && (!(status instanceof String text) || text.isBlank()
                || !List.of(text.split(",", -1)).stream().allMatch(STATUSES::contains))) {
            throw new ValidationException("Contract status must contain documented comma-separated values");
        }
        validateDateValue(query.get("end_date_from"), "end_date_from");
        validateDateValue(query.get("end_date_to"), "end_date_to");
        Object days = query.get("ending_within_days");
        if (days != null && (!(days instanceof Integer value) || value < 0 || value > 3650)) {
            throw new ValidationException("ending_within_days must be an integer from 0 to 3650");
        }
        Object sort = query.get("sort");
        if (sort != null && (!(sort instanceof String value) || !SORTS.contains(value))) {
            throw new ValidationException("Contract sort must be end_date or updated_at, optionally prefixed by -");
        }
        Object expand = query.get("expand");
        if (expand != null && !"document".equals(expand)) {
            throw new ValidationException("Contract expand must be document");
        }
        for (String key : List.of("counterparty", "external_system", "external_id")) {
            Object value = query.get(key);
            if (value != null && !(value instanceof String)) {
                throw new ValidationException(key + " must be a string");
            }
        }
    }

    private static void validateSetRequest(Map<String, Object> fields) {
        if (fields.isEmpty()) throw new ValidationException("Contract request must include at least one attribute");
        validateEnum(fields, "status", STATUSES);
        validateNullableText(fields, "counterparty_name", 255);
        validateNullableNonblank(fields, "counterparty_taxpayer_id");
        validateMoney(fields.get("value"));
        Object currency = fields.get("currency");
        if (currency != null && (!(currency instanceof String text) || !CURRENCY.matcher(text).matches())) {
            throw new ValidationException("Contract currency must be a three-letter code or null");
        }
        LocalDate start = validateDateValue(fields.get("start_date"), "start_date");
        LocalDate end = validateDateValue(fields.get("end_date"), "end_date");
        if (start != null && end != null && end.isBefore(start)) {
            throw new ValidationException("Contract end_date must be on or after start_date");
        }
        validateRange(fields.get("renewal_term_months"), "renewal_term_months", 1, 600);
        validateRange(fields.get("notice_period_days"), "notice_period_days", 0, 3650);
        validateNullableNonblank(fields, "owner_id");
        validateMetadata(fields.get("metadata"));
        validateExternalReferences(fields.get("external_references"));
        validateNullableNonblank(fields, "parent_document_id");
        validateEnum(fields, "relation_type", RELATION_TYPES);
        validateOptionalTimestamp(fields, "updated_at", true);
    }

    private static void validateRenewRequest(Map<String, Object> fields) {
        Object endDateValue = fields.get("end_date");
        LocalDate endDate = endDateValue instanceof String text && text.isBlank()
                ? null : validateDateValue(endDateValue, "end_date");
        validateMoney(fields.get("value"));
        Object term = fields.get("term");
        if (term != null && (!(term instanceof Integer value) || value < 1)) {
            throw new ValidationException("Contract renewal term must be a positive integer");
        }
        if (endDate == null && term == null) {
            throw new ValidationException("Contract renewal requires term or a nonnull end_date");
        }
        validateOptionalTimestamp(fields, "updated_at", false);
    }

    private static void validateTerminateRequest(Map<String, Object> fields) {
        validateNullableText(fields, "reason", 500);
        validateOptionalTimestamp(fields, "updated_at", false);
    }

    private static void validateEnum(Map<String, Object> fields, String key, Set<String> allowed) {
        if (!fields.containsKey(key)) return;
        Object value = fields.get(key);
        if (!(value instanceof String text) || !allowed.contains(text)) {
            throw new ValidationException(key + " has an unsupported value");
        }
    }

    private static void validateNullableText(Map<String, Object> fields, String key, int maximum) {
        if (!fields.containsKey(key) || fields.get(key) == null) return;
        Object value = fields.get(key);
        if (!(value instanceof String text) || text.codePointCount(0, text.length()) > maximum) {
            throw new ValidationException(key + " must be a string of at most " + maximum + " characters or null");
        }
    }

    private static void validateNullableNonblank(Map<String, Object> fields, String key) {
        if (!fields.containsKey(key) || fields.get(key) == null) return;
        Object value = fields.get(key);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new ValidationException(key + " must be a nonblank string or null");
        }
    }

    private static void validateMoney(Object value) {
        if (value == null) return;
        if (!(value instanceof BigDecimal amount) || amount.signum() < 0
                || amount.compareTo(MAX_VALUE) > 0 || Math.max(amount.stripTrailingZeros().scale(), 0) > 2) {
            throw new ValidationException("Contract value must be nonnegative with at most two decimal places");
        }
    }

    private static LocalDate validateDateValue(Object value, String key) {
        if (value == null) return null;
        if (!(value instanceof String text) || text.isBlank()) {
            throw new ValidationException(key + " must use YYYY-MM-DD");
        }
        try {
            LocalDate date = LocalDate.parse(text);
            if (date.getYear() < 1000) throw new ValidationException(key + " must be from year 1000");
            return date;
        } catch (DateTimeParseException e) {
            throw new ValidationException(key + " must be a valid date in YYYY-MM-DD form");
        }
    }

    private static void validateRange(Object value, String key, int minimum, int maximum) {
        if (value != null && (!(value instanceof Integer number)
                || number < minimum || number > maximum)) {
            throw new ValidationException(key + " must be an integer from " + minimum + " to " + maximum + " or null");
        }
    }

    private static void validateOptionalTimestamp(
            Map<String, Object> fields, String key, boolean nullable) {
        if (!fields.containsKey(key)) return;
        Object value = fields.get(key);
        if (value == null && nullable) return;
        if (!(value instanceof String text)) {
            throw new ValidationException(key + " must be an ISO-8601 timestamp with an offset");
        }
        try {
            OffsetDateTime.parse(text);
        } catch (DateTimeParseException e) {
            throw new ValidationException(key + " must be an ISO-8601 timestamp with an offset");
        }
    }

    private static void validateMetadata(Object value) {
        if (value == null) return;
        if (!(value instanceof Map<?, ?> metadata) || metadata.size() > 50) {
            throw new ValidationException("Contract metadata must contain at most 50 fields or null");
        }
        for (Map.Entry<?, ?> entry : metadata.entrySet()) {
            if (!(entry.getKey() instanceof String key) || !METADATA_KEY.matcher(key).matches()
                    || !(entry.getValue() instanceof String text)
                    || text.codePointCount(0, text.length()) > 500) {
                throw new ValidationException(
                        "Contract metadata keys use 1-40 letters, digits, _, -, or . and values are at most 500 characters");
            }
        }
    }

    private static void validateExternalReferences(Object value) {
        if (value == null) return;
        if (!(value instanceof List<?> references) || references.size() > 20) {
            throw new ValidationException("Contract external references must contain at most 20 entries or null");
        }
        for (Object item : references) {
            if (!(item instanceof ContractExternalReference reference)
                    || reference.getSystem() == null
                    || !EXTERNAL_SYSTEM.matcher(reference.getSystem()).matches()
                    || reference.getId() == null || reference.getId().isBlank()
                    || reference.getId().codePointCount(0, reference.getId().length()) > 255
                    || reference.getId().codePoints().anyMatch(codePoint -> codePoint > Character.MAX_VALUE)) {
                throw new ValidationException("Contract external reference system or id is invalid");
            }
        }
    }
}
