package com.assinafy.sdk.request;

import java.util.List;
import java.util.Map;

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

    public UploadAndRequestSignaturesRequest() {}

    public static Builder builder() {
        return new Builder();
    }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public List<SignerEntry> getSigners() { return signers; }
    public void setSigners(List<SignerEntry> signers) { this.signers = signers; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public boolean isWaitForReady() { return waitForReady; }
    public void setWaitForReady(boolean waitForReady) { this.waitForReady = waitForReady; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public List<String> getCopyReceivers() { return copyReceivers; }
    public void setCopyReceivers(List<String> copyReceivers) { this.copyReceivers = copyReceivers; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public static class SignerEntry {
        private String name;
        private String email;
        private String whatsappPhoneNumber;
        private String cpf;
        private Map<String, Object> metadata;

        public SignerEntry() {}

        public static Builder builder() { return new Builder(); }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getWhatsappPhoneNumber() { return whatsappPhoneNumber; }
        public void setWhatsappPhoneNumber(String phone) { this.whatsappPhoneNumber = phone; }

        public String getCpf() { return cpf; }
        public void setCpf(String cpf) { this.cpf = cpf; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

        public static final class Builder {
            private final SignerEntry entry = new SignerEntry();

            public Builder name(String name) { entry.setName(name); return this; }
            public Builder email(String email) { entry.setEmail(email); return this; }
            public Builder whatsappPhoneNumber(String phone) { entry.setWhatsappPhoneNumber(phone); return this; }
            public Builder cpf(String cpf) { entry.setCpf(cpf); return this; }
            public Builder metadata(Map<String, Object> metadata) { entry.setMetadata(metadata); return this; }
            public SignerEntry build() { return entry; }
        }
    }

    public static final class Builder {
        private final UploadAndRequestSignaturesRequest req = new UploadAndRequestSignaturesRequest();

        public Builder fileData(byte[] data) { req.setFileData(data); return this; }
        public Builder fileName(String name) { req.setFileName(name); return this; }
        public Builder signers(List<SignerEntry> signers) { req.setSigners(signers); return this; }
        public Builder message(String message) { req.setMessage(message); return this; }
        public Builder metadata(Map<String, Object> metadata) { req.setMetadata(metadata); return this; }
        public Builder waitForReady(boolean wait) { req.setWaitForReady(wait); return this; }
        public Builder expiresAt(String expiresAt) { req.setExpiresAt(expiresAt); return this; }
        public Builder copyReceivers(List<String> receivers) { req.setCopyReceivers(receivers); return this; }
        public Builder accountId(String accountId) { req.setAccountId(accountId); return this; }
        public UploadAndRequestSignaturesRequest build() { return req; }
    }
}
