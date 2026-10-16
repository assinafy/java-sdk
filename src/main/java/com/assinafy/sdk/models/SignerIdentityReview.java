package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Account-visible identity review summary. Evidence contains no image bytes or biometrics; images
 * are available only during InReview.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignerIdentityReview {

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("verification_method")
    private String verificationMethod;

    @JsonProperty("reason_codes")
    private List<Map<String, String>> reasonCodes;

    @JsonProperty("evidence_available")
    private Boolean evidenceAvailable;

    @JsonProperty("expected")
    private Map<String, String> expected;

    @JsonProperty("document")
    private Map<String, String> document;

    @JsonProperty("matches")
    private Map<String, Object> matches;

    @JsonProperty("checks")
    private List<Map<String, Object>> checks;

    @JsonProperty("images")
    private List<String> images;

    @JsonProperty("ruleset_version")
    private String rulesetVersion;

    @JsonProperty("evidence_sha256")
    private String evidenceSha256;

    @JsonProperty("reviewed_by")
    private Map<String, String> reviewedBy;

    @JsonProperty("reviewed_at")
    private String reviewedAt;

    @JsonProperty("review_note")
    private String reviewNote;

    @JsonProperty("completed_at")
    private String completedAt;

    /** Creates an empty identity review. */
    public SignerIdentityReview() {}

    /** Verification ID.
     * @return the id value
     */
    public String getId() { return id; }

    /** Sets the id.
     * @param id the id value
     */
    public void setId(String id) { this.id = id; }

    /** Pending, InReview, Approved, Declined, Expired, Abandoned, or a future status.
     * @return the status value
     */
    public String getStatus() { return status; }

    /** Sets the status.
     * @param status the status value
     */
    public void setStatus(String status) { this.status = status; }

    /** Verification provider, currently AssinafyVerify.
     * @return the provider value
     */
    public String getProvider() { return provider; }

    /** Sets the provider.
     * @param provider the provider value
     */
    public void setProvider(String provider) { this.provider = provider; }

    /** Verification method, currently IdDocumentFaceMatchBasic.
     * @return the verification_method value
     */
    public String getVerificationMethod() { return verificationMethod; }

    /** Sets the verificationMethod.
     * @param verificationMethod the verification_method value
     */
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }

    /** Ordered objects with code and description; unknown codes retain their code as description.
     * @return the reason_codes value
     */
    public List<Map<String, String>> getReasonCodes() { return reasonCodes; }

    /** Sets the reasonCodes.
     * @param reasonCodes the reason_codes value
     */
    public void setReasonCodes(List<Map<String, String>> reasonCodes) { this.reasonCodes = reasonCodes; }

    /** Whether an evidence summary could be fetched.
     * @return the evidence_available value
     */
    public Boolean getEvidenceAvailable() { return evidenceAvailable; }

    /** Sets the evidenceAvailable.
     * @param evidenceAvailable the evidence_available value
     */
    public void setEvidenceAvailable(Boolean evidenceAvailable) { this.evidenceAvailable = evidenceAvailable; }

    /** Registered name and cpf, both nullable; the object may be null.
     * @return the expected value
     */
    public Map<String, String> getExpected() { return expected; }

    /** Sets the expected.
     * @param expected the expected value
     */
    public void setExpected(Map<String, String> expected) { this.expected = expected; }

    /** Extracted type, name and cpf, each nullable; the object may be null.
     * @return the document value
     */
    public Map<String, String> getDocument() { return document; }

    /** Sets the document.
     * @param document the document value
     */
    public void setDocument(Map<String, String> document) { this.document = document; }

    /** Nullable cpf and birth_date booleans, and name similarity from 0 to 1; the object may be null.
     * @return the matches value
     */
    public Map<String, Object> getMatches() { return matches; }

    /** Sets the matches.
     * @param matches the matches value
     */
    public void setMatches(Map<String, Object> matches) { this.matches = matches; }

    /** Checks with name, result, nullable score and reasons (a string array).
     * @return the checks value
     */
    public List<Map<String, Object>> getChecks() { return checks; }

    /** Sets the checks.
     * @param checks the checks value
     */
    public void setChecks(List<Map<String, Object>> checks) { this.checks = checks; }

    /** Available names: document_front, document_back, document_pdf and face; empty after review.
     * @return the images value
     */
    public List<String> getImages() { return images; }

    /** Sets the images.
     * @param images the images value
     */
    public void setImages(List<String> images) { this.images = images; }

    /** Provider ruleset version, or null.
     * @return the ruleset_version value
     */
    public String getRulesetVersion() { return rulesetVersion; }

    /** Sets the rulesetVersion.
     * @param rulesetVersion the ruleset_version value
     */
    public void setRulesetVersion(String rulesetVersion) { this.rulesetVersion = rulesetVersion; }

    /** SHA-256 digest of evidence, or null.
     * @return the evidence_sha256 value
     */
    public String getEvidenceSha256() { return evidenceSha256; }

    /** Sets the evidenceSha256.
     * @param evidenceSha256 the evidence_sha256 value
     */
    public void setEvidenceSha256(String evidenceSha256) { this.evidenceSha256 = evidenceSha256; }

    /** Reviewer id and name, or null.
     * @return the reviewed_by value
     */
    public Map<String, String> getReviewedBy() { return reviewedBy; }

    /** Sets the reviewedBy.
     * @param reviewedBy the reviewed_by value
     */
    public void setReviewedBy(Map<String, String> reviewedBy) { this.reviewedBy = reviewedBy; }

    /** ISO 8601 review timestamp, or null.
     * @return the reviewed_at value
     */
    public String getReviewedAt() { return reviewedAt; }

    /** Sets the reviewedAt.
     * @param reviewedAt the reviewed_at value
     */
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }

    /** Private review note, hidden from signers, or null.
     * @return the review_note value
     */
    public String getReviewNote() { return reviewNote; }

    /** Sets the reviewNote.
     * @param reviewNote the review_note value
     */
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    /** ISO 8601 completion timestamp, or null.
     * @return the completed_at value
     */
    public String getCompletedAt() { return completedAt; }

    /** Sets the completedAt.
     * @param completedAt the completed_at value
     */
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
}
