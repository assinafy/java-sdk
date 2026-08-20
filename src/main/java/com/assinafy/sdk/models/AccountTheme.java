package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Account branding theme, as returned by {@code GET /accounts/{accountId}/theme}: the display
 * name, primary/secondary brand colours (6-char hex) and the logo URL ({@code null} when no logo
 * has been uploaded).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountTheme {

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("primary_color")
    private String primaryColor;

    @JsonProperty("secondary_color")
    private String secondaryColor;

    @JsonProperty("logo")
    private String logo;

    /**
     * Creates an empty account theme.
     */
    public AccountTheme() {}

    /**
     * Returns the account name.
     *
     * @return the account name
     */
    public String getAccountName() { return accountName; }

    /**
     * Sets the account name.
     *
     * @param accountName the account name
     */
    public void setAccountName(String accountName) { this.accountName = accountName; }

    /**
     * Returns the primary color.
     *
     * @return the primary color
     */
    public String getPrimaryColor() { return primaryColor; }

    /**
     * Sets the primary color.
     *
     * @param primaryColor the primary color
     */
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

    /**
     * Returns the secondary color.
     *
     * @return the secondary color
     */
    public String getSecondaryColor() { return secondaryColor; }

    /**
     * Sets the secondary color.
     *
     * @param secondaryColor the secondary color
     */
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }

    /**
     * Returns the logo.
     *
     * @return the logo
     */
    public String getLogo() { return logo; }

    /**
     * Sets the logo.
     *
     * @param logo the logo
     */
    public void setLogo(String logo) { this.logo = logo; }
}
