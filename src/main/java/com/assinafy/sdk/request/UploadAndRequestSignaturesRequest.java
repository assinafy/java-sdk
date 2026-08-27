package com.assinafy.sdk.request;

import java.util.List;
import java.util.Map;

/**
 * Request payload for uploading a document and requesting signatures.
 */
public class UploadAndRequestSignaturesRequest {

    private byte[] fileData;
    private String fileName;
    private List<SignerEntry> signers;
    private String message;
    private Map<String, Object> metadata;
    private boolean waitForReady = true;
    private String expiresAt;
    private List<String> copyReceivers;
    private String accountId;

    /**
     * Creates an empty document upload-and-signature request.
     */
    public UploadAndRequestSignaturesRequest() {}

    /**
     * Creates a builder for a document upload-and-signature request.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the PDF file data.
     *
     * @return the PDF file data
     */
    public byte[] getFileData() { return fileData; }

    /**
     * Sets the PDF file data.
     *
     * @param fileData the PDF file data
     */
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    /**
     * Returns the PDF file name.
     *
     * @return the PDF file name
     */
    public String getFileName() { return fileName; }

    /**
     * Sets the PDF file name.
     *
     * @param fileName the PDF file name
     */
    public void setFileName(String fileName) { this.fileName = fileName; }

    /**
     * Returns the signers.
     *
     * @return the signers
     */
    public List<SignerEntry> getSigners() { return signers; }

    /**
     * Sets the signers.
     *
     * @param signers the signers
     */
    public void setSigners(List<SignerEntry> signers) { this.signers = signers; }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() { return message; }

    /**
     * Sets the message.
     *
     * @param message the message
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Returns the metadata.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() { return metadata; }

    /**
     * Sets the metadata.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    /**
     * Returns whether the wait-for-ready flag is enabled.
     *
     * @return true when the wait-for-ready flag is enabled; otherwise false
     */
    public boolean isWaitForReady() { return waitForReady; }

    /**
     * Sets the wait-for-ready flag.
     *
     * @param waitForReady the wait-for-ready flag
     */
    public void setWaitForReady(boolean waitForReady) { this.waitForReady = waitForReady; }

    /**
     * Returns the expiration timestamp.
     *
     * @return the expiration timestamp
     */
    public String getExpiresAt() { return expiresAt; }

    /**
     * Sets the expiration timestamp.
     *
     * @param expiresAt ISO-8601 expiration timestamp; whole-second UTC form is recommended
     */
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    /**
     * Returns the copy recipients.
     *
     * @return the copy recipients
     */
    public List<String> getCopyReceivers() { return copyReceivers; }

    /**
     * Sets the copy recipients.
     *
     * @param copyReceivers the copy recipients
     */
    public void setCopyReceivers(List<String> copyReceivers) { this.copyReceivers = copyReceivers; }

    /**
     * Returns the account ID.
     *
     * @return the account ID
     */
    public String getAccountId() { return accountId; }

    /**
     * Sets the account ID.
     *
     * @param accountId the account ID
     */
    public void setAccountId(String accountId) { this.accountId = accountId; }

    /**
     * Represents a signer entry nested in an API request.
     */
    public static class SignerEntry {
        private String name;
        private String email;
        private String whatsappPhoneNumber;
        private String cpf;

        /**
         * Creates an empty signer entry.
         */
        public SignerEntry() {}

        /**
         * Creates a builder for a signer entry.
         *
         * @return a new builder
         */
        public static Builder builder() { return new Builder(); }

        /**
         * Returns the name.
         *
         * @return the name
         */
        public String getName() { return name; }

        /**
         * Sets the name.
         *
         * @param name the name
         */
        public void setName(String name) { this.name = name; }

        /**
         * Returns the email address.
         *
         * @return the email address
         */
        public String getEmail() { return email; }

        /**
         * Sets the email address.
         *
         * @param email the email address
         */
        public void setEmail(String email) { this.email = email; }

        /**
         * Returns the WhatsApp phone number.
         *
         * @return the WhatsApp phone number
         */
        public String getWhatsappPhoneNumber() { return whatsappPhoneNumber; }

        /**
         * Sets the WhatsApp phone number.
         *
         * @param phone the WhatsApp phone number
         */
        public void setWhatsappPhoneNumber(String phone) { this.whatsappPhoneNumber = phone; }

        /**
         * Returns the CPF or CNPJ stored through the signer's {@code government_id} update.
         *
         * @return the CPF or CNPJ
         */
        public String getCpf() { return cpf; }

        /**
         * Sets the CPF or CNPJ to persist through the signer's {@code government_id} update.
         *
         * @param cpf the CPF or CNPJ
         */
        public void setCpf(String cpf) { this.cpf = cpf; }

        /**
         * Builder for {@link SignerEntry}.
         */
        public static final class Builder {
            private final SignerEntry entry = new SignerEntry();

            /** Creates an empty builder. */
            public Builder() {}

            /**
             * Sets the name for the object being built.
             *
             * @param name the name
             * @return this builder
             */
            public Builder name(String name) { entry.setName(name); return this; }

            /**
             * Sets the email address for the object being built.
             *
             * @param email the email address
             * @return this builder
             */
            public Builder email(String email) { entry.setEmail(email); return this; }

            /**
             * Sets the WhatsApp phone number for the object being built.
             *
             * @param phone the WhatsApp phone number
             * @return this builder
             */
            public Builder whatsappPhoneNumber(String phone) { entry.setWhatsappPhoneNumber(phone); return this; }

            /**
             * Sets the CPF or CNPJ for the object being built.
             *
             * @param cpf the CPF or CNPJ
             * @return this builder
             */
            public Builder cpf(String cpf) { entry.setCpf(cpf); return this; }

            /**
             * Builds the configured signer entry.
             *
             * @return the configured signer entry
             */
            public SignerEntry build() { return entry; }
        }
    }

    /**
     * Builder for {@link UploadAndRequestSignaturesRequest}.
     */
    public static final class Builder {
        private final UploadAndRequestSignaturesRequest req = new UploadAndRequestSignaturesRequest();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets the PDF file data for the object being built.
         *
         * @param data the data
         * @return this builder
         */
        public Builder fileData(byte[] data) { req.setFileData(data); return this; }

        /**
         * Sets the PDF file name for the object being built.
         *
         * @param name the name
         * @return this builder
         */
        public Builder fileName(String name) { req.setFileName(name); return this; }

        /**
         * Sets the signers for the object being built.
         *
         * @param signers the signers
         * @return this builder
         */
        public Builder signers(List<SignerEntry> signers) { req.setSigners(signers); return this; }

        /**
         * Sets the message for the object being built.
         *
         * @param message the message
         * @return this builder
         */
        public Builder message(String message) { req.setMessage(message); return this; }

        /**
         * Sets the metadata for the object being built.
         *
         * @param metadata the metadata
         * @return this builder
         */
        public Builder metadata(Map<String, Object> metadata) { req.setMetadata(metadata); return this; }

        /**
         * Sets the wait-for-ready flag for the object being built.
         *
         * @param wait the wait
         * @return this builder
         */
        public Builder waitForReady(boolean wait) { req.setWaitForReady(wait); return this; }

        /**
         * Sets the expiration timestamp for the object being built.
         *
         * @param expiresAt ISO-8601 expiration timestamp; whole-second UTC form is recommended
         * @return this builder
         */
        public Builder expiresAt(String expiresAt) { req.setExpiresAt(expiresAt); return this; }

        /**
         * Sets the copy recipients for the object being built.
         *
         * @param receivers the receivers
         * @return this builder
         */
        public Builder copyReceivers(List<String> receivers) { req.setCopyReceivers(receivers); return this; }

        /**
         * Sets the account ID for the object being built.
         *
         * @param accountId the account ID
         * @return this builder
         */
        public Builder accountId(String accountId) { req.setAccountId(accountId); return this; }

        /**
         * Builds the configured document upload-and-signature request.
         *
         * @return the configured document upload-and-signature request
         */
        public UploadAndRequestSignaturesRequest build() { return req; }
    }
}
