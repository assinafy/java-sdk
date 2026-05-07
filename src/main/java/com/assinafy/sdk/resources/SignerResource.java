package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.request.CreateSignerRequest;
import com.assinafy.sdk.request.ListParams;
import com.assinafy.sdk.request.UpdateSignerRequest;
import com.assinafy.sdk.util.ResponseHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignerResource extends BaseResource {

    private static final Pattern EMAIL_RE = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public SignerResource(ApiHttpClient http, String defaultAccountId, Logger logger) {
        super(http, defaultAccountId, logger);
    }

    public SignerResource(ApiHttpClient http, String defaultAccountId) {
        super(http, defaultAccountId);
    }

    public SignerResource(ApiHttpClient http) {
        super(http);
    }

    public Signer create(CreateSignerRequest request) {
        return create(request, null);
    }

    public Signer create(CreateSignerRequest request, String accountId) {
        assertEmail(request.getEmail());
        String id = accountId(accountId);

        Signer existing = findByEmail(request.getEmail(), id);
        if (existing != null) {
            logger.info("Using existing signer", Map.of("email", request.getEmail()));
            return existing;
        }

        logger.info("Creating signer", Map.of("email", request.getEmail()));
        try {
            String body = serialise(normaliseSignerRequest(request));
            return call("Failed to create signer", () -> http.post("/accounts/" + id + "/signers", body), Signer.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 409) {
                Signer duplicate = findByEmail(request.getEmail(), id);
                if (duplicate != null) {
                    logger.info("Signer already exists, using existing signer", Map.of("email", request.getEmail()));
                    return duplicate;
                }
            }
            throw e;
        }
    }

    public Signer get(String signerId) {
        return get(signerId, null);
    }

    public Signer get(String signerId, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        return call("Failed to fetch signer", () -> http.get("/accounts/" + id + "/signers/" + sid), Signer.class);
    }

    public PaginatedResult<Signer> list() {
        return list(new ListParams(), null);
    }

    public PaginatedResult<Signer> list(ListParams params) {
        return list(params, null);
    }

    public PaginatedResult<Signer> list(ListParams params, String accountId) {
        String id = accountId(accountId);
        Map<String, Object> queryParams = params != null ? params.toQueryParams() : Map.of();
        return callList("Failed to list signers", () -> http.get("/accounts/" + id + "/signers", queryParams), Signer.class);
    }

    public Signer update(String signerId, UpdateSignerRequest request) {
        return update(signerId, request, null);
    }

    public Signer update(String signerId, UpdateSignerRequest request, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        String body = serialise(normaliseUpdateRequest(request));
        return call("Failed to update signer", () -> http.put("/accounts/" + id + "/signers/" + sid, body), Signer.class);
    }

    public void delete(String signerId) {
        delete(signerId, null);
    }

    public void delete(String signerId, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        callVoid("Failed to delete signer", () -> http.delete("/accounts/" + id + "/signers/" + sid));
    }

    public Signer findByEmail(String email) {
        return findByEmail(email, null);
    }

    public Signer findByEmail(String email, String accountId) {
        assertEmail(email);
        try {
            ListParams params = ListParams.builder().search(email).perPage(100).build();
            PaginatedResult<Signer> result = list(params, accountId);
            String lower = email.toLowerCase();
            return result.getData().stream()
                    .filter(s -> s.getEmail() != null && s.getEmail().toLowerCase().equals(lower))
                    .findFirst()
                    .orElse(null);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) return null;
            throw e;
        }
    }

    private void assertEmail(String email) {
        if (email == null || !EMAIL_RE.matcher(email).matches()) {
            throw new ValidationException("Invalid email address", Map.of("email", email != null ? email : ""));
        }
    }

    private Map<String, Object> normaliseSignerRequest(CreateSignerRequest req) {
        Map<String, Object> map = new HashMap<>();
        if (req.getFullName() != null) map.put("full_name", req.getFullName());
        if (req.getEmail() != null) map.put("email", req.getEmail());
        String phone = req.getWhatsappPhoneNumber();
        if (phone != null) map.put("whatsapp_phone_number", phone);
        if (req.getCpf() != null) map.put("cpf", req.getCpf().replaceAll("\\D", ""));
        if (req.getMetadata() != null) map.put("metadata", req.getMetadata());
        return map;
    }

    private Map<String, Object> normaliseUpdateRequest(UpdateSignerRequest req) {
        Map<String, Object> map = new HashMap<>();
        if (req.getFullName() != null) map.put("full_name", req.getFullName());
        if (req.getEmail() != null) map.put("email", req.getEmail());
        if (req.getWhatsappPhoneNumber() != null) map.put("whatsapp_phone_number", req.getWhatsappPhoneNumber());
        if (req.getCpf() != null) map.put("cpf", req.getCpf().replaceAll("\\D", ""));
        return map;
    }

    public Signer getSelf(String signerAccessCode) {
        requireId(signerAccessCode, "Signer access code");
        return call("Failed to fetch signer self info",
                () -> http.get("/signers/self?signer-access-code=" + signerAccessCode),
                Signer.class);
    }

    public Map<String, Object> acceptTerms(String signerAccessCode) {
        requireId(signerAccessCode, "Signer access code");
        String json = serialise(Map.of("signer-access-code", signerAccessCode));
        return callMap("Failed to accept terms",
                () -> http.put("/signers/accept-terms", json));
    }

    public Map<String, Object> verifyEmail(String signerAccessCode, String verificationCode) {
        requireId(signerAccessCode, "Signer access code");
        requireId(verificationCode, "Verification code");
        String json = serialise(Map.of(
                "signer-access-code", signerAccessCode,
                "verification-code", verificationCode
        ));
        return callMap("Failed to verify email",
                () -> http.post("/verify", json));
    }

    public void uploadSignature(String signerAccessCode, String type, byte[] imageData) {
        requireId(signerAccessCode, "Signer access code");
        requireId(type, "Signature type");
        logger.info("Uploading signature", Map.of("signerAccessCode", signerAccessCode, "type", type));
        callVoid("Failed to upload signature",
                () -> http.postSignature("/signature?signer-access-code=" + signerAccessCode + "&type=" + type, imageData));
    }

    public byte[] downloadSignature(String signerAccessCode, String type) {
        requireId(signerAccessCode, "Signer access code");
        requireId(type, "Signature type");
        return callBinary("Failed to download signature",
                () -> http.getBinary("/signature/" + type + "?signer-access-code=" + signerAccessCode));
    }
}
