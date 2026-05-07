package com.assinafy.sdk;

public class AssinafyClientOptions {

    public static final String DEFAULT_BASE_URL = "https://api.assinafy.com.br/v1";
    public static final long DEFAULT_TIMEOUT_MS = 30_000L;

    private String apiKey;
    private String token;
    private String accountId;
    private String baseUrl = DEFAULT_BASE_URL;
    private String webhookSecret;
    private long timeoutMs = DEFAULT_TIMEOUT_MS;
    private Logger logger;

    public AssinafyClientOptions() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }

    public long getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }

    public Logger getLogger() { return logger; }
    public void setLogger(Logger logger) { this.logger = logger; }

    public static final class Builder {
        private final AssinafyClientOptions opts = new AssinafyClientOptions();

        public Builder apiKey(String apiKey) { opts.setApiKey(apiKey); return this; }
        public Builder token(String token) { opts.setToken(token); return this; }
        public Builder accountId(String accountId) { opts.setAccountId(accountId); return this; }
        public Builder baseUrl(String baseUrl) { opts.setBaseUrl(baseUrl); return this; }
        public Builder webhookSecret(String webhookSecret) { opts.setWebhookSecret(webhookSecret); return this; }
        public Builder timeoutMs(long timeoutMs) { opts.setTimeoutMs(timeoutMs); return this; }
        public Builder logger(Logger logger) { opts.setLogger(logger); return this; }
        public AssinafyClientOptions build() { return opts; }
    }
}
