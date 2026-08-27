package com.assinafy.sdk;

/** Authentication, endpoint, timeout, webhook, and logging options for {@link AssinafyClient}. */
public class AssinafyClientOptions {

    /** Production Assinafy API base URL. */
    public static final String DEFAULT_BASE_URL = "https://api.assinafy.com.br/v1";

    /** Sandbox Assinafy API base URL for development and testing. */
    public static final String SANDBOX_BASE_URL = "https://sandbox.assinafy.com.br/v1";

    /** Default call, connection, read, and write timeout in milliseconds. */
    public static final long DEFAULT_TIMEOUT_MS = 30_000L;

    /** API-key credential. */
    private String apiKey;
    /** Bearer credential. */
    private String token;
    /** Default account identifier. */
    private String accountId;
    /** API endpoint. */
    private String baseUrl = DEFAULT_BASE_URL;
    /** Secret for an optional out-of-band webhook HMAC arrangement. */
    private String webhookSecret;
    /** Transport timeout. */
    private long timeoutMs = DEFAULT_TIMEOUT_MS;
    /** Diagnostic logger. */
    private Logger logger;

    /** Create options initialized with the production URL and default timeout. */
    public AssinafyClientOptions() {}

    /** {@return a new options builder} */
    public static Builder builder() {
        return new Builder();
    }

    /** {@return the API key, or {@code null} when API-key authentication is not configured} */
    public String getApiKey() { return apiKey; }

    /**
     * Set the API-key credential.
     *
     * @param apiKey API key sent in the {@code X-Api-Key} header
     */
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    /** {@return the bearer token, or {@code null} when bearer authentication is not configured} */
    public String getToken() { return token; }

    /**
     * Set the bearer credential.
     *
     * @param token bearer token used when no API key is configured
     */
    public void setToken(String token) { this.token = token; }

    /** {@return the default account ID for account-scoped operations} */
    public String getAccountId() { return accountId; }

    /**
     * Set the default account.
     *
     * @param accountId default account ID for account-scoped operations
     */
    public void setAccountId(String accountId) { this.accountId = accountId; }

    /** {@return the API base URL} */
    public String getBaseUrl() { return baseUrl; }

    /**
     * Set the API endpoint.
     *
     * @param baseUrl HTTPS API base URL without a query or fragment; HTTP is accepted only for a
     *                loopback host used in local tests
     */
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    /** {@return the out-of-band webhook HMAC secret, or {@code null} when not configured} */
    public String getWebhookSecret() { return webhookSecret; }

    /**
     * Set a secret for tenants with an out-of-band HMAC-SHA256 webhook arrangement. Assinafy does
     * not publish a platform webhook-signature scheme.
     *
     * @param webhookSecret shared secret supplied by that separate arrangement
     */
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }

    /** {@return the transport timeout in milliseconds} */
    public long getTimeoutMs() { return timeoutMs; }

    /**
     * Set the transport timeout.
     *
     * @param timeoutMs positive transport timeout in milliseconds
     */
    public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }

    /** {@return the SDK logger, or {@code null} to use the no-op logger} */
    public Logger getLogger() { return logger; }

    /**
     * Set the diagnostic logger.
     *
     * @param logger logger for SDK diagnostic events; {@code null} selects the no-op logger
     */
    public void setLogger(Logger logger) { this.logger = logger; }

    /** Fluent builder for {@link AssinafyClientOptions}. */
    public static final class Builder {
        private final AssinafyClientOptions opts = new AssinafyClientOptions();

        /** Create a builder initialized with default options. */
        public Builder() {}

        /**
         * Set the API-key credential.
         *
         * @param apiKey API key sent in the {@code X-Api-Key} header
         * @return this builder
         */
        public Builder apiKey(String apiKey) { opts.setApiKey(apiKey); return this; }

        /**
         * Set the bearer credential.
         *
         * @param token bearer token used when no API key is configured
         * @return this builder
         */
        public Builder token(String token) { opts.setToken(token); return this; }

        /**
         * Set the default account.
         *
         * @param accountId default account ID for account-scoped operations
         * @return this builder
         */
        public Builder accountId(String accountId) { opts.setAccountId(accountId); return this; }

        /**
         * Set the API endpoint.
         *
         * @param baseUrl HTTPS API base URL without a query or fragment; HTTP is accepted only for
         *                a loopback host used in local tests
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) { opts.setBaseUrl(baseUrl); return this; }

        /**
         * Set a secret for an out-of-band HMAC-SHA256 webhook arrangement.
         *
         * @param webhookSecret shared secret supplied by that separate arrangement
         * @return this builder
         */
        public Builder webhookSecret(String webhookSecret) { opts.setWebhookSecret(webhookSecret); return this; }

        /**
         * Set the transport timeout.
         *
         * @param timeoutMs positive transport timeout in milliseconds
         * @return this builder
         */
        public Builder timeoutMs(long timeoutMs) { opts.setTimeoutMs(timeoutMs); return this; }

        /**
         * Set the diagnostic logger.
         *
         * @param logger logger for SDK diagnostic events; {@code null} selects the no-op logger
         * @return this builder
         */
        public Builder logger(Logger logger) { opts.setLogger(logger); return this; }

        /** {@return the configured options instance} */
        public AssinafyClientOptions build() { return opts; }
    }
}
