package com.assinafy.sdk.models;

/**
 * Everything one connection attempt needs: the consent URL to redirect the browser to, and the
 * secrets that must survive until the user comes back.
 *
 * <p>Store the whole object in the user's session before redirecting. The callback handler needs
 * {@link #state()} and {@link #issuer()} to prove the response is yours, and the token exchange
 * needs {@link #codeVerifier()}. Never reuse an instance across attempts: a repeated verifier or
 * {@code state} defeats PKCE and CSRF protection respectively.
 *
 * <p>Navigate to {@link #url()} with a full page load — an AJAX request cannot show a consent
 * screen.
 *
 * @param url the authorization-server URL to send the browser to
 * @param state the CSRF value echoed back on the redirect URI
 * @param codeVerifier the RFC 7636 PKCE verifier to send to the token endpoint
 * @param issuer the issuer identifier the callback's {@code iss} must equal
 * @param nonce the OpenID Connect nonce echoed in the {@code id_token}, or {@code null} when the
 *              {@code openid} scope was not requested
 */
public record OAuthAuthorizationRequest(
        String url,
        String state,
        String codeVerifier,
        String issuer,
        String nonce) {
}
