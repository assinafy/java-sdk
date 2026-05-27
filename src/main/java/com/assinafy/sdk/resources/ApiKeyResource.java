package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.ApiKey;

import java.util.Map;

/**
 * API-key management for the authenticated user ({@code /users/api-keys}).
 *
 * <p>These endpoints operate on the user behind the current credentials and are not
 * account-scoped. {@link #get()} returns a masked key (last four characters only);
 * {@link #create(String)} returns the full key exactly once and invalidates any previous
 * key. Never expose a generated key to a frontend.
 */
public class ApiKeyResource extends BaseResource {

    public ApiKeyResource(ApiHttpClient http, Logger logger) {
        super(http, null, logger);
    }

    public ApiKeyResource(ApiHttpClient http) {
        super(http);
    }

    /**
     * {@code GET /users/api-keys} — retrieve the masked current API key, or {@code null}
     * when no key has been generated yet.
     */
    public ApiKey get() {
        return call("Failed to fetch API key", () -> http.get("/users/api-keys"), ApiKey.class);
    }

    /**
     * {@code POST /users/api-keys} — generate a new API key for the user, returning the
     * full value. The previously active key (if any) is immediately invalidated.
     *
     * @param password the user's account password (required by the API)
     */
    public ApiKey create(String password) {
        requireId(password, "Password");
        String body = serialise(Map.of("password", password));
        logger.info("Generating new API key");
        return call("Failed to generate API key", () -> http.post("/users/api-keys", body), ApiKey.class);
    }

    /** {@code DELETE /users/api-keys} — delete the existing API key. */
    public void delete() {
        callVoid("Failed to delete API key", () -> http.delete("/users/api-keys"));
    }
}
